package ru.stepanoff.model.login;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {
    @NotBlank(message = "Name can't be blank")
    private String name;

    @NotBlank(message = "Password can't be blank")
    private String password;
}
