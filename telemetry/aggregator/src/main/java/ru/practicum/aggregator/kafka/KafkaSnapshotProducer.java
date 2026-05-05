package ru.practicum.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaSnapshotProducer {
    private final KafkaTemplate<String, SensorsSnapshotAvro> kafkaTemplate;

    @Value("${aggregator.kafka.topic.snapshots}")
    private String snapshotTopic;

    public void sendSync(SensorsSnapshotAvro avro) throws Exception {
        log.debug("Синхронная отправка снапшота для hubId: {}", avro.getHubId());

        kafkaTemplate.send(snapshotTopic, avro.getHubId(), avro).get(30000, TimeUnit.MILLISECONDS);
    }

    public void send(SensorsSnapshotAvro avro) {
        kafkaTemplate.send(snapshotTopic, avro.getHubId(), avro)
                .whenComplete((result, exception) -> {
                    if (exception == null) {
                        log.info("Событие сенсора: {} для hubId: {} успешно отправлено, offset: {}",
                                avro, avro.getHubId(), result.getRecordMetadata().offset());
                    } else {
                        log.error("Не удалось отправить событие сенсора: {} для hubId: {}: {}",
                                    avro, avro.getHubId(), exception.getMessage()
                                );
                    }
                });
    }
}
