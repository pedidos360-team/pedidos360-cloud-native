export type AsyncStatus = 'idle' | 'loading' | 'success' | 'error';

export type ProductAvailability = 'AVAILABLE' | 'OUT_OF_STOCK';
export type AvailabilityFilter = 'TODOS' | 'DISPONIBLES' | 'SIN_STOCK';
export type OrderFilter = 'TODOS' | 'POR_PREPARAR' | 'LISTOS';
export type OrderStatus =
  | 'NUEVO'
  | 'EN_PREPARACION'
  | 'LISTO'
  | 'ENTREGADO'
  | 'CANCELADO'
  | 'PENDIENTE'
  | 'COMPLETADO'
  | string;

export interface Product {
  id: number;
  nombre: string;
  precio: number;
  stock: number;
  sku?: string;
  imageUrl?: string;
  imageAlt?: string;
}

export interface Order {
  id: number;
  cliente: string;
  total: number;
  estado: OrderStatus;
  fecha?: string;
  fulfillment?: 'RECOGIDA' | 'ENTREGA';
  requestedTime?: string;
  items?: OrderItem[];
}

export interface OrderItem {
  product: string;
  quantity: number;
  unitPrice: number;
  lineTotal?: number;
}

export interface ApiState<T> {
  data: T;
  status: AsyncStatus;
  errorMessage: string | null;
}
