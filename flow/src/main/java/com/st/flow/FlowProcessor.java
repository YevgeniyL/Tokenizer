package com.st.flow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.st.flow.api.AuthRequest;
import com.st.flow.encoders.EncoderAES128;
import kafka.utils.ShutdownableThread;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Properties;

public class FlowProcessor extends ShutdownableThread {
    private static final Logger logger = LoggerFactory.getLogger(FlowProcessor.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final Encoder encoder = new EncoderAES128();

    private final KafkaConsumer<String, String> consumer;
    private final String inputTopic;
    private final Jedis jedis;
    private final KafkaProducer<String, String> producer;
    private final String outputTopic;

    FlowProcessor(Configuration conf) throws NoSuchAlgorithmException {
        super("FlowProcessor", false);
        Properties consumerProp = conf.getConsumerProp();
        consumerProp.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProp.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumer = new KafkaConsumer<>(consumerProp);
        this.inputTopic = conf.getInputTopicName();

        Properties producerProp = conf.getProducerProp();
        producerProp.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProp.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(producerProp);
        this.outputTopic = conf.getOutputTopicName();

        this.jedis = new Jedis(conf.getRedisServerProp().getProperty("host"), Integer.valueOf(conf.getRedisServerProp().getProperty("port")));
    }


    @Override
    public void doWork() {
        consumer.subscribe(Collections.singletonList(inputTopic));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

        for (ConsumerRecord<String, String> record : records) {
            String transactionId = record.key();
            String jsonData = record.value();
            if (transactionId == null || jsonData == null) {
                logger.info("Wrong data " + transactionId == null ? " - empty transactionId." : "for transactionId = " + transactionId);
                continue;
            }
            logger.info(LocalDateTime.now() + ". From '" + inputTopic + "' topic received message with transactionId: " + transactionId);

            AuthRequest authRequest = null;
            try {
                authRequest = mapper.readValue(jsonData, AuthRequest.class);
            } catch (IOException e) {
                logger.error(LocalDateTime.now() + ". Deserialize json data to object exception", e);
                continue;
            }

            Encoder.CardData cardData = new Encoder.CardData(authRequest.getCardNumber(), authRequest.getExpirationDate(), authRequest.getCvcNumber());
            Encoder.Data result = null;
            try {
                result = encoder.encode(cardData);
            } catch (Exception e) {
                logger.error(LocalDateTime.now() + ". Encode data exception", e);
                continue;
            }

            jedis.set(transactionId, result.getSecretKey());
            producer.send(new ProducerRecord<>(outputTopic, transactionId, result.getToken()));
            logger.info(LocalDateTime.now() + ". To '" + outputTopic + "' topic sended message with transactionId: " + transactionId);
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
