package ru.practicum.analyzer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "grpc.client.hub-router")
public class GrpcClientProperties {
    private final String address;
    private final boolean enableKeepAlive;
    private final boolean keepAliveWithoutCalls;
    private final String negotiationType;
}
