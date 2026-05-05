package ru.practicum.analyzer.service;


import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public interface HubEventProcessor {
    void processScenarioAddedEvent(HubEventAvro hubEvent);

    void removeScenario(String hubId, String name);
}
