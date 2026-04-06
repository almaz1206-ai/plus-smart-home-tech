package ru.practicum.kafka.telemetry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.kafka.telemetry.config.KafkaProducerConfig;
import ru.practicum.kafka.telemetry.mapper.EventMapper;
import ru.practicum.kafka.telemetry.model.hub.*;
import ru.practicum.kafka.telemetry.model.sensor.*;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorServiceImpl implements CollectorService {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProducerConfig config;
    private final EventMapper eventMapper;

    @Override
    public void sendSensorEvent(SensorEvent sensorEvent) {
        if (sensorEvent == null) {
            log.error("Попытка отправить null sensorEvent");
            throw new IllegalArgumentException("SensorEvent не может быть null");
        }

        String sensorTopic = config.getSensorTopic();

        SensorEventAvro sensorEventAvro = switch (sensorEvent) {
            case ClimateSensorEvent climateEvent -> eventMapper.toAvro(climateEvent);
            case LightSensorEvent lightEvent -> eventMapper.toAvro(lightEvent);
            case MotionSensorEvent motionEvent -> eventMapper.toAvro(motionEvent);
            case SwitchSensorEvent switchEvent -> eventMapper.toAvro(switchEvent);
            case TemperatureSensorEvent temperatureEvent -> eventMapper.toAvro(temperatureEvent);
            default -> throw new IllegalArgumentException("Неподдерживаемый тип сенсорного события: " +
                    sensorEvent.getType());
        };

        log.info("В топик: {} отправляется событие сенсора: {}", sensorTopic, sensorEventAvro);

        long eventTimestamp = sensorEventAvro.getTimestamp().toEpochMilli();

        kafkaTemplate.send(sensorTopic, null, eventTimestamp, sensorEventAvro.getHubId(), sensorEventAvro)
                .whenComplete((result, exception) -> {
                    if (exception == null) {
                        log.info("Событие сенсора для hubId: {} успешно отправлено, offset: {}",
                                sensorEventAvro.getHubId(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Не удалось отправить событие сенсора для hubId: {}: {}",
                                sensorEventAvro.getHubId(),
                                exception.getMessage());
                    }
                });
    }

    @Override
    public void sendHubEvent(HubEvent hubEvent) {
        String hubTopic = config.getHubTopic();

        HubEventAvro hubEventAvro = switch (hubEvent) {
            case DeviceAddedEvent deviceAddedEvent -> eventMapper.toAvro(deviceAddedEvent);
            case DeviceRemovedEvent deviceRemovedEvent -> eventMapper.toAvro(deviceRemovedEvent);
            case ScenarioAddedEvent scenarioAddedEvent -> eventMapper.toAvro(scenarioAddedEvent);
            case ScenarioRemovedEvent scenarioRemovedEvent -> eventMapper.toAvro(scenarioRemovedEvent);
            default -> throw new IllegalArgumentException("Неподдерживаемый тип события хаба: " + hubEvent.getType());
        };

        log.info("В топик: {} отправляется событие хаба: {}", hubTopic, hubEventAvro);

        long eventTimestamp = hubEventAvro.getTimestamp().toEpochMilli();

        kafkaTemplate.send(hubTopic, null, eventTimestamp, hubEventAvro.getHubId(), hubEventAvro)
                .whenComplete((result, exception) -> {
                    if (exception == null) {
                        log.info("Событие хаба для hubId: {} успешно отправлено, offset: {}",
                                hubEventAvro.getHubId(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Не удалось отправить событие хаба для hubId: {}: {}",
                                hubEventAvro.getHubId(),
                                exception.getMessage());
                    }
                });
    }
}
