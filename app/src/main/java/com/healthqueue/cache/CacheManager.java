package com.healthqueue.cache;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.healthqueue.models.Values.GetNearestBranches;
import com.healthqueue.models.jooq.HealthQueueDB;
import com.healthqueue.utils.Utils;
import com.healthqueue.utils.Utils.GoReturn;

import redis.clients.jedis.RedisClient;

public class CacheManager {
    private final RedisClient client;
    private final HealthQueueDB db = Utils.getDB();
    private final SingleFlight<String, GoReturn<Object>> singleFlight = new SingleFlight<>();
    private final ObjectMapper mapper = Utils.MAPPER;

    public static class CacheException extends Exception {
        public CacheException(Throwable cause) {
            super(cause);
        }
    }

    public CacheManager(RedisClient client) {
        this.client = client;
    }

    public List<GetNearestBranches> getNearestBranches(double userLongitude, double userLatitude)
            throws Exception {
        final String cacheKey = Constants.GET_NEAREST_BRANCHES(userLongitude, userLatitude);
        GoReturn<?> flightResult = singleFlight.doCall(cacheKey, () -> {
            return Utils.tryGo(() -> {
                List<GetNearestBranches> result = null;
                String value = client.get(cacheKey);
                if (value == null) {
                    List<GetNearestBranches> fetched = db.organizations().getNearestBranches(userLongitude,
                            userLatitude);
                    result = fetched;
                    Utils.executeAsync(() -> {
                        client.set(cacheKey, mapper.writeValueAsString(fetched));
                    });
                } else {
                    result = mapper.readValue(
                            value,
                            mapper.getTypeFactory().constructCollectionType(List.class, GetNearestBranches.class));
                }

                return result;
            });
        });

        if (flightResult.error != null) {
            throw new CacheException(flightResult.error);
        }

        return (List<GetNearestBranches>) flightResult.value;
    }
}
