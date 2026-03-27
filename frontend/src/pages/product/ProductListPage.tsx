import React, { useState, useMemo } from 'react'
import { useQuery } from '@tanstack/react-query'
import { productsApi } from '@/api'
import { ProductCardSkeleton, Pagination } from '@/components/ui'
import ProductCard from '@/components/product/ProductCard'
import { AlertCircle } from 'lucide-react'

/**
 * ProductListPage
 * Displays products with filters and pagination
 * Uses React Query caching: staleTime=10s (backend Redis TTL)
 */
const ProductListPage: React.FC = () => {
  // State
  const [currentPage, setCurrentPage] = useState(1)
  const [selectedCategory, setSelectedCategory] = useState<string>('all')
  const [priceRange, setPriceRange] = useState<[number, number]>([0, 1000])
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
    setPriceRange([0, 1000])
    setSearchQuery('')
    setCurrentPage(1)
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 md:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl md:text-4xl font-bold text-gray-900 mb-2">
            Our Products
          </h1>
          <p className="text-gray-600">
            Discover our wide selection of quality products
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* Sidebar - Filters */}
          <aside className="lg:col-span-1">
            <div className="bg-white rounded-lg shadow p-6 sticky top-20 space-y-6">
              {/* Search */}
              <div>
                <label className="block text-sm font-semibold text-gray-900 mb-2">
                  Search Products
                </label>
                <input
                  type="text"
                  placeholder="Search..."
                  value={searchQuery}
                  onChange={(e) => {
                    setSearchQuery(e.target.value)
                    setCurrentPage(1)
                  }}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>

              {/* Category Filter */}
              <div>
                <label className="block text-sm font-semibold text-gray-900 mb-3">
                  Category
                </label>
                <div className="space-y-2">
                  {categories.map((cat) => (
                    <label key={cat} className="flex items-center gap-3 cursor-pointer">
                      <input
                        type="radio"
                        name="category"
                        value={cat}
                        checked={selectedCategory === cat}
                        onChange={(e) => {
                          setSelectedCategory(e.target.value)
                          setCurrentPage(1)
                        }}
                        className="w-4 h-4 text-blue-600"
                      />
                      <span className="text-sm text-gray-700 capitalize">{cat}</span>
                    </label>
                  ))}
                </div>
              </div>

              {/* Price Range Filter */}
              <div>
                <label className="block text-sm font-semibold text-gray-900 mb-3">
                  Price Range
                </label>
                <div className="space-y-3">
                  <div>
                    <label className="text-xs text-gray-600 mb-1 block">Min Price</label>
                    <input
                      type="number"
                      min="0"
                      max="1000"
                      value={priceRange[0]}
                      onChange={(e) => {
                        setPriceRange([Number(e.target.value), priceRange[1]])
                        setCurrentPage(1)
                      }}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    />
                  </div>
                  <div>
                    <label className="text-xs text-gray-600 mb-1 block">Max Price</label>
                    <input
                      type="number"
                      min="0"
                      max="1000"
                      value={priceRange[1]}
                      onChange={(e) => {
                        setPriceRange([priceRange[0], Number(e.target.value)])
                        setCurrentPage(1)
                      }}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
                    />
                  </div>
                  <div className="text-xs text-gray-600">
                    ${priceRange[0]} - ${priceRange[1]}
                  </div>
                </div>
              </div>

              {/* Reset Button */}
              <button
                onClick={handleResetFilters}
                className="w-full px-4 py-2 bg-gray-200 text-gray-900 rounded-lg hover:bg-gray-300 transition-colors text-sm font-medium"
              >
                Reset Filters
              </button>
            </div>
          </aside>

          {/* Main Content - Products Grid */}
          <main className="lg:col-span-3">
            {/* Results Info */}
            <div className="mb-6">
              <p className="text-sm text-gray-600">
                Showing {data?.content?.length || 0} of {totalProducts} products
              </p>
            </div>

            {/* Error State */}
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-lg p-4 flex items-start gap-3 mb-6">
                <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
                <div>
                  <h3 className="font-semibold text-red-900">Error loading products</h3>
                  <p className="text-sm text-red-700 mt-1">
                    {error instanceof Error ? error.message : 'Failed to fetch products'}
                  </p>
                </div>
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
              <div className="text-center py-12">
                <div className="text-gray-400 text-5xl mb-4">📦</div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">No products found</h3>
                <p className="text-gray-600 mb-6">
                  Try adjusting your filters or search query
                </p>
                <button
                  onClick={handleResetFilters}
                  className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                >
                  Clear Filters
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
              <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={handlePageChange}
                disabled={isLoading}
              />
            )}
          </main>
        </div>
      </div>
    </div>
  )
}

export default ProductListPage
