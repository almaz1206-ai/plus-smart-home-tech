package ru.practicum.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.aggregator.config.AggregatorKafkaConsumerConfig;
import ru.practicum.aggregator.kafka.KafkaSnapshotProducer;
import ru.practicum.aggregator.service.SensorSnapshotService;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaSnapshotProducer producer;
    private final SensorSnapshotService snapshotService;
    private final AggregatorKafkaConsumerConfig config;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new ConcurrentHashMap<>();


    public void start() {

        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            String topic = config.getSensorTopic();
            consumer.subscribe(Collections.singletonList(topic));
            log.info("Подписались на топик: {}, group-id: {}", topic, config.getClientGroupId());
            log.info("Настройки консьюмера: maxPollRecords={}, fetchMaxWaitMs={}, fetchMinBytes={}",
                    config.getMaxPollRecords(), config.getFetchMaxWaitMs(), config.getFetchMinBytes());

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records =
                        consumer.poll(Duration.ofMillis(config.getFetchMaxWaitMs()));
                handleMessage(records);
            }

        } catch (WakeupException e) {
            log.info("Получен сигнал на завершение работы консьюмера");
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщений", e);
        } finally {
            commitOffsets();
        }
    }

    private void handleMessage(ConsumerRecords<String, SensorEventAvro> avroConsumerRecords) {
        for (ConsumerRecord<String, SensorEventAvro> avroConsumerRecord : avroConsumerRecords) {
            try {
                SensorEventAvro sensorEventAvro = avroConsumerRecord.value();

                log.debug("Обработка события: offset={}, partition={}, hubId={}, sensorId={}",
                        avroConsumerRecord.offset(), avroConsumerRecord.partition(), sensorEventAvro.getHubId(), sensorEventAvro.getId());

                snapshotService.updateSnapshot(sensorEventAvro).ifPresent(snapshot -> {
                    producer.send(snapshot);
                    log.debug("Снапшот отправлен для hubId={}, timestamp={}", snapshot.getHubId(), snapshot.getTimestamp());
                    currentOffsets.put(new TopicPartition(avroConsumerRecord.topic(), avroConsumerRecord.partition()),
                            new OffsetAndMetadata(avroConsumerRecord.offset() + 1));
                });
            } catch (Exception e) {
                log.error("Ошибка при обработке сообщения: topic={}, partition={}, offset={}",
                        avroConsumerRecord.topic(), avroConsumerRecord.partition(), avroConsumerRecord.offset());
            }
        }

        if (!currentOffsets.isEmpty()) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.error("Ошибка при коммите оффсетов {}", offsets, exception);
                } else {
                    log.info("Успешно закоммичены оффсеты для {} партиций", offsets.size());
                }
            });
        }
    }

    private void commitOffsets() {
        try {
            consumer.commitSync(currentOffsets);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            log.info("Закрываем консьюмер");
            consumer.close();
        }
    }
}
