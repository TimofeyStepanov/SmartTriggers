package ru.stepanoff.repository.processor;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.stepanoff.entity.processor.CallEntity;

@Repository
public interface CallRepository extends CrudRepository<CallEntity, Long> {
}
