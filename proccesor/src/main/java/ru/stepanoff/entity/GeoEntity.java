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
public class GeoEntity {
    private UUID id;

    private Long userId;

    private String phone;

    private Double latitude;

    private Double longitude;

    private Integer radius;

    private Timestamp time;
}
