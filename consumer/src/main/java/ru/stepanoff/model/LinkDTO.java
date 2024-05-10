package ru.stepanoff.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class LinkDTO {
    @NonNull
    private Long userId;

    @NotNull
    private String phone;

    @NonNull
    private String url;

    @NonNull
    private Long time;
}
