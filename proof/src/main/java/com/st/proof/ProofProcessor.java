package com.st.proof;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.st.proof.api.AuthRequest;
import com.st.proof.api.Transaction;
import kafka.utils.ShutdownableThread;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.Properties;

public class ProofProcessor extends ShutdownableThread {
    private static final Logger logger = LoggerFactory.getLogger(ProofProcessor.class);
    private final KafkaConsumer<String, String> consumer;
    private final Jedis jedis;
    private final ObjectMapper mapper = new ObjectMapper();
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

    //It can be in external lib, but in to the maven commands will need add command 'install' to local repo
    private static byte[] decryptData(byte[] rawMessage, SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(rawMessage);
    }

    @Override
    public void doWork() {
        consumer.subscribe(Collections.singletonList(outputTopic));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
        for (ConsumerRecord<String, String> topicRecord : records) {
            String transactionId = topicRecord.key();
            if (topicRecord.value() == null) {
                logger.warn("Topic '" + outputTopic + "' not contain token for transactionId = " + transactionId);
                continue;
            }

            String secretInBase64String = jedis.get(transactionId);
            if (secretInBase64String == null) {
                logger.warn("Redis not contain data for transactionId = " + transactionId);
                continue;
            }
            logger.info(LocalDateTime.now() + ". Received message with transactionId: " + transactionId);

            SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(secretInBase64String), "AES");
            byte[] tokenInAes = Base64.getDecoder().decode(topicRecord.value());
            try {
                byte[] encryptedData = decryptData(tokenInAes, secretKey);
                String data = new String(encryptedData, StandardCharsets.UTF_8);
                AuthRequest authRequest = mapper.readValue(data, AuthRequest.class);
                Transaction transaction = new Transaction(authRequest);
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
