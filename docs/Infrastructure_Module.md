# Infrastructure Module Documentation

## Overview

This module covers the full infrastructure setup for the Spring Boot E-commerce & Inventory system:
- **Dockerfile** (multi-stage build)
- **docker-compose.yml** (MySQL + 2 app instances + Nginx)
- **Nginx** (round-robin load balancer)
- **Caffeine Cache** (product list, TTL 10s)

---

## 1. Dockerfile (Multi-Stage Build)

```
Stage 1 (builder): maven:3.9.6-eclipse-temurin-21
    → Compile & package app (mvn package -DskipTests)

Stage 2 (runtime): eclipse-temurin:21-jre-alpine
    → Copy only the .jar → smaller final image (~200MB vs ~600MB)
```

**Key decisions:**
- `maven dependency:go-offline` first → Docker layer cache for dependencies (faster rebuilds)
- Alpine JRE → minimal runtime, no build tools in production image
- All secrets injected via ENV vars (not hardcoded)

---

## 2. docker-compose.yml

### Services

| Service | Image | Port | Role |
|---------|-------|------|------|
| `mysqldb` | mysql:8.0 | 3307→3306 | Primary database |
| `app1` | local build | internal:8080 | Spring Boot instance 1 |
| `app2` | local build | internal:8080 | Spring Boot instance 2 |
| `nginx` | nginx:alpine | 80→80 | Load balancer (entry point) |

### Startup Order
```
mysqldb (healthcheck: mysqladmin ping)
    ↓ (condition: service_healthy)
app1, app2
    ↓ (depends_on)
nginx
```

### Volumes
- `db_data:/var/lib/mysql` — MySQL data persists across container restarts

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://mysqldb:3306/inventory_db` | DB connection (uses Docker service name) |
| `SPRING_DATASOURCE_USERNAME` | `root` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | `1234` | DB password |
| `JWT_SECRET` | (hardcoded fallback) | JWT signing key |
| `JWT_EXPIRATION` | `86400000` (24h) | Token TTL in ms |

---

## 3. Nginx Load Balancer

**Config:** `config/nginx/nginx.conf`

```nginx
upstream inventory_app {
    server app1:8080;   # Instance 1
    server app2:8080;   # Instance 2
    # Default: round-robin
}
```

**Traffic flow:**
```
Client → http://localhost:80
    → Nginx upstream (round-robin)
        → app1:8080  (odd requests)
        → app2:8080  (even requests)
            → MySQL:3306
```

**Headers forwarded:**
- `X-Real-IP` — real client IP
- `X-Forwarded-For` — proxy chain
- `X-Forwarded-Proto` — protocol

---

## 4. Caffeine Cache

| API | Cached? | TTL |
|-----|---------|-----|
| `GET /api/products` | ✅ Yes | 10s |
| `GET /api/products/{id}` | ❌ No | — |
| `GET /api/inventory` | ❌ No | — |

**Why product list only?**
- List is expensive (paginated query), benefits from caching
- Detail page needs to always be fresh (stock, price changes)
- Inventory is write-heavy (stock changes frequently)

**Eventual consistency note:**
- With 2 stateless app instances, each has its own in-memory Caffeine cache
- A write to `app1` evicts `app1`'s cache but NOT `app2`'s cache
- For up to 10 seconds, `app2` may serve stale product list → acceptable for this use case
- Production solution: use Redis as shared cache instead of Caffeine

---

## 5. Start System Locally

```bash
# 1. Build and start all services
docker-compose up --build -d

# 2. Verify services are running
docker-compose ps

# 3. Check logs
docker-compose logs -f app1 app2

# 4. Access API via Nginx (load balanced)
curl http://localhost/api/products

# 5. Access DB directly (local dev)
mysql -h 127.0.0.1 -P 3307 -u root -p1234 inventory_db

# 6. Stop everything
docker-compose down

# 7. Stop and wipe DB volume (fresh start)
docker-compose down -v
```

---

## 6. Stateless App Notes

Both `app1` and `app2` are **fully stateless**:
- No session stored on server (JWT-based auth)
- Any request can be handled by either instance
- Safe to scale horizontally (add `app3`, `app4`...)
- DB is the single source of truth

**Eventual consistency:**
- Caffeine cache per-instance → slight temporary inconsistency on writes
- JPA `ddl-auto=update` creates tables on startup (safe for dev, use Flyway/Liquibase in prod)

---

## 7. Nginx — Chia Tải Bằng Logic Gì?

### Round-Robin là gì?
Nginx đếm request theo thứ tự và phân phối lần lượt:

```
Request 1  → app1
Request 2  → app2
Request 3  → app1
Request 4  → app2
...cứ thế lặp lại
```

Không quan tâm request từ user nào, nặng nhẹ ra sao — chỉ đơn giản "1 cho mày, 1 cho nó".

### Nginx biết app còn sống không?
Mặc định: **không kiểm tra**. Nếu app1 chết, Nginx vẫn forward → client nhận lỗi.

Muốn tự động bỏ qua app chết:
```nginx
upstream inventory_app {
    server app1:8080 max_fails=3 fail_timeout=30s;
    server app2:8080 max_fails=3 fail_timeout=30s;
}
```
→ Thử lỗi 3 lần → loại khỏi pool 30s → tự thêm lại sau.

### Nginx KHÔNG làm gì?
- Không xử lý business logic, không đọc JWT, không biết user là ai
- Chỉ là "người gác cổng phân luồng"

---

## 8. Cache Hiện Tại — Caffeine Hoạt Động Thế Nào?

### Flow khi KHÔNG có cache:
```
User GET /api/products → app1 → query MySQL → trả kết quả
User GET /api/products (lần 2) → app1 → query MySQL LẠI → lãng phí!
```

### Flow khi CÓ Caffeine:
```
Lần 1: User → app1 → MISS cache → query MySQL → lưu vào RAM app1 → trả kết quả
Lần 2: User → app1 → HIT cache  → lấy từ RAM → trả kết quả  ✅ (không đụng DB)
Lần 3: User → app2 → MISS cache (app2 có RAM riêng!) → query MySQL lại ❌
```

### Vấn đề khi Admin xóa sản phẩm:
```
Admin DELETE → request vào app1
→ @CacheEvict → xóa cache trong RAM app1 ✅
→ app2 KHÔNG biết gì → cache cũ vẫn còn ❌
→ User vào app2 → nhận danh sách có product đã xóa ❌
→ Tự fix sau 10 giây khi TTL hết
```

**Độ nghiêm trọng:** Thấp — chỉ 10s, chỉ ảnh hưởng product list, không ảnh hưởng tồn kho/đơn hàng.

---

## 9. Nếu Thêm Redis — Thay Đổi Gì?

### Architecture:
```
TRƯỚC (Caffeine):
  app1 [RAM cache riêng]
  app2 [RAM cache riêng]   ← không đồng bộ nhau

SAU (Redis):
  app1 ──┐
         ├── Redis (shared cache) ── MySQL
  app2 ──┘
  → 1 cache chung, mọi instance đọc/ghi vào đây
```

### Flow với Redis:
```
Lần 1: User → app1 → MISS Redis → query MySQL → lưu Redis → trả kết quả
Lần 2: User → app2 → HIT Redis  → trả kết quả ✅ (không query DB)
Admin DELETE → app1 → @CacheEvict → xóa key trong Redis
Lần 3: User → app2 → MISS Redis → query MySQL → fresh data ✅
```

### Redis có fix hết không?
| Vấn đề | Caffeine | Redis |
|--------|----------|-------|
| Cache không đồng bộ 2 instances | ❌ | ✅ Fix |
| Evict không lan sang instance khác | ❌ | ✅ Fix |
| Cache mất khi restart app | ❌ | ✅ Fix |
| Redis chết | N/A | ⚠️ App vẫn chạy, chỉ chậm |
| MySQL single node chết | ❌ | ❌ Không fix |

### Code thay đổi bao nhiêu?
```
Không đổi 1 dòng business code nào!
@Cacheable, @CacheEvict giữ nguyên.
Chỉ thêm Redis dependency + 1 bean config.
```

---

## 10. Redis — Kiến Thức Phỏng Vấn

### Redis là gì?
Key-value store lưu trong RAM → đọc/ghi nhanh hơn MySQL ~100 lần.
Dùng làm: cache, session store, message queue, rate limiter.

### Lệnh cơ bản:
```bash
SET products "data" EX 10    # lưu key, tự xóa sau 10 giây
GET products                  # lấy value
DEL products                  # xóa key thủ công (= evict)
EXISTS products               # key có tồn tại không? (1/0)
TTL products                  # còn bao nhiêu giây sống
KEYS products*                # tìm key theo pattern
FLUSHALL                      # xóa toàn bộ (dùng cẩn thận!)
```

### Lý thuyết cần biết:
| Khái niệm | Giải thích |
|-----------|-----------|
| **Cache-aside** | App check cache → miss → query DB → lưu cache. `@Cacheable` làm tự động. |
| **TTL** | Thời gian sống. Hết TTL tự xóa → lần sau query DB lại. |
| **Cache eviction** | Khi write → chủ động xóa cache → tránh stale data. |
| **Cache stampede** | Redis chết → 1000 user cùng query DB → DB crash. |
| **Eviction policy** | Redis đầy RAM → xóa key nào? `allkeys-lru` = xóa key ít dùng nhất. |
| **Persistence** | Redis ghi xuống disk (RDB/AOF) → restart không mất data. |
| **Single-threaded** | 1 thread nhưng 100k ops/s vì toàn bộ in-memory. |

### Câu hỏi phỏng vấn + trả lời chuẩn:

**Q: Redis khác Caffeine thế nào?**
> "Caffeine là local cache trong RAM của từng process, các instance không biết nhau. Redis là external service, tất cả instance kết nối chung 1 Redis. Khi scale nhiều instance thì Redis mới giải quyết được đồng bộ cache."

**Q: Nếu Redis chết thì sao?**
> "App vẫn chạy. Cache miss → fallback query thẳng DB. Chậm hơn nhưng không lỗi. DB luôn là source of truth — đây là pattern cache-aside."

**Q: Tại sao không cache inventory?**
> "Inventory thay đổi liên tục theo order, cache dễ stale → bán sai tồn kho. Em dùng atomic SQL trực tiếp: `UPDATE SET quantity = quantity - X WHERE quantity >= X` để đảm bảo chính xác."

**Q: Cache stampede là gì, xử lý sao?**
> "Khi cache hết hạn, nhiều request cùng lúc đổ vào DB → quá tải. Xử lý: cache warm-up lúc khởi động, hoặc distributed lock (chỉ 1 thread được query DB, thread khác đợi)."

**Q: TTL bao nhiêu là hợp lý?**
> "Tùy use case. Product list ít thay đổi → 10-60s. Session → vài giờ. Real-time inventory → không cache."
