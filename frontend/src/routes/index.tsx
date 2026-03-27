import React from 'react'
import { Navigate, Route, Routes as RouterRoutes } from 'react-router-dom'
import { useAuthStore } from '@stores/authStore'

// Layout imports
import PublicLayout from '@layouts/PublicLayout'
import UserLayout from '@layouts/UserLayout'
import AdminLayout from '@layouts/AdminLayout'

// Auth page imports
import LoginPage from '@pages/auth/LoginPage'
import RegisterPage from '@pages/auth/RegisterPage'

// Product page imports
import ProductListPage from '@pages/product/ProductListPage'
import ProductDetailPage from '@pages/product/ProductDetailPage'

// Placeholder components (will be implemented)
const NotFoundPage = () => <div className="p-8 text-center">404 - Page Not Found</div>

/**
 * Protected Route Component
 */
const ProtectedRoute: React.FC<{ children: React.ReactNode; requiredRole?: 'ADMIN' | 'USER' }> = ({
  children,
  requiredRole,
}) => {
  const { isAuthenticated, isAdmin } = useAuthStore()

  if (!isAuthenticated) {
    return <Navigate to="/auth/login" replace />
  }

  if (requiredRole === 'ADMIN' && !isAdmin()) {
    return <Navigate to="/" replace />
  }

  return <>{children}</>
}

/**
 * Main Routes Configuration
 */
const Routes = () => {
  return (
    <RouterRoutes>
      {/* Public Routes */}
      <Route element={<PublicLayout />}>
        <Route path="/auth/login" element={<LoginPage />} />
        <Route path="/auth/register" element={<RegisterPage />} />
      </Route>

      {/* User Routes */}
      <Route
        element={
          <ProtectedRoute requiredRole="USER">
            <UserLayout />
          </ProtectedRoute>
        }
      >
        {/* Products */}
        <Route path="/shop/products" element={<ProductListPage />} />
        <Route path="/shop/products/:productId" element={<ProductDetailPage />} />
        
        {/* Cart & Orders */}
        {/* <Route path="/shop/cart" element={<CartPage />} /> */}
        {/* <Route path="/shop/checkout" element={<CheckoutPage />} /> */}
        {/* <Route path="/shop/order-confirmation/:id" element={<OrderConfirmationPage />} /> */}
        {/* <Route path="/shop/order-history" element={<OrderHistoryPage />} /> */}
        {/* <Route path="/shop/orders/:id" element={<OrderDetailPage />} /> */}
      </Route>

      {/* Admin Routes */}
      <Route
        element={
          <ProtectedRoute requiredRole="ADMIN">
            <AdminLayout />
          </ProtectedRoute>
        }
      >
        {/* Admin Dashboard & Management */}
        {/* <Route path="/admin/dashboard" element={<AdminDashboardPage />} /> */}
        {/* <Route path="/admin/products" element={<AdminProductsPage />} /> */}
        {/* <Route path="/admin/products/create" element={<AdminProductFormPage />} /> */}
        {/* <Route path="/admin/products/:id/edit" element={<AdminProductFormPage />} /> */}
        {/* <Route path="/admin/inventory" element={<AdminInventoryPage />} /> */}
        {/* <Route path="/admin/orders" element={<AdminOrdersPage />} /> */}
        {/* <Route path="/admin/orders/:id" element={<AdminOrderDetailPage />} /> */}
      </Route>

      {/* Catch-all */}
      <Route path="*" element={<NotFoundPage />} />
    </RouterRoutes>
  )
}

export default Routes
