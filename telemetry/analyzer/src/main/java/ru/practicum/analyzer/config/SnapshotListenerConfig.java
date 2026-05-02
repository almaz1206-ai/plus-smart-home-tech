package ru.practicum.analyzer.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SnapshotListenerConfig {
    private final ConsumerFactory<String, SensorsSnapshotAvro> snapshotConsumerFactory;
    private final KafkaProperties kafkaProperties;

    @Bean("snapshotKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, SensorsSnapshotAvro> snapshotKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, SensorsSnapshotAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(snapshotConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        log.info("Создание фабрики snapshotKafkaListenerContainer для чтения сообщений на темы: {}", kafkaProperties.getSnapshot().getConsumerTopics());

        return factory;
    }
}
