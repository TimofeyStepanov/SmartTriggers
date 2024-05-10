package ru.stepanoff.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Call {
    @NotNull
    private Long userId;

    @NotNull
    private String phoneA;

    @NotNull
    private String phoneB;

    @NotNull
    private Long time; // timestamp (long = миллисекунды c 1970)
}
