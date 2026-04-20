package ru.practicum.kafka.telemetry.handler.sensor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.telemetry.mapper.EventMapper;
import ru.practicum.kafka.telemetry.producer.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class ClimateSensorGrpcHandler implements GrpcSensorEventHandler {
    private final KafkaEventProducer producer;
    private final EventMapper mapper;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto eventProto) {
        ClimateSensorAvro payload = ClimateSensorAvro.newBuilder()
                .setTemperatureC(eventProto.getClimateSensorEvent().getTemperatureC())
                .setCo2Level(eventProto.getClimateSensorEvent().getCo2Level())
                .setHumidity(eventProto.getClimateSensorEvent().getHumidity())
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
