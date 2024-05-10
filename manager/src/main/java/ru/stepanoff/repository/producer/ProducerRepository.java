package ru.stepanoff.repository.producer;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.stepanoff.entity.producer.ProducerEntity;

@Repository
public interface ProducerRepository extends CrudRepository<ProducerEntity, Long> {
}
