package ru.practicum.analyzer.service;

import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

public interface SnapshotProcessor {
    void update(SensorsSnapshotAvro snapshotAvro);
}
