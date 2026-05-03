package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.mapper.AvroToEntityMapper;
import ru.practicum.analyzer.model.*;
import ru.practicum.analyzer.repository.*;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HubEventScenarioProcess implements HubEventProcessor {
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;
    private final AvroToEntityMapper mapper;

    @Transactional
    public void processScenarioAddedEvent(HubEventAvro hubEvent) {
        String hubId = hubEvent.getHubId();
        ScenarioAddedEventAvro event = (ScenarioAddedEventAvro) hubEvent.getPayload();

        Scenario scenario = saveScenario(hubId, event);

        cleanupRelations(scenario.getId());

        processConditions(scenario, event.getConditions(), hubId);
        processActions(scenario, event.getActions(), hubId);

        log.info("Сценарий сохранен: {}, условий={}, действий={}",
                scenario.getName(),
                event.getConditions() != null ? event.getConditions().size() : 0,
                event.getActions() != null ? event.getActions() : 0
        );
    }

    private Scenario saveScenario(String hubId, ScenarioAddedEventAvro evt) {
        Scenario scenario = scenarioRepository
                .findByHubIdAndName(hubId, evt.getName())
                .orElseGet(() -> mapper.toScenario(hubId, evt));

        scenario.setHubId(hubId);
        scenario.setName(evt.getName());

        Scenario saved = scenarioRepository.save(scenario);
        log.debug("Сценарий сохранён: id={}", saved.getId());
        return saved;
    }

    private void cleanupRelations(Long scenarioId) {
        log.debug("Очистка старых связей для сценария: {}", scenarioId);
        scenarioConditionRepository.deleteByIdScenarioId(scenarioId);
        scenarioActionRepository.deleteByIdScenarioId(scenarioId);
    }

    private void processConditions(Scenario scenario, List<ScenarioConditionAvro> conditions, String hubId) {
        if (conditions == null || conditions.isEmpty()) {
            log.debug("Нет условий для обработки");
            return;
        }

        log.debug("Обработка {} условий", conditions.size());
        for (ScenarioConditionAvro avro : conditions) {
            saveScenarioCondition(scenario, avro, hubId);
        }
    }

    private void saveScenarioCondition(Scenario scenario, ScenarioConditionAvro avro, String hubId) {
        String sensorId = avro.getSensorId();

        Sensor sensor = findOrCreateSensor(sensorId, hubId);

        Condition condition = conditionRepository.save(mapper.toCondition(avro));

        ScenarioCondition sc = ScenarioCondition.builder()
                .id(new ScenarioConditionId(scenario.getId(), sensorId, condition.getId()))
                .scenario(scenario)
                .sensor(sensor)
                .condition(condition)
                .build();

        scenarioConditionRepository.save(sc);
        log.debug("Условие сохранено: sensor={}, condition={}", sensorId, condition.getId());
    }

    private void processActions(Scenario scenario, List<DeviceActionAvro> actions, String hubId) {
        if (actions == null || actions.isEmpty()) {
            log.debug("Нет действий для обработки");
            return;
        }

        log.debug("Обработка {} действий", actions.size());
        for (DeviceActionAvro avro : actions) {
            saveScenarioAction(scenario, avro, hubId);
        }
    }

    private void saveScenarioAction(Scenario scenario, DeviceActionAvro avro, String hubId) {
        String sensorId = avro.getSensorId();

        Sensor sensor = findOrCreateSensor(sensorId, hubId);

        Action action = actionRepository.save(mapper.toAction(avro));

        ScenarioActionId saId = new ScenarioActionId(scenario.getId(), sensorId, action.getId());

        if (!scenarioActionRepository.existsById(saId)) {
            ScenarioAction sa = ScenarioAction.builder()
                    .id(new ScenarioActionId(scenario.getId(), sensorId, action.getId()))
                    .scenario(scenario)
                    .sensor(sensor)
                    .action(action)
                    .build();

            scenarioActionRepository.save(sa);
            log.debug("Действие сохранено: sensor={}, action={}", sensorId, action.getId());
        }
    }

    private Sensor findOrCreateSensor(String sensorId, String hubId) {
        return sensorRepository.findById(sensorId)
                .orElseGet(() -> {
                    Sensor newSensor = new Sensor(sensorId, hubId);
                    Sensor saved = sensorRepository.save(newSensor);
                    log.debug("Сенсор создан: {} (hub: {})", sensorId, hubId);
                    return saved;
                });
    }

    @Transactional
    public void removeScenario(String hubId, String name) {
        scenarioRepository.findByHubIdAndName(hubId, name).ifPresent(scenario -> {
            cleanupRelations(scenario.getId());
            scenarioRepository.delete(scenario);
            log.info("Сценарий удалён: {} (hub: {})", name, hubId);
        });
    }
}
