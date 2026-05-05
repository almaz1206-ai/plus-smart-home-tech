package ru.practicum.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.analyzer.client.HubRouterClient;
import ru.practicum.analyzer.model.Condition;
import ru.practicum.analyzer.model.Scenario;
import ru.practicum.analyzer.model.ScenarioAction;
import ru.practicum.analyzer.model.ScenarioCondition;
import ru.practicum.analyzer.model.enums.ConditionOperation;
import ru.practicum.analyzer.model.enums.ConditionType;
import ru.practicum.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioEvaluator implements SnapshotProcessor {
    private final ScenarioRepository scenarioRepository;
    private final HubRouterClient hubRouterClient;

    @Override
    @Transactional(readOnly = true)
    public void update(SensorsSnapshotAvro snapshotAvro) {
        String hubId = snapshotAvro.getHubId();
        Map<String, SensorStateAvro> stateMap = snapshotAvro.getSensorsState();

        List<Scenario> scenarios = scenarioRepository.findAllByHubId(hubId);

        for (Scenario scenario : scenarios) {
            if (evaluateScenario(scenario, stateMap)) {
                executeActions(hubId, scenario);
            }
        }
    }

    private boolean evaluateScenario(Scenario scenario, Map<String, SensorStateAvro> stateMap) {
        boolean allOk = scenario.getConditions().stream()
                .allMatch(sc -> evaluateCondition(sc, stateMap));

        if (allOk) {
            log.info("Сценарий сработал: {}", scenario.getName());
        } else {
            log.debug("Сценарий не сработал: {}", scenario.getName());
        }

        return allOk;
    }

    private boolean evaluateCondition(ScenarioCondition sc, Map<String, SensorStateAvro> stateMap) {
        String sensorId = sc.getSensor().getId();
        SensorStateAvro sensorState = stateMap.get(sensorId);

        if (sensorState == null) {
            log.debug("Датчика нет в снапшоте: {}", sensorId);
            return false;
        }

        return extractValue(sensorState, sc.getCondition())
                .map(actual -> {
                    Condition condition = sc.getCondition();
                    return evaluate(condition.getOperation(), actual, condition.getValue());
                })
                .orElse(false);
    }

    private Optional<Integer> extractValue(SensorStateAvro state, Condition condition) {
        Object data = state.getData();
        ConditionType type = condition.getType();

        return switch (data) {
            case ClimateSensorAvro c -> switch (type) {
                case TEMPERATURE -> Optional.of(c.getTemperatureC());
                case HUMIDITY -> Optional.of(c.getHumidity());
                case CO2LEVEL -> Optional.of(c.getCo2Level());
                default -> {
                    log.warn("Тип условия {} неприменим к ClimateSensorAvro", type);
                    yield Optional.empty();
                }
            };
            case LightSensorAvro l -> switch (type) {
                case LUMINOSITY -> Optional.of(l.getLuminosity());
                default -> Optional.empty();
            };
            case MotionSensorAvro m -> switch (type) {
                case MOTION -> Optional.of(m.getMotion() ? 1 : 0);
                default -> Optional.empty();
            };
            case SwitchSensorAvro s -> switch (type) {
                case SWITCH -> Optional.of(s.getState() ? 1 : 0);
                default -> Optional.empty();
            };
            default -> {
                log.warn("Неизвестный тип сенсора: {}", data.getClass().getSimpleName());
                yield Optional.empty();
            }
        };
    }

    private void executeActions(String hubId, Scenario scenario) {
        Set<ScenarioAction> actions = scenario.getActions();

        if (actions == null || actions.isEmpty()) {
            log.debug("Нет действий для сценария: {}", scenario.getName());
            return;
        }

        log.debug("Выполнение {} действий: сценарий={}", actions.size(), scenario.getName());


        for (ScenarioAction action : actions) {
            try {
                hubRouterClient.sendDeviceAction(hubId, scenario.getName(), action);
            } catch (Exception e) {
                log.error("Ошибка отправки: scenario={}, action={}",
                        scenario.getName(), action.getId(), e);
            }
        }
    }

    private boolean evaluate(ConditionOperation op, int actual, int target) {
        return switch (op) {
            case EQUALS -> actual == target;
            case GREATER_THAN -> actual > target;
            case LOWER_THAN -> actual < target;
        };
    }
}
