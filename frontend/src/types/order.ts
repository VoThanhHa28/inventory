export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  SHIPPED = 'SHIPPED',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
}

export interface OrderItem {
  productId: number;
  productName: string;
  quantity: number;
  price: number;
  subtotal: number;
}

export interface Order {
  id: number;
  userId: number;
  items: OrderItem[];
  totalAmount: number;
  status: OrderStatus;
  shippingAddress?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateOrderRequest {
  items: {
    productId: number;
    quantity: number;
  }[];
  shippingAddress: string;
}

export interface UpdateOrderStatusRequest {
  status: OrderStatus;
}
