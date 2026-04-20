package ru.practicum.kafka.telemetry.handler.sensor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.telemetry.producer.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class SwitchSensorGrpcHandler implements GrpcSensorEventHandler {
    private final KafkaEventProducer producer;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto eventProto) {
        SwitchSensorAvro payload = SwitchSensorAvro.newBuilder()
                .setState(eventProto.getSwitchSensorEvent().getState())
                .build();

        SensorEventAvro avro = SensorEventAvro.newBuilder()
                .setHubId(eventProto.getHubId())
                .setId(eventProto.getId())
                .setTimestamp(Instant.ofEpochSecond(
                        eventProto.getTimestamp().getSeconds(),
                        eventProto.getTimestamp().getNanos()))
                .setPayload(payload)
                .build();

        producer.sendSensorEvent(avro);
    }
}
