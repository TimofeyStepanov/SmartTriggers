package ru.stepanoff.entity.processor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "processor")
public class ProcessorEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    private SiteEntity siteEntity;

    @OneToOne
    private CallEntity callEntity;

    @OneToOne
    private GeoEntity geoEntity;

    private Integer intervalInSeconds;
}
