package com.st.flow;

import javax.crypto.SecretKey;
import java.util.Properties;

class Configuration {
    private Properties redisServerProp;
    private Properties producerProp;
    private Properties consumerProp;
    private String inputTopicName;
    private String outputTopicName;
    private SecretKey secretKey;

    Configuration(Properties redisServerProp, Properties producerProp, Properties consumerProp, String inputTopicName, String outputTopicName, SecretKey secretKey) {
        this.redisServerProp = redisServerProp;
        this.producerProp = producerProp;
        this.consumerProp = consumerProp;
        this.inputTopicName = inputTopicName;
        this.outputTopicName = outputTopicName;
        this.secretKey = secretKey;
    }

    Properties getRedisServerProp() {
        return redisServerProp;
    }

    Properties getProducerProp() {
        return producerProp;
    }

    Properties getConsumerProp() {
        return consumerProp;
    }

    String getInputTopicName() {
        return inputTopicName;
    }

    String getOutputTopicName() {
        return outputTopicName;
    }

    SecretKey getSecretKey() {
        return secretKey;
    }
}
