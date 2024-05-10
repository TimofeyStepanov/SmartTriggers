package ru.stepanoff.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

@Data
public class Link {
    @NonNull
    private Long userId;

    @NotNull
    private String phone;

    @NonNull
    private String url;

    @NonNull
    private Long time;
}
