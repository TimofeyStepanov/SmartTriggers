package ru.stepanoff.model.processor;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CallDTO {
    @NotBlank(message = "Empty phone")
    private String phoneB;
}
