package com.st.source.configuration;

import com.st.source.api.AuthRequest;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.StreamSupport;

@Configuration
@PropertySource(value = "file:${config.file}")
@EnableConfigurationProperties
public class KafkaConfiguration {

    private final String producerPrefix = "kafka.configuration.producer.";
    private Properties resourceProperties;

    @Autowired
    public KafkaConfiguration(Environment env) {
        Properties props = new Properties();
        MutablePropertySources propSrcs = ((AbstractEnvironment) env).getPropertySources();
        StreamSupport.stream(propSrcs.spliterator(), false)
                .filter(ps -> ps instanceof ResourcePropertySource)
                .map(ps -> ((EnumerablePropertySource) ps).getPropertyNames())
                .flatMap(Arrays::stream)
                .forEach(propName -> props.setProperty(propName, env.getProperty(propName)));
        this.resourceProperties = props;
    }

    @Bean
    public KafkaTemplate<String, AuthRequest> kafkaTemplate() {
        Map<String, Object> props = new HashMap<>();
        for (Map.Entry<Object, Object> propEntry : resourceProperties.entrySet()) {
            String propName = String.valueOf(propEntry.getKey());
            if (propName.startsWith(producerPrefix)) {
                props.put(propName.replace(producerPrefix, ""), propEntry.getValue());
            }
        }
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }
}
