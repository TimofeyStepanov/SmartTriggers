package ru.stepanoff.repository.processor;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.stepanoff.entity.processor.LinkEntity;

@Repository
public interface LinkRepository extends CrudRepository<LinkEntity, Long> {
}
