import { useQuery } from '@tanstack/react-query'
import apiClient from './client'
import { ApiResponse, DashboardStats, ActivityItem } from '@types'

/**
 * Dashboard API Service
 * Provides access to dashboard statistics and activity logs
 * Uses React Query for caching with 30-second staleTime (balances freshness vs performance)
 */

const DASHBOARD_CACHE_TIME = 30000 // 30 seconds - provides fresh data without excessive API calls

/**
 * Dashboard API endpoints and hooks
 */
export const dashboardApi = {
  /**
   * Fetch dashboard statistics
   * Returns aggregated KPIs: total products, low stock count, total sales, efficiency %
   */
  getStats: async (): Promise<DashboardStats> => {
    const response = await apiClient.get<ApiResponse<DashboardStats>>('/dashboard/stats')
    return response.data.data
  },

  /**
   * Fetch recent activity logs
   * Returns list of recent product changes (create, update, delete)
   */
  getRecentActivity: async (limit: number = 10): Promise<ActivityItem[]> => {
    const response = await apiClient.get<ApiResponse<ActivityItem[]>>('/dashboard/recent-activity', {
      params: { limit },
    })
    return response.data.data
  },
}

/**
 * React Query hooks for dashboard data
 */
export const useDashboardStats = () => {
  return useQuery({
    queryKey: ['dashboard', 'stats'],
    queryFn: () => dashboardApi.getStats(),
    staleTime: DASHBOARD_CACHE_TIME,
    gcTime: 60000, // 1 minute cache in memory
    retry: 2,
    refetchOnWindowFocus: false,
  })
}

export const useRecentActivity = (limit: number = 10) => {
  return useQuery({
    queryKey: ['dashboard', 'recent-activity', limit],
    queryFn: () => dashboardApi.getRecentActivity(limit),
    staleTime: DASHBOARD_CACHE_TIME,
    gcTime: 60000, // 1 minute cache in memory
    retry: 2,
    refetchOnWindowFocus: false,
  })
}
