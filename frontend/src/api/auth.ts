import apiClient from './client';
import { useAuthStore } from '@stores/authStore';
import { ApiResponse, AuthResponse, LoginRequest, RegisterRequest, User } from '@types';

/**
 * Auth API Service
 * Handles login, register, and user data fetching
 */
export const authApi = {
  /**
   * Register a new user
   */
  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/register', data);
    const { token, user } = response.data.data;
    useAuthStore.getState().setAuth(user, token);
    return response.data.data;
  },

  /**
   * Login user
   */
  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/auth/login', data);
    const { token, user } = response.data.data;
    useAuthStore.getState().setAuth(user, token);
    return response.data.data;
  },

  /**
   * Get current user profile
   */
  async getCurrentUser(): Promise<User> {
    const response = await apiClient.get<ApiResponse<User>>('/auth/me');
    return response.data.data;
  },

  /**
   * Logout (client-side only)
   */
  logout(): void {
    useAuthStore.getState().logout();
  },
};

export default authApi;
