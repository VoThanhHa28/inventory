import { useQuery } from '@tanstack/react-query';
import apiClient from './client';
import { Page, Product, ApiResponse } from '@types';

/**
 * Products API Service
 * React Query is used for caching (matches backend 10s Redis cache)
 */

const PRODUCTS_CACHE_TIME = 10000; // 10 seconds - match Redis TTL

/**
 * Fetch products list with pagination
 * Cached for 10 seconds to match backend Redis cache
 */
export const useProducts = (page: number = 0, size: number = 10) => {
  return useQuery({
    queryKey: ['products', page, size],
    queryFn: async () => {
      const response = await apiClient.get<ApiResponse<Page<Product>>>('/products', {
        params: { page, size },
      });
      return response.data.data;
    },
    staleTime: PRODUCTS_CACHE_TIME,
    gcTime: PRODUCTS_CACHE_TIME * 3, // Cache for 30 seconds
  });
};

/**
 * Fetch product detail
 * NOT cached - always fetch fresh from backend
 */
export const useProductDetail = (productId: number) => {
  return useQuery({
    queryKey: ['product', productId],
    queryFn: async () => {
      const response = await apiClient.get<ApiResponse<Product>>(`/products/${productId}`);
      return response.data.data;
    },
    enabled: !!productId,
    staleTime: 0, // Always fresh
  });
};

/**
 * Create product (admin only)
 */
export const createProduct = async (data: any) => {
  const response = await apiClient.post<ApiResponse<Product>>('/products', data);
  return response.data.data;
};

/**
 * Update product (admin only)
 */
export const updateProduct = async (id: number, data: any) => {
  const response = await apiClient.put<ApiResponse<Product>>(`/products/${id}`, data);
  return response.data.data;
};

/**
 * Delete product (admin only)
 */
export const deleteProduct = async (id: number) => {
  const response = await apiClient.delete<ApiResponse<void>>(`/products/${id}`);
  return response.data;
};

export default {
  useProducts,
  useProductDetail,
  createProduct,
  updateProduct,
  deleteProduct,
};
