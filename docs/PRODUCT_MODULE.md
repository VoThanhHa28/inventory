# Product Module - Complete Documentation

## 📋 Overview

The **Product Module** is a comprehensive Spring Boot implementation of product management with advanced features including **soft delete**, **audit logging**, **caching**, and **role-based access control (RBAC)**.

### Key Features
- ✅ Full CRUD operations with @Transactional support
- ✅ Soft delete pattern (data preservation + audit trail)
- ✅ Automatic audit logging to ProductHistory table
- ✅ Cache management (10s TTL, 100 max entries)
- ✅ RBAC enforcement (ADMIN-only write operations)
- ✅ Version field for optimistic locking
- ✅ Pagination support
- ✅ SecurityContext integration for user tracking

---

## 🏗️ **Architecture & Components**

### 1. **Entity: Product.java**
```java
@Entity
@SuperBuilder
public class Product extends BaseEntity {
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "price", nullable = false)
    private Double price;
    
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;
    
    private String image;
    
    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted = false;
    
    @Version
    @Column(name = "version")
    private Long version;
}
```

**Key Fields:**
- `isDeleted`: Boolean flag for soft delete (never hard delete from DB)
- `version`: JPA @Version for optimistic locking (auto-incremented on updates)
- `createdAt`, `updatedAt`: Inherited from BaseEntity with @CreationTimestamp/@LastModifiedDate
- Inherits from `BaseEntity` which provides audit timestamps and JPA auditing

### 2. **Audit Entity: ProductHistory.java**
```java
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long productId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private Double price;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductAction action; // CREATE, UPDATE, DELETE
    
    @Column(nullable = false)
    private String changedBy; // Username from SecurityContext
    
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private LocalDateTime changedAt;
}
```

**Purpose:**
- Complete audit trail of all product changes
- Tracks WHO changed WHAT and WHEN
- Immutable (no updates, only inserts)
- Queries: `findByProductIdOrderByChangedAtDesc()` for change history

### 3. **Enum: ProductAction.java**
```java
public enum ProductAction {
    CREATE,  // New product created
    UPDATE,  // Product updated
    DELETE   // Product soft deleted
}
```

---

## 🔄 **Soft Delete Pattern - Why & How**

### **Why Soft Delete?**

1. **Data Preservation**: Never lose historical data. Deleted products remain in DB.
2. **Audit Trail**: Can trace all changes, including deletions.
3. **Referential Integrity**: Foreign key relationships remain intact in ProductHistory.
4. **Compliance**: Regulatory requirements (GDPR, data retention policies) often require preserving deletion records.
5. **Recovery**: Can restore accidentally deleted products.

### **Implementation Strategy**

**Repository Level** (ProductRepository.java):
```java
// Query methods exclude soft-deleted products
@Query("SELECT p FROM Product p WHERE p.isDeleted = false")
Page<Product> findAllActive(Pageable pageable);

@Query("SELECT p FROM Product p WHERE p.id = ?1 AND p.isDeleted = false")
Optional<Product> findActiveById(Long id);

// Native query for soft delete (UPDATE, not DELETE)
@Modifying
@Query(value = "UPDATE products SET is_deleted = true WHERE id = ?1", nativeQuery = true)
int softDeleteById(Long id);
```

**Service Level** (ProductServiceImpl.java):
```java
@Transactional
public void deleteProduct(Long id) {
    // 1. Find active product (OR throw ResourceNotFoundException)
    Product product = productRepository.findActiveById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    
    // 2. Soft delete: UPDATE is_deleted = true (1 row affected)
    int rowsAffected = productRepository.softDeleteById(id);
    if (rowsAffected == 0) {
        throw new ResourceNotFoundException("Failed to delete product with ID: " + id);
    }
    
    // 3. Refresh product state from DB
    product = productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    
    // 4. Log to audit trail (ProductAction.DELETE)
    logProductHistory(product, ProductAction.DELETE);
}
```

**Database Level** (queries automatically filter):
```sql
-- These queries automatically exclude soft-deleted products:
SELECT * FROM products WHERE is_deleted = false;

-- Hard DELETE is NEVER executed:
-- DELETE FROM products WHERE id = ?; ❌ NEVER
```

### **Verification in Tests**
```java
// Test: Soft delete sets is_deleted = true (not hard delete)
void testDeleteProduct_ShouldUseSoftDeleteNotHardDelete() {
    // Verify softDeleteById() was called
    verify(productRepository, times(1)).softDeleteById(productId);
    
    // Verify hard deleteById() was NEVER called
    verify(productRepository, never()).deleteById(productId);
}
```

---

## 💾 **Audit Logging - ProductHistory**

### **How Audit Logging Works**

**Automatic Logging on Every Change:**
```java
private void logProductHistory(Product product, ProductAction action) {
    // Extract username from SecurityContext (null-safe)
    String username = "SYSTEM"; // default fallback
    
    try {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context != null && context.getAuthentication() != null) {
            Authentication auth = context.getAuthentication();
            username = auth.getName(); // Authenticated user's username
        }
    } catch (Exception e) {
        log.warn("Could not extract username from SecurityContext", e);
    }
    
    // Create and save ProductHistory record
    ProductHistory history = ProductHistory.builder()
        .productId(product.getId())
        .name(product.getName())
        .description(product.getDescription())
        .price(product.getPrice())
        .action(action)
        .changedBy(username) // WHO made the change
        .changedAt(LocalDateTime.now()) // WHEN
        .build();
    
    productHistoryRepository.save(history);
}
```

### **Audit Trail Scenarios**

| Scenario | Action | ChangedBy | Purpose |
|----------|--------|-----------|---------|
| Admin creates product | CREATE | admin | Track who created |
| Admin updates price | UPDATE | admin | Track change history |
| Admin soft deletes | DELETE | admin | Track deletions + recovery |
| Scheduled job | - | SYSTEM | Automated processes |

### **Query Audit History**
```java
// Get all changes for a product (newest first)
List<ProductHistory> history = productHistoryRepository
    .findByProductIdOrderByChangedAtDesc(productId);

// Timeline example:
// 2024-01-20 10:30 - admin - DELETE
// 2024-01-15 14:22 - admin - UPDATE (price changed $1299.99 → $1599.99)
// 2024-01-10 09:15 - admin - CREATE
```

---

## ⚡ **Caching Strategy**

### **Global CacheConfig.java**
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("products");
        cacheManager.setCaffeine(Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .recordStats()); // For monitoring
        return cacheManager;
    }
}
```

### **Cache Behavior**

**Cached Endpoints:**
```java
@GetMapping
@Cacheable(value = "products", cacheManager = "cacheManager")
@Transactional(readOnly = true)
public ResponseEntity<ApiResponse<Page<ProductResponseDTO>>> getAllProducts(Pageable pageable) {
    // Cached with 10s TTL
    // Cache key: "products:PageRequest(page=0, size=10, sort=id:ASC,..."
}
```

**Non-Cached Endpoints:**
```java
@GetMapping("/{id}")
@Transactional(readOnly = true)
public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(@PathVariable Long id) {
    // NOT cached - always returns latest data
}
```

**Cache Invalidation on Write:**
```java
@PostMapping
@PostAuthorize("hasRole('ADMIN')")
@CacheEvict(value = "products", allEntries = true) // Clear ALL cache
@Transactional
public ResponseEntity<ApiResponse<ProductResponseDTO>> createProduct(...) { }

@PutMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
@CacheEvict(value = "products", allEntries = true) // Clear ALL cache
@Transactional
public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(...) { }

@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
@CacheEvict(value = "products", allEntries = true) // Clear ALL cache
@Transactional
public ResponseEntity<ApiResponse<Void>> deleteProduct(...) { }
```

### **Cache Strategy Rationale**

| Feature | Rationale |
|---------|-----------|
| **List Cached (10s TTL)** | Acceptable eventual consistency for product catalog. Most users see same list. |
| **Detail NOT Cached** | Single product should always be latest (audit, price, inventory). |
| **Inventory NOT Cached** | Stock changes must be immediate (prevent overselling). |
| **Clear on Writes** | Any CREATE/UPDATE/DELETE invalidates entire cache (safety first). |
| **100 Max Size** | ~100 products per page * 10 pages = ~1MB RAM. Conservative. |

### **Cache Monitoring**
```
Cache Stats (from Caffeine recordStats):
- Hit Rate: X/Y requests from cache
- Miss Rate: (Y-X)/Y requests hit DB
- Eviction Count: Products removed after 10s TTL
```

---

## 🔐 **RBAC - Role-Based Access Control**

### **ADMIN-Only Operations**
```java
@PostMapping("/")
@PreAuthorize("hasRole('ADMIN')")  // Only ADMIN can create
@Transactional
public ResponseEntity<ApiResponse<ProductResponseDTO>> createProduct(...) { }

@PutMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")  // Only ADMIN can update
@Transactional
public ResponseEntity<ApiResponse<ProductResponseDTO>> updateProduct(...) { }

@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")  // Only ADMIN can delete
@Transactional
public ResponseEntity<ApiResponse<Void>> deleteProduct(...) { }
```

### **Public Read Operations**
```java
@GetMapping("/")
@Cacheable(value = "products", cacheManager = "cacheManager")
@Transactional(readOnly = true)
public ResponseEntity<ApiResponse<Page<ProductResponseDTO>>> getAllProducts(...) { }
// No @PreAuthorize - everyone can read

@GetMapping("/{id}")
@Transactional(readOnly = true)
public ResponseEntity<ApiResponse<ProductResponseDTO>> getProductById(...) { }
// No @PreAuthorize - everyone can read
```

### **Security Enforcement Stack**
1. **@EnableMethodSecurity** - SecurityConfig.java enables method-level security
2. **@PreAuthorize** - Controllers enforce ADMIN role at endpoint level
3. **@Transactional** - All write ops are transactional (atomicity)
4. **@CacheEvict** - Write ops clear cache (consistency)

---

## 🧪 **Unit Testing**

### **Test Scenarios (12 Total)**

#### **Scenario 1: Create Product**
```java
@Test
void testCreateProduct_ShouldSaveAndLogHistory() {
    // When: Admin creates product
    // Then: 
    //   1. Product saved to DB
    //   2. ProductHistory record created with action=CREATE
    //   3. ChangedBy = "admin" from SecurityContext
}
```

#### **Scenario 2: Update Product**
```java
@Test
void testUpdateProduct_ShouldUpdateAndLogHistory() {
    // When: Admin updates product
    // Then:
    //   1. Product fields updated
    //   2. ProductHistory record created with action=UPDATE
    //   3. Version field auto-incremented (optimistic lock)
    //   4. Cache evicted (getAllProducts must be refreshed)
}
```

#### **Scenario 3: Soft Delete**
```java
@Test
void testDeleteProduct_ShouldSoftDeleteAndLogHistory() {
    // When: Admin deletes product
    // Then:
    //   1. softDeleteById() called (UPDATE is_deleted=true)
    //   2. deleteById() NEVER called (hard delete forbidden)
    //   3. ProductHistory record created with action=DELETE
    //   4. Product still in DB (findById returns it with is_deleted=true)
}
```

#### **Scenario 4: Audit Logging**
```java
@Test
void testAuditLog_ShouldCaptureCorrectUsernameFromSecurityContext() {
    // When: Operation with authenticated user
    // Then: ProductHistory.changedBy = authenticated username
    
    // When: No authenticated user
    // Then: ProductHistory.changedBy = "SYSTEM" (fallback)
}
```

#### **Scenario 5: Get Operations**
```java
@Test
void testGetProductById_ShouldReturnActiveProduct() {
    // When: Get product by ID
    // Then: Only returns products where is_deleted=false
}

@Test
void testGetAllProducts_ShouldReturnPagedActiveProducts() {
    // When: Get paginated products
    // Then: Only returns products where is_deleted=false, respects pagination
}
```

### **Running Tests**
```bash
# Run all ProductService tests
./mvnw test -Dtest=ProductServiceTest

# Run specific test
./mvnw test -Dtest=ProductServiceTest#testDeleteProduct_ShouldUseSoftDeleteNotHardDelete

# With coverage
./mvnw test -X
```

### **Test Results**
```
ProductServiceTest ✅ 12/12 PASSED
  ✅ testCreateProduct_ShouldSaveAndLogHistory
  ✅ testUpdateProduct_ShouldUpdateAndLogHistory
  ✅ testDeleteProduct_ShouldSoftDeleteAndLogHistory
  ✅ testDeleteProduct_WhenProductNotFound_ShouldThrowException
  ✅ testGetProductById_ShouldReturnActiveProduct
  ✅ testGetProductById_WhenProductNotFound_ShouldThrowException
  ✅ testGetAllProducts_ShouldReturnPagedActiveProducts
  ✅ testAuditLog_ShouldCaptureCorrectUsernameFromSecurityContext
  ✅ testAuditLog_ShouldUseSYSTEMWhenNoAuthenticatedUser
  ✅ testDeleteProduct_ShouldUseSoftDeleteNotHardDelete
```

---

## 📡 **API Endpoints**

### **Create Product (ADMIN-only)**
```http
POST /api/products
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "name": "MacBook Pro",
  "description": "High-performance laptop",
  "price": 1299.99,
  "stockQuantity": 50,
  "image": "macbook.jpg"
}

Response: 201 Created
{
  "code": 201,
  "message": "Product created successfully",
  "data": {
    "id": 1,
    "name": "MacBook Pro",
    "description": "High-performance laptop",
    "price": 1299.99,
    "stockQuantity": 50,
    "image": "macbook.jpg",
    "isDeleted": false,
    "createdAt": "2024-01-20T10:15:00Z",
    "updatedAt": "2024-01-20T10:15:00Z"
  }
}
```

### **Get All Products (Public, Cached)**
```http
GET /api/products?page=0&size=10&sort=name,asc
Accept: application/json

Response: 200 OK
{
  "code": 200,
  "message": "Retrieved products successfully",
  "data": {
    "content": [
      { "id": 1, "name": "MacBook Pro", ... },
      { "id": 2, "name": "iPhone 15", ... }
    ],
    "totalPages": 10,
    "totalElements": 100,
    "size": 10,
    "number": 0,
    "sort": [...]
  }
}
```

### **Get Product by ID (Public, NOT Cached)**
```http
GET /api/products/1
Accept: application/json

Response: 200 OK
{
  "code": 200,
  "message": "Retrieved product successfully",
  "data": {
    "id": 1,
    "name": "MacBook Pro",
    "description": "High-performance laptop",
    "price": 1299.99,
    "stockQuantity": 50,
    "isDeleted": false,
    "createdAt": "2024-01-20T10:15:00Z",
    "updatedAt": "2024-01-20T10:15:00Z"
  }
}
```

### **Update Product (ADMIN-only)**
```http
PUT /api/products/1
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "name": "MacBook Pro M3",
  "description": "M3 chip",
  "price": 1599.99,
  "stockQuantity": 100
}

Response: 200 OK
```

### **Delete Product (ADMIN-only, Soft Delete)**
```http
DELETE /api/products/1
Authorization: Bearer {jwt_token}

Response: 204 No Content
(Product.isDeleted = true, but row still in DB)
```

---

## 🔧 **Database Schema**

### **products Table**
```sql
CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(19,2) NOT NULL,
    stock_quantity INT NOT NULL,
    image VARCHAR(255),
    is_deleted BOOLEAN DEFAULT FALSE NOT NULL,
    version BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

CREATE INDEX idx_is_deleted ON products(is_deleted);
CREATE INDEX idx_created_at ON products(created_at);
```

### **product_history Table**
```sql
CREATE TABLE product_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(19,2) NOT NULL,
    action VARCHAR(20) NOT NULL,  -- CREATE, UPDATE, DELETE
    changed_by VARCHAR(100) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(product_id) REFERENCES products(id)
);

CREATE INDEX idx_product_id ON product_history(product_id);
CREATE INDEX idx_changed_at ON product_history(changed_at);
```

---

## 🚀 **Deployment**

### **Docker Deployment**
```yaml
# docker-compose.yml excerpt
services:
  app1:
    build: .
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://db:3306/inventory
      SPRING_CACHE_TYPE: caffeine
      LOGGING_LEVEL_COM_PROJECT_INVENTORY: DEBUG
    ports:
      - "8080:8080"
    depends_on:
      - db
  
  db:
    image: mysql:8.0
    volumes:
      - db_data:/var/lib/mysql
    environment:
      MYSQL_DATABASE: inventory
      MYSQL_ROOT_PASSWORD: rootpassword
```

### **Deployment Steps**
```bash
# 1. Build Docker image
docker-compose build

# 2. Start services
docker-compose up -d

# 3. Verify
docker-compose logs -f app1

# 4. Access API
curl http://localhost/api/products
```

---

## 💡 **Interview Preparation Notes**

### **Q: Why use soft delete instead of hard delete?**
**A:**
1. **Data Recovery**: Accidentally deleted products can be restored.
2. **Audit Trail**: Complete history preserved (ProductHistory records explain why deletion occurred).
3. **Referential Integrity**: Orders/Inventory linked to products remain valid.
4. **Compliance**: GDPR retention policies require preserving deletion records.
5. **Business Logic**: Can archive products without losing order history.

### **Q: How does cache invalidation prevent data inconsistency?**
**A:**
1. **Problem**: Cache might serve stale data after updates.
2. **Solution**: `@CacheEvict(allEntries = true)` on ALL write operations.
3. **Trade-off**: Next request hits DB (slightly slower), but data is always correct.
4. **Why not selective eviction?** We use `allEntries = true` for safety - complex cache keys make selective eviction error-prone.

### **Q: How does audit logging work with SecurityContext?**
**A:**
1. **Extraction**: `SecurityContextHolder.getContext().getAuthentication().getName()`
2. **Null Safety**: Falls back to "SYSTEM" if no authenticated user.
3. **Timing**: Logged AFTER transaction commits (ensures data consistency).
4. **Immutable**: ProductHistory records are never updated, only inserted.

### **Q: What's the purpose of the @Version field?**
**A:**
1. **Optimistic Locking**: Prevents lost updates when two users edit same product.
2. **How It Works**: 
   - User A reads Product (version=5)
   - User B reads Product (version=5)
   - User A updates → version=6
   - User B tries update → version mismatch → OptimisticLockingFailureException
3. **Better than pessimistic lock** (which blocks reads) for high-concurrency scenarios.

### **Q: Why is the detail endpoint NOT cached?**
**A:**
1. **Correctness**: Single product detail should be latest (price, stock, audit records).
2. **Cache hit rate**: Individual product views less frequent than list views.
3. **Complexity**: Cache key would need product ID, making selective eviction hard.
4. **Trade-off**: Sacrifice cache hit for data freshness.

### **Q: How does pagination work with caching?**
**A:**
1. **Cache key includes pagination params**: `products::PageRequest(page=0, size=10)`
2. **Page 1 cached separately** from Page 2.
3. **On write**: @CacheEvict clears ALL pages at once (safety).
4. **Result**: Each page cached, but any update requires full refresh.

---

## 📝 **File Structure**

```
src/main/java/com/project/inventory/
├── entity/
│   ├── Product.java              # Product entity with soft delete
│   ├── ProductHistory.java        # Audit trail entity
│   └── ProductAction.java         # Enum: CREATE, UPDATE, DELETE
├── repository/
│   ├── ProductRepository.java     # Soft delete queries
│   └── ProductHistoryRepository.java
├── service/
│   ├── ProductService.java        # Interface
│   └── impl/
│       └── ProductServiceImpl.java # Soft delete + audit logic
├── controller/
│   └── ProductController.java     # RBAC + caching
├── dto/
│   └── product/
│       ├── ProductRequestDTO.java
│       └── ProductResponseDTO.java
└── configuration/
    └── CacheConfig.java           # Caffeine cache config

src/test/java/com/project/inventory/
└── service/
    └── ProductServiceTest.java    # 12 unit tests
```

---

## ✅ **Checklist - Product Module Complete**

- [x] **Entities**: Product (soft delete), ProductHistory (audit), ProductAction (enum)
- [x] **Repository**: Soft delete queries (findAllActive, findActiveById, softDeleteById)
- [x] **Service**: Create/Update/Delete with audit, @Transactional on all write ops
- [x] **Controller**: RBAC (@PreAuthorize ADMIN), caching (@Cacheable/@CacheEvict)
- [x] **Cache**: Caffeine 10s TTL, 100 max size, global CacheConfig
- [x] **Unit Tests**: 12 tests covering soft delete, audit logging, cache, RBAC
- [x] **Documentation**: PRODUCT_MODULE.md with examples
- [x] **Git**: All commits pushed to `feature/product-module`

---

## 🎯 **Next Steps**

- Merge `feature/product-module` to `main`
- Code review by team
- Proceed with Order Module / User & RBAC Module

---

**Created**: 2024-01-20  
**Module**: Product Management  
**Status**: ✅ COMPLETE (8/8 Batches)
