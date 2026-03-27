# Quick Reference Guide

## 📁 Project Navigation

### Backend (Java)
```
inventory/
├── src/main/java/com/project/inventory/
│   ├── configuration/
│   │   ├── CacheConfig.java          ← Dual cache: products (10s), userOrders (60s)
│   │   ├── SecurityConfig.java       ← JWT, role-based access
│   │   └── ...
│   ├── controller/
│   │   ├── OrderController.java      ← @Cacheable on /my-orders, @CacheEvict on status
│   │   ├── ProductController.java    ← @Cacheable on list, @CacheEvict on write
│   │   └── ...
│   ├── service/
│   │   ├── OrderService.java
│   │   ├── ProductService.java
│   │   └── ...
│   └── ...
└── pom.xml                            ← Maven dependencies
```

### Frontend (React)
```
frontend/
├── src/
│   ├── api/
│   │   ├── client.ts                 ← Axios + JWT interceptor
│   │   ├── auth.ts                   ← Login, register endpoints
│   │   ├── products.ts               ← useProducts (10s cache), useProductDetail (uncached)
│   │   └── orders.ts                 ← useMyOrders (60s cache), useUpdateOrderStatus
│   ├── stores/
│   │   ├── authStore.ts              ← User, token, logout, isAdmin()
│   │   └── cartStore.ts              ← Cart items, add/remove, getTotal()
│   ├── types/
│   │   ├── api.ts                    ← ApiResponse, Page, ApiError
│   │   ├── auth.ts                   ← User, Role, LoginRequest
│   │   ├── product.ts                ← Product, ProductFilter
│   │   └── order.ts                  ← Order, OrderStatus
│   ├── layouts/
│   │   ├── PublicLayout.tsx          ← Auth pages
│   │   ├── UserLayout.tsx            ← Shop pages
│   │   └── AdminLayout.tsx           ← Admin pages
│   ├── pages/                        ← 20 pages (to implement)
│   │   ├── auth/
│   │   ├── shop/
│   │   └── admin/
│   ├── components/                   ← Reusable UI components
│   ├── hooks/                        ← Custom React hooks
│   ├── utils/                        ← Constants, formatters, validators
│   ├── routes/index.tsx              ← Route setup, ProtectedRoute
│   ├── App.tsx                       ← React Query + Router provider
│   └── main.tsx                      ← Entry point
├── package.json                       ← Dependencies
├── vite.config.ts                    ← Build config
├── tsconfig.json                     ← TypeScript config
└── tailwind.config.js                ← Styling config
```

### Documentation
```
docs/
├── CACHING_STRATEGY.md               ← Backend caching Option B (13KB)
├── IMPLEMENTATION_SUMMARY.md         ← This project overview
├── PRODUCT_MODULE.md                 ← Product entity & API
├── order_module.txt                  ← Order specs
├── Infrastructure_Module.md          ← Docker, Redis setup
└── ...
```

---

## 🎯 Key Files to Remember

### For Backend (Option B Caching)
1. **`src/main/java/com/project/inventory/configuration/CacheConfig.java`**
   - Two caches: "products" (10s), "userOrders" (60s)
   - RedisCacheManagerBuilder setup

2. **`src/main/java/com/project/inventory/controller/OrderController.java`**
   - Line: `@GetMapping("/my-orders")` with `@Cacheable(...)`
   - Line: `@PutMapping("/{orderId}/status")` with `@CacheEvict(...)`

3. **`docs/CACHING_STRATEGY.md`**
   - All caching rationale, testing, interview answers

### For Frontend (React Setup)
1. **`frontend/src/api/client.ts`**
   - Axios instance + JWT interceptor + 401 handling

2. **`frontend/src/stores/authStore.ts`** & **`cartStore.ts`**
   - All state management

3. **`frontend/src/routes/index.tsx`**
   - All 20 routes configuration, ProtectedRoute component

4. **`frontend/README.md`**
   - Complete frontend guide, caching strategy, dev workflow

5. **`frontend/src/types/index.ts`**
   - All TypeScript type definitions (re-export all)

---

## 🚀 Quick Start Commands

### Backend
```bash
# Run backend (from inventory/ folder)
mvn spring-boot:run

# Or with Docker
docker-compose up -d

# API available at: http://localhost:8080/api
# Swagger UI: http://localhost:8080/swagger-ui.html
```

### Frontend
```bash
# Install dependencies
cd frontend
npm install

# Start dev server (http://localhost:3000)
npm run dev

# Build for production
npm run build

# Lint code
npm run lint
```

---

## 📋 Implementation Roadmap

### ✅ Completed (Phase 1-3)
- [x] Backend caching strategy (Option B)
- [x] React frontend setup
- [x] Type-safe API integration
- [x] State management (Zustand)
- [x] Route configuration
- [x] Layout components

### ⏳ To Implement (Phase 4-5)
- [ ] Auth pages (Login, Register)
- [ ] Product pages (List, Detail)
- [ ] Cart & Checkout
- [ ] Order pages
- [ ] Admin pages (10 pages)
- [ ] Testing & Polish

---

## 🔑 Cache Time Reference

| Endpoint | Frontend | Backend | Time | Reason |
|----------|----------|---------|------|--------|
| `GET /api/products` | React Query 10s | Redis 10s | 10s | High read, low write |
| `GET /api/orders/my-orders` | React Query 60s | Redis 60s | 60s | Medium read, eventual consistency |
| `GET /api/products/{id}` | No cache | No cache | Live | Always fresh product detail |
| `GET /api/orders/{id}` | No cache | No cache | Live | Mutable order status |
| `GET /api/inventory/*` | No cache | No cache | Live | Atomic operations, no stale |

---

## 🎯 Interview Bullet Points

### Option B Caching
- ✅ Analyzed 7 GET endpoints, selected 2 for Redis caching
- ✅ Products: 10s cache (100:1 read-write ratio, admin-controlled)
- ✅ Orders: 60s cache (user-scoped, eventual consistency acceptable)
- ✅ Inventory NOT cached (atomic operations, overselling risk)
- ✅ Invalidation: Batch flush on write (simple, acceptable for low-frequency updates)

### Frontend Architecture
- ✅ React 18 + Vite (10x faster builds)
- ✅ Zustand for auth & cart (minimal boilerplate)
- ✅ React Query matching backend TTLs (10s, 60s)
- ✅ Axios with JWT interceptor (secure, auto-refresh on 401)
- ✅ Type-safe end-to-end (TypeScript + strict)

### 20 Pages Planned
- ✅ 2 public (auth)
- ✅ 8 user (shop, cart, orders)
- ✅ 10 admin (management dashboards)
- ✅ Role-based layouts + route protection

---

## 🐛 Troubleshooting

### "API calls 401 Unauthorized"
→ Check token in Zustand store: `useAuthStore.getState().token`  
→ Verify Authorization header in Axios  
→ Test with Swagger UI on backend

### "React Query cache not working"
→ Check `staleTime` and `gcTime` values in useQuery  
→ Verify query key matches (includes pagination params)  
→ Clear localStorage auth token: DevTools → Application → Local Storage

### "Styles not applied (Tailwind)"
→ Check index.css is imported in main.tsx  
→ Run `npm run build` to generate CSS bundle  
→ Restart dev server: `npm run dev`

### "Backend API response wrong shape"
→ Check types in `frontend/src/types/`  
→ Compare with backend API endpoint response  
→ Use Swagger UI to test endpoint: http://localhost:8080/swagger-u...

---

## 📞 Support & Next Steps

**Questions**:
- Caching strategy → See `docs/CACHING_STRATEGY.md`
- Frontend setup → See `frontend/README.md`
- Project overview → See `docs/IMPLEMENTATION_SUMMARY.md`
- Type definitions → See `frontend/src/types/`

**Next Sprint**:
1. Implement Auth pages (4 hours)
2. Implement Product pages (6 hours)
3. Implement Cart & Checkout (8 hours)
4. Implement Admin pages (10 hours)
5. Polish & test (6 hours)

**Total**: ~34 hours to complete all 20 pages

---

**Last Updated**: March 27, 2026  
**Status**: 🟢 Ready for implementation
