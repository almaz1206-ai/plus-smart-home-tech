package ru.practicum.analyzer.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class HubEventConsumerConfig {
    private final KafkaProperties kafkaProperties;

    @Bean("hubEventConsumerFactory")
    public ConsumerFactory<String, HubEventAvro> hubEventConsumerFactory() {
        KafkaProperties.HubConfig hub = kafkaProperties.getHub();

        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, hub.getBootstrapServers());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, hub.getGroupId());
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, hub.getClientId());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, hub.getKeyDeserializer());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, hub.getValueDeserializer());
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, hub.isEnableAutoCommit());
        config.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, hub.getAutoCommitInterval());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, hub.getAutoOffsetReset());
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, hub.getMaxPollRecords());

        log.info("Hub consumer factory configured: groupId={}, topics={}",
                hub.getGroupId(), hub.getConsumerTopics());

        return new DefaultKafkaConsumerFactory<>(config);
    }
}
