package ru.stepanoff.model.consumer;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ConsumerDTO {
    @NotBlank(message = "Empty bootstrapServers")
    private final String bootstrapServers;

    @NotNull(message = "Empty requestTimoutMs")
    @Min(value = 1, message = "Too small requestTimoutMs")
    private final Long requestTimoutMs;

    @NotNull(message =  "Empty kafkaAutoOffsetReset")
    @Pattern(regexp = "^(LATEST|EARLIEST)$", message = "KafkaAutoOffsetReset must be in {(LATEST, EARLIEST}")
    private final String kafkaAutoOffsetReset;

    // topics
    private final String urlTopicName;
    private final String callTopicName;
    private final String geoTopicName;

    @Min(value = 1, message = "consumerNumber must be > 0")
    private Integer consumerNumber;
}
