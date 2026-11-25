import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Product } from './models';

export interface CreateProductPayload {
  nombre: string;
  precio: number;
  stock: number;
  imageUrl?: string;
}

@Injectable({ providedIn: 'root' })
export class CatalogService {
  private readonly http = inject(HttpClient);

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${environment.apiBaseUrl}/api/productos`);
  }

  createProduct(payload: CreateProductPayload): Observable<Product> {
    return this.http.post<Product>(`${environment.apiBaseUrl}/api/productos`, payload);
  }

  deleteProduct(id: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiBaseUrl}/api/productos/${id}`);
  }

  updateStock(id: number, stock: number): Observable<Product> {
    return this.http.patch<Product>(`${environment.apiBaseUrl}/api/productos/${id}/stock`, { stock });
  }
}