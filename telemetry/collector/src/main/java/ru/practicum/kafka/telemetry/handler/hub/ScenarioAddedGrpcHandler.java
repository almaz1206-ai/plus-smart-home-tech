package ru.practicum.kafka.telemetry.handler.hub;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.telemetry.producer.KafkaEventProducer;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScenarioAddedGrpcHandler implements GrpcHubEventHandler {
    private final KafkaEventProducer producer;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    public void handle(HubEventProto eventProto) {
        List<ScenarioConditionAvro> conditionsAvro = eventProto.getScenarioAdded().getConditionList()
                .stream()
                .map(this::toScenarioConditionAvro)
                .toList();

        List<DeviceActionAvro> actionsAvro = eventProto.getScenarioAdded().getActionList()
                .stream()
                .map(this::toDeviceActionAvro)
                .toList();

        ScenarioAddedEventAvro payload = ScenarioAddedEventAvro.newBuilder()
                .setName(eventProto.getScenarioAdded().getName())
                .setActions(actionsAvro)
                .setConditions(conditionsAvro)
                .build();


        HubEventAvro avro = HubEventAvro.newBuilder()
                .setHubId(eventProto.getHubId())
                .setTimestamp(Instant.ofEpochSecond(
                        eventProto.getTimestamp().getSeconds(),
                        eventProto.getTimestamp().getNanos()
                ))
                .setPayload(payload)
                .build();
        producer.sendHubEvent(avro);
    }

    private ScenarioConditionAvro toScenarioConditionAvro(ScenarioConditionProto condition) {
        ConditionTypeAvro conditionTypeAvro = switch (condition.getType()) {
            case ConditionTypeProto.CO2LEVEL -> ConditionTypeAvro.CO2LEVEL;
            case ConditionTypeProto.HUMIDITY -> ConditionTypeAvro.HUMIDITY;
            case ConditionTypeProto.LUMINOSITY -> ConditionTypeAvro.LUMINOSITY;
            case ConditionTypeProto.MOTION -> ConditionTypeAvro.MOTION;
            case ConditionTypeProto.SWITCH -> ConditionTypeAvro.SWITCH;
            case ConditionTypeProto.TEMPERATURE -> ConditionTypeAvro.TEMPERATURE;
            default -> null;
        };

        ConditionOperationAvro conditionOperationAvro = switch (condition.getOperation()) {
            case ConditionOperationProto.EQUALS -> ConditionOperationAvro.EQUALS;
            case ConditionOperationProto.GREATER_THAN -> ConditionOperationAvro.GREATER_THAN;
            case ConditionOperationProto.LOWER_THAN -> ConditionOperationAvro.LOWER_THAN;
            default -> null;
        };

        Object value = extractValue(condition);

        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(conditionTypeAvro)
                .setValue(value)
                .setOperation(conditionOperationAvro)
                .build();
    }

    private Object extractValue(ScenarioConditionProto conditionProto) {
        return switch (conditionProto.getValueCase()) {
            case BOOL_VALUE -> conditionProto.getBoolValue();
            case INT_VALUE -> conditionProto.getIntValue();
            case VALUE_NOT_SET -> throw new IllegalArgumentException("Значение для условия не задано");
            default -> throw new IllegalArgumentException("Неизвестный тип значения: " + conditionProto.getValueCase());
        };
    }

    private DeviceActionAvro toDeviceActionAvro(DeviceActionProto actionProto) {
        ActionTypeAvro actionTypeAvro = switch (actionProto.getType()) {
            case ActionTypeProto.ACTIVATE -> ActionTypeAvro.ACTIVATE;
            case ActionTypeProto.DEACTIVATE -> ActionTypeAvro.DEACTIVATE;
            case ActionTypeProto.INVERSE -> ActionTypeAvro.INVERSE;
            case ActionTypeProto.SET_VALUE -> ActionTypeAvro.SET_VALUE;
            default -> throw new IllegalArgumentException("Неизвестный тип действия");
        };

        return DeviceActionAvro.newBuilder()
                .setSensorId(actionProto.getSensorId())
                .setValue(actionProto.getValue())
                .setType(actionTypeAvro)
                .build();
    }
}
