package ru.stepanoff.service;

import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import ru.stepanoff.model.DTO.UserDTO;

@Validated
public interface ProducerService {
    void processUser(@Valid UserDTO userDTO);
}
