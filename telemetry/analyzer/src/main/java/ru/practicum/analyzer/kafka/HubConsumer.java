package ru.practicum.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.mapper.AvroToEntityMapper;
import ru.practicum.analyzer.model.Sensor;
import ru.practicum.analyzer.repository.SensorRepository;
import ru.practicum.analyzer.service.HubEventProcessor;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubConsumer {
    private final SensorRepository sensorRepository;
    private final HubEventProcessor hubEventProcessor;
    private final AvroToEntityMapper mapper;

    @KafkaListener(
            topics = "${spring.kafka.hub.consumer-topics}",
            containerFactory = "hubKafkaListenerContainerFactory"
    )
    public void listenHub(HubEventAvro hubEventAvro) {
        String hubId = hubEventAvro.getHubId();
        Object payload = hubEventAvro.getPayload();
        String eventType = payload.getClass().getSimpleName();

        log.info("Обработка события хаба: hubId={}, тип={}", hubId, eventType);

        try {
            processEvent(hubEventAvro, payload);
        } catch (Exception e) {
            log.error("payload={}, eventType={}", payload, eventType);
            log.error("Ошибка при обработке события {} для хаба {}: {}",
                    eventType, hubId, e.getMessage(), e);
            throw new RuntimeException("Failed to process hub event", e);
        }
    }

    private void processEvent(HubEventAvro hubEvent, Object payload) {
        switch (payload) {
            case DeviceAddedEventAvro added -> handleDeviceAdded(hubEvent, added);
            case DeviceRemovedEventAvro removed -> handleDeviceRemoved(removed);
            case ScenarioAddedEventAvro added -> handleScenarioAdded(hubEvent, added);
            case ScenarioRemovedEventAvro removed -> handleScenarioRemoved(hubEvent.getHubId(), removed);
            default -> log.warn("Необработанный тип события: {}", payload.getClass().getSimpleName());
        }
    }

    private void handleDeviceAdded(HubEventAvro hubEvent, DeviceAddedEventAvro added) {
        Sensor sensor = mapper.toSensor(hubEvent.getHubId(), added);
        sensorRepository.save(sensor);
        log.info("Сенсор добавлен: id={}, hubId={}, тип={}",
                added.getId(), hubEvent.getHubId(), added.getType());
    }

    private void handleDeviceRemoved(DeviceRemovedEventAvro removed) {
        sensorRepository.deleteById(removed.getId());
        log.info("Сенсор удалён: id={}", removed.getId());
    }

    private void handleScenarioAdded(HubEventAvro hubEvent, ScenarioAddedEventAvro added) {
        String scenarioName = added.getName();
        hubEventProcessor.processScenarioAddedEvent(hubEvent);
        log.info("Сценарий сохранён: имя={}, hubId={}", scenarioName, hubEvent.getHubId());
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro removed) {
        String scenarioName = removed.getName();
        hubEventProcessor.removeScenario(hubId, scenarioName);
        log.info("Сценарий удалён: имя={}, hubId={}", scenarioName, hubId);
    }
}
