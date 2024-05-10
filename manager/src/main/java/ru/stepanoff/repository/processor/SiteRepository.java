package ru.stepanoff.repository.processor;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.stepanoff.entity.processor.SiteEntity;

@Repository
public interface SiteRepository extends CrudRepository<SiteEntity, Long> {
}
