package com.project.inventory.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test Cache Configuration — Override Redis with In-Memory Cache
 * 
 * This configuration replaces the production Redis cache with a simple
 * in-memory cache for tests. Loaded automatically via @ActiveProfiles("test")
 */
@TestConfiguration
@EnableCaching
public class TestCacheConfig {

    /**
     * Provides in-memory CacheManager for testing
     * - No Redis dependency required
     * - Uses ConcurrentHashMap under the hood
     * - Cache names: "products", "userOrders" (matches production cache names)
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("products", "userOrders");
    }
}
