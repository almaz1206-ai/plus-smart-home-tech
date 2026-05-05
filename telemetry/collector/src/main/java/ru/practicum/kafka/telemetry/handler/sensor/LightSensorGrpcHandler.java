package ru.practicum.kafka.telemetry.handler.sensor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.telemetry.producer.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class LightSensorGrpcHandler implements GrpcSensorEventHandler {
    private final KafkaEventProducer producer;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto eventProto) {
        LightSensorAvro payload = LightSensorAvro.newBuilder()
                .setLuminosity(eventProto.getLightSensorEvent().getLuminosity())
                .setLinkQuality(eventProto.getLightSensorEvent().getLinkQuality())
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
