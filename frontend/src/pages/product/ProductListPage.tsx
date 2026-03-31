import React, { useState, useEffect } from 'react'
import { useQuery } from '@tanstack/react-query'
import { productsApi } from '@/api'
import { Product } from '@types'

/**
 * ProductListPage
 * Customer product browsing with sidebar filters, search, and pagination
 * Updated design: InventoryCore template with filters, search, pagination, and API integration
 */
const ProductListPage: React.FC = () => {
  
  // State
  const [currentPage, setCurrentPage] = useState(0)
  const [searchInput, setSearchInput] = useState('')
  const [debouncedSearch, setDebouncedSearch] = useState('')
  const [selectedCategory, setSelectedCategory] = useState<string>('all')
  const [sortBy, setSortBy] = useState('newest')
  const pageSize = 12

  // Debounce search - reduce API calls
  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(searchInput)
    }, 300) // Reduced from 500ms for faster responsiveness
    return () => clearTimeout(timer)
  }, [searchInput])

  // Fetch categories from API (dynamic, not hardcoded)
  const { data: categoriesData } = useQuery({
    queryKey: ['categories'],
    staleTime: 1000 * 60 * 5, // Cache for 5 minutes
    gcTime: 1000 * 60 * 30,
    queryFn: async () => {
      const response = await fetch('/api/products/categories/list')
      const json = await response.json()
      return json.data || [] // Extract data from API response
    },
  })
  const categories = categoriesData || []

  // Fetch products with React Query
  const { data, isLoading } = useQuery({
    queryKey: ['products', currentPage, pageSize, selectedCategory, debouncedSearch],
    staleTime: 1000 * 60 * 5, // Cache fresh for 5 minutes (reduced from 10 for fresher data)
    gcTime: 1000 * 60 * 30, // Keep in memory for 30 minutes
    retry: 2, // Retry failed requests 2 times
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
    enabled: true, // Always fetch immediately
    queryFn: () =>
      productsApi.getProducts({
        page: currentPage,
        size: pageSize,
        category: selectedCategory === 'all' ? undefined : selectedCategory,
        search: debouncedSearch || undefined,
      }),
  })

  const products = data?.content || []
  const totalElements = data?.totalElements || 0
  const totalPages = Math.ceil(totalElements / pageSize)

  const handlePageChange = (page: number) => {
    setCurrentPage(page)
    window.scrollTo({ top: 256, behavior: 'smooth' })
  }

  const handleResetFilters = () => {
    setSearchInput('')
    setDebouncedSearch('')
    setSelectedCategory('all')
    setCurrentPage(0)
  }

  const getStockStatus = (quantity: number) => {
    if (quantity === 0) return { label: 'Out of Stock', color: 'bg-red-50 text-red-600', indicator: 'bg-red-500' }
    if (quantity <= 10) return { label: 'Low Stock', color: 'bg-amber-50 text-amber-600', indicator: 'bg-amber-500' }
    return { label: 'In Stock', color: 'bg-green-50 text-green-600', indicator: 'bg-green-500' }
  }

  const generateSKU = (id: number, category?: string): string => {
    const cat = category ? category.slice(0, 1).toUpperCase() : 'P'
    return `${cat}${String(id).padStart(4, '0')}`
  }


  return (
    <div className="bg-surface font-body text-on-surface antialiased">
      {/* Header */}
      <header className="fixed top-0 w-full z-50 bg-white/70 dark:bg-slate-900/70 backdrop-blur-xl shadow-sm shadow-blue-500/5">
        <div className="flex justify-between items-center h-16 px-6 lg:px-12 max-w-full mx-auto">
          <div className="flex items-center gap-2">
            <span className="material-symbols-outlined text-blue-600 dark:text-blue-400">inventory_2</span>
            <h1 className="text-xl font-bold tracking-tighter text-slate-900 dark:text-white font-headline">InventoryCore</h1>
          </div>
          <nav className="hidden md:flex items-center gap-8">
            <a className="text-blue-600 dark:text-blue-400 font-semibold border-b-2 border-blue-600 py-5 font-headline text-sm tracking-wide" href="/shop/products">
              Products
            </a>
            <a className="text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 transition-colors py-5 font-headline text-sm tracking-wide" href="/shop/cart">
              Cart
            </a>
            <a className="text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 transition-colors py-5 font-headline text-sm tracking-wide" href="/shop/order-history">
              My Orders
            </a>
          </nav>
          <div className="flex items-center gap-4">
            <button className="hover:bg-slate-100/50 dark:hover:bg-slate-800/50 rounded-lg p-2 transition-colors">
              <span className="material-symbols-outlined text-on-surface-variant">search</span>
            </button>
            <button className="text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 transition-colors font-inter text-sm font-medium">
              Logout
            </button>
          </div>
        </div>
      </header>

      <div className="flex pt-16 min-h-screen">
        {/* Sidebar */}
        <aside className="hidden lg:block h-screen w-64 fixed left-0 top-16 bg-slate-50 dark:bg-slate-950 flex flex-col py-8 pr-4 overflow-y-auto">
          <div className="px-6 mb-8">
            <p className="font-manrope font-bold tracking-wide text-xs uppercase text-slate-500 dark:text-slate-400">
              Inventory Management
            </p>
          </div>
          <nav className="space-y-1">
            <a
              className="flex items-center gap-4 py-3 px-6 bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300 rounded-r-full font-bold hover:translate-x-1 transition-transform duration-200"
              href="/shop/products"
            >
              <span className="material-symbols-outlined">grid_view</span>
              <span className="font-headline text-xs tracking-wide uppercase">Products</span>
            </a>
            <a
              className="flex items-center gap-4 py-3 px-6 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-900 rounded-r-full hover:translate-x-1 transition-transform duration-200"
              href="/shop/cart"
            >
              <span className="material-symbols-outlined">shopping_cart</span>
              <span className="font-headline text-xs tracking-wide uppercase">Cart</span>
            </a>
            <a
              className="flex items-center gap-4 py-3 px-6 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-900 rounded-r-full hover:translate-x-1 transition-transform duration-200"
              href="/shop/order-history"
            >
              <span className="material-symbols-outlined">package_2</span>
              <span className="font-headline text-xs tracking-wide uppercase">My Orders</span>
            </a>
            <a
              className="flex items-center gap-4 py-3 px-6 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-900 rounded-r-full hover:translate-x-1 transition-transform duration-200"
              href="#"
            >
              <span className="material-symbols-outlined">settings</span>
              <span className="font-headline text-xs tracking-wide uppercase">Settings</span>
            </a>
          </nav>

          {/* Filters */}
          <div className="mt-12 px-6 space-y-8">
            {/* Availability */}
            <div>
              <h3 className="font-headline text-xs font-bold uppercase tracking-widest text-on-surface-variant mb-4">
                Availability
              </h3>
              <div className="space-y-3">
                <label className="flex items-center gap-3 cursor-pointer group">
                  <input type="checkbox" defaultChecked className="rounded border-outline-variant text-primary focus:ring-primary/20 w-5 h-5 bg-surface-container-low" />
                  <span className="text-sm font-medium text-on-surface group-hover:text-primary transition-colors">In Stock</span>
                </label>
                <label className="flex items-center gap-3 cursor-pointer group">
                  <input type="checkbox" className="rounded border-outline-variant text-primary focus:ring-primary/20 w-5 h-5 bg-surface-container-low" />
                  <span className="text-sm font-medium text-on-surface group-hover:text-primary transition-colors">Low Stock</span>
                </label>
              </div>
            </div>

            {/* Categories */}
            <div>
              <h3 className="font-headline text-xs font-bold uppercase tracking-widest text-on-surface-variant mb-4">
                Categories
              </h3>
              <div className="flex flex-wrap gap-2">
                <button
                  onClick={() => setSelectedCategory('all')}
                  className={`px-3 py-1 rounded-full text-xs font-semibold transition-colors ${
                    selectedCategory === 'all'
                      ? 'bg-primary text-white'
                      : 'bg-surface-container-high text-on-surface-variant hover:bg-primary hover:text-white'
                  }`}
                >
                  All
                </button>
                {categories.map((category) => (
                  <button
                    key={category}
                    onClick={() => setSelectedCategory(category)}
                    className={`px-3 py-1 rounded-full text-xs font-semibold transition-colors ${
                      selectedCategory === category
                        ? 'bg-primary text-white'
                        : 'bg-surface-container-high text-on-surface-variant hover:bg-primary hover:text-white'
                    }`}
                  >
                    {category}
                  </button>
                ))}
              </div>
            </div>

            <button
              onClick={handleResetFilters}
              className="w-full py-2 text-sm font-bold text-primary border border-primary rounded-lg hover:bg-primary/10 transition-colors"
            >
              Reset Filters
            </button>
          </div>
        </aside>

        {/* Main Content */}
        <main className="flex-1 lg:ml-64 p-6 lg:p-12 bg-surface">
          {/* Header Section */}
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
            <div>
              <h2 className="text-4xl font-headline font-extrabold tracking-tight text-on-surface">Products</h2>
              <p className="text-on-surface-variant mt-2 font-body italic">
                Curating your global inventory landscape.
              </p>
            </div>
            <div className="flex flex-col sm:flex-row items-center gap-4 w-full md:w-auto">
              <div className="relative w-full sm:w-80">
                <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-outline">
                  search
                </span>
                <input
                  className="w-full pl-12 pr-4 py-3 bg-surface-container-low border-none rounded-xl focus:ring-2 focus:ring-primary/20 focus:bg-surface-container-lowest transition-all placeholder:text-outline/70"
                  placeholder="Search product name or SKU..."
                  type="text"
                  value={searchInput}
                  onChange={(e) => setSearchInput(e.target.value)}
                />
              </div>
              <div className="relative w-full sm:w-auto">
                <select
                  className="w-full sm:w-48 appearance-none py-3 pl-4 pr-10 bg-surface-container-lowest border-none rounded-xl text-sm font-semibold text-on-surface shadow-sm shadow-primary/5 focus:ring-2 focus:ring-primary/20"
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value)}
                >
                  <option value="newest">Sort: Newest</option>
                  <option value="price-low">Price: Low to High</option>
                  <option value="price-high">Price: High to Low</option>
                  <option value="stock">Stock Level</option>
                </select>
              </div>
            </div>
          </div>

          {/* Loading State */}
          {isLoading && (
            <div className="col-span-full text-center py-20">
              <div className="inline-flex flex-col items-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mb-4"></div>
                <p className="text-on-surface-variant font-medium">Loading products...</p>
              </div>
            </div>
          )}

          {/* Empty State */}
          {!isLoading && products.length === 0 && (
            <div className="col-span-full text-center py-20">
              <div className="text-6xl mb-4">📦</div>
              <p className="text-on-surface-variant font-medium text-lg">No products found</p>
              <p className="text-on-surface-variant text-sm mt-2">Try adjusting your filters or search</p>
            </div>
          )}

          {/* Product Grid */}
          {!isLoading && products.length > 0 && (
            <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-10">
              {products.map((product: Product) => {
                const stockStatus = getStockStatus(product.stockQuantity)
                const sku = generateSKU(product.id, product.category)

                return (
                  <div
                    key={product.id}
                    className="group relative bg-surface-container-lowest rounded-[1.5rem] overflow-hidden transition-all duration-300 hover:-translate-y-2 hover:shadow-2xl hover:shadow-primary/10"
                  >
                    <div className="aspect-[4/3] overflow-hidden bg-surface-container-low">
                      <img
                        alt={product.name}
                        className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                        loading="lazy"
                        decoding="async"
                        src={product.imageUrl || `https://via.placeholder.com/400x300?text=${product.id}`}
                      />
                    </div>
                    <div className="p-6">
                      <div className="flex justify-between items-start mb-2">
                        <span className="flex items-center gap-2">
                          <span className={`w-2 h-2 rounded-full shadow-lg ${stockStatus.indicator}`}></span>
                          <span className={`text-[10px] font-bold uppercase tracking-widest font-label ${stockStatus.color}`}>
                            {stockStatus.label}
                          </span>
                        </span>
                        <span className="text-xs font-mono text-outline-variant">{sku}</span>
                      </div>
                      <h3 className="text-lg font-headline font-bold text-on-surface mb-1">{product.name}</h3>
                      <p className="text-sm text-on-surface-variant mb-6 line-clamp-1">{product.description}</p>
                      <div className="flex items-center justify-between mt-auto">
                        <div>
                          <p className="text-[10px] font-bold text-outline uppercase tracking-tighter">Price Per Unit</p>
                          <p className="text-xl font-headline font-extrabold text-on-primary-fixed">
                            ${product.price.toFixed(2)}
                          </p>
                        </div>
                        <button
                          disabled={product.stockQuantity === 0}
                          className={`p-3 rounded-xl transition-all shadow-lg ${
                            product.stockQuantity === 0
                              ? 'bg-surface-container-high text-outline cursor-not-allowed'
                              : 'signature-gradient text-white hover:opacity-90 active:scale-95 shadow-primary/20'
                          }`}
                        >
                          <span className="material-symbols-outlined block text-2xl font-bold" style={{ fontVariationSettings: "'FILL' 0, 'wght' 600" }}>
                            {product.stockQuantity === 0 ? 'remove_shopping_cart' : 'add_shopping_cart'}
                          </span>
                        </button>
                      </div>
                    </div>
                  </div>
                )
              })}
            </div>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="mt-20 flex flex-col sm:flex-row items-center justify-between gap-6 border-t border-outline-variant/10 pt-10">
              <p className="text-sm font-medium text-on-surface-variant">
                Showing <span className="text-on-surface font-bold">{currentPage * pageSize + 1}</span>-
                <span className="text-on-surface font-bold">{Math.min((currentPage + 1) * pageSize, totalElements)}</span> of{' '}
                <span className="text-on-surface font-bold">{totalElements}</span> products
              </p>
              <div className="flex items-center gap-2">
                <button
                  disabled={currentPage === 0}
                  onClick={() => handlePageChange(currentPage - 1)}
                  className="w-10 h-10 flex items-center justify-center rounded-xl bg-surface-container-low text-on-surface-variant hover:bg-surface-container-high transition-colors disabled:opacity-50"
                >
                  <span className="material-symbols-outlined text-lg">chevron_left</span>
                </button>
                {Array.from({ length: Math.min(5, totalPages) }).map((_, i) => (
                  <button
                    key={i}
                    onClick={() => handlePageChange(i)}
                    className={`w-10 h-10 flex items-center justify-center rounded-xl font-bold text-sm transition-colors ${
                      currentPage === i
                        ? 'bg-primary text-white shadow-lg shadow-primary/30'
                        : 'bg-surface-container-low text-on-surface-variant hover:bg-surface-container-high'
                    }`}
                  >
                    {i + 1}
                  </button>
                ))}
                {totalPages > 5 && <span className="px-2 text-outline">...</span>}
                <button
                  disabled={currentPage >= totalPages - 1}
                  onClick={() => handlePageChange(currentPage + 1)}
                  className="w-10 h-10 flex items-center justify-center rounded-xl bg-surface-container-low text-on-surface-variant hover:bg-surface-container-high transition-colors disabled:opacity-50"
                >
                  <span className="material-symbols-outlined text-lg">chevron_right</span>
                </button>
              </div>
            </div>
          )}
        </main>
      </div>

      {/* Mobile Bottom Nav */}
      <div className="md:hidden fixed bottom-0 left-0 w-full bg-white/90 backdrop-blur-xl border-t border-outline-variant/20 px-4 h-20 flex items-center justify-around z-50">
        <a className="flex flex-col items-center gap-1 text-primary" href="/shop/products">
          <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>
            grid_view
          </span>
          <span className="text-[10px] font-bold uppercase tracking-widest font-headline">Products</span>
        </a>
        <a className="flex flex-col items-center gap-1 text-outline" href="/shop/cart">
          <span className="material-symbols-outlined">shopping_cart</span>
          <span className="text-[10px] font-bold uppercase tracking-widest font-headline">Cart</span>
        </a>
        <a className="flex flex-col items-center gap-1 text-outline" href="/shop/order-history">
          <span className="material-symbols-outlined">package_2</span>
          <span className="text-[10px] font-bold uppercase tracking-widest font-headline">Orders</span>
        </a>
      </div>
    </div>
  )
}

export default ProductListPage
