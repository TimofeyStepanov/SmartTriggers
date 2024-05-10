package ru.stepanoff.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConsumerDTO {
    @NotBlank
    private String bootstrapServers;

    @NotBlank
    private String kafkaAutoOffsetReset;

    @NotNull
    private Integer requestTimoutMs;

    private String urlTopicName;
    private String callTopicName;
    private String geoTopicName;

    @Min(1)
    private int consumerNumber;
}
