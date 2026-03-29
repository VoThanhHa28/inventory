import React, { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { productsApi } from '@/api'
import { useAuthStore } from '@stores/authStore'

/**
 * AdminInventoryPage
 * Admin product inventory management with table view
 * Features: Search, Pagination, KPI metrics, Edit/Delete actions
 */
const AdminInventoryPage: React.FC = () => {
  const navigate = useNavigate()
  const { logout, user } = useAuthStore()
  const [isMobileNavOpen, setIsMobileNavOpen] = useState(false)
  const [currentPage, setCurrentPage] = useState(0)
  const [searchQuery, setSearchQuery] = useState('')
  const pageSize = 10

  // Fetch products
  const { data, isLoading } = useQuery({
    queryKey: ['admin-products', currentPage, pageSize, searchQuery],
    queryFn: () =>
      productsApi.getProducts({
        page: currentPage,
        size: pageSize,
        search: searchQuery || undefined,
      }),
    staleTime: 5 * 1000,
  })

  // SKU Generator - Cách 4: Category code + padded ID
  const generateSKU = (productId: number, category?: string): string => {
    const cat = category ? category.slice(0, 1).toUpperCase() : 'P'
    const id = String(productId).padStart(4, '0')
    return `${cat}${id}`
  }

  // Calculate stats
  const stats = {
    totalProducts: data?.totalElements || 0,
    inStock: data?.content?.filter((p) => p.stockQuantity > 10).length || 0,
    lowStock: data?.content?.filter((p) => p.stockQuantity > 0 && p.stockQuantity <= 10).length || 0,
    outOfStock: data?.content?.filter((p) => p.stockQuantity === 0).length || 0,
  }

  // Get low stock threshold from config (default: 10)
  const LOW_STOCK_THRESHOLD = 10

  const getStockStatus = (quantity: number) => {
    if (quantity === 0) return 'outOfStock'
    if (quantity <= LOW_STOCK_THRESHOLD) return 'lowStock'
    return 'inStock'
  }

  const getStockColor = (status: string) => {
    switch (status) {
      case 'outOfStock':
        return 'text-red-600 bg-red-50'
      case 'lowStock':
        return 'text-amber-600 bg-amber-50'
      default:
        return 'text-green-600 bg-green-50'
    }
  }

  const handleLogout = () => {
    logout()
    navigate('/auth/login')
  }

  return (
    <div className="flex h-screen bg-slate-50 overflow-hidden">
      {/* ============ SIDEBAR ============ */}
      <aside className="w-64 fixed left-0 top-0 h-screen bg-slate-50 z-50 flex flex-col p-6 gap-y-2 border-r border-outline-variant/20">
        <div className="mb-10 px-2">
          <h1 className="font-headline font-extrabold text-slate-900 text-xl">InventoryApp</h1>
          <p className="text-[11px] font-inter font-medium tracking-wide uppercase text-slate-500">Admin Panel</p>
        </div>
        <nav className="flex-1 space-y-1">
          <a
            href="/admin/dashboard"
            className="flex items-center gap-3 px-4 py-3 text-slate-500 hover:translate-x-1 transition-transform duration-200 font-inter font-medium tracking-wide uppercase text-[11px]"
          >
            <span className="material-symbols-outlined">dashboard</span>
            <span>Dashboard</span>
          </a>
          <button
            onClick={() => navigate('/admin/inventory')}
            className="w-full flex items-center gap-3 px-4 py-3 bg-white text-primary rounded-lg shadow-sm font-bold font-inter tracking-wide uppercase text-[11px] scale-[0.98] transition-all"
          >
            <span className="material-symbols-outlined">inventory_2</span>
            <span>Inventory</span>
          </button>
          <a
            href="/shop/products"
            className="flex items-center gap-3 px-4 py-3 text-slate-500 hover:translate-x-1 transition-transform duration-200 font-inter font-medium tracking-wide uppercase text-[11px]"
          >
            <span className="material-symbols-outlined">shopping_cart</span>
            <span>View Store</span>
          </a>
        </nav>
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
                value={searchQuery}
                onChange={(e) => {
                  setSearchQuery(e.target.value)
                  setCurrentPage(0)
                }}
                className="pl-10 pr-4 py-2 bg-surface-container-low border-none rounded-xl focus:ring-2 focus:ring-primary/20 w-64 font-inter text-sm transition-all outline-none"
              />
            </div>
          </div>
          <div className="flex items-center gap-4">
            <button className="p-2 text-slate-500 hover:bg-blue-50/50 rounded-full transition-all">
              <span className="material-symbols-outlined">notifications</span>
            </button>
            <div className="h-8 w-8 rounded-full bg-primary flex items-center justify-center text-white font-bold text-sm">
              {user?.fullName?.[0] || 'A'}
            </div>
          </div>
        </header>

        {/* ============ CONTENT AREA ============ */}
        <div className="flex-1 overflow-y-auto pt-24 px-12 pb-12">
          {/* Editorial Header */}
          <div className="flex justify-between items-end mb-10">
            <div>
              <h2 className="font-headline text-3xl font-bold tracking-tight text-on-surface">Product Inventory</h2>
              <p className="font-body text-on-surface-variant mt-1">
                Manage and monitor your curated collection of {stats.totalProducts} assets.
              </p>
            </div>
            <div className="flex gap-3">
              <button className="flex items-center gap-2 px-5 py-2.5 bg-surface-container-high text-on-surface rounded-lg font-medium transition-all hover:bg-surface-container-highest">
                <span className="material-symbols-outlined text-lg">filter_list</span>
                <span>Filters</span>
              </button>
              <button className="flex items-center gap-2 px-5 py-2.5 bg-gradient-to-br from-primary to-primary-container text-white rounded-lg font-semibold shadow-lg shadow-primary/20 hover:scale-[1.02] transition-all active:scale-95">
                <span className="material-symbols-outlined text-lg">add</span>
                <span>New Product</span>
              </button>
            </div>
          </div>

          {/* Dashboard Bento Grid (KPIs) */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-12">
            {/* Total SKU Count */}
            <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant/10">
              <p className="font-label text-[11px] uppercase tracking-wider text-on-surface-variant mb-2">Total SKU Count</p>
              <h3 className="font-headline text-3xl font-extrabold text-on-surface">{stats.totalProducts}</h3>
              <div className="mt-4 flex items-center text-xs text-slate-600 font-semibold">
                <span className="material-symbols-outlined text-sm mr-1">inventory_2</span>
                Total products in system
              </div>
            </div>

            {/* In Stock */}
            <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant/10">
              <p className="font-label text-[11px] uppercase tracking-wider text-on-surface-variant mb-2">In Stock</p>
              <h3 className="font-headline text-3xl font-extrabold text-on-surface">{stats.inStock}</h3>
              <div className="mt-4 flex items-center gap-2">
                <div className="w-2 h-2 rounded-full bg-green-500 shadow-[0_0_8px_rgba(34,197,94,0.6)]"></div>
                <span className="text-xs text-on-surface-variant">Optimal levels</span>
              </div>
            </div>

            {/* Low Stock */}
            <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant/10">
              <p className="font-label text-[11px] uppercase tracking-wider text-on-surface-variant mb-2">Low Stock</p>
              <h3 className="font-headline text-3xl font-extrabold text-on-surface">{stats.lowStock}</h3>
              <div className="mt-4 flex items-center gap-2">
                <div className="w-2 h-2 rounded-full bg-amber-500 shadow-[0_0_8px_rgba(245,158,11,0.6)]"></div>
                <span className="text-xs text-on-surface-variant">Needs attention</span>
              </div>
            </div>

            {/* Out of Stock */}
            <div className="bg-surface-container-lowest p-6 rounded-xl border border-outline-variant/10">
              <p className="font-label text-[11px] uppercase tracking-wider text-on-surface-variant mb-2">Out of Stock</p>
              <h3 className="font-headline text-3xl font-extrabold text-error">{stats.outOfStock}</h3>
              <div className="mt-4 flex items-center gap-2">
                <div className="w-2 h-2 rounded-full bg-error shadow-[0_0_8px_rgba(186,26,26,0.6)]"></div>
                <span className="text-xs text-error font-medium">Critical deficit</span>
              </div>
            </div>
          </div>

          {/* Product List Table Container */}
          <div className="bg-surface-container-lowest rounded-2xl shadow-xl shadow-slate-200/50 overflow-hidden">
            {isLoading ? (
              <div className="flex items-center justify-center h-64">
                <div className="text-center">
                  <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
                  <p className="text-slate-600 font-medium">Loading products...</p>
                </div>
              </div>
            ) : (
              <>
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className="bg-surface-container-low">
                      <th className="px-6 py-4 font-label text-[11px] uppercase tracking-wider text-on-surface-variant font-semibold">
                        Product
                      </th>
                      <th className="px-6 py-4 font-label text-[11px] uppercase tracking-wider text-on-surface-variant font-semibold">
                        SKU / ID
                      </th>
                      <th className="px-6 py-4 font-label text-[11px] uppercase tracking-wider text-on-surface-variant font-semibold">
                        Category
                      </th>
                      <th className="px-6 py-4 font-label text-[11px] uppercase tracking-wider text-on-surface-variant font-semibold text-right">
                        Price
                      </th>
                      <th className="px-6 py-4 font-label text-[11px] uppercase tracking-wider text-on-surface-variant font-semibold text-center">
                        Stock
                      </th>
                      <th className="px-6 py-4 font-label text-[11px] uppercase tracking-wider text-on-surface-variant font-semibold text-right">
                        Actions
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-surface-container">
                    {data?.content && data.content.length > 0 ? (
                      data.content.map((product) => {
                        const status = getStockStatus(product.stockQuantity)
                        const sku = generateSKU(product.id, product.category)

                        return (
                          <tr
                            key={product.id}
                            className={`group transition-colors ${
                              status === 'outOfStock' ? 'bg-red-50/30' : status === 'lowStock' ? 'bg-amber-50/30' : 'hover:bg-surface-container-low'
                            }`}
                          >
                            <td className="px-6 py-5">
                              <div className="flex items-center gap-4">
                                <div className="h-12 w-12 rounded-xl bg-surface-container overflow-hidden flex-shrink-0">
                                  {product.imageUrl ? (
                                    <img
                                      alt={product.name}
                                      className="w-full h-full object-cover"
                                      src={product.imageUrl}
                                    />
                                  ) : (
                                    <div className="w-full h-full bg-gradient-to-br from-primary/20 to-primary/10 flex items-center justify-center">
                                      <span className="material-symbols-outlined text-primary text-lg">image</span>
                                    </div>
                                  )}
                                </div>
                                <div>
                                  <div className="font-headline font-bold text-on-surface">{product.name}</div>
                                  <div className="text-xs text-on-surface-variant">
                                    {product.category || 'Uncategorized'}
                                  </div>
                                </div>
                              </div>
                            </td>
                            <td className="px-6 py-5">
                              <span className="font-mono text-xs text-on-surface-variant bg-surface-container-low px-2 py-1 rounded">
                                {sku}
                              </span>
                            </td>
                            <td className="px-6 py-5">
                              <span className="text-sm">{product.category || '-'}</span>
                            </td>
                            <td className="px-6 py-5 text-right">
                              <span className="font-semibold text-on-surface">${product.price.toFixed(2)}</span>
                            </td>
                            <td className="px-6 py-5">
                              <div className="flex flex-col items-center">
                                <span className={`text-sm font-bold ${getStockColor(status)}`}>
                                  {product.stockQuantity} Units
                                </span>
                                <div className="w-16 h-1 bg-surface-container rounded-full mt-1 overflow-hidden">
                                  <div
                                    className={`h-full transition-all ${
                                      status === 'outOfStock'
                                        ? 'w-0 bg-error'
                                        : status === 'lowStock'
                                          ? 'w-1/4 bg-amber-500'
                                          : 'w-3/4 bg-green-500'
                                    }`}
                                  ></div>
                                </div>
                              </div>
                            </td>
                            <td className="px-6 py-5 text-right">
                              <div className="flex justify-end gap-2">
                                <button className="p-2 text-on-surface-variant hover:text-primary hover:bg-primary/10 rounded-lg transition-all">
                                  <span className="material-symbols-outlined text-lg">edit</span>
                                </button>
                                <button className="p-2 text-on-surface-variant hover:text-error hover:bg-error/10 rounded-lg transition-all">
                                  <span className="material-symbols-outlined text-lg">delete</span>
                                </button>
                              </div>
                            </td>
                          </tr>
                        )
                      })
                    ) : (
                      <tr>
                        <td colSpan={6} className="px-6 py-8 text-center">
                          <p className="text-on-surface-variant">No products found</p>
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>

                {/* Pagination (Footer) */}
                <div className="px-8 py-5 border-t border-surface-container bg-surface-container-low/30 flex items-center justify-between">
                  <span className="text-sm text-on-surface-variant font-medium">
                    Showing {data?.content?.length || 0} of {data?.totalElements || 0} products
                  </span>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                      disabled={currentPage === 0}
                      className="p-2 rounded-lg border border-outline-variant hover:bg-surface-container transition-all disabled:opacity-30"
                    >
                      <span className="material-symbols-outlined">chevron_left</span>
                    </button>
                    <div className="flex gap-1">
                      {Array.from({ length: data?.totalPages || 1 }).map((_, idx) => (
                        <button
                          key={idx}
                          onClick={() => setCurrentPage(idx)}
                          className={`w-10 h-10 rounded-lg font-medium text-sm transition-all ${
                            currentPage === idx
                              ? 'bg-primary text-white'
                              : 'hover:bg-surface-container text-on-surface'
                          }`}
                        >
                          {idx + 1}
                        </button>
                      ))}
                    </div>
                    <button
                      onClick={() => setCurrentPage(currentPage + 1)}
                      disabled={currentPage >= (data?.totalPages || 1) - 1}
                      className="p-2 rounded-lg border border-outline-variant hover:bg-surface-container transition-all disabled:opacity-30"
                    >
                      <span className="material-symbols-outlined">chevron_right</span>
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      </main>
    </div>
  )
}

export default AdminInventoryPage
