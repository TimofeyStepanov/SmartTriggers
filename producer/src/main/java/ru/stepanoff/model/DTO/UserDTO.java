package ru.stepanoff.model.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDTO {
    @NotBlank
    private String userId;
}
