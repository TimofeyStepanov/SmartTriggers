package ru.stepanoff.component;

import lombok.Builder;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.Closeable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Builder
public class KafkaWriter implements Closeable {
    private static final Map<Short, String> acksCodeAndItsStringValue = Map.of(
            (short)-1, "all",
            (short)0, "none",
            (short)1, "leader"
    );

    private final String bootstrapServers;
    private final String topic;
    private final Integer batchSize;
    private final Integer requestTimoutMs;
    private final Integer bufferMemory;
    private final Long lingerMs;
    private final String acks;

    private KafkaProducer<String, String> kafkaProducer;

    public synchronized void write(String message) {
        kafkaProducer = Optional.ofNullable(kafkaProducer).orElseGet(() -> new KafkaProducer<>(
                Map.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ProducerConfig.CLIENT_ID_CONFIG, UUID.randomUUID().toString(),

                        ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimoutMs,
                        ProducerConfig.BATCH_SIZE_CONFIG, batchSize,
                        ProducerConfig.BUFFER_MEMORY_CONFIG, bufferMemory,
                        ProducerConfig.LINGER_MS_CONFIG, lingerMs,
                        ProducerConfig.ACKS_CONFIG, acks
                ),
                new StringSerializer(),
                new StringSerializer()
        ));
        kafkaProducer.send(new ProducerRecord<>(topic, message));
    }

    @Override
    public void close() {
        if (kafkaProducer == null) {
            return;
        }
        kafkaProducer.close();
        kafkaProducer = null;
    }
}
