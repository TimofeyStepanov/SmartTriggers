package ru.stepanoff.model.producer;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class KafkaProducerDTO {
    @NotNull
    @NotBlank(message = "No bootstrapServers")
    private String bootstrapServers;

    @NotNull(message = "No batchSize")
    @Min(value = 1, message = "Too small batchSize")
    private Integer batchSize;

    @NotNull(message = "No requestTimoutMs")
    @Min(value = 1, message = "Too small requestTimoutMs")
    private Integer requestTimoutMs;

    @NotNull(message = "No bufferMemory")
    @Min(value = 1, message = "Too small bufferMemory")
    private Integer bufferMemory;

    @NotNull(message = "No lingerMs")
    @Min(value = 0, message = "Too small lingerMs")
    private Long lingerMs;

    @NotNull(message = "No acks")
    @Pattern(regexp = "^(-1|0|1)$", message = "Wrong acks. Choose {-1, 0, 1}")
    private String acks;

    @NotBlank(message = "Empty topic")
    private String topic;
}
