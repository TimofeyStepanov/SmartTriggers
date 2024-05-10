package ru.stepanoff.repository.consumer;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.stepanoff.entity.consumer.ConsumerEntity;

@Repository
public interface ConsumerRepository extends CrudRepository<ConsumerEntity, Long> {
}
