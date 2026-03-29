import React, { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { productsApi } from '@/api'
import { ProductCardSkeleton, Pagination } from '@/components/ui'
import ProductCard from '@/components/product/ProductCard'

/**
 * ProductListPage
 * Displays products with filters and pagination
 * Uses React Query caching: staleTime=10s (backend Redis TTL)
 */
const ProductListPage: React.FC = () => {
  // State
  const [currentPage, setCurrentPage] = useState(1)
  const [selectedCategory, setSelectedCategory] = useState<string>('all')
  const [priceRange, setPriceRange] = useState<[number, number]>([0, 10000])
  const [searchQuery, setSearchQuery] = useState('')
  const pageSize = 12

  // Fetch products with React Query caching
  // staleTime: 10s matches backend Redis product list TTL
  const { data, isLoading, error } = useQuery({
    queryKey: ['products', currentPage, pageSize, selectedCategory, priceRange, searchQuery],
    queryFn: () =>
      productsApi.getProducts({
        page: currentPage,
        size: pageSize,
        category: selectedCategory === 'all' ? undefined : selectedCategory,
        minPrice: priceRange[0],
        maxPrice: priceRange[1],
        search: searchQuery || undefined,
      }),
    staleTime: 10 * 1000, // 10s - matches backend cache
    gcTime: 5 * 60 * 1000, // 5 minutes
  })

  // Sample categories (in real app, fetch from API)
  const categories = ['all', 'Electronics', 'Fashion', 'Home & Garden', 'Sports', 'Books']

  // Calculate stats
  const totalProducts = data?.totalElements || 0
  const totalPages = Math.ceil(totalProducts / pageSize)

  // Handle page change
  const handlePageChange = (page: number) => {
    setCurrentPage(page)
    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  // Handle filter reset
  const handleResetFilters = () => {
    setSelectedCategory('all')
    setPriceRange([0, 10000])
    setSearchQuery('')
    setCurrentPage(1)
  }

  return (
    <div className="min-h-screen bg-white">
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-blue-600 to-blue-800 text-white py-12 md:py-16">
        <div className="max-w-7xl mx-auto px-4 md:px-6 lg:px-8">
          <h1 className="text-4xl md:text-5xl font-bold mb-3">
            Explore Our Products
          </h1>
          <p className="text-blue-50 text-lg max-w-2xl">
            Discover thousands of premium products across all categories. Find exactly what you're looking for.
          </p>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 md:px-6 lg:px-8 py-10">
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* Sidebar - Filters */}
          <aside className="lg:col-span-1">
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 sticky top-20 space-y-6">
              <div>
                <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center gap-2">
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
                  </svg>
                  Filters
                </h2>
              </div>

              {/* Search */}
              <div>
                <label className="block text-sm font-semibold text-gray-900 mb-2.5">
                  🔍 Search Products
                </label>
                <input
                  type="text"
                  placeholder="Search by name..."
                  value={searchQuery}
                  onChange={(e) => {
                    setSearchQuery(e.target.value)
                    setCurrentPage(1)
                  }}
                  className="w-full px-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                />
              </div>

              {/* Category Filter */}
              <div className="border-t pt-6">
                <label className="block text-sm font-semibold text-gray-900 mb-3.5">
                  📦 Category
                </label>
                <div className="space-y-2.5">
                  {categories.map((cat) => (
                    <label key={cat} className="flex items-center gap-3 cursor-pointer p-2.5 hover:bg-blue-50 rounded-lg transition-colors">
                      <input
                        type="radio"
                        name="category"
                        value={cat}
                        checked={selectedCategory === cat}
                        onChange={(e) => {
                          setSelectedCategory(e.target.value)
                          setCurrentPage(1)
                        }}
                        className="w-4 h-4 text-blue-600 accent-blue-600"
                      />
                      <span className="text-sm text-gray-700 font-medium capitalize">
                        {cat === 'all' ? '✨ All Products' : cat}
                      </span>
                    </label>
                  ))}
                </div>
              </div>

              {/* Price Range Filter */}
              <div className="border-t pt-6">
                <label className="block text-sm font-semibold text-gray-900 mb-3.5">
                  💰 Price Range
                </label>
                <div className="space-y-3.5 bg-gray-50 p-4 rounded-lg">
                  <div>
                    <label className="text-xs font-semibold text-gray-600 mb-2 block">Min: ${priceRange[0]}</label>
                    <input
                      type="number"
                      min="0"
                      max="10000"
                      value={priceRange[0]}
                      onChange={(e) => {
                        setPriceRange([Number(e.target.value), priceRange[1]])
                        setCurrentPage(1)
                      }}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div>
                    <label className="text-xs font-semibold text-gray-600 mb-2 block">Max: ${priceRange[1]}</label>
                    <input
                      type="number"
                      min="0"
                      max="10000"
                      value={priceRange[1]}
                      onChange={(e) => {
                        setPriceRange([priceRange[0], Number(e.target.value)])
                        setCurrentPage(1)
                      }}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                </div>
              </div>

              {/* Reset Button */}
              <button
                onClick={handleResetFilters}
                className="w-full px-4 py-3 bg-gradient-to-r from-blue-500 to-blue-600 text-white rounded-lg hover:from-blue-600 hover:to-blue-700 transition-all transform hover:scale-105 text-sm font-semibold shadow-sm"
              >
                ↺ Reset All Filters
              </button>
            </div>
          </aside>

          {/* Main Content - Products Grid */}
          <main className="lg:col-span-3">
            {/* Results Info */}
            <div className="mb-8">
              <div className="flex flex-wrap items-center justify-between gap-4 bg-blue-50 p-4 rounded-xl border border-blue-100">
                <div>
                  <p className="text-sm font-semibold text-gray-900">
                    Found <span className="text-blue-600">{totalProducts}</span> products
                  </p>
                  <p className="text-xs text-gray-600 mt-1">
                    Showing {data?.content?.length || 0} per page
                  </p>
                </div>
                {(selectedCategory !== 'all' || searchQuery || priceRange[0] > 0 || priceRange[1] < 10000) && (
                  <button
                    onClick={handleResetFilters}
                    className="px-3 py-1.5 bg-white border border-blue-300 text-blue-600 rounded-lg text-xs font-semibold hover:bg-blue-50 transition-colors"
                  >
                    Clear Filters ×
                  </button>
                )}
              </div>
            </div>

            {/* Error State */}
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-xl p-6 flex items-start gap-4 mb-8">
                <div className="text-red-600 text-2xl flex-shrink-0">⚠️</div>
                <div className="flex-grow">
                  <h3 className="font-semibold text-red-900">Failed to load products</h3>
                  <p className="text-sm text-red-700 mt-2">
                    {error instanceof Error ? error.message : 'Please try again or contact support'}
                  </p>
                </div>
                <button
                  onClick={handleResetFilters}
                  className="px-4 py-2 bg-red-600 text-white rounded-lg text-sm font-semibold hover:bg-red-700 transition-colors"
                >
                  Retry
                </button>
              </div>
            )}

            {/* Loading State */}
            {isLoading && (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {Array.from({ length: pageSize }).map((_, i) => (
                  <ProductCardSkeleton key={i} />
                ))}
              </div>
            )}

            {/* Empty State */}
            {!isLoading && (!data?.content || data.content.length === 0) && (
              <div className="text-center py-20">
                <div className="text-6xl mb-6">🔍</div>
                <h3 className="text-2xl font-bold text-gray-900 mb-3">No products found</h3>
                <p className="text-gray-600 mb-8 max-w-md mx-auto">
                  We couldn't find any products matching your filters. Try adjusting your search criteria.
                </p>
                <button
                  onClick={handleResetFilters}
                  className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-semibold"
                >
                  Clear All Filters
                </button>
              </div>
            )}

            {/* Products Grid */}
            {!isLoading && data?.content && data.content.length > 0 && (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                {data.content.map((product) => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </div>
            )}

            {/* Pagination */}
            {!isLoading && totalPages > 1 && (
              <div className="mt-12">
                <Pagination
                  currentPage={currentPage}
                  totalPages={totalPages}
                  onPageChange={handlePageChange}
                  disabled={isLoading}
                />
              </div>
            )}
          </main>
        </div>
      </div>
    </div>
  )
}

export default ProductListPage
