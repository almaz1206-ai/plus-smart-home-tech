package ru.practicum.analyzer.client;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.model.ScenarioAction;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequestProto;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubRouterClient {
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub stub;

    public void sendDeviceAction(String hubId, String scenarioName, ScenarioAction action) {
        try {
            ActionTypeProto typeProto = ActionTypeProto.valueOf(action.getAction().getType().name());

            DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                    .setSensorId(action.getId().getSensorId())
                    .setType(typeProto);

            Integer value = action.getAction().getValue();

            if (value != null) {
                actionBuilder.setValue(value);
            }

            DeviceActionProto actionProto = actionBuilder.build();


            DeviceActionRequestProto requestProto = DeviceActionRequestProto.newBuilder()
                    .setHubId(hubId)
                    .setScenarioName(scenarioName)
                    .setAction(actionProto)
                    .setTimestamp(currentTimestamp())
                    .build();

            stub.handleDeviceAction(requestProto);
            log.info("gRPC hub-router: hubId={}, scenario={}, action={}", hubId, scenarioName, actionProto);
        } catch (Exception e) {
            log.error("Ошибка отправки в hub-router: {}", e.getMessage(), e);
        }
    }

    private Timestamp currentTimestamp() {
        Instant now = Instant.now();
        return Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();
    }
}
