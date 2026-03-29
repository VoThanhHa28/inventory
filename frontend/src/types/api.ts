// Common API Response Types
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp?: string;
}

// Pagination
export interface Page<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  isLast: boolean;
  isEmpty: boolean;
}

// Error Response
export interface ApiError {
  code: number;
  message: string;
  details?: Record<string, string[]>;
  timestamp?: string;
}

// Dashboard Types
export interface DashboardStats {
  totalProducts: number;
  lowStockCount: number;
  totalSales: number;
  efficiencyPercentage: number;
}

export interface ActivityItem {
  productId: number;
  productName: string;
  action: 'CREATE' | 'UPDATE' | 'DELETE';
  changedBy: string;
  changedAt: string; // ISO datetime
}

