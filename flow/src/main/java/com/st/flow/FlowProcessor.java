package com.st.flow;

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

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.Properties;

public class FlowProcessor extends ShutdownableThread {
    private static final Logger logger = LoggerFactory.getLogger(FlowProcessor.class);
    private final KafkaConsumer<String, String> consumer;
    private final String inputTopic;
    private final SecretKey secretKey;
    private final Jedis jedis;

    private final KafkaProducer<String, String> producer;
    private final String outputTopic;

    FlowProcessor(Configuration conf) {
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
        this.secretKey = conf.getSecretKey();
    }

    //It can be in external lib, but in to maven commands will need add command 'install' to local repo
    private static byte[] encryptData(byte[] rawMessage, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(rawMessage);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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
            try {
                jedis.set(transactionId, Base64.getEncoder().withoutPadding().encodeToString(secretKey.getEncoded()));
                byte[] tokenInAes = encryptData(jsonData.getBytes(StandardCharsets.UTF_8), secretKey);
                String tokenAesInString = Base64.getEncoder().withoutPadding().encodeToString(tokenInAes);
                producer.send(new ProducerRecord<>(outputTopic, transactionId, tokenAesInString));
                logger.info(LocalDateTime.now() + ". To '" + outputTopic + "' topic sended message with transactionId: " + transactionId);
            } catch (Exception e) {
                logger.error(LocalDateTime.now() + ". Exception on work data: ", e);
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
