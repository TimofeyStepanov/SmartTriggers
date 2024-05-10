package ru.stepanoff.repository.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPooled;
import ru.stepanoff.repository.SetRepository;

import java.util.Optional;

@Component
public class RedisSetRepository implements SetRepository<Long> {
    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;

    private JedisPooled jedis;

    @Override
    public boolean contains(Long key) {
        return getJedis().get(String.valueOf(key)) != null;
    }

    @Override
    public void add(Long key, long secondsToLive) {
        String strKey = String.valueOf(key);
        getJedis().set(strKey, "");
        getJedis().expire(strKey, secondsToLive);
    }

    @Override
    public void clear() {
        getJedis().flushAll();
    }

    private JedisPooled getJedis() {
        jedis = Optional.ofNullable(jedis).orElseGet(() -> new JedisPooled(host, port));
        return jedis;
    }
}
