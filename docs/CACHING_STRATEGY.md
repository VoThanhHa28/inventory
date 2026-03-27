# Caching Strategy - Redis Implementation

## 📋 Overview

This document describes the complete caching strategy for the E-commerce & Inventory system using **Redis** for distributed caching across multiple application instances.

**Goal**: Optimize read-heavy endpoints with minimal inconsistency risk, suitable for phased rollouts and interview scenarios.

---

## 🎯 Option B: Smart Caching Strategy

### Selected Endpoints to Cache

#### **1. ✅ GET /api/products (Existing)**
- **Purpose**: List all products with pagination
- **TTL**: 10 seconds
- **Cache Key**: Auto-generated from Spring Cache `@Cacheable` with pagination params
- **Invalidation**: Full cache evict on ANY product write (CREATE/UPDATE/DELETE)
- **Read Frequency**: HIGH (daily thousands of requests)
- **Write Frequency**: LOW (admin-only, ~2-5 times per day)
- **Stale Data Tolerance**: YES (10s is acceptable for product listing)

**Rationale**: 
- Asymmetric 100:1 read-to-write ratio
- Products controlled by trusted admins only
- Users expect eventual consistency for product list

**Implementation**:
```java
@GetMapping
@Cacheable(value = "products")  // Caches by pagination params
public ResponseEntity<ApiResponse<Page<ProductResponseDTO>>> getProducts(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
) { ... }

@PostMapping
@CacheEvict(value = "products", allEntries = true)  // Clears all product cache
public ResponseEntity<ApiResponse<ProductResponseDTO>> createProduct(...) { ... }

@PutMapping("/{id}")
@CacheEvict(value = "products", allEntries = true)  // Clears all product cache
public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(...) { ... }

@DeleteMapping("/{id}")
@CacheEvict(value = "products", allEntries = true)  // Clears all product cache
public ResponseEntity<ApiResponse<Void>> deleteProduct(...) { ... }
```

---

#### **2. ✅ NEW: GET /api/orders/my-orders (New Addition)**
- **Purpose**: List authenticated user's orders with pagination
- **TTL**: 60 seconds (longer tolerance, users refresh manually)
- **Cache Key**: `my-orders:{userId}:{page}:{size}` (compound key)
- **Invalidation**: Flush all pagination variants for affected user on order status update
- **Read Frequency**: MEDIUM-HIGH (users check order history regularly)
- **Write Frequency**: LOW (status updates by admin only)
- **Stale Data Tolerance**: YES (60s for order history, higher freshness sensitivity than product list)

**Rationale**:
- Users tolerate 60 seconds of stale data on order history
- Users can manually refresh for latest status
- Order list doesn't change as frequently as product writes
- Cache key includes userId → no cross-user data leakage

**Implementation**:

```java
// File: src/main/java/com/project/inventory/controller/OrderController.java

@GetMapping("/my-orders")
@Cacheable(
    value = "userOrders",
    key = "T(java.lang.String).format('my-orders:%d:page:%d:size:%d', " +
          "@authentication.principal.user.id, #page, #size)"
)
@Transactional(readOnly = true)
public ResponseEntity<ApiResponse<Page<OrderResponseDTO>>> getUserOrders(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
) {
    Long userId = getCurrentUserId();
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<OrderResponseDTO> userOrders = orderService.getUserOrders(userId, pageable);
    return ResponseEntity.ok(ApiResponse.success(userOrders, "User orders retrieved successfully"));
}

// File: src/main/java/com/project/inventory/service/impl/OrderServiceImpl.java

@Override
@Transactional(readOnly = true)
public Page<OrderResponseDTO> getUserOrders(Long userId, Pageable pageable) {
    Page<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    return orders.map(OrderResponseDTO::from);
}

// File: src/main/java/com/project/inventory/repository/OrderRepository.java

Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
```

**Cache Invalidation on Status Update**:

```java
// File: src/main/java/com/project/inventory/controller/OrderController.java

@PutMapping("/{orderId}/status")
@PreAuthorize("hasRole('ADMIN')")
@CacheEvict(
    value = "userOrders",
    allEntries = true  // Clear ALL user order caches (all users, all pages)
)
public ResponseEntity<ApiResponse<OrderResponseDTO>> updateOrderStatus(
    @PathVariable Long orderId,
    @RequestParam String status
) {
    OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
    OrderResponseDTO updatedOrder = orderService.updateOrderStatus(orderId, newStatus);
    return ResponseEntity.ok(ApiResponse.success(updatedOrder, "Order status updated"));
}
```

**Note on Invalidation Strategy**:
- Current implementation: Flush ALL user order pages on ANY status change
- Future optimization: Use SpEL to target specific userId
  ```java
  @CacheEvict(
      value = "userOrders",
      key = "T(java.lang.String).format('my-orders:%d:*', #order.user.id)"  // Pseudo-code
  )
  ```
- For now, `allEntries = true` is simpler and acceptable for low-frequency writes

---

## ❌ Endpoints NOT Cached (And Why)

### Intentionally Excluded from Caching

| Endpoint | Reason |
|----------|--------|
| **GET /api/products/{id}** | Different data shape (full detail vs list summary); dual cache invalidation complexity |
| **GET /api/inventory** | Real-time atomic operations; stale cache = overselling risk |
| **GET /api/inventory/{id}** | Part of transactional inventory system; atomicity contraindicated |
| **GET /api/inventory/product/{productId}** | Real-time stock levels; must be accurate |
| **GET /api/orders/{orderId}** | Highly mutable (status updates frequent); per-user access control; high invalidation overhead |

**Core Principle**: *"Cache high-read, low-write endpoints with acceptable staleness. Never cache transactional or real-time critical data."*

---

## 🏗️ Configuration

### CacheConfig.java Updates

```java
// File: src/main/java/com/project/inventory/configuration/CacheConfig.java

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new Jackson2JsonRedisSerializer<>(Object.class)
                )
            )
            .entryTtl(Duration.ofSeconds(10));  // Default TTL

        return RedisCacheManager.create(
            RedisCacheManagerBuilder
                .fromConnectionFactory(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(Map.ofEntries(
                    Map.entry("products", 
                        defaultConfig.entryTtl(Duration.ofSeconds(10))
                    ),
                    Map.entry("userOrders",
                        defaultConfig.entryTtl(Duration.ofSeconds(60))  // 60s TTL
                    )
                ))
                .build()
        );
    }
}
```

---

## 📊 Caching Impact Analysis

### Before Caching (Current State)
- Every product list request → Database query
- Every user order history request → Database query
- Database load increases with user traffic

### After Caching (Option B)
| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| Product List Latency | ~150-200ms | ~5-10ms (cache hit) | **20-30x faster** |
| Database Queries/sec (products) | 100 req/s | ~10 req/s (10s TTL)⁵ | **90% reduction** |
| User Orders Latency | ~120-150ms | ~3-8ms (cache hit) | **15-25x faster** |
| Memory usage (Redis) | 0 | ~50-100MB (1M products + orders) | Acceptable |

**Note**: Latency improvements vary by system load, database indices, and network conditions.

---

## 🔄 Invalidation Strategy Overview

| Scenario | Action | Effect |
|----------|--------|--------|
| Admin creates product | Clear all "products" cache | Product list updated after 10s TTL or immediate refresh |
| Admin updates product | Clear all "products" cache | Product list updated after 10s TTL or immediate refresh |
| Admin deletes product | Clear all "products" cache | Product list updated after 10s TTL or immediate refresh |
| Admin updates order status | Clear all "userOrders" cache | All users' order history refreshed on next fetch |
| User views product list | Check "products" cache first | Returns cache if exists, else DB query + cache |
| User checks order history | Check "userOrders:{userId}" cache | Returns cache if exists, else DB query + cache |

---

## 🎯 Interview-Ready Explanation

### Q: Why cache products but not inventory?

**Answer**: 
> *"Products are read-heavy (hundreds of reads per write) and updated only by trusted admins. Caching for 10 seconds is acceptable because users expect eventual consistency for product listings.*
>
> *Inventory, however, is transactional and atomic. Users expect stock numbers to be accurate before checkout. Caching creates an overselling risk: a user sees 'In Stock' but inventory reserve fails because the cache was stale.*
>
> *So we cache high-read, low-write, low-consistency-requirement data (products). We don't cache real-time, transactional, or user-critical data (inventory, order detail)."*

### Q: Why user-scoped cache keys for order history?

**Answer**:
> *"User-scoped keys ensure no cross-user data leakage in the cache. Without userId in the key, all users would see the same order list, which is a security risk.*
>
> *Example: Without userId: key='my-orders:page:1' → User A and B both get the same cached data. With userId: key='my-orders:123:page:1' → Only User 123 gets their orders."*

### Q: What if Redis goes down?

**Answer**:
> *"Cache misses fall back to the database. Redis is a performance optimization, not a requirement for correctness. If Redis is unavailable, the app continues working but with higher database load and latency. We'd see increased CPU/connection pool usage.*
>
> *For production resilience, we monitor Redis health and auto-restart the service if it fails."*

---

## 🚀 Future Extensions

### Potential Enhancements (Not Implemented Yet)

1. **Cache Warming**: Pre-load popular products on startup
2. **Cache Compression**: For large paginated results
3. **Cache Partitioning**: By product category or region
4. **Conditional Invalidation**: Invalidate only specific cache keys instead of `allEntries=true`
5. **Rate Limiting**: Use Redis for request throttling
6. **Session Caching**: Store user sessions in Redis (currently JWT-based, stateless)

---

## 📝 Testing Caching

### Manual Testing

#### Test 1: Product List Cache Hits
```bash
# Request 1: Cache miss → DB query
curl http://localhost:8080/api/products?page=0&size=10 -H "Authorization: Bearer $TOKEN"
# Response time: ~150-200ms expected

# Request 2 (within 10 seconds): Cache hit → Redis
curl http://localhost:8080/api/products?page=0&size=10 -H "Authorization: Bearer $TOKEN"
# Response time: ~5-10ms expected

# Wait 11 seconds, Request 3: Cache expired → DB query
sleep 11
curl http://localhost:8080/api/products?page=0&size=10 -H "Authorization: Bearer $TOKEN"
# Response time: ~150-200ms expected
```

#### Test 2: Cache Invalidation on Write
```bash
# Create product → should clear "products" cache
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"New Product","price":99.99}'

# Product list now refreshes from DB (cache cleared)
curl http://localhost:8080/api/products?page=0&size=10 -H "Authorization: Bearer $TOKEN"
# Response time: ~150-200ms expected (no cache)
```

#### Test 3: User Orders Cache
```bash
# Request 1: Cache miss → DB query
curl http://localhost:8080/api/orders/my-orders?page=0&size=10 \
  -H "Authorization: Bearer $USER_TOKEN"
# Response time: ~120-150ms expected

# Request 2 (within 60 seconds): Cache hit → Redis
curl http://localhost:8080/api/orders/my-orders?page=0&size=10 \
  -H "Authorization: Bearer $USER_TOKEN"
# Response time: ~3-8ms expected

# Admin updates order status → clears all "userOrders" cache
curl -X PUT http://localhost:8080/api/orders/123/status?status=COMPLETED \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# User's order history refreshes from DB (cache cleared)
curl http://localhost:8080/api/orders/my-orders?page=0&size=10 \
  -H "Authorization: Bearer $USER_TOKEN"
# Response time: ~120-150ms expected (no cache)
```

### Verify Redis Data

```bash
# Connect to Redis CLI (inside Docker)
docker exec -it inventory-redis redis-cli

# List all cached product entries
KEYS "products:*"

# View cache size
DBSIZE

# Check TTL of a specific key
TTL "products:page:0:size:10"

# Clear all cache (dangerous in production!)
FLUSHALL
```

---

## 🔐 Production Considerations

1. **Monitoring**: Track cache hit/miss rates via Spring Boot Actuator metrics
2. **Failover**: Redis persistence (AOF/RDB) for recovery after crashes
3. **Memory Limits**: Set Redis `maxmemory` policy to `allkeys-lru` (already in docker-compose.yml)
4. **TTL Policy**: Review TTLs periodically—too short = high DB load, too long = stale data
5. **Security**: Redis bound to internal Docker network (not exposed to internet)

---

## 📚 Related Documentation

- [Product Module](./PRODUCT_MODULE.md) - Product endpoints
- [Order Module](./order_module.txt) - Order endpoints
- [Infrastructure Module](./Infrastructure_Module.md) - Docker/Redis setup
- [Frontend Caching](./FRONTEND_CACHING_STRATEGY.md) - Frontend React Query integration (to be created)

---

## ✅ Summary

**Caching Strategy Implemented**: Option B (Smart Caching)
- ✅ Products cache: 10 seconds (high-read, low-write)
- ✅ User orders cache: 60 seconds (user-scoped, medium-read, low-write)
- ❌ Inventory: Not cached (real-time critical)
- ❌ Order detail: Not cached (mutable, access-controlled)

**Result**: Optimized performance with acceptable stale-data trade-offs and clear interview-ready justifications.
