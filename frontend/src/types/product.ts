export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  image?: string;
  isDeleted: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProductRequest {
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  image?: string;
}

export interface UpdateProductRequest extends CreateProductRequest {}

export interface ProductFilter {
  search?: string;
  minPrice?: number;
  maxPrice?: number;
  inStockOnly?: boolean;
}
