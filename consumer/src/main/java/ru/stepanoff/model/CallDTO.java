package ru.stepanoff.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CallDTO {
    @NotNull
    private Long userId;

    @NotNull
    private String phoneA;

    @NotNull
    private String phoneB;

    @NotNull
    private Long time;
}
