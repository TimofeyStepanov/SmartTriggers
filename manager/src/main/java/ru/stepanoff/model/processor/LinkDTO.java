package ru.stepanoff.model.processor;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LinkDTO {
    @NotBlank(message = "Empty url")
    private String url;
}
