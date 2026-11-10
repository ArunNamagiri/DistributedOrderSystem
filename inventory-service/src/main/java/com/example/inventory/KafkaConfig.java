
package com.example.inventory;

import com.example.orders.OrderCreatedEvent;
import java.util.Objects;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    public KafkaTemplate<Object, Object> dltKafkaTemplate(
            KafkaProperties kafkaProperties) {

        var properties = Objects.requireNonNull(
                kafkaProperties.buildProducerProperties(null));

        ProducerFactory<Object, Object> factory = new DefaultKafkaProducerFactory<>(
                properties,
                new org.springframework.kafka.support.serializer.DelegatingByTypeSerializer(
                        java.util.Map.<Class<?>, org.apache.kafka.common.serialization.Serializer<?>>of(
                                String.class,
                                new StringSerializer(),
                                Object.class,
                                new JsonSerializer<Object>())),
                new JsonSerializer<Object>());

        return new KafkaTemplate<>(factory);
    }

    @Bean
    public KafkaTemplate<String, OrderCreatedEvent> replayKafkaTemplate(
            KafkaProperties kafkaProperties) {

        var properties = Objects.requireNonNull(
                kafkaProperties.buildProducerProperties(null));

        ProducerFactory<String, OrderCreatedEvent> factory = new DefaultKafkaProducerFactory<>(
                properties,
                new StringSerializer(),
                new JsonSerializer<>());

        return new KafkaTemplate<>(factory);
    }

    @Bean
    public DefaultErrorHandler errorHandler(
            KafkaTemplate<Object, Object> dltKafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                Objects.requireNonNull(dltKafkaTemplate),
                (record, exception) -> new TopicPartition(
                        record.topic() + ".DLT",
                        record.partition()));

        FixedBackOff backOff = new FixedBackOff(2000L, 3L);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}
