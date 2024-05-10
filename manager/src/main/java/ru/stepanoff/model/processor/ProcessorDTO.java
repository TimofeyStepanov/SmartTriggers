package ru.stepanoff.model.processor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ProcessorDTO {
    private SiteDTO siteDTO;
    private CallDTO callInfo;
    private GeoDTO geoInfo;

    @Min(value = 1, message = "Too small interval")
    @Max(value = 30 * 24 * 60 * 60, message = "Too large interval")
    private Integer intervalInSeconds;
}
