package com.ecommerce.product.config;

import com.ecommerce.product.event.ProductEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
@Slf4j
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:product-service-group}")
    private String consumerGroupId;

    // Topic name constants
    public static final String PRODUCT_CREATED_TOPIC = "product.created";
    public static final String PRODUCT_UPDATED_TOPIC = "product.updated";
    public static final String PRODUCT_DELETED_TOPIC = "product.deleted";
    public static final String PRODUCT_VIEWED_TOPIC = "product.viewed";
    public static final String PRODUCT_CACHE_INVALIDATION_TOPIC = "product.cache.invalidation";
    public static final String INVENTORY_UPDATE_TOPIC = "inventory.update";

    // ===== PRODUCER CONFIGURATION =====

    @Bean
    public ProducerFactory<String, ProductEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Reliability settings
        config.put(ProducerConfig.ACKS_CONFIG, "all");
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        // Performance settings
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        config.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, ProductEvent> kafkaTemplate() {
        KafkaTemplate<String, ProductEvent> template = new KafkaTemplate<>(producerFactory());
        template.setObservationEnabled(true);
        return template;
    }

    // ===== CONSUMER CONFIGURATION =====

    @Bean
    public ConsumerFactory<String, ProductEvent> consumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);  // Manual commit for reliability
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        config.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 45000);
        config.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 15000);

        // Error-handling deserializer wraps actual deserializer
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        config.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.ecommerce.product.event");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ProductEvent.class.getName());
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ProductEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3); // 3 consumer threads
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setObservationEnabled(true);

        // Retry with backoff before sending to DLT
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                new FixedBackOff(1000L, 3L) // 3 retries, 1 second apart
        );
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    // ===== TOPIC DEFINITIONS =====

    @Bean
    public NewTopic productCreatedTopic() {
        return TopicBuilder.name(PRODUCT_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productUpdatedTopic() {
        return TopicBuilder.name(PRODUCT_UPDATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productDeletedTopic() {
        return TopicBuilder.name(PRODUCT_DELETED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productViewedTopic() {
        return TopicBuilder.name(PRODUCT_VIEWED_TOPIC)
                .partitions(6) // Higher partitions for high-volume event
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic cacheInvalidationTopic() {
        return TopicBuilder.name(PRODUCT_CACHE_INVALIDATION_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
