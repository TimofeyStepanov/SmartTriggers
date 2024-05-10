package ru.stepanoff.entity.producer;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "producer")
public class ProducerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    private PostgresEntity postgresEntity;

    @OneToOne
    private KafkaProducerEntity kafkaProducer;

    private boolean writeInFile;
}
