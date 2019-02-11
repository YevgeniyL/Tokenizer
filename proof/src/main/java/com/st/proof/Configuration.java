package com.st.proof;

import java.util.Properties;

class Configuration {
    private Properties redisServerProp;
    private Properties consumerProp;
    private String outputTopicName;

    Configuration(Properties redisServerProp, Properties consumerProp, String outputTopicName) {
        this.redisServerProp = redisServerProp;
        this.consumerProp = consumerProp;
        this.outputTopicName = outputTopicName;
    }

    Properties getRedisServerProp() {
        return redisServerProp;
    }

    Properties getConsumerProp() {
        return consumerProp;
    }

    String getOutputTopicName() {
        return outputTopicName;
    }
}
