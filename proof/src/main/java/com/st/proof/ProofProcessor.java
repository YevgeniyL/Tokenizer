package com.st.proof;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.st.proof.api.Transaction;
import com.st.proof.decoders.DecoderAES128;
import kafka.utils.ShutdownableThread;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Properties;

public class ProofProcessor extends ShutdownableThread {
    private static final Logger logger = LoggerFactory.getLogger(ProofProcessor.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final Decoder decoder = new DecoderAES128();
    private final KafkaConsumer<String, String> consumer;
    private final Jedis jedis;
    private String outputTopic;

    ProofProcessor(Configuration conf) {
        super("ProofProcessor", false);
        Properties consumerProps = conf.getConsumerProp();
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumer = new KafkaConsumer<>(consumerProps);

        this.jedis = new Jedis(conf.getRedisServerProp().getProperty("host"), Integer.valueOf(conf.getRedisServerProp().getProperty("port")));
        this.outputTopic = conf.getOutputTopicName();
    }

    @Override
    public void doWork() {
        consumer.subscribe(Collections.singletonList(outputTopic));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
        for (ConsumerRecord<String, String> topicRecord : records) {
            String transactionId = topicRecord.key();
            String token = topicRecord.value();
            if (token == null) {
                logger.warn("Topic '" + outputTopic + "' not contain token for transactionId = " + transactionId);
                continue;
            }

            String secretKeyBase64String = jedis.get(transactionId);
            if (secretKeyBase64String == null) {
                logger.warn("Redis not contain data for transactionId = " + transactionId);
                continue;
            }
            logger.info(LocalDateTime.now() + ". Received message with transactionId: " + transactionId);
            Decoder.Data data = new Decoder.Data(token, secretKeyBase64String);
            try {
                Decoder.CardData cardData = decoder.decode(data);
                Transaction transaction = new Transaction(cardData);
                logger.info(mapper.writeValueAsString(transaction));
            } catch (Exception e) {
                logger.error("Decryption failed for transactionId = " + transactionId + ". ", e);
            }
        }
    }

    @Override
    public String name() {
        return null;
    }

    @Override
    public boolean isInterruptible() {
        return false;
    }
}
