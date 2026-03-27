/**
 * Application-wide constants
 */

export const APP_NAME = import.meta.env.VITE_APP_NAME || 'E-Commerce Inventory';
export const APP_VERSION = import.meta.env.VITE_APP_VERSION || '1.0.0';

// API
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
export const API_TIMEOUT = import.meta.env.VITE_API_TIMEOUT || 30000;

// Cache times (milliseconds)
export const CACHE_PRODUCT_LIST = 10000; // 10s - match Redis
export const CACHE_MY_ORDERS = 60000; // 60s - match Redis
export const CACHE_GENERIC = 300000; // 5 minutes

// Pagination
export const DEFAULT_PAGE_SIZE = 10;
export const PAGE_SIZES = [10, 20, 50];

// Price formatting
export const CURRENCY = '$';
export const DECIMAL_PLACES = 2;

// Routes
export const ROUTES = {
  HOME: '/',
  LOGIN: '/auth/login',
  REGISTER: '/auth/register',
  PRODUCTS: '/shop/products',
  PRODUCT_DETAIL: '/shop/products/:id',
  CART: '/shop/cart',
  CHECKOUT: '/shop/checkout',
  ORDER_CONFIRMATION: '/shop/order-confirmation/:id',
  ORDER_HISTORY: '/shop/order-history',
  ORDER_DETAIL: '/shop/orders/:id',
  ADMIN_DASHBOARD: '/admin/dashboard',
  ADMIN_PRODUCTS: '/admin/products',
  ADMIN_PRODUCT_CREATE: '/admin/products/create',
  ADMIN_PRODUCT_EDIT: '/admin/products/:id/edit',
  ADMIN_INVENTORY: '/admin/inventory',
  ADMIN_ORDERS: '/admin/orders',
  ADMIN_ORDER_DETAIL: '/admin/orders/:id',
};

// HTTP Status codes
export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  SERVER_ERROR: 500,
};
