package ru.practicum.analyzer.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SnapshotConsumerConfig {
    private final KafkaProperties kafkaProperties;

    @Bean("snapshotConsumerFactory")
    public ConsumerFactory<String, SensorsSnapshotAvro> snapshotConsumerFactory() {
        KafkaProperties.SnapshotConfig snapshot = kafkaProperties.getSnapshot();

        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, snapshot.getBootstrapServers());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, snapshot.getGroupId());
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, snapshot.getClientId());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, snapshot.getKeyDeserializer());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, snapshot.getValueDeserializer());
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, snapshot.isEnableAutoCommit());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, snapshot.getAutoOffsetReset());
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, snapshot.getMaxPollRecords());
        config.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, snapshot.getMaxPollInterval());
        config.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, snapshot.getIsolationLevel());

        log.info("Snapshot consumer factory configured: groupId={}, topics={}",
                snapshot.getGroupId(), snapshot.getConsumerTopics());

        return new DefaultKafkaConsumerFactory<>(config);
    }
}
