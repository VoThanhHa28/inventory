import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'

/**
 * CartPage - Shopping Cart for customers
 * Features: View cart items, quantity control, checkout with standard layout
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
    <div className="bg-surface font-body text-on-surface antialiased">
      {/* Header */}
      <header className="fixed top-0 w-full z-50 bg-white/70 dark:bg-slate-900/70 backdrop-blur-xl shadow-sm shadow-blue-500/5">
        <div className="flex justify-between items-center h-16 px-6 lg:px-12 max-w-full mx-auto">
          <div className="flex items-center gap-2">
            <span className="material-symbols-outlined text-blue-600 dark:text-blue-400">shopping_cart</span>
            <h1 className="text-xl font-bold tracking-tighter text-slate-900 dark:text-white font-headline">InventoryCore</h1>
          </div>
          <nav className="hidden md:flex items-center gap-8">
            <a className="text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 transition-colors py-5 font-headline text-sm tracking-wide" href="/shop/products">
              Products
            </a>
            <a className="text-blue-600 dark:text-blue-400 font-semibold border-b-2 border-blue-600 py-5 font-headline text-sm tracking-wide" href="/shop/cart">
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
              Shopping
            </p>
          </div>
          <nav className="space-y-1">
            <a
              className="flex items-center gap-4 py-3 px-6 text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-900 rounded-r-full hover:translate-x-1 transition-transform duration-200"
              href="/shop/products"
            >
              <span className="material-symbols-outlined">grid_view</span>
              <span className="font-headline text-xs tracking-wide uppercase">Products</span>
            </a>
            <a
              className="flex items-center gap-4 py-3 px-6 bg-blue-50 dark:bg-blue-900/20 text-blue-700 dark:text-blue-300 rounded-r-full font-bold hover:translate-x-1 transition-transform duration-200"
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

          {/* Order Info */}
          <div className="mt-12 px-6 space-y-6">
            <div>
              <h3 className="font-headline text-xs font-bold uppercase tracking-widest text-on-surface-variant mb-4">
                Order Summary
              </h3>
              <div className="space-y-3 text-sm">
                <div className="flex justify-between">
                  <span className="text-on-surface-variant">Subtotal</span>
                  <span className="font-semibold text-on-surface">${calculateTotal()}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-on-surface-variant">Shipping</span>
                  <span className="font-semibold text-on-surface">$0.00</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-on-surface-variant">Tax</span>
                  <span className="font-semibold text-on-surface">$0.00</span>
                </div>
                <div className="pt-3 border-t border-outline-variant/20">
                  <div className="flex justify-between font-bold">
                    <span className="text-on-surface">Total</span>
                    <span className="text-primary">${calculateTotal()}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </aside>

        {/* Main Content */}
        <main className="flex-1 lg:ml-64 p-6 lg:p-12 bg-surface">
          {/* Header Section */}
          <div className="flex flex-col md:flex-row md:items-end justify-between gap-6 mb-12">
            <div>
              <h2 className="text-4xl font-headline font-extrabold tracking-tight text-on-surface">Shopping Cart</h2>
              <p className="text-on-surface-variant mt-2 font-body italic">
                {cartItems.length === 0 ? 'Your cart is empty' : `You have ${cartItems.length} item${cartItems.length !== 1 ? 's' : ''}`}
              </p>
            </div>
          </div>

          {cartItems.length === 0 ? (
            // Empty Cart State
            <div className="text-center py-20">
              <div className="text-6xl mb-4">🛒</div>
              <p className="text-on-surface-variant font-medium text-lg">Your cart is empty</p>
              <p className="text-on-surface-variant text-sm mt-2">Start shopping to add items to your cart</p>
              <button
                onClick={() => navigate('/shop/products')}
                className="mt-8 px-8 py-3 signature-gradient text-white font-bold rounded-xl hover:opacity-90 active:scale-95 transition-all shadow-lg shadow-primary/20 inline-flex items-center gap-2"
              >
                <span className="material-symbols-outlined">arrow_back</span>
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
                    className="bg-surface-container-lowest rounded-[1.5rem] p-6 border border-outline-variant/20 hover:shadow-lg transition-shadow"
                  >
                    <div className="flex gap-6">
                      {/* Product Image */}
                      <div className="h-24 w-24 flex-shrink-0 rounded-xl overflow-hidden bg-surface-container">
                        <img alt={item.name} className="w-full h-full object-cover" loading="lazy" src={item.image} />
                      </div>

                      {/* Product Details */}
                      <div className="flex-1 min-w-0">
                        <h3 className="font-headline font-bold text-lg text-on-surface mb-1">{item.name}</h3>
                        <p className="text-sm text-on-surface-variant mb-3">
                          SKU: <span className="font-mono font-semibold text-on-surface">{item.sku}</span>
                        </p>
                        <div className="flex items-center justify-between">
                          <div className="text-xl font-bold text-on-primary-fixed">${item.price.toFixed(2)}</div>
                          <div className="flex items-center gap-3 bg-surface-container-low rounded-xl p-1">
                            <button
                              onClick={() => handleQuantityChange(item.id, item.quantity - 1)}
                              className="p-1.5 hover:bg-surface-container rounded transition-colors"
                            >
                              <span className="material-symbols-outlined text-lg">remove</span>
                            </button>
                            <span className="font-semibold text-on-surface w-6 text-center">{item.quantity}</span>
                            <button
                              onClick={() => handleQuantityChange(item.id, item.quantity + 1)}
                              className="p-1.5 hover:bg-surface-container rounded transition-colors"
                            >
                              <span className="material-symbols-outlined text-lg">add</span>
                            </button>
                          </div>
                        </div>
                      </div>

                      {/* Remove Button */}
                      <button
                        onClick={() => handleRemoveItem(item.id)}
                        className="flex-shrink-0 p-2 text-on-surface-variant hover:text-error hover:bg-error/10 rounded-lg transition-all"
                      >
                        <span className="material-symbols-outlined">close</span>
                      </button>
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
                  className="w-full py-3 px-6 text-primary font-semibold hover:bg-primary/10 rounded-xl transition-colors flex items-center justify-center gap-2 mt-6"
                >
                  <span className="material-symbols-outlined">arrow_back</span>
                  Continue Shopping
                </button>
              </div>

              {/* Order Summary Sidebar */}
              <div className="lg:col-span-1">
                <div className="bg-surface-container-lowest border border-outline-variant/20 rounded-[1.5rem] p-8 shadow-sm sticky top-24">
                  <h3 className="font-headline text-xl font-bold text-on-surface mb-6">Order Summary</h3>

                  {/* Summary */}
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
                      <span className="font-headline text-3xl font-bold text-on-primary-fixed">${calculateTotal()}</span>
                    </div>
                  </div>

                  {/* Checkout Button */}
                  <button className="w-full py-4 px-6 bg-gradient-to-r from-blue-600 to-blue-700 text-white font-bold rounded-xl hover:from-blue-700 hover:to-blue-800 active:scale-95 transition-all shadow-lg shadow-blue-600/30 mb-3 flex items-center justify-center gap-2">
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
        </main>
      </div>

      {/* Mobile Bottom Nav */}
      <div className="md:hidden fixed bottom-0 left-0 w-full bg-white/90 backdrop-blur-xl border-t border-outline-variant/20 px-4 h-20 flex items-center justify-around z-50">
        <a className="flex flex-col items-center gap-1 text-outline" href="/shop/products">
          <span className="material-symbols-outlined">grid_view</span>
          <span className="text-[10px] font-bold uppercase tracking-widest font-headline">Products</span>
        </a>
        <a className="flex flex-col items-center gap-1 text-primary" href="/shop/cart" style={{ fontVariationSettings: "'FILL' 1" }}>
          <span className="material-symbols-outlined">shopping_cart</span>
          <span className="text-[10px] font-bold uppercase tracking-widest font-headline">Cart</span>
        </a>
        <a className="flex flex-col items-center gap-1 text-outline" href="/shop/order-history">
          <span className="material-symbols-outlined">package_2</span>
          <span className="text-[10px] font-bold uppercase tracking-widest font-headline">Orders</span>
        </a>
        <a className="flex flex-col items-center gap-1 text-outline" href="#">
          <span className="material-symbols-outlined">settings</span>
          <span className="text-[10px] font-bold uppercase tracking-widest font-headline">Account</span>
        </a>
      </div>
    </div>
  )
}

export default CartPage
