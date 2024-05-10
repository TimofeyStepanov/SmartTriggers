package ru.stepanoff.repository;

public interface SetRepository<K> {
    boolean contains(K key);
    void add(K key, long secondsToLive);
    void clear();
}
