package ru.stepanoff.model.processor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class SiteDTO {
    @NotNull(message = "No links")
    @Valid
    private Set<LinkDTO> links;

    @Min(value = 0, message = "ClickNumber must be >= 0")
    private Integer clickNumber;
}
