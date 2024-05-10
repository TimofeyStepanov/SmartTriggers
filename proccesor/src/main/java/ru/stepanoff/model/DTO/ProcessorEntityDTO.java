package ru.stepanoff.model.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProcessorEntityDTO {
    @NotNull
    private Long triggerIntervalInSeconds;

    private String phoneB;

    private Double latitude;
    private Double longitude;
    private Integer radiusMeters;
    private Long geoIntervalInSeconds;

    private Integer clickNumber;
    private String links;
}
