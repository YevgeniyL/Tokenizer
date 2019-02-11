package com.st.proof;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        String confFilePath = System.getProperty("config.file");
        if (confFilePath == null) {
            logger.error("Not found '-Dconfig.file' property");
            return;
        }
        if (confFilePath.length() == 0) {
            logger.error("Empty path in parameter '-Dconfig.file'");
            return;
        }

        Properties fileProps = new Properties();
        try {
            fileProps.load(new FileInputStream(confFilePath));
            Configuration configuration = getProperties(fileProps);
            new ProofProcessor(configuration).run();
        } catch (IOException e) {
            logger.error("Configuration file not exist.", e);
        }
    }

    private static Configuration getProperties(Properties props) {
        final String redisConfPrefix = "redis.configuration.server.";
        final String kafkaConsumerConfPrefix = "kafka.configuration.consumer.";
        final String topicOutputName = "kafka.configuration.topic.output.name";

        Properties redisConfig = new Properties();
        Properties consumerConfig = new Properties();
        String outputTopicName = null;

        for (Map.Entry<Object, Object> propEntry : props.entrySet()) {
            String key = String.valueOf(propEntry.getKey());
            if (key.startsWith(redisConfPrefix)) {
                redisConfig.put(key.replace(redisConfPrefix, ""), propEntry.getValue());
            } else if (key.startsWith(kafkaConsumerConfPrefix)) {
                consumerConfig.put(key.replace(kafkaConsumerConfPrefix, ""), propEntry.getValue());
            } else if (key.equals(topicOutputName)) {
                outputTopicName = String.valueOf(propEntry.getValue());
            }
        }
        return new Configuration(redisConfig, consumerConfig, outputTopicName);
    }
}
