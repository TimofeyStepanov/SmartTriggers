package ru.stepanoff.entity;

import lombok.*;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CallEntity {

    private UUID id = UUID.randomUUID();

    private Long userId;

    private String phoneA;

    private String phoneB;

    private Timestamp time;
}
