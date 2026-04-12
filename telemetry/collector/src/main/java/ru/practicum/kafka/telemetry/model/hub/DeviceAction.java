package ru.practicum.kafka.telemetry.model.hub;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.kafka.telemetry.model.hub.enums.ActionType;

@Getter
@Setter
@ToString
public class DeviceAction {
    String sensorId;
    ActionType type;
    Integer value;
}
