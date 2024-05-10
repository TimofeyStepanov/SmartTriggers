package ru.stepanoff.service;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import ru.stepanoff.model.login.UserRequest;

@Validated
public interface LoginService {
    String login(@Valid UserRequest userRequest);
}
