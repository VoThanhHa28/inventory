import React from 'react'
import { Outlet, Link } from 'react-router-dom'
import { useAuthStore } from '@stores/authStore'
import { useCartStore } from '@stores/cartStore'

/**
 * User Layout
 * Used for authenticated user pages (shop, cart, orders)
 */
const UserLayout: React.FC = () => {
  const { user, logout } = useAuthStore()
  const { getItemCount } = useCartStore()
  const itemCount = getItemCount()

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      {/* Header/Navbar */}
      <header className="bg-white shadow-sm">
        <nav className="container mx-auto px-4 py-4 flex justify-between items-center">
          <Link to="/" className="text-2xl font-bold text-blue-600">
            E-Commerce
          </Link>

          <div className="flex gap-6 items-center">
            <Link to="/shop/products" className="hover:text-blue-600">
              Products
            </Link>
            <Link to="/shop/order-history" className="hover:text-blue-600">
              My Orders
            </Link>
            <Link to="/shop/cart" className="relative hover:text-blue-600">
              Cart
              {itemCount > 0 && (
                <span className="absolute top-0 right-0 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-sm transform translate-x-2 -translate-y-2">
                  {itemCount}
                </span>
              )}
            </Link>

            <div className="flex gap-2 items-center">
              <span className="text-sm text-gray-600">{user?.username}</span>
              <button
                onClick={() => {
                  logout()
                  window.location.href = '/auth/login'
                }}
                className="px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600"
              >
                Logout
              </button>
            </div>
          </div>
        </nav>
      </header>

      {/* Main Content */}
      <main className="flex-1 container mx-auto px-4 py-8">
        <Outlet />
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200 py-4 mt-8">
        <div className="container mx-auto px-4 text-center text-gray-600">
          <p>&copy; 2026 E-Commerce Inventory System. All rights reserved.</p>
        </div>
      </footer>
    </div>
  )
}

export default UserLayout
