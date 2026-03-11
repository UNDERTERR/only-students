package com.onlystudents.note.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.onlystudents.note.cache.TwoLevelCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class TwoLevelCacheConfig {

    public static final String NOTE_DETAIL_CACHE = "noteDetail";
    public static final String HOT_NOTES_CACHE = "hotNotes";
    public static final String LATEST_NOTES_CACHE = "latestNotes";

    @Bean
    public CaffeineCacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(500)
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }

    @Bean
    @Primary
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                      CaffeineCacheManager caffeineCacheManager) {
        // 配置 RedisTemplate
        RedisTemplate<String, Object> redisTemplate = createRedisTemplate(connectionFactory);

        // 配置各缓存的策略
        Map<String, TwoLevelCacheManager.CacheConfig> cacheConfigs = new HashMap<>();

        // noteDetail: 二级缓存 (Caffeine 1分钟 + Redis 5分钟)
        cacheConfigs.put(NOTE_DETAIL_CACHE, new TwoLevelCacheManager.CacheConfig()
                .caffeineTTL(1)
                .redisTTL(5)
                .twoLevel(true));

        // hotNotes: 一级缓存 (仅 Caffeine 1分钟)
        cacheConfigs.put(HOT_NOTES_CACHE, new TwoLevelCacheManager.CacheConfig()
                .caffeineTTL(1)
                .twoLevel(false));

        // latestNotes: 一级缓存 (仅 Caffeine 1分钟)
        cacheConfigs.put(LATEST_NOTES_CACHE, new TwoLevelCacheManager.CacheConfig()
                .caffeineTTL(1)
                .twoLevel(false));

        return new TwoLevelCacheManager(cacheConfigs, caffeineCacheManager, redisTemplate);
    }

    private RedisTemplate<String, Object> createRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }
}
