package com.onlystudents.note.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class TwoLevelCache implements Cache {

    private final String name;
    private final Cache caffeineCache;
    private final RedisTemplate<String, Object> redisTemplate;
    private final long caffeineTTLMinutes;
    private final long redisTTLMinutes;

    public TwoLevelCache(String name,
                          Cache caffeineCache,
                          RedisTemplate<String, Object> redisTemplate,
                          long caffeineTTLMinutes,
                          long redisTTLMinutes) {
        this.name = name;
        this.caffeineCache = caffeineCache;
        this.redisTemplate = redisTemplate;
        this.caffeineTTLMinutes = caffeineTTLMinutes;
        this.redisTTLMinutes = redisTTLMinutes;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    @Override
    public ValueWrapper get(Object key) {
        Object value = doGet(key);
        return value != null ? new SimpleValueWrapper(value) : null;
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        Object value = doGet(key);
        if (value != null && type != null && !type.isInstance(value)) {
            throw new IllegalStateException("Cached value is not of required type [" + type.getName() + "]: " + value);
        }
        return (T) value;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object value = doGet(key);
        if (value == null) {
            synchronized (this) {
                value = doGet(key);
                if (value == null) {
                    try {
                        value = valueLoader.call();
                        if (value != null) {
                            doPut(key, value);
                        }
                    } catch (Exception e) {
                        throw new ValueRetrievalException(key, valueLoader, e);
                    }
                }
            }
        }
        return (T) value;
    }

    private Object doGet(Object key) {
        String keyStr = key.toString();

        // 1. 查一级缓存 Caffeine
        ValueWrapper caffeineValue = caffeineCache.get(key);
        if (caffeineValue != null) {
            return caffeineValue.get();
        }

        // 2. 查二级缓存 Redis
        try {
            Object redisValue = redisTemplate.opsForValue().get(keyStr);
            if (redisValue != null) {
                // 回填到一级缓存
                caffeineCache.put(key, redisValue);
                return redisValue;
            }
        } catch (Exception e) {
            // Redis 异常不影响正常流程
        }

        return null;
    }

    @Override
    public void put(Object key, Object value) {
        doPut(key, value);
    }

    private void doPut(Object key, Object value) {
        String keyStr = key.toString();

        // 同时写入两层缓存
        caffeineCache.put(key, value);
        try {
            redisTemplate.opsForValue().set(keyStr, value, redisTTLMinutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            // Redis 写入失败不影响流程
        }
    }

    @Override
    public void evict(Object key) {
        String keyStr = key.toString();

        // 同时清除两层缓存
        caffeineCache.evict(key);
        try {
            redisTemplate.delete(keyStr);
        } catch (Exception e) {
            // Redis 删除失败不影响流程
        }
    }

    @Override
    public void clear() {
        // 清空一级缓存
        caffeineCache.clear();

        // 清空二级缓存（需要匹配前缀，这里简化处理）
        try {
            // 注意：生产环境建议使用 Redis 的 SCAN 命令配合前缀删除
            // 这里简化处理，假设 key 前缀与缓存名相关
            redisTemplate.delete(redisTemplate.keys(name + ":*"));
        } catch (Exception e) {
            // Redis 清除失败不影响流程
        }
    }
    public ValueWrapper putIfAbsent(Object key, Object value) {
        String keyStr = key.toString();

        // 尝试写入 Redis（如果不存在）
        Boolean success = redisTemplate.opsForValue().setIfAbsent(keyStr, value, redisTTLMinutes, TimeUnit.MINUTES);
        if (Boolean.TRUE.equals(success)) {
            caffeineCache.put(key, value);
            return null;
        }
        // 存在则返回原有值
        Object existing = doGet(key);
        return existing != null ? new SimpleValueWrapper(existing) : null;
    }
}
