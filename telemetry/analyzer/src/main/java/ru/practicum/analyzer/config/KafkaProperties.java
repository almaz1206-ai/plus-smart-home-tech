package ru.practicum.analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaProperties {
    private final HubConfig hub = new HubConfig();
    private final SnapshotConfig snapshot = new SnapshotConfig();

    @Data
    public static class HubConfig {
        private String bootstrapServers;
        private String groupId;
        private String clientId;
        private List<String> consumerTopics;
        private String valueDeserializer;
        private String keyDeserializer;
        private boolean enableAutoCommit;
        private int autoCommitInterval;
        private String autoOffsetReset;
        private int maxPollRecords;
    }

    @Data
    public static class SnapshotConfig {
        private String bootstrapServers;
        private String groupId;
        private String clientId;
        private List<String> consumerTopics;
        private String valueDeserializer;
        private String keyDeserializer;
        private boolean enableAutoCommit;
        private String autoOffsetReset;
        private int maxPollRecords;
        private int maxPollInterval;
        private String isolationLevel;
    }
}
