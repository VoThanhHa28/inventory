package com.project.inventory.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Global Cache Configuration
 * 
 * Centralized caching strategy for the entire application
 * Uses Caffeine cache provider with configurable TTLs
 * 
 * Cache Policies:
 * - products: Product list cache with 10s TTL (eventual consistency acceptable)
 * - inventory: NOT CACHED (critical data, must be real-time)
 * - product_details: NOT CACHED (always live data)
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Caffeine Cache Manager bean
     * Supports multiple cache definitions with different TTLs
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("products");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .recordStats()
        );
        return cacheManager;
    }
}
