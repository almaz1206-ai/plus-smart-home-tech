package ru.practicum.kafka.telemetry.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.practicum.kafka.telemetry.config.KafkaProducerConfig;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventProducer {
    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;
    private final KafkaProducerConfig config;

    public void sendSensorEvent(SensorEventAvro sensorEvent) {
        if (sensorEvent == null) {
            log.error("Попытка отправить null sensorEvent");
            throw new IllegalArgumentException("SensorEvent не может быть null");
        }

        String sensorTopic = config.getSensorTopic();

        log.info("В топик: {} отправляется событие сенсора: {}", sensorTopic, sensorEvent);

        long eventTimestamp = sensorEvent.getTimestamp().toEpochMilli();

        sendEvent(sensorTopic, eventTimestamp, sensorEvent.getHubId(), sensorEvent, "Событие сенсора");
    }

    public void sendHubEvent(HubEventAvro hubEvent) {
        String hubTopic = config.getHubTopic();

        log.info("В топик: {} отправляется событие хаба: {}", hubTopic, hubEvent);

        long eventTimestamp = hubEvent.getTimestamp().toEpochMilli();

        sendEvent(hubTopic, eventTimestamp, hubEvent.getHubId(), hubEvent, "Событие хаба");
    }

    private void sendEvent(String topic, long timestamp, String hubId, SpecificRecordBase event, String eventType) {
        kafkaTemplate.send(topic, null, timestamp, hubId, event)
                .whenComplete((result, exception) -> {
                    if (exception == null) {
                        log.info("{} для hubId: {} успешно отправлен в топик {}, offset: {}",
                                eventType, hubId, topic, result.getRecordMetadata().offset());
                    } else {
                        log.error("Не удалось отправить {} для hubId: {} в топик {}: {}",
                                eventType, hubId, topic, exception.getMessage());
                    }
                });
    }
}
