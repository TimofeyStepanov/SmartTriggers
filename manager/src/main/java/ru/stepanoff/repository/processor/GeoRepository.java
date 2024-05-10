package ru.stepanoff.repository.processor;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.stepanoff.entity.processor.GeoEntity;

@Repository
public interface GeoRepository extends CrudRepository<GeoEntity, Long> {
}
