package com.healthqueue.cache;

import java.util.List;

import com.healthqueue.models.Values.GetNearestBranches;

import redis.clients.jedis.RedisClient;

public class CacheManager {
    private final RedisClient client;

    public CacheManager(RedisClient client) {
        this.client = client;
    }

    public List<GetNearestBranches> getNearestBranches(double userLongitude, double userLatitude) {
        
        return null;
    }
}
