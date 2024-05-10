package ru.stepanoff.entity.producer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "kafka_producer")
public class KafkaProducerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String bootstrapServers;

    private Integer batchSize;

    private Integer requestTimoutMs;

    private Integer bufferMemory;

    private Long lingerMs;

    private Short acks;

    private String topic;
}
