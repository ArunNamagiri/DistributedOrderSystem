
package com.example.inventory;

import org.apache.kafka.common.TopicPartition;
import java.util.Objects;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler errorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate) {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        Objects.requireNonNull(kafkaTemplate),
                        (record, exception) -> new TopicPartition(
                                record.topic() + ".DLT",
                                record.partition()));

        // 3 retries, 2-second delay between attempts
        FixedBackOff backOff = new FixedBackOff(2000L, 3L);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}
