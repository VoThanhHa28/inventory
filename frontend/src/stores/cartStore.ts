import { create } from 'zustand';
import { Product } from '@types';

export interface CartItem {
  product: Product;
  quantity: number;
}

interface CartState {
  // State
  items: CartItem[];

  // Actions
  addItem: (product: Product, quantity: number) => void;
  removeItem: (productId: number) => void;
  updateQuantity: (productId: number, quantity: number) => void;
  clearCart: () => void;

  // Computed
  getTotal: () => number;
  getLineTotal: (productId: number) => number;
  getItemCount: () => number;
  isEmpty: () => boolean;
}

/**
 * Cart Store (Zustand)
 * Manages shopping cart state (in-memory, lost on page refresh)
 * Cart data is NOT persisted to localStorage or backend
 * Users must complete checkout before losing data
 */
export const useCartStore = create<CartState>((set, get) => ({
  items: [],

  addItem: (product, quantity) => {
    const { items } = get();
    const existingItem = items.find((item) => item.product.id === product.id);

    if (existingItem) {
      existingItem.quantity += quantity;
      set({ items: [...items] });
    } else {
      set({ items: [...items, { product, quantity }] });
    }
  },

  removeItem: (productId) => {
    set({ items: get().items.filter((item) => item.product.id !== productId) });
  },

  updateQuantity: (productId, quantity) => {
    if (quantity <= 0) {
      get().removeItem(productId);
      return;
    }

    const items = get().items.map((item) =>
      item.product.id === productId ? { ...item, quantity } : item
    );
    set({ items });
  },

  clearCart: () => set({ items: [] }),

  getTotal: () => {
    return get().items.reduce((total, item) => total + item.product.price * item.quantity, 0);
  },

  getLineTotal: (productId) => {
    const item = get().items.find((item) => item.product.id === productId);
    return item ? item.product.price * item.quantity : 0;
  },

  getItemCount: () => {
    return get().items.reduce((count, item) => count + item.quantity, 0);
  },

  isEmpty: () => get().items.length === 0,
}));
