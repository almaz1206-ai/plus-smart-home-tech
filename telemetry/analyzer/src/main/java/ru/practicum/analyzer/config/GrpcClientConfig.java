package ru.practicum.analyzer.config;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

@Slf4j
@Configuration
@EnableConfigurationProperties(GrpcClientProperties.class)
@RequiredArgsConstructor
public class GrpcClientConfig {
    private final GrpcClientProperties properties;

    @Bean(destroyMethod = "shutdown")
    public ManagedChannel hubRouterChannel() {
        boolean plainText = "plaintext".equalsIgnoreCase(properties.getNegotiationType());

        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forTarget(properties.getAddress());

        if (plainText) {
            builder.usePlaintext();
        }

        if (properties.isEnableKeepAlive()) {
            builder.keepAliveWithoutCalls(properties.isKeepAliveWithoutCalls());
        }

        ManagedChannel channel = builder.build();
        log.info("Инициализация gRPC канала для hub-router: address={}, plaintext={}, enableKeepAlive={}, keepAliveWithoutCalls={}",
                properties.getAddress(),
                plainText,
                properties.isEnableKeepAlive(),
                properties.isKeepAliveWithoutCalls()
                );

        return channel;
    }

    @Bean
    public HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterStub(ManagedChannel hubRouterChannel) {
        return HubRouterControllerGrpc.newBlockingStub(hubRouterChannel);
    }
}
