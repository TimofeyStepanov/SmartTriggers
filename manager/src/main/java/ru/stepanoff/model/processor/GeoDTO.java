package ru.stepanoff.model.processor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GeoDTO {

    @Min(0)
    @Max(90)
    private Double latitude;

    @Min(value = 0)
    @Max(90)
    private Double longitude;

    @Min(value = 1, message = "Radius must be > 0")
    @Max(value = 10000, message = "Too large radius")
    private Integer radiusMeters;

    @Min(value = 1, message = "Geo interval in seconds must be > 0")
    @NotNull(message = "Empty geo interval")
    private Long intervalInSeconds;
}
