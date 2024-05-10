package ru.stepanoff.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GeoDTO {
    @NotNull
    private Long userId;

    @NotNull
    private String phone;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotNull
    private Integer radius;

    @NotNull
    private Long time;
}
