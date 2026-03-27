import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from './client';
import { Page, Order, ApiResponse, CreateOrderRequest } from '@types';

/**
 * Orders API Service
 * React Query caching strategy:
 * - /my-orders: 60s cache (matches backend Redis cache)
 * - /orders/{id}: No cache (always fresh, mutable)
 */

const MY_ORDERS_CACHE_TIME = 60000; // 60 seconds - match Redis TTL

/**
 * Fetch user's orders (cached for 60s)
 * Cache key includes userId implicitly from auth context
 */
export const useMyOrders = (page: number = 0, size: number = 10) => {
  return useQuery({
    queryKey: ['myOrders', page, size],
    queryFn: async () => {
      const response = await apiClient.get<ApiResponse<Page<Order>>>('/orders/my-orders', {
        params: { page, size },
      });
      return response.data.data;
    },
    staleTime: MY_ORDERS_CACHE_TIME,
    gcTime: MY_ORDERS_CACHE_TIME * 3, // Cache for 180 seconds
  });
};

/**
 * Fetch order detail (NOT cached - always fresh)
 */
export const useOrderDetail = (orderId: number) => {
  return useQuery({
    queryKey: ['order', orderId],
    queryFn: async () => {
      const response = await apiClient.get<ApiResponse<Order>>(`/orders/${orderId}`);
      return response.data.data;
    },
    enabled: !!orderId,
    staleTime: 0, // Always fresh
  });
};

/**
 * Create order (place order)
 */
export const useCreateOrder = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: async (data: CreateOrderRequest) => {
      const response = await apiClient.post<ApiResponse<Order>>('/orders', data);
      return response.data.data;
    },
    onSuccess: () => {
      // Invalidate my orders cache to refresh
      queryClient.invalidateQueries({ queryKey: ['myOrders'] });
    },
  });
};

/**
 * Update order status (admin only)
 */
export const useUpdateOrderStatus = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: async ({ orderId, status }: { orderId: number; status: string }) => {
      const response = await apiClient.put<ApiResponse<Order>>(
        `/orders/${orderId}/status`,
        {},
        { params: { status } }
      );
      return response.data.data;
    },
    onSuccess: () => {
      // Invalidate all order-related caches
      queryClient.invalidateQueries({ queryKey: ['myOrders'] });
      queryClient.invalidateQueries({ queryKey: ['order'] });
    },
  });
};

export default {
  useMyOrders,
  useOrderDetail,
  useCreateOrder,
  useUpdateOrderStatus,
};
