# E-Commerce Inventory Frontend - React/TypeScript

## 🎯 Project Overview

Modern React frontend for the E-Commerce & Inventory system with:
- **React 18** + **Vite** (builds in <1 second)
- **TypeScript** for full type safety
- **Tailwind CSS** + **shadcn/ui** for clean, responsive UI
- **Zustand** for lightweight state management
- **React Query** for server state & caching (matches Redis TTL)
- **Axios** with JWT interceptor for secure API calls

**Option B Caching Strategy Implemented**:
- ✅ Product List: 10s cache (matches backend Redis)
- ✅ User Orders: 60s cache (matches backend Redis)  
- ❌ Inventory: No cache (real-time critical)
- ❌ Order Detail: No cache (mutable, access-controlled)

---

## 📁 Folder Structure

```
frontend/
├── src/
│   ├── pages/                    # 20 page components (route level)
│   │   ├── auth/                 # Login, Register
│   │   ├── shop/                 # User: Products, Cart, Orders
│   │   └── admin/                # Admin: Management pages
│   │
│   ├── components/               # Reusable UI components
│   │   ├── common/               # Header, Navbar, Footer, Spinner
│   │   ├── ui/                   # Buttons, Cards, Modals, Forms
│   │   ├── shop/                 # ProductCard, ProductGrid, CartItem
│   │   └── admin/                # Tables, Forms, Stats
│   │
│   ├── stores/                   # Zustand state management
│   │   ├── authStore.ts          # User auth, token, role
│   │   └── cartStore.ts          # Shopping cart items
│   │
│   ├── api/                      # HTTP services + React Query hooks
│   │   ├── client.ts             # Axios instance with JWT interceptor
│   │   ├── auth.ts               # Auth endpoints
│   │   ├── products.ts           # Product endpoints + caching
│   │   └── orders.ts             # Order endpoints + caching
│   │
│   ├── hooks/                    # Custom React hooks
│   │   ├── useAuth.ts            # Auth logic
│   │   ├── useCart.ts            # Cart logic
│   │   ├── useProducts.ts        # Product data fetching
│   │   └── useOrders.ts          # Order management
│   │
│   ├── types/                    # TypeScript type definitions
│   │   ├── api.ts                # API response/error types
│   │   ├── auth.ts               # User, Role, AuthRequest types
│   │   ├── product.ts            # Product, ProductFilter types
│   │   ├── order.ts              # Order, OrderStatus types
│   │   └── index.ts              # Export all types
│   │
│   ├── utils/                    # Helper utilities
│   │   ├── constants.ts          # App constants, routes, cache timing
│   │   ├── formatters.ts         # formatPrice, formatDate, etc.
│   │   └── validators.ts         # Form validation functions
│   │
│   ├── layouts/                  # Layout wrappers
│   │   ├── PublicLayout.tsx      # Auth pages (header, footer)
│   │   ├── UserLayout.tsx        # Shop pages (navbar, cart badge)
│   │   └── AdminLayout.tsx       # Admin pages (sidebar, header)
│   │
│   ├── routes/                   # Routing configuration
│   │   └── index.tsx             # All routes, ProtectedRoute component
│   │
│   ├── App.tsx                   # Root component (React Query + Router setup)
│   ├── main.tsx                  # Entry point
│   └── index.css                 # Global Tailwind styles
│
├── package.json                  # Dependencies
├── tsconfig.json                 # TypeScript config
├── vite.config.ts                # Vite build config
├── tailwind.config.js            # Tailwind styles config
├── postcss.config.js             # PostCSS config (for Tailwind)
├── .env.example                  # Environment variables template
├── .gitignore
├── index.html
└── README.md                     # This file
```

---

## 🚀 Getting Started

### 1. Prerequisites
- Node.js 16+ and npm/yarn
- Backend running on `http://localhost:8080`
- Copy `.env.example` to `.env.local` if needed

### 2. Installation

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server (http://localhost:3000)
npm run dev
```

### 3. Build for Production

```bash
# Build optimized production bundle
npm run build

# Preview production build locally
npm run preview
```

---

## 📚 Technology Stack

| Tool | Purpose | Why? |
|------|---------|------|
| **React 18** | UI framework | Modern, hooks, performance |
| **Vite** | Build tool | ~10x faster than webpack |
| **TypeScript** | Type safety | Catch errors at compile time |
| **Tailwind CSS** | Styling | Utility-first, rapid development |
| **shadcn/ui** | UI components | Unstyled, customizable components |
| **Zustand** | State management | Lightweight, no boilerplate |
| **React Query** | Server state | Caching, synchronization, refetching |
| **Axios** | HTTP client | Promise-based, interceptors |
| **React Router v6** | Routing | Modern, type-safe routing |

---

## 🔐 Authentication Flow

### Login/Register
```typescript
// 1. User submits email + password
const { data } = await authApi.login(credentials)
// { token: "jwt...", user: { id, email, role } }

// 2. Token stored in Zustand + localStorage
useAuthStore.setState({ token, user })
localStorage.setItem('auth_token', token)

// 3. Axios interceptor adds Authorization header
apiClient.interceptors.request.use((config) => {
  config.headers.Authorization = `Bearer ${token}`
  return config
})

// 4. On 401 response, auto logout
if (error.response?.status === 401) {
  useAuthStore.getState().logout()
  window.location.href = '/auth/login'
}
```

### Token Restoration
```typescript
// On app startup (App.tsx useEffect)
restoreAuthSession() {
  const token = localStorage.getItem('auth_token')
  if (token) {
    useAuthStore.setState({ token, isAuthenticated: true })
    // Optional: Validate token with backend
  }
}
```

---

## 📊 Caching Strategy (Option B)

### Product List - 10s Cache
```typescript
// api/products.ts
export const useProducts = (page = 0, size = 10) => {
  return useQuery({
    queryKey: ['products', page, size],  // Cache key includes pagination
    queryFn: async () => {
      // Hits Redis on backend (10s TTL)
      const response = await apiClient.get('/products', { params: { page, size } })
      return response.data.data
    },
    staleTime: 10000,     // 10s - mark as stale after 10s
    gcTime: 30000,        // 30s - keep in memory for 30s
  })
}

// Usage in component
const { data: products, isLoading } = useProducts(0, 10)
```

**Flow**:
1. Component mounts → React Query checks cache
2. Cache miss → API call to backend
3. Backend checks Redis (cache miss) → DB query
4. Response cached for 10s
5. Within 10s, same query → instant from cache
6. After 10s → marked stale, background refetch
7. User navigates away → kept for 30s, then GC'd

### User Orders - 60s Cache
```typescript
// api/orders.ts
export const useMyOrders = (page = 0, size = 10) => {
  return useQuery({
    queryKey: ['myOrders', page, size],
    queryFn: async () => {
      // Hits Redis on backend (60s TTL)
      const response = await apiClient.get('/orders/my-orders', { params: { page, size } })
      return response.data.data
    },
    staleTime: 60000,     // 60s - longer than product list
    gcTime: 180000,       // 180s - keep for 3 minutes
  })
}

// On order status update, invalidate cache
const useUpdateOrderStatus = () => {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: async ({ orderId, status }) => {
      await apiClient.put(`/orders/${orderId}/status`, {}, { params: { status } })
    },
    onSuccess: () => {
      // Force refetch all order caches
      queryClient.invalidateQueries({ queryKey: ['myOrders'] })
    },
  })
}
```

### No Cache: Product Detail
```typescript
// Always fresh - no cache
export const useProductDetail = (productId) => {
  return useQuery({
    queryKey: ['product', productId],
    queryFn: async () => {
      // Always hits backend (no Redis cache)
      const response = await apiClient.get(`/products/${productId}`)
      return response.data.data
    },
    staleTime: 0,  // Always stale, immediate refetch
  })
}
```

---

## 🛒 State Management

### Auth Store (Zustand)
```typescript
// stores/authStore.ts
const { user, token, isAuthenticated, logout, isAdmin } = useAuthStore()

// Persist token to localStorage
setAuth(user, token)  // Stores in localStorage + state
```

### Cart Store (Zustand)
```typescript
// stores/cartStore.ts
const { items, addItem, removeItem, getTotal, isEmpty } = useCartStore()

// Add to cart
useCartStore.getState().addItem(product, quantity)

// IMPORTANT: Cart is in-memory only (not persisted)
// Lost on page refresh - users must complete checkout
```

---

## 🌐 API Integration

### Axios Instance with JWT
```typescript
// api/client.ts
const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000,
})

// Request interceptor: Add JWT
apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Response interceptor: Handle 401
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout()
      window.location.href = '/auth/login'
    }
    return Promise.reject(error)
  }
)
```

### API Services (Typed)
```typescript
// api/products.ts
interface Product {
  id: number
  name: string
  price: number
  stockQuantity: number
}

export const useProducts = (page: number, size: number) => {
  return useQuery({
    queryKey: ['products', page, size],
    queryFn: async (): Promise<Page<Product>> => {
      const response = await apiClient.get<ApiResponse<Page<Product>>>('/products', ...)
      return response.data.data
    },
  })
}
```

---

## 📝 Implementation Checklist

### Phase 1: Core Setup ✅
- [x] Folder structure
- [x] ConfigurationFiles (vite, tsconfig, tailwind)
- [x] Types & interfaces
- [x] API client with JWT interceptor
- [x] Zustand stores (auth, cart)
- [x] React Query setup
- [x] Layouts (Public, User, Admin)
- [x] Routing

### Phase 2: Auth Pages (To Implement)
- [ ] Login page with form
- [ ] Register page with validation
- [ ] Password strength indicator
- [ ] Error handling & messages

### Phase 3: Shop Pages (To Implement)
- [ ] Product listing (with pagination, filters, caching)
- [ ] Product detail (images, description, add-to-cart)
- [ ] Shopping cart (list items, update quantities)
- [ ] Checkout (address form, order placement)
- [ ] Order confirmation
- [ ] Order history (cached 60s)
- [ ] Order detail (individual order tracking)

### Phase 4: Admin Pages (To Implement)
- [ ] Admin dashboard (stats, graphs)
- [ ] Product management (list, create, edit, delete)
- [ ] Inventory management (stock operations)
- [ ] Order management (list, status updates)
- [ ] User management (optional)

### Phase 5: Polish (ToImplement)
- [ ] Error boundaries & error pages
- [ ] Loading states & skeletons
- [ ] Toast notifications
- [ ] Form validation & error messages
- [ ] Responsive design mobile/tablet
- [ ] Accessibility (a11y)
- [ ] Unit tests
- [ ] Integration tests

---

## 🔧 Development Workflow

### Adding a New Page
1. Create `.tsx` file in `src/pages/{section}/`
2. Add route in `src/routes/index.tsx`
3. Create API hooks in `src/api/`
4. Create stores if needed in `src/stores/`
5. Add types in `src/types/`

### Adding a New Component
1. Create `.tsx` file in `src/components/{category}/`
2. Use TypeScript + Tailwind for styling
3. Export from `src/components/index.ts`

### API Call Pattern
```typescript
// In API file
export const useFetchData = (id: number) => {
  return useQuery({
    queryKey: ['data', id],
    queryFn: async () => {
      const response = await apiClient.get<ApiResponse<Data>>(`/data/${id}`)
      return response.data.data
    },
  })
}

// In component
const { data, isLoading, error } = useFetchData(id)
```

---

## 🧪 Testing

```bash
# Unit and integration tests (to configure)
npm run test

# Coverage report
npm run test:coverage
```

---

## 📦 Deployment

### Build for Production
```bash
npm run build
# Output: dist/ folder (ready for nginx, Vercel, AWS S3, etc.)
```

### Serve with Nginx
```nginx
server {
  listen 80;
  root /var/www/dist;
  index index.html;
  
  location / {
    try_files $uri /index.html;
  }
  
  location /api {
    proxy_pass http://backend:8080/api;
  }
}
```

---

## 🐛 Debugging

### React DevTools
- Install React DevTools Chrome extension
- Inspect component tree, props, hooks state

### Redux DevTools (Future)
- When switching to Redux, use Redux DevTools for time-travel debugging

### Network Debugging
- Open Chrome DevTools → Network tab
- See all API calls, response times, caching headers
- Check Authorization headers, JWT tokens

### Query Debugging
```typescript
// Add React Query Devtools (development only)
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      {/* Components */}
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  )
}
```

---

## 📖 Related Documentation

- [Backend Caching Strategy](../docs/CACHING_STRATEGY.md)
- [API Documentation](../docs/PRODUCT_MODULE.md)
- [Infrastructure Setup](../docs/Infrastructure_Module.md)

---

## ✅ Summary

**Option B Frontend Implementation**:
- ✅ React 18 + Vite + TypeScript
- ✅ Tailwind CSS + shadcn/ui for UI
- ✅ Zustand for state (auth, cart)
- ✅ React Query with smart caching (10s products, 60s orders)
- ✅ Axios with JWT interceptor
- ✅ 20 pages planned (2 public, 8 user, 10 admin)
- ✅ Type-safe end-to-end architecture
- ✅ Production-ready folder structure

**Next Steps**:
1. Implement Auth pages (Login, Register)
2. Implement Shop pages (Product list/detail, Cart)
3. Implement Admin pages (Management dashboards)
4. Add error handling, validation, notifications
5. Test with backend APIs
6. Deploy to production

---

**Status**: ✅ **Framework and infrastructure ready. Pages pending implementation.**
