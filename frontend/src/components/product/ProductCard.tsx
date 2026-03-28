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
      className="group cursor-pointer bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-200 h-full flex flex-col"
    >
      {/* Image Container */}
      <div className="relative overflow-hidden bg-gray-100 aspect-square">
        <img
          src={product.imageUrl || 'https://via.placeholder.com/400'}
          alt={product.name}
          className="w-full h-full object-contain bg-white group-hover:scale-105 transition-transform duration-200"
          loading="lazy"
        />

        {/* Discount Badge */}
        {discountPercent > 0 && (
          <div className="absolute top-2 right-2 bg-red-500 text-white px-2 py-1 rounded text-sm font-semibold">
            -{discountPercent}%
          </div>
        )}

        {/* Stock Status Badge */}
        {isOutOfStock && (
          <div className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center">
            <span className="text-white font-semibold text-lg">Out of Stock</span>
          </div>
        )}
        {isLowStock && !isOutOfStock && (
          <div className="absolute bottom-2 left-2 bg-yellow-500 text-white px-2 py-1 rounded text-xs font-semibold">
            Only {product.stock} left
          </div>
        )}
      </div>

      {/* Content */}
      <div className="p-4 space-y-3 flex flex-col flex-grow">
        {/* Category */}
        <p className="text-xs text-gray-500 uppercase tracking-wide">{product.category}</p>

        {/* Product Name - Truncate to 2 lines */}
        <h3 className="text-base font-semibold text-gray-900 line-clamp-2 hover:text-blue-600 transition-colors min-h-[2.5rem]">
          {product.name}
        </h3>

        {/* Rating (if available) */}
        {product.rating && (
          <div className="flex items-center gap-1">
            <span className="text-yellow-400">★</span>
            <span className="text-sm text-gray-600">{product.rating.toFixed(1)}</span>
            <span className="text-xs text-gray-500">({product.reviews || 0})</span>
          </div>
        )}

        {/* Price - Flex grow to push button to bottom */}
        <div className="space-y-1 flex-grow">
          <div className="flex items-center gap-2">
            <span className="text-lg font-bold text-gray-900">
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
          className="w-full"
          variant={isOutOfStock ? 'secondary' : 'primary'}
        >
          {isOutOfStock ? 'Out of Stock' : 'Add to Cart'}
        </Button>
      </div>
    </div>
  )
}

export default ProductCard
