package ru.practicum.kafka.telemetry.model.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.kafka.telemetry.model.hub.enums.ConditionOperation;
import ru.practicum.kafka.telemetry.model.hub.enums.ConditionType;

@Getter
@Setter
@ToString
public class ScenarioCondition {
    String sensorId;
    ConditionType type;
    ConditionOperation operation;
    Integer value;
}
