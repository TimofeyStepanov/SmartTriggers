package ru.stepanoff.model.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProducerEntityDTO {
    private String bootstrapServers;
    private Integer batchSize;
    private Integer requestTimoutMs;
    private Integer bufferMemory;
    private Long lingerMs;
    private String acks;
    private String topic;

    private String postgresUrl;
    private String nickname;
    private String password;
    private String tableName;

    @NotNull
    private Boolean writeInFile;
}
