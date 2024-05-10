package ru.stepanoff.entity.processor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "geo")
public class GeoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private Double latitude;

    private Double longitude;

    private Integer radiusMeters;

    private Integer intervalInSeconds;
}
