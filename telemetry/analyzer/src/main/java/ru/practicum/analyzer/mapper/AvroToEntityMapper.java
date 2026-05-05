package ru.practicum.analyzer.mapper;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.model.Action;
import ru.practicum.analyzer.model.Condition;
import ru.practicum.analyzer.model.Scenario;
import ru.practicum.analyzer.model.Sensor;
import ru.practicum.analyzer.model.enums.ActionType;
import ru.practicum.analyzer.model.enums.ConditionOperation;
import ru.practicum.analyzer.model.enums.ConditionType;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class AvroToEntityMapper {

    public Sensor toSensor(String hubId, DeviceAddedEventAvro avro) {
        if (avro == null) {
            return null;
        }
        Sensor sensor = new Sensor();
        sensor.setId(avro.getId());
        sensor.setHubId(hubId);

        return sensor;
    }

    public Scenario toScenario(String hubId, ScenarioAddedEventAvro avro) {
        if (avro == null) {
            return null;
        }
        Scenario scenario = new Scenario();
        scenario.setHubId(hubId);
        scenario.setName(avro.getName());
        return scenario;
    }

    public Condition toCondition(ScenarioConditionAvro avro) {
        if (avro == null) {
            return null;
        }
        Condition condition = new Condition();
        condition.setType(map(avro.getType()));
        condition.setOperation(map(avro.getOperation()));
        condition.setValue(unionToInteger(avro.getValue()));
        return condition;
    }

    public Set<Condition> toConditionSet(List<ScenarioConditionAvro> avros) {
        if (avros == null) {
            return null;
        }
        Set<Condition> conditions = new HashSet<>(avros.size());
        for (ScenarioConditionAvro avro : avros) {
            conditions.add(toCondition(avro));
        }
        return conditions;
    }

    public Action toAction(DeviceActionAvro avro) {
        if (avro == null) {
            return null;
        }
        Action action = new Action();
        action.setType(map(avro.getType()));
        action.setValue(unionToInteger(avro.getValue()));
        return action;
    }

    public Integer unionToInteger(Object value) {
        return switch (value) {
            case null -> null;
            case Integer i -> i;
            case Boolean b -> b ? 1 : 0;
            case Long l -> l.intValue();
            case Double d -> d.intValue();
            case String s -> {
                try {
                    yield Integer.parseInt(s);
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            default -> {
                try {
                    yield Integer.parseInt(value.toString());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
        };
    }

    public ConditionType map(ConditionTypeAvro avro) {
        if (avro == null) {
            return null;
        }
        try {
            return ConditionType.valueOf(avro.name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public ConditionOperation map(ConditionOperationAvro avro) {
        if (avro == null) {
            return null;
        }
        try {
            return ConditionOperation.valueOf(avro.name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public ActionType map(ActionTypeAvro avro) {
        if (avro == null) {
            return null;
        }
        try {
            return ActionType.valueOf(avro.name());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
