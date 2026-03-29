import React, { useState } from 'react'
import { Outlet, Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '@stores/authStore'
import { useCartStore } from '@stores/cartStore'

/**
 * User Layout - Customer Shopping Interface
 * Used for authenticated user pages (shop, cart, orders)
 * Matches admin design system with sidebar + navbar
 */
const UserLayout: React.FC = () => {
  const navigate = useNavigate()
  const { user, logout, isAdmin } = useAuthStore()
  const { getItemCount } = useCartStore()
  const itemCount = getItemCount()

  const handleLogout = () => {
    logout()
    navigate('/auth/login')
  }

  return (
    <div className="flex h-screen bg-surface overflow-hidden">
      {/* ============ SIDEBAR ============ */}
      <aside className="w-64 fixed left-0 top-0 h-screen bg-slate-50 z-50 flex flex-col p-6 gap-y-2 border-r border-outline-variant/20">
        <div className="mb-10 px-2">
          <h1 className="font-headline font-extrabold text-slate-900 text-xl">InventoryApp</h1>
          <p className="text-[11px] font-inter font-medium tracking-wide uppercase text-slate-500">Store</p>
        </div>
        <nav className="flex-1 space-y-1">
          <Link
            to="/shop/products"
            className="flex items-center gap-3 px-4 py-3 text-slate-500 hover:translate-x-1 transition-transform duration-200 font-inter font-medium tracking-wide uppercase text-[11px]"
          >
            <span className="material-symbols-outlined">shopping_bag</span>
            <span>Products</span>
          </Link>
          <Link
            to="/shop/cart"
            className="flex items-center gap-3 px-4 py-3 text-slate-500 hover:translate-x-1 transition-transform duration-200 font-inter font-medium tracking-wide uppercase text-[11px] relative"
          >
            <span className="material-symbols-outlined">shopping_cart</span>
            <span>Cart</span>
            {itemCount > 0 && (
              <span className="absolute right-2 inline-flex items-center justify-center px-2 py-1 text-[10px] font-bold leading-none text-white transform translate-x-1/2 -translate-y-1/2 bg-primary rounded-full">
                {itemCount}
              </span>
            )}
          </Link>
          <Link
            to="/shop/order-history"
            className="flex items-center gap-3 px-4 py-3 text-slate-500 hover:translate-x-1 transition-transform duration-200 font-inter font-medium tracking-wide uppercase text-[11px]"
          >
            <span className="material-symbols-outlined">package_2</span>
            <span>My Orders</span>
          </Link>

          {/* Admin Access */}
          {isAdmin() && (
            <>
              <div className="my-4 border-t border-outline-variant/20"></div>
              <Link
                to="/admin/dashboard"
                className="flex items-center gap-3 px-4 py-3 text-slate-500 hover:translate-x-1 transition-transform duration-200 font-inter font-medium tracking-wide uppercase text-[11px]"
              >
                <span className="material-symbols-outlined">security</span>
                <span>Admin Panel</span>
              </Link>
            </>
          )}
        </nav>

        {/* Logout */}
        <div className="mt-auto">
          <button
            onClick={handleLogout}
            className="w-full flex items-center gap-3 px-4 py-3 text-slate-500 hover:translate-x-1 transition-transform duration-200 font-inter font-medium tracking-wide uppercase text-[11px]"
          >
            <span className="material-symbols-outlined">logout</span>
            <span>Logout</span>
          </button>
        </div>
      </aside>

      {/* ============ MAIN CONTENT ============ */}
      <main className="ml-64 min-h-screen flex flex-col flex-1">
        {/* ============ HEADER ============ */}
        <header className="fixed top-0 right-0 left-64 z-40 bg-white/70 backdrop-blur-xl shadow-sm h-16 flex items-center justify-between px-8">
          <div className="flex items-center gap-6">
            <div className="relative group">
              <span className="absolute inset-y-0 left-3 flex items-center text-slate-400">
                <span className="material-symbols-outlined text-xl">search</span>
              </span>
              <input
                type="text"
                placeholder="Search products..."
                className="pl-10 pr-4 py-2 bg-surface-container-low border-none rounded-xl focus:ring-2 focus:ring-primary/20 w-64 font-inter text-sm transition-all outline-none"
              />
            </div>
          </div>

          <div className="flex items-center gap-4">
            <button className="p-2 text-slate-500 hover:bg-blue-50/50 rounded-full transition-all">
              <span className="material-symbols-outlined">notifications</span>
            </button>
            <div className="h-8 w-8 rounded-full bg-primary flex items-center justify-center text-white font-bold text-sm">
              {user?.fullName?.[0] || user?.username?.[0] || 'U'}
            </div>
          </div>
        </header>

        {/* Page Content */}
        <div className="flex-1 overflow-y-auto pt-16">
          <Outlet />
        </div>

        {/* Footer */}
        <footer className="mt-auto py-8 bg-surface-container-lowest border-t border-surface-container">
          <div className="max-w-7xl mx-auto px-8 flex flex-col md:flex-row justify-between items-center gap-4">
            <div className="text-on-surface-variant font-label text-xs tracking-wide uppercase">
              © 2026 InventoryApp. All rights reserved.
            </div>
            <div className="flex gap-8">
              <a href="#" className="text-on-surface-variant font-label text-xs tracking-wide uppercase hover:text-primary transition-colors opacity-80 hover:opacity-100">
                Privacy Policy
              </a>
              <a href="#" className="text-on-surface-variant font-label text-xs tracking-wide uppercase hover:text-primary transition-colors opacity-80 hover:opacity-100">
                Terms of Service
              </a>
              <a href="#" className="text-on-surface-variant font-label text-xs tracking-wide uppercase hover:text-primary transition-colors opacity-80 hover:opacity-100">
                Support
              </a>
            </div>
          </div>
        </footer>
      </main>
    </div>
  )
}

export default UserLayout
