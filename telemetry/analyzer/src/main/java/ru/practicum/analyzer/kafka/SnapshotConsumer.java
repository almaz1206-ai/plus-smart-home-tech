package ru.practicum.analyzer.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.practicum.analyzer.service.SnapshotProcessor;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotConsumer {
    private final SnapshotProcessor snapshotProcessor;

    @KafkaListener(
            containerFactory = "snapshotKafkaListenerContainerFactory",
            topics = "${spring.kafka.snapshot.consumer-topics}"
    )
    public void listenSnapshot(SensorsSnapshotAvro snapshotAvro) {
        String hubId = snapshotAvro.getHubId();

        try {
            log.info("Получен снимок для hubId: {}", hubId);
            snapshotProcessor.update(snapshotAvro);
        } catch (Exception e) {
            log.error("Ошибка при обработке снимка для hubId: {}", hubId, e);
        }
    }
}
