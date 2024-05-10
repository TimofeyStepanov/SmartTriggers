package ru.stepanoff.repository.producer;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.stepanoff.entity.producer.PostgresEntity;

@Repository
public interface PostgresRepository extends CrudRepository<PostgresEntity, Long> {
}
