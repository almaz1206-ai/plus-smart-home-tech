package ru.practicum.kafka.telemetry.handler.hub;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.telemetry.producer.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.DeviceTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Getter
public class DeviceAddedGrpcHandler implements GrpcHubEventHandler {
    private final KafkaEventProducer producer;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    public void handle(HubEventProto eventProto) {

        DeviceTypeAvro deviceTypeAvro = switch (eventProto.getDeviceAdded().getType()) {
            case DeviceTypeProto.MOTION_SENSOR -> DeviceTypeAvro.MOTION_SENSOR;
            case DeviceTypeProto.CLIMATE_SENSOR -> DeviceTypeAvro.CLIMATE_SENSOR;
            case DeviceTypeProto.LIGHT_SENSOR -> DeviceTypeAvro.LIGHT_SENSOR;
            case DeviceTypeProto.SWITCH_SENSOR -> DeviceTypeAvro.SWITCH_SENSOR;
            case DeviceTypeProto.TEMPERATURE_SENSOR -> DeviceTypeAvro.TEMPERATURE_SENSOR;
            default -> null;
        };

        DeviceAddedEventAvro payload = DeviceAddedEventAvro.newBuilder()
                .setId(eventProto.getDeviceAdded().getId())
                .setType(deviceTypeAvro)
                .build();

        HubEventAvro avro = HubEventAvro.newBuilder()
                .setHubId(eventProto.getHubId())
                .setTimestamp(Instant.ofEpochSecond(
                        eventProto.getTimestamp().getSeconds(),
                        eventProto.getTimestamp().getNanos()))
                .setPayload(payload)
                .build();
        producer.sendHubEvent(avro);
    }
}
