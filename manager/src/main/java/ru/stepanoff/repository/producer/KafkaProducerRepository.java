package ru.stepanoff.repository.producer;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.stepanoff.entity.producer.KafkaProducerEntity;

@Repository
public interface KafkaProducerRepository extends CrudRepository<KafkaProducerEntity, Long> {
}
