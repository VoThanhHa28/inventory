import React from 'react'
import { Outlet, Link, useLocation } from 'react-router-dom'
import { useAuthStore } from '@stores/authStore'

/**
 * Admin Layout
 * Used for admin management pages (products, inventory, orders)
 */
const AdminLayout: React.FC = () => {
  const { user, logout } = useAuthStore()
  const location = useLocation()

  const isActive = (path: string) => location.pathname.startsWith(path)

  return (
    <div className="min-h-screen bg-gray-100 flex">
      {/* Sidebar */}
      <aside className="w-64 bg-gray-800 text-white">
        <div className="p-4 border-b border-gray-700">
          <Link to="/" className="text-xl font-bold text-blue-400">
            Admin Panel
          </Link>
        </div>

        <nav className="mt-4 space-y-2 px-4">
          <Link
            to="/admin/dashboard"
            className={`block px-4 py-2 rounded ${
              isActive('/admin/dashboard') ? 'bg-blue-600' : 'hover:bg-gray-700'
            }`}
          >
            Dashboard
          </Link>
          <Link
            to="/admin/products"
            className={`block px-4 py-2 rounded ${
              isActive('/admin/products') ? 'bg-blue-600' : 'hover:bg-gray-700'
            }`}
          >
            Products
          </Link>
          <Link
            to="/admin/inventory"
            className={`block px-4 py-2 rounded ${
              isActive('/admin/inventory') ? 'bg-blue-600' : 'hover:bg-gray-700'
            }`}
          >
            Inventory
          </Link>
          <Link
            to="/admin/orders"
            className={`block px-4 py-2 rounded ${
              isActive('/admin/orders') ? 'bg-blue-600' : 'hover:bg-gray-700'
            }`}
          >
            Orders
          </Link>
        </nav>

        <div className="absolute bottom-0 w-64 p-4 border-t border-gray-700">
          <div className="flex justify-between items-center mb-2">
            <span className="text-sm">{user?.username}</span>
          </div>
          <button
            onClick={() => {
              logout()
              window.location.href = '/auth/login'
            }}
            className="w-full px-3 py-2 bg-red-600 text-white rounded hover:bg-red-700"
          >
            Logout
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <div className="flex-1 flex flex-col">
        <header className="bg-white shadow-sm py-4 px-8 border-b border-gray-200">
          <h1 className="text-2xl font-bold text-gray-800">Admin Dashboard</h1>
        </header>

        <main className="flex-1 p-8 overflow-auto">
          <Outlet />
        </main>
      </div>
    </div>
  )
}

export default AdminLayout
