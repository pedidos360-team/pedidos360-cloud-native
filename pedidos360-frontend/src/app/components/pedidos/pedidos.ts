import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { OrdersService } from '../../core/orders.service';
import { formatCurrency, orderStatusClass, orderStatusLabel } from '../../core/formatters';
import { Order, OrderFilter } from '../../core/models';

@Component({
  selector: 'app-pedidos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrl: './pedidos.css',
  templateUrl: './pedidos.html'
})
export class Pedidos implements OnInit {
  private readonly ordersService = inject(OrdersService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  orders: Order[] = [];
  status: 'loading' | 'success' | 'error' = 'loading';
  errorMessage = '';
  searchTerm = '';
  orderFilter: OrderFilter = 'TODOS';
  selectedId: number | null = null;
  readonly formatCurrency = formatCurrency;
  readonly orderStatusClass = orderStatusClass;
  readonly orderStatusLabel = orderStatusLabel;
  readonly todayLabel = new Intl.DateTimeFormat('es-CL', { dateStyle: 'medium' }).format(new Date());

  ngOnInit(): void {
    this.route.paramMap.subscribe((params) => {
      const rawId = params.get('id');
      this.selectedId = rawId ? Number(rawId) : null;
    });
    this.loadOrders();
  }

  get filteredOrders(): Order[] {
    const query = this.searchTerm.trim().toLocaleLowerCase('es-CL');

    return this.orders.filter((order) => {
      const matchesSearch = !query
        || String(order.id).includes(query)
        || order.cliente.toLocaleLowerCase('es-CL').includes(query);
      const matchesFilter = this.orderFilter === 'TODOS'
        || (this.orderFilter === 'POR_PREPARAR' && ['NUEVO', 'EN_PREPARACION', 'PENDIENTE'].includes(order.estado))
        || (this.orderFilter === 'LISTOS' && ['LISTO', 'COMPLETADO'].includes(order.estado));

      return matchesSearch && matchesFilter;
    });
  }

  get selectedOrder(): Order | null {
    return this.orders.find((order) => order.id === this.selectedId) ?? null;
  }

  get pendingCount(): number {
    return this.orders.filter((order) => ['NUEVO', 'EN_PREPARACION', 'PENDIENTE'].includes(order.estado)).length;
  }

  get readyCount(): number {
    return this.orders.filter((order) => ['LISTO', 'COMPLETADO'].includes(order.estado)).length;
  }

  get totalToday(): number {
    return this.orders.reduce((sum, order) => sum + order.total, 0);
  }

  loadOrders(): void {
    this.status = 'loading';
    this.errorMessage = '';
    this.ordersService.getOrders().subscribe({
      next: (orders) => {
        this.orders = orders;
        this.status = 'success';
        if (!this.selectedId && orders.length > 0) {
          this.selectedId = orders[0].id;
        }
      },
      error: () => {
        this.status = 'error';
        this.errorMessage = 'No pudimos cargar los pedidos.';
      }
    });
  }

  selectOrder(order: Order): void {
    this.selectedId = order.id;
    void this.router.navigate(['/workspace/pedidos', order.id]);
  }

  orderItems(order: Order): { product: string; quantity: number; unitPrice: number; lineTotal: number }[] {
    if (order.items?.length) {
      return order.items.map((item) => ({
        ...item,
        lineTotal: item.lineTotal ?? item.quantity * item.unitPrice
      }));
    }

    return [{
      product: 'Detalle registrado en el pedido',
      quantity: 1,
      unitPrice: order.total,
      lineTotal: order.total
    }];
  }

  fulfillmentLabel(value?: string): string {
    return value === 'ENTREGA' ? 'Entrega' : 'Recogida';
  }

  goBackToQueue(): void {
    void this.router.navigate(['/workspace/pedidos']);
  }
}
