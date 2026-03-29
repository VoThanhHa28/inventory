package com.project.inventory.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Global Cache Configuration — Redis-backed Distributed Cache
 * 
 * Features:
 * - Distributed shared cache across multiple app instances (app1, app2 share same Redis)
 * - Data consistency without duplicate caches
 * - TTL: 10 minutes (600 seconds) to match backend data refresh rates
 * - JSON serialization for better compatibility and debugging
 * - Supports @Cacheable, @CacheEvict, @CachePut annotations
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Redis-based Cache Manager configuration with JSON serialization
     * - Cache names: "products" (product lists), "userOrders" (user order history)
     * - TTL: 10 minutes - stale data automatically evicted from Redis
     * - Null caching disabled: prevents caching null results
     * - JSON serialization: compatible with all object types
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Setup Jackson ObjectMapper for JSON serialization
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        // Setup Jackson2JsonRedisSerializer for cache values
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = 
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        // Configure Redis cache with 10-minute TTL (600 seconds) and JSON serialization
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(600))  // 10 minutes
                .disableCachingNullValues()         // Don't cache null results
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}
