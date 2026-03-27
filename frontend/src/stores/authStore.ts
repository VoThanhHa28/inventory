import { create } from 'zustand';
import { User, Role } from '@types';

interface AuthState {
  // State
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;

  // Actions
  setAuth: (user: User, token: string) => void;
  logout: () => void;
  setLoading: (loading: boolean) => void;
  
  // Helpers
  isAdmin: () => boolean;
  isUser: () => boolean;
}

/**
 * Auth Store (Zustand)
 * Manages user authentication state
 * Token is stored in-memory (lost on page refresh)
 * For persistence, use localStorage with careful security consideration
 */
export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  token: null,
  isAuthenticated: false,
  isLoading: false,

  setAuth: (user, token) => {
    set({ user, token, isAuthenticated: true });
    // Optional: persist token to localStorage
    localStorage.setItem('auth_token', token);
  },

  logout: () => {
    set({ user: null, token: null, isAuthenticated: false });
    // Clear from localStorage
    localStorage.removeItem('auth_token');
  },

  setLoading: (loading) => set({ isLoading: loading }),

  isAdmin: () => get().user?.role === Role.ADMIN,
  isUser: () => get().user?.role === Role.USER,
}));

/**
 * Restore user session from localStorage (if needed)
 * Call this once on app startup
 */
export const restoreAuthSession = () => {
  const token = localStorage.getItem('auth_token');
  if (token) {
    useAuthStore.setState({ token, isAuthenticated: true });
    // In production, validate token with backend
  }
};
