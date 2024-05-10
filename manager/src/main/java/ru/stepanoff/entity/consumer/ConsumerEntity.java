package ru.stepanoff.entity.consumer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "consumer")
public class ConsumerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String bootstrapServers;
    private Long requestTimoutMs;
    private String kafkaAutoOffsetReset;

    private String urlTopicName;
    private String callTopicName;
    private String geoTopicName;

    private int consumerNumber;
}


