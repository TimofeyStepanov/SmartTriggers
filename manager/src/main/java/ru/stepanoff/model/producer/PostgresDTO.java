package ru.stepanoff.model.producer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostgresDTO {
    @NotBlank(message = "Empty postgres url")
    private String url;

    @NotBlank(message = "Empty postgres userName")
    private String nickname;

    @NotBlank(message = "Empty postgres password")
    private String password;

    @NotBlank(message = "Empty table name")
    private String tableName;
}
