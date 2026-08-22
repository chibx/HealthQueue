package com.healthqueue.cache;

import redis.clients.jedis.RedisClient;

public class CacheManager {
    private final RedisClient client;

    public CacheManager(RedisClient client) {
        this.client = client;
    }

}
