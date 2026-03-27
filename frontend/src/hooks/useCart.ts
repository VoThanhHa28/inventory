import { useCartStore } from '@stores/cartStore'

/**
 * Custom hook for cart-related logic
 * Provides easy access to cart state and functions
 */
export const useCart = () => {
  const { items, addItem, removeItem, updateQuantity, clearCart, getTotal, getItemCount, isEmpty } =
    useCartStore()

  return {
    items,
    addItem,
    removeItem,
    updateQuantity,
    clearCart,
    getTotal: getTotal(),
    getItemCount: getItemCount(),
    isEmpty: isEmpty(),
  }
}

export default useCart
