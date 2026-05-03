package ru.practicum.analyzer.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HubEventListenerConfig {
    private final ConsumerFactory<String, HubEventAvro> hubEventConsumerFactory;
    private final KafkaProperties kafkaProperties;

    @Bean("hubKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, HubEventAvro> hubKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, HubEventAvro> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(hubEventConsumerFactory);
        log.info("Создание фабрики hubKafkaListenerContainer для чтения сообщений на темы: {}", kafkaProperties.getHub().getConsumerTopics());
        return factory;
    }
}
