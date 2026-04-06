package ru.practicum.kafka.telemetry.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.kafka.telemetry.model.hub.*;
import ru.practicum.kafka.telemetry.model.hub.enums.ActionType;
import ru.practicum.kafka.telemetry.model.hub.enums.ConditionOperation;
import ru.practicum.kafka.telemetry.model.hub.enums.ConditionType;
import ru.practicum.kafka.telemetry.model.hub.enums.DeviceType;
import ru.practicum.kafka.telemetry.model.sensor.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.List;

@Component
public class EventMapper {
    public SensorEventAvro toAvro(ClimateSensorEvent event) {
        ClimateSensorAvro payload = ClimateSensorAvro.newBuilder()
                .setTemperatureC(event.getTemperatureC())
                .setCo2Level(event.getCo2Level())
                .setHumidity(event.getHumidity())
                .build();

        return SensorEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setId(event.getId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    public SensorEventAvro toAvro(LightSensorEvent event) {
        LightSensorAvro payload = LightSensorAvro.newBuilder()
                .setLuminosity(event.getLuminosity())
                .setLinkQuality(event.getLinkQuality())
                .build();

        return SensorEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setId(event.getId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    public SensorEventAvro toAvro(MotionSensorEvent event) {
        MotionSensorAvro payload = MotionSensorAvro.newBuilder()
                .setLinkQuality(event.getLinkQuality())
                .setMotion(event.getMotion())
                .setVoltage(event.getVoltage())
                .build();

        return SensorEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setId(event.getId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    public SensorEventAvro toAvro(SwitchSensorEvent event) {
        SwitchSensorAvro payload = SwitchSensorAvro.newBuilder()
                .setState(event.getState())
                .build();

        return SensorEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setId(event.getId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    public SensorEventAvro toAvro(TemperatureSensorEvent event) {
        TemperatureSensorAvro payload = TemperatureSensorAvro.newBuilder()
                .setTemperatureC(event.getTemperatureC())
                .setTemperatureF(event.getTemperatureF())
                .build();

        return SensorEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setId(event.getId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    public HubEventAvro toAvro(DeviceAddedEvent event) {
        DeviceTypeAvro deviceTypeAvro = switch (event.getDeviceType()) {
            case DeviceType.MOTION_SENSOR -> DeviceTypeAvro.MOTION_SENSOR;
            case DeviceType.CLIMATE_SENSOR -> DeviceTypeAvro.CLIMATE_SENSOR;
            case DeviceType.LIGHT_SENSOR -> DeviceTypeAvro.LIGHT_SENSOR;
            case DeviceType.SWITCH_SENSOR -> DeviceTypeAvro.SWITCH_SENSOR;
            case DeviceType.TEMPERATURE_SENSOR -> DeviceTypeAvro.TEMPERATURE_SENSOR;
        };

        DeviceAddedEventAvro payload = DeviceAddedEventAvro.newBuilder()
                .setId(event.getId())
                .setType(deviceTypeAvro)
                .build();

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    public HubEventAvro toAvro(DeviceRemovedEvent event) {
        DeviceRemovedEventAvro payload = DeviceRemovedEventAvro.newBuilder()
                .setId(event.getId())
                .build();

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    public HubEventAvro toAvro(ScenarioAddedEvent event) {
        List<ScenarioConditionAvro> conditionsAvro = event.getConditions().stream()
                .map(this::toScenarioConditionAvro)
                .toList();

        List<DeviceActionAvro> actionsAvro = event.getActions().stream()
                .map(this::toDeviceActionAvro)
                .toList();

        ScenarioAddedEventAvro payload = ScenarioAddedEventAvro.newBuilder()
                .setName(event.getName())
                .setActions(actionsAvro)
                .setConditions(conditionsAvro)
                .build();

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    public HubEventAvro toAvro(ScenarioRemovedEvent event) {
        ScenarioRemovedEventAvro payload = ScenarioRemovedEventAvro.newBuilder()
                .setName(event.getName())
                .build();

        return HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp())
                .setPayload(payload)
                .build();
    }

    private ScenarioConditionAvro toScenarioConditionAvro(ScenarioCondition condition) {
        ConditionTypeAvro conditionTypeAvro = switch (condition.getType()) {
            case ConditionType.CO2LEVEL -> ConditionTypeAvro.CO2LEVEL;
            case ConditionType.HUMIDITY -> ConditionTypeAvro.HUMIDITY;
            case ConditionType.LUMINOSITY -> ConditionTypeAvro.LUMINOSITY;
            case ConditionType.MOTION -> ConditionTypeAvro.MOTION;
            case ConditionType.SWITCH -> ConditionTypeAvro.SWITCH;
            case ConditionType.TEMPERATURE -> ConditionTypeAvro.TEMPERATURE;
        };

        ConditionOperationAvro conditionOperationAvro = switch (condition.getOperation()) {
            case ConditionOperation.EQUALS -> ConditionOperationAvro.EQUALS;
            case ConditionOperation.GREATER_THAN -> ConditionOperationAvro.GREATER_THAN;
            case ConditionOperation.LOWER_THAN -> ConditionOperationAvro.LOWER_THAN;
        };

        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(conditionTypeAvro)
                .setValue(condition.getValue())
                .setOperation(conditionOperationAvro)
                .build();
    }

    private DeviceActionAvro toDeviceActionAvro(DeviceAction action) {
        ActionTypeAvro actionTypeAvro = switch (action.getType()) {
            case ActionType.ACTIVATE -> ActionTypeAvro.ACTIVATE;
            case ActionType.DEACTIVATE -> ActionTypeAvro.DEACTIVATE;
            case ActionType.INVERSE -> ActionTypeAvro.INVERSE;
            case ActionType.SET_VALUE -> ActionTypeAvro.SET_VALUE;
        };

        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setValue(action.getValue())
                .setType(actionTypeAvro)
                .build();
    }
}
