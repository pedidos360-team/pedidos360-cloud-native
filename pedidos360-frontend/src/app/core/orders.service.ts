import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Order } from './models';

export interface CreateOrderPayload {
  cliente: string;
  total: number;
  estado: string;
}

@Injectable({ providedIn: 'root' })
export class OrdersService {
  private readonly http = inject(HttpClient);

  getOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${environment.ordersApiBaseUrl}/api/pedidos`);
  }

  createOrder(payload: CreateOrderPayload): Observable<Order> {
    return this.http.post<Order>(`${environment.ordersApiBaseUrl}/api/pedidos`, payload);
  }

  updateEstado(id: number, estado: string): Observable<Order> {
    return this.http.patch<Order>(`${environment.ordersApiBaseUrl}/api/pedidos/${id}/estado`, { estado });
  }
}