package com.acme.saga.orchestration.products.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * Configuration class to set up KafkaTemplate, Kafka topics.
 */
@Configuration
public class KafkaConfig {

    @Value("${products.events.topic.name}")
    private String productsEventsTopicName;

    private final static Integer TOPIC_REPLICATION_FACTOR = 3;

    private final static Integer TOPIC_PARTITIONS = 3;

    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * To create a new topic for products events if it does not exist.
     */
    @Bean
    NewTopic createProductsEventsTopic() {
        return TopicBuilder.name(productsEventsTopicName)
                .partitions(TOPIC_PARTITIONS)
                .replicas(TOPIC_REPLICATION_FACTOR)
                .build();
    }
}
