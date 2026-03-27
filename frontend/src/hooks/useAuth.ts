import { useAuthStore } from '@stores/authStore'

/**
 * Custom hook for auth-related logic
 * Provides easy access to auth state and functions
 */
export const useAuth = () => {
  const { user, token, isAuthenticated, logout, isAdmin, isUser } = useAuthStore()

  return {
    user,
    token,
    isAuthenticated,
    isAdmin: isAdmin(),
    isUser: isUser(),
    logout,
  }
}

export default useAuth
