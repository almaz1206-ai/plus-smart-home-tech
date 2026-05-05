package ru.practicum.aggregator.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.service.SensorSnapshotService;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SensorEventListener {
    private final SensorSnapshotService snapshotService;
    private final KafkaSnapshotProducer producer;

    @KafkaListener(
            topics = "${aggregator.kafka.topic.sensors}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleMessage(ConsumerRecord<Field.Str, SensorEventAvro> record, Acknowledgment acknowledgment) {
        SensorEventAvro sensorEventAvro = record.value();

        log.debug("Обработка события: offset={}, partition={}, hubId={}, sensorId={}",
                record.offset(), record.partition(), sensorEventAvro.getHubId(), sensorEventAvro.getId());

        try {
            Optional<SensorsSnapshotAvro> snapshot = snapshotService.updateSnapshot(sensorEventAvro);

            if (snapshot.isPresent()) {
                SensorsSnapshotAvro snapshotAvro = snapshot.get();

                try {
                    producer.sendSync(snapshotAvro);

                    log.info("Снапшот отправлен для hubId={}, sensorsCount={}, offset={}",
                            snapshotAvro.getHubId(),
                            snapshotAvro.getSensorsState().size(),
                            record.offset());

                    acknowledgment.acknowledge();
                } catch (Exception e) {
                    log.error("Ошибка при отправке снапшота для hubId={}, offset={}",
                            snapshotAvro.getHubId(), record.offset(), e);
                }
            } else {
                acknowledgment.acknowledge();
                log.debug("Снапшот не требует отправки, offset подтвержден: {}", record.offset());
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset(), e);
        }
    }
}
