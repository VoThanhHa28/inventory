import axios, { AxiosInstance, AxiosError } from 'axios';
import { useAuthStore } from '@stores/authStore';
import { ApiError, ApiResponse } from '@types/api';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
const API_TIMEOUT = import.meta.env.VITE_API_TIMEOUT || 30000;

/**
 * Axios instance with JWT interceptor
 * Automatically adds Authorization header from Zustand auth store
 */
export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: Number(API_TIMEOUT),
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Request interceptor: Add JWT token from store
 */
apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/**
 * Response interceptor: Handle errors and token expiry
 */
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      useAuthStore.getState().logout();
      window.location.href = '/auth/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
