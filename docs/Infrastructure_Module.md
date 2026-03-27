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
