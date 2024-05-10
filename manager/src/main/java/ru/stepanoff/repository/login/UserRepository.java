package ru.stepanoff.repository.login;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.stepanoff.entity.login.UserEntity;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserEntity, Long> {
    Optional<UserEntity> findUserEntityByUserName(String userName);
}
