package ru.practicum.kafka.telemetry.handler.sensor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.telemetry.producer.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class MotionSensorGrpcHandler implements GrpcSensorEventHandler {
    private final KafkaEventProducer producer;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto eventProto) {
        MotionSensorAvro payload = MotionSensorAvro.newBuilder()
                .setLinkQuality(eventProto.getMotionSensorEvent().getLinkQuality())
                .setMotion(eventProto.getMotionSensorEvent().getMotion())
                .setVoltage(eventProto.getMotionSensorEvent().getVoltage())
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
