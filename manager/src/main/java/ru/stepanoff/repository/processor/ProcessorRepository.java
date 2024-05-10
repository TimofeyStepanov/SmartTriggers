package ru.stepanoff.repository.processor;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.stepanoff.entity.processor.ProcessorEntity;

@Repository
public interface ProcessorRepository extends CrudRepository<ProcessorEntity, Long> {
}
