package ru.practicum.aggregator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SensorSnapshotService {
    private final Map<String, SensorsSnapshotAvro> snapshotAvroMap = new ConcurrentHashMap<>();

    public Optional<SensorsSnapshotAvro> updateSnapshot(SensorEventAvro sensorEventAvro) {
        SensorsSnapshotAvro currentSnapshot = snapshotAvroMap.computeIfAbsent(sensorEventAvro.getHubId(), hubId ->
            SensorsSnapshotAvro.newBuilder()
                    .setHubId(hubId)
                    .setSensorsState(new HashMap<>())
                    .setTimestamp(sensorEventAvro.getTimestamp())
                    .build()
        );

        SensorStateAvro prevSensorState = currentSnapshot.getSensorsState().get(sensorEventAvro.getId());

        if (prevSensorState != null && (
                prevSensorState.getTimestamp().isAfter(sensorEventAvro.getTimestamp()) ||
                prevSensorState.getData().equals(sensorEventAvro.getPayload())
                )) {
            return Optional.empty();
        }

        SensorStateAvro newSensorState = SensorStateAvro.newBuilder()
                .setData(sensorEventAvro.getPayload())
                .setTimestamp(sensorEventAvro.getTimestamp())
                .build();

        currentSnapshot.getSensorsState().put(sensorEventAvro.getId(), newSensorState);
        currentSnapshot.setTimestamp(sensorEventAvro.getTimestamp());

        return Optional.of(currentSnapshot);
    }
}
