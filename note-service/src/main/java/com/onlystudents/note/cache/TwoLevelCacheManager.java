package com.onlystudents.note.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class TwoLevelCacheManager implements CacheManager {

    private final Map<String, Cache> cacheMap = new LinkedHashMap<>();

    public TwoLevelCacheManager(Map<String, CacheConfig> cacheConfigs,
                                 CaffeineCacheManager caffeineCacheManager,
                                 RedisTemplate<String, Object> redisTemplate) {
        // 初始化所有缓存
        cacheConfigs.forEach((name, config) -> {
            if (config.isTwoLevel()) {
                // 二级缓存： Caffeine + Redis
                Cache caffeine = caffeineCacheManager.getCache(name);
                if (caffeine == null) {
                    caffeine = createCaffeineCache(name, config.getCaffeineTTLMinutes());
                }
                TwoLevelCache twoLevelCache = new TwoLevelCache(
                    name,
                    caffeine,
                    redisTemplate,
                    config.getCaffeineTTLMinutes(),
                    config.getRedisTTLMinutes()
                );
                cacheMap.put(name, twoLevelCache);
            } else {
                // 一级缓存：仅 Caffeine
                Cache caffeine = createCaffeineCache(name, config.getCaffeineTTLMinutes());
                cacheMap.put(name, caffeine);
            }
        });

    }

    private Cache createCaffeineCache(String name, long ttlMinutes) {
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES);

        return new CaffeineCache(name, caffeine.build());
    }

    @Override
    public Cache getCache(String name) {
        return cacheMap.get(name);
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(cacheMap.keySet());
    }

    public static class CacheConfig {
        private long caffeineTTLMinutes = 1;
        private long redisTTLMinutes = 5;
        private boolean twoLevel = true;

        public CacheConfig() {}

        public CacheConfig caffeineTTL(long minutes) {
            this.caffeineTTLMinutes = minutes;
            return this;
        }

        public CacheConfig redisTTL(long minutes) {
            this.redisTTLMinutes = minutes;
            return this;
        }

        public CacheConfig twoLevel(boolean twoLevel) {
            this.twoLevel = twoLevel;
            return this;
        }

        public long getCaffeineTTLMinutes() {
            return caffeineTTLMinutes;
        }

        public long getRedisTTLMinutes() {
            return redisTTLMinutes;
        }

        public boolean isTwoLevel() {
            return twoLevel;
        }
    }
}
