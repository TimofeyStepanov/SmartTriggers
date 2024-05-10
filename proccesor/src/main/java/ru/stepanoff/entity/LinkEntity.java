package ru.stepanoff.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkEntity {
    private UUID id = UUID.randomUUID();

    private Long userId;

    private String phone;

    private String url;

    private Timestamp time;
}
