package ru.stepanoff.model.producer;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProducerDTO {
    private PostgresDTO postgresDTO;
    private KafkaProducerDTO kafkaParamsDTO;

    @NotNull(message = "boolean flag write in file must be not null")
    private Boolean writeInFile;
}
