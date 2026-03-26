# Inventory Module Documentation

## Overview
The Inventory Module manages product stock, reservations, and sales with **atomic operations** and **transaction safety**. All operations are implemented using native SQL queries to ensure atomicity at the database level.

---

## Model / Entity

### Inventory Entity
**Table:** `inventory`

**Fields:**
```java
- id (PK): Long
- product (FK): One-to-One relationship with Product (unique)
- stock: Integer (min 0) - Available stock quantity
- reserved: Integer (min 0) - Quantity reserved for orders
- soldCount: Integer (min 0) - Total quantity sold
- status: InventoryStatus enum - Current status
- createdAt: LocalDateTime - Record creation timestamp
- updatedAt: LocalDateTime - Last update timestamp
```

### InventoryStatus Enum
```java
AVAILABLE - Stock is available for purchase
RESERVED  - Stock is reserved for an order
SOLD      - Stock has been sold
```

**Helper Methods:**
- `getAvailable()` - Returns: `stock - reserved`
- `hasSufficientStock(quantity)` - Returns: `getAvailable() >= quantity`

---

## API Endpoints

### 1. Add Stock (Admin Only)
```http
POST /api/inventory/add-stock
Authorization: Bearer <JWT>
Content-Type: application/json

Request:
{
  "productId": 1,
  "quantity": 100
}

Response (200 OK):
{
  "id": 5,
  "productId": 1,
  "productName": "Laptop",
  "productPrice": 999.99,
  "stock": 150,
  "reserved": 20,
  "available": 130,
  "soldCount": 50,
  "status": "AVAILABLE",
  "createdAt": "2026-03-26T10:30:00",
  "updatedAt": "2026-03-26T11:45:00"
}

Error (400):
{
  "error": "Product ID must be valid"
}
```

### 2. Remove Stock (Admin Only)
```http
POST /api/inventory/remove-stock
Authorization: Bearer <JWT>

Request:
{
  "productId": 1,
  "quantity": 30
}

Response (200 OK): [Same structure as add-stock]

Error (400):
{
  "error": "Insufficient stock. Available: 100"
}
```

### 3. Reserve Stock (Admin Only)
Decreases available stock and increases reserved quantity.
```http
POST /api/inventory/reserve
Authorization: Bearer <JWT>

Request:
{
  "productId": 1,
  "quantity": 20
}

Response (200 OK):
{
  ...
  "stock": 130,
  "reserved": 20,
  "available": 110,
  "status": "RESERVED"
}

Error (400):
{
  "error": "Insufficient available stock. Available: 100"
}
```

### 4. Mark as Sold (Admin Only)
Decreases reserved and increases soldCount.
```http
POST /api/inventory/sold
Authorization: Bearer <JWT>

Request:
{
  "productId": 1,
  "quantity": 15
}

Response (200 OK):
{
  ...
  "reserved": 5,
  "soldCount": 65,
  "status": "SOLD"
}

Error (400):
{
  "error": "Insufficient reserved stock. Reserved: 10"
}
```

### 5. Release Reserved Stock (Admin Only)
Returns reserved stock back to available.
```http
POST /api/inventory/release
Authorization: Bearer <JWT>

Request:
{
  "productId": 1,
  "quantity": 5
}

Response (200 OK):
{
  ...
  "stock": 135,
  "reserved": 0,
  "available": 135
}
```

### 6. List All Inventory
```http
GET /api/inventory
Authorization: Bearer <JWT>

Response (200 OK):
[
  {
    "id": 5,
    "productId": 1,
    "productName": "Laptop",
    "stock": 150,
    "reserved": 0,
    "available": 150,
    "soldCount": 50,
    "status": "AVAILABLE"
  },
  {
    "id": 6,
    "productId": 2,
    "productName": "Mouse",
    "stock": 500,
    "reserved": 50,
    "available": 450,
    "soldCount": 200,
    "status": "RESERVED"
  }
]
```

### 7. Get Inventory by ID
```http
GET /api/inventory/{id}
Authorization: Bearer <JWT>

Response (200 OK): [Same as add-stock response]

Error (404): Not Found
```

### 8. Get Inventory by Product ID
```http
GET /api/inventory/product/{productId}
Authorization: Bearer <JWT>

Response (200 OK): [Same as add-stock response]

Error (404): Not Found
```

---

## Service Layer

### InventoryService Interface
Handles business logic with transaction safety.

**Methods:**
```java
InventoryResponseDTO addStock(InventoryRequestDTO request)
InventoryResponseDTO removeStock(InventoryRequestDTO request)
InventoryResponseDTO reserve(InventoryRequestDTO request)
InventoryResponseDTO markAsSold(InventoryRequestDTO request)
InventoryResponseDTO releaseReserved(InventoryRequestDTO request)
List<InventoryResponseDTO> getAll()
InventoryResponseDTO getById(Long inventoryId)
InventoryResponseDTO getByProductId(Long productId)
```

### Atomic Operations (Native SQL)
All state-changing operations use native SQL with `@Modifying` and `@Transactional`:

```java
// Example: Reserve operation is atomic
UPDATE inventory 
SET stock = stock - :quantity, 
    reserved = reserved + :quantity 
WHERE product_id = :productId AND stock >= :quantity
```

**Why atomic at DB level?**
- Prevents race conditions in concurrent requests
- Single SQL statement = single DB operation
- Fails entirely or succeeds entirely (no partial updates)
- Safer than read + update + write pattern

---

## Exception Handling

### Custom Exceptions

**1. InsufficientStockException**
```java
Thrown when: available stock < requested quantity
Example: "Insufficient stock. Available: 50"
```

**2. InvalidProductException**
```java
Thrown when: product doesn't exist or inventory not found
Example: "Product not found: 999"
```

**3. NegativeQuantityException**
```java
Thrown when: quantity is negative or zero
Example: "Quantity must be positive"
```

**Generic Exceptions:**
- `IllegalArgumentException` - For invalid product IDs, null values
- Standard HTTP error responses from controller

---

## Security

### Access Control
- **Write Operations** (POST): `@PreAuthorize("hasRole('ADMIN')")`
  - `/api/inventory/add-stock`
  - `/api/inventory/remove-stock`
  - `/api/inventory/reserve`
  - `/api/inventory/sold`
  - `/api/inventory/release`

- **Read Operations** (GET): All authenticated users
  - `/api/inventory`
  - `/api/inventory/{id}`
  - `/api/inventory/product/{productId}`

### JWT Token Required
All endpoints require valid JWT token in `Authorization: Bearer <token>` header.

---

## Caching Strategy

### Why NOT Cached?
⚠️ **Inventory is NOT cached** - Critical Data
- Stock levels must always be current
- Race conditions if cached data used for decisions
- "Overselling" risk if working with stale cache

### What IS Cached?
✅ **Product list API only** (Caffeine, 10s TTL)
- Product metadata changes rarely
- Safe to serve from cache
- Improves performance for read-heavy operations

**Implementation:**
```java
@EnableCaching  // Configured in CacheConfig
@Cacheable(value = "products", cacheManager = "cacheManager")
public List<ProductResponseDTO> getAllProducts()
```

---

## Data Flow Examples

### Scenario 1: Order Creation
```
1. User orders 5 units of Product #1
   → reserve(productId=1, quantity=5)
   → stock: 100→95, reserved: 0→5

2. Order confirmed/payment processed
   → markAsSold(productId=1, quantity=5)
   → stock: 95 (unchanged), reserved: 5→0, soldCount: 45→50

3. Order cancelled before payment
   → releaseReserved(productId=1, quantity=5)
   → stock: 95→100, reserved: 5→0
```

### Scenario 2: Admin Stock Adjustment
```
1. New shipment received (100 units)
   → addStock(productId=1, quantity=100)
   → stock: 95→195

2. Damaged units found (5 units)
   → removeStock(productId=1, quantity=5)
   → stock: 195→190
```

---

## Testing Instructions

### Prerequisites
- MySQL running
- JWT token for admin user
- Postman or curl

### Test Cases

#### Test 1: Add Stock
```bash
curl -X POST http://localhost:8080/api/inventory/add-stock \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 100}'

# Expected: 200 OK, stock = 100
```

#### Test 2: Reserve Stock
```bash
curl -X POST http://localhost:8080/api/inventory/reserve \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 20}'

# Expected: stock = 80, reserved = 20
```

#### Test 3: Mark as Sold
```bash
curl -X POST http://localhost:8080/api/inventory/sold \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 20}'

# Expected: reserved = 0, soldCount = 20
```

#### Test 4: Remove Stock (Insufficient)
```bash
curl -X POST http://localhost:8080/api/inventory/remove-stock \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 200}'

# Expected: 400 Bad Request - "Insufficient stock"
```

#### Test 5: List All Inventory
```bash
curl -X GET http://localhost:8080/api/inventory \
  -H "Authorization: Bearer <user-token>"

# Expected: 200 OK, array of inventory objects
```

#### Test 6: Unauthorized Access (User tries to add stock)
```bash
curl -X POST http://localhost:8080/api/inventory/add-stock \
  -H "Authorization: Bearer <user-token>" \
  -H "Content-Type: application/json" \
  -d '{"productId": 1, "quantity": 10}'

# Expected: 403 Forbidden - Only ADMIN role allowed
```

---

## Performance Considerations

1. **Database Indexing**
   - Add index on `product_id` in inventory table
   ```sql
   CREATE INDEX idx_inventory_product_id ON inventory(product_id);
   ```

2. **Atomic Queries**
   - All native SQL queries are single-statement atoms
   - No N+1 query problems
   - Eager loading via joins where needed

3. **Transaction Isolation**
   - Default isolation level: READ_COMMITTED
   - Sufficient for e-commerce use cases

---

## Future Enhancements

- [ ] Batch operations (add/remove multiple products at once)
- [ ] Inventory audit trail (track all changes)
- [ ] Low stock alerts (notify when stock < threshold)
- [ ] Warehouse location tracking (multi-warehouse support)
- [ ] SKU variant management (sizes, colors, etc.)
