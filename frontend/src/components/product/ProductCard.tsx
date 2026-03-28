import React from 'react'
import { useNavigate } from 'react-router-dom'
import { Product } from '@/types/product'
import { Button } from '@/components/ui'
import { useCart } from '@/hooks'

interface ProductCardProps {
  product: Product
}

/**
 * ProductCard Component
 * Displays product information with add to cart button
 * Responsive: 1 col (mobile) → 2 col (tablet) → 4 col (desktop)
 */
const ProductCard: React.FC<ProductCardProps> = ({ product }) => {
  const navigate = useNavigate()
  const { addItem } = useCart()

  const handleAddToCart = (e: React.MouseEvent) => {
    e.stopPropagation()
    addItem({
      id: product.id,
      name: product.name,
      price: product.price,
      image: product.imageUrl,
      quantity: 1,
    })
  }

  const handleViewDetails = () => {
    navigate(`/shop/products/${product.id}`)
  }

  // Calculate discount percentage if applicable
  const discountPercent = product.originalPrice
    ? Math.round(((product.originalPrice - product.price) / product.originalPrice) * 100)
    : 0

  // Determine stock status
  const isOutOfStock = product.stock === 0
  const isLowStock = product.stock > 0 && product.stock <= 5

  return (
    <div
      onClick={handleViewDetails}
      className="group cursor-pointer bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden hover:shadow-xl hover:border-blue-300 transition-all duration-300 h-full flex flex-col transform hover:scale-102"
    >
      {/* Image Container */}
      <div className="relative overflow-hidden bg-gray-100 aspect-square">
        <img
          src={product.imageUrl || 'https://via.placeholder.com/400'}
          alt={product.name}
          className="w-full h-full object-contain bg-white group-hover:scale-110 transition-transform duration-300"
          loading="lazy"
        />

        {/* Discount Badge */}
        {discountPercent > 0 && (
          <div className="absolute top-3 right-3 bg-red-500 text-white px-3 py-1.5 rounded-full text-xs font-bold shadow-lg">
            Save {discountPercent}%
          </div>
        )}

        {/* Stock Status Badge */}
        {isOutOfStock && (
          <div className="absolute inset-0 bg-black bg-opacity-60 flex items-center justify-center backdrop-blur-sm">
            <div className="text-center">
              <div className="text-3xl mb-2">❌</div>
              <span className="text-white font-bold">Out of Stock</span>
            </div>
          </div>
        )}
        {isLowStock && !isOutOfStock && (
          <div className="absolute bottom-3 left-3 bg-amber-500 text-white px-3 py-1.5 rounded-full text-xs font-bold shadow-lg flex items-center gap-1">
            <span>⏰</span> Only {product.stock} left
          </div>
        )}
      </div>

      {/* Content */}
      <div className="p-5 space-y-4 flex flex-col flex-grow">
        {/* Category Badge */}
        <div className="inline-flex items-center gap-1 w-fit">
          <span className="px-2.5 py-1 bg-blue-100 text-blue-700 rounded-full text-xs font-semibold">
            {product.category || 'Product'}
          </span>
        </div>

        {/* Product Name - Truncate to 2 lines */}
        <h3 className="text-sm font-bold text-gray-900 line-clamp-2 hover:text-blue-600 transition-colors min-h-[2.5rem] leading-snug">
          {product.name}
        </h3>

        {/* Rating (if available) */}
        {product.rating && (
          <div className="flex items-center gap-2">
            <div className="flex">
              <span className="text-yellow-400 text-sm">★</span>
              <span className="text-yellow-400 text-sm">★</span>
              <span className="text-yellow-400 text-sm">★</span>
              <span className="text-yellow-400 text-sm">★</span>
              <span className="text-gray-300 text-sm">★</span>
            </div>
            <span className="text-xs font-semibold text-gray-700">{product.rating.toFixed(1)}</span>
            <span className="text-xs text-gray-500">({product.reviews || 0})</span>
          </div>
        )}

        {/* Price - Flex grow to push button to bottom */}
        <div className="space-y-2 flex-grow">
          <div className="flex items-baseline gap-2">
            <span className="text-2xl font-bold text-gray-900">
              ${product.price.toFixed(2)}
            </span>
            {product.originalPrice && product.originalPrice > product.price && (
              <span className="text-sm text-gray-500 line-through">
                ${product.originalPrice.toFixed(2)}
              </span>
            )}
          </div>
        </div>

        {/* Add to Cart Button */}
        <Button
          onClick={handleAddToCart}
          disabled={isOutOfStock}
          className="w-full mt-4 font-semibold py-2.5 rounded-lg transition-all transform hover:scale-105"
          variant={isOutOfStock ? 'secondary' : 'primary'}
        >
          {isOutOfStock ? '❌ Out of Stock' : '🛒 Add to Cart'}
        </Button>
      </div>
    </div>
  )
}

export default ProductCard
