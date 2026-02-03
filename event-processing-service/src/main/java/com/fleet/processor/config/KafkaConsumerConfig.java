package com.fleet.processor.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.fleet.processor.model.TelemetryData;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Consumer Configuration
 * 
 * This class configures the Kafka consumer that will read telemetry data from Kafka.
 * 
 * Key Learning Points:
 * - Consumer configuration properties
 * - Deserialization (converting bytes from Kafka to Java objects)
 * - Consumer groups for load balancing
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    /**
     * Configure the Consumer Factory
     * 
     * This creates the factory that will produce Kafka consumers.
     * We configure:
     * - Bootstrap servers: Where Kafka is running
     * - Group ID: Identifies this consumer group (for load balancing)
     * - Key deserializer: How to convert bytes to the message key (String)
     * - Value deserializer: How to convert bytes to the message value (JSON -> Object)
     */
    @Bean
    public ConsumerFactory<String, TelemetryData> consumerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Kafka broker address
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // Consumer group ID - consumers in the same group share the load
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        
        // How to deserialize the message key
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        
        // How to deserialize the message value (JSON -> TelemetryData object)
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        
        // Configure JsonDeserializer programmatically (not via properties)
        JsonDeserializer<TelemetryData> valueDeserializer = new JsonDeserializer<>(TelemetryData.class, false);
        valueDeserializer.addTrustedPackages("com.fleet.processor.model");

        
        return new DefaultKafkaConsumerFactory<>(
            configProps,
            new StringDeserializer(),
            valueDeserializer
        );
    }

    /**
     * Kafka Listener Container Factory
     * 
     * This factory creates the container that manages the Kafka listener.
     * The listener will automatically poll Kafka for new messages and call our handler methods.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TelemetryData> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TelemetryData> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}
