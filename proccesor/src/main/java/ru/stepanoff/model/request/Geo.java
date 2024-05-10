package ru.stepanoff.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Geo {

    @NotNull
    private Long userId;

    @NotNull
    private String phone;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotNull
    private Long time;
}
