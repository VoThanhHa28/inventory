import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'

/**
 * CartPage - Shopping Cart for customers
 * Features: View cart items, quantity control, checkout
 */
const CartPage: React.FC = () => {
  const navigate = useNavigate()
  const [cartItems, setCartItems] = useState([
    {
      id: 1,
      name: 'Rogue Runner X1',
      sku: 'E0001',
      price: 149.0,
      quantity: 2,
      image: 'https://via.placeholder.com/80/0053db/ffffff?text=E0001',
    },
    {
      id: 2,
      name: 'Titanium Chrono',
      sku: 'L0002',
      price: 320.0,
      quantity: 1,
      image: 'https://via.placeholder.com/80/0053db/ffffff?text=L0002',
    },
  ])

  const calculateSubtotal = (price: number, quantity: number) => (price * quantity).toFixed(2)

  const calculateTotal = () => {
    return cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0).toFixed(2)
  }

  const handleQuantityChange = (id: number, newQuantity: number) => {
    if (newQuantity <= 0) {
      handleRemoveItem(id)
      return
    }
    setCartItems(cartItems.map((item) => (item.id === id ? { ...item, quantity: newQuantity } : item)))
  }

  const handleRemoveItem = (id: number) => {
    setCartItems(cartItems.filter((item) => item.id !== id))
  }

  const handleClearCart = () => {
    setCartItems([])
  }

  return (
    <div className="min-h-screen bg-surface">
      {/* Page Header */}
      <div className="bg-gradient-to-r from-primary/10 to-primary/5 border-b border-surface-container py-8 md:py-12">
        <div className="max-w-6xl mx-auto px-4 md:px-6 lg:px-8">
          <h1 className="font-headline text-4xl md:text-5xl font-bold text-on-surface mb-2">Your Cart</h1>
          <p className="text-on-surface-variant font-body">
            {cartItems.length === 0 ? 'Your cart is empty' : `You have ${cartItems.length} item${cartItems.length !== 1 ? 's' : ''} in your cart`}
          </p>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-4 md:px-6 lg:px-8 py-12">
        {cartItems.length === 0 ? (
          // Empty Cart State
          <div className="text-center py-20">
            <div className="mb-6">
              <span className="text-6xl">🛒</span>
            </div>
            <h2 className="font-headline text-3xl font-bold text-on-surface mb-3">Your cart is empty</h2>
            <p className="text-on-surface-variant text-lg mb-8 max-w-md mx-auto">
              Start shopping to add items to your cart. Discover our premium collection of products.
            </p>
            <button
              onClick={() => navigate('/shop/products')}
              className="px-8 py-4 bg-primary text-white font-bold rounded-xl hover:bg-blue-700 transition-all shadow-lg shadow-primary/30 active:scale-95"
            >
              Continue Shopping
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Cart Items */}
            <div className="lg:col-span-2 space-y-4">
              {cartItems.map((item) => (
                <div
                  key={item.id}
                  className="bg-surface-container-lowest border border-outline-variant/20 rounded-2xl p-6 shadow-sm hover:shadow-md transition-shadow"
                >
                  <div className="flex gap-6">
                    {/* Product Image */}
                    <div className="h-24 w-24 flex-shrink-0 rounded-xl overflow-hidden bg-surface-container">
                      <img alt={item.name} className="w-full h-full object-cover" src={item.image} />
                    </div>

                    {/* Product Details */}
                    <div className="flex-1">
                      <h3 className="font-headline font-bold text-lg text-on-surface mb-1">{item.name}</h3>
                      <p className="text-sm text-on-surface-variant mb-3">
                        SKU: <span className="font-mono font-semibold text-on-surface">{item.sku}</span>
                      </p>
                      <div className="flex items-center justify-between">
                        <div className="text-2xl font-bold text-primary">${item.price.toFixed(2)}</div>
                      </div>
                    </div>

                    {/* Quantity & Remove */}
                    <div className="flex flex-col items-end justify-between">
                      <button
                        onClick={() => handleRemoveItem(item.id)}
                        className="p-2 text-on-surface-variant hover:text-error hover:bg-error/10 rounded-lg transition-all"
                      >
                        <span className="material-symbols-outlined">close</span>
                      </button>

                      {/* Quantity Selector */}
                      <div className="flex items-center gap-3 bg-surface-container-low rounded-xl p-2">
                        <button
                          onClick={() => handleQuantityChange(item.id, item.quantity - 1)}
                          className="p-1.5 hover:bg-surface-container rounded transition-colors"
                        >
                          <span className="material-symbols-outlined text-lg">remove</span>
                        </button>
                        <span className="font-semibold text-on-surface w-8 text-center">{item.quantity}</span>
                        <button
                          onClick={() => handleQuantityChange(item.id, item.quantity + 1)}
                          className="p-1.5 hover:bg-surface-container rounded transition-colors"
                        >
                          <span className="material-symbols-outlined text-lg">add</span>
                        </button>
                      </div>
                    </div>
                  </div>

                  {/* Subtotal */}
                  <div className="mt-4 pt-4 border-t border-outline-variant/10 flex justify-end">
                    <div className="text-right">
                      <p className="text-sm text-on-surface-variant mb-1">Subtotal</p>
                      <p className="text-xl font-bold text-on-surface">
                        ${calculateSubtotal(item.price, item.quantity)}
                      </p>
                    </div>
                  </div>
                </div>
              ))}

              {/* Continue Shopping */}
              <button
                onClick={() => navigate('/shop/products')}
                className="w-full py-3 px-6 text-primary font-semibold hover:bg-primary/10 rounded-xl transition-colors flex items-center justify-center gap-2"
              >
                <span className="material-symbols-outlined">arrow_back</span>
                Continue Shopping
              </button>
            </div>

            {/* Order Summary Sidebar */}
            <div className="lg:col-span-1">
              <div className="bg-surface-container-lowest border border-outline-variant/20 rounded-2xl p-8 shadow-sm sticky top-24">
                <h3 className="font-headline text-xl font-bold text-on-surface mb-6">Order Summary</h3>

                {/* Summary Items */}
                <div className="space-y-4 mb-6 pb-6 border-b border-outline-variant/10">
                  <div className="flex justify-between items-center">
                    <span className="text-on-surface-variant">Subtotal ({cartItems.length} items)</span>
                    <span className="font-semibold text-on-surface">${calculateTotal()}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-on-surface-variant">Shipping</span>
                    <span className="font-semibold text-on-surface">$0.00</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-on-surface-variant">Tax</span>
                    <span className="font-semibold text-on-surface">$0.00</span>
                  </div>
                </div>

                {/* Total */}
                <div className="mb-8 pb-8 border-b border-outline-variant/10">
                  <div className="flex justify-between items-center">
                    <span className="font-headline font-bold text-lg text-on-surface">Total</span>
                    <span className="font-headline text-3xl font-bold text-primary">${calculateTotal()}</span>
                  </div>
                </div>

                {/* Checkout Button */}
                <button className="w-full py-4 px-6 bg-gradient-to-r from-primary to-blue-700 text-white font-bold rounded-xl hover:shadow-xl hover:shadow-primary/30 transition-all active:scale-95 mb-3 flex items-center justify-center gap-2">
                  <span className="material-symbols-outlined">shopping_bag</span>
                  Proceed to Checkout
                </button>

                {/* Clear Cart */}
                <button
                  onClick={handleClearCart}
                  className="w-full py-3 px-6 text-error font-semibold hover:bg-error/10 rounded-xl transition-colors"
                >
                  Clear Cart
                </button>

                {/* Trust Badges */}
                <div className="mt-8 pt-8 border-t border-outline-variant/10 space-y-3">
                  <div className="flex items-center gap-2 text-sm text-on-surface-variant">
                    <span className="material-symbols-outlined text-lg text-primary">check_circle</span>
                    <span>Secure checkout</span>
                  </div>
                  <div className="flex items-center gap-2 text-sm text-on-surface-variant">
                    <span className="material-symbols-outlined text-lg text-primary">local_shipping</span>
                    <span>Free shipping over $50</span>
                  </div>
                  <div className="flex items-center gap-2 text-sm text-on-surface-variant">
                    <span className="material-symbols-outlined text-lg text-primary">assignment_return</span>
                    <span>Easy returns (30 days)</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}

export default CartPage
