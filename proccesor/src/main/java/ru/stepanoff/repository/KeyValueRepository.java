package ru.stepanoff.repository;

import ru.stepanoff.exception.WrongKeyException;

public interface KeyValueRepository<K, V> {
    void save(K key, V value);
    V get(K key) throws WrongKeyException;
    void clear();
}