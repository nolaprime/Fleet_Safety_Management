package com.fleet.telemetry.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.fleet.telemetry.model.TelemetryData;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Producer Configuration
 * 
 * This class configures the Kafka producer that will send telemetry data to Kafka.
 * 
 * Key Learning Points:
 * - Producer configuration properties
 * - Serialization (converting Java objects to bytes for Kafka)
 * - KafkaTemplate for sending messages
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Configure the Producer Factory
     * 
     * This creates the factory that will produce Kafka producers.
     * We configure:
     * - Bootstrap servers: Where Kafka is running
     * - Key serializer: How to convert the message key to bytes (String)
     * - Value serializer: How to convert the message value to bytes (JSON)
     */
    @Bean
    public ProducerFactory<String, TelemetryData> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        
        // Kafka broker address
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        
        // How to serialize the message key (truck ID)
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        
        // How to serialize the message value (TelemetryData object -> JSON)
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    /**
     * KafkaTemplate Bean
     * 
     * KafkaTemplate is the main class we'll use to send messages to Kafka.
     * It's a high-level abstraction that simplifies sending messages.
     */
    @Bean
    public KafkaTemplate<String, TelemetryData> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
