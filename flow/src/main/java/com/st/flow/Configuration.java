package com.st.flow;

import java.util.Properties;

class Configuration {
    private Properties redisServerProp;
    private Properties producerProp;
    private Properties consumerProp;
    private String inputTopicName;
    private String outputTopicName;

    Configuration(Properties redisServerProp, Properties producerProp, Properties consumerProp, String inputTopicName, String outputTopicName) {
        this.redisServerProp = redisServerProp;
        this.producerProp = producerProp;
        this.consumerProp = consumerProp;
        this.inputTopicName = inputTopicName;
        this.outputTopicName = outputTopicName;
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
}
