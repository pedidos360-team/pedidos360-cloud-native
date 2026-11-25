import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
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
  private readonly cdr = inject(ChangeDetectorRef);

  orders: Order[] = [];
  status: 'loading' | 'success' | 'error' = 'loading';
  errorMessage = '';
  searchTerm = '';
  orderFilter: OrderFilter = 'TODOS';
  selectedId: number | null = null;

  showCreateForm = false;
  isSubmitting = false;
  createError = '';
  newCliente = '';
  newTotal = '';
  newEstado = 'PENDIENTE';

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
        this.cdr.detectChanges();
      },
      error: () => {
        this.status = 'error';
        this.errorMessage = 'No pudimos cargar los pedidos.';
        this.cdr.detectChanges();
      }
    });
  }

  toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
    this.createError = '';
  }

  submitOrder(): void {
    if (this.isSubmitting) {
      return;
    }

    const cliente = this.newCliente.trim();
    // CLP no tiene decimales: cualquier "." o "," que se haya escrito es un
    // separador de miles (ej. "25.990"), nunca una parte decimal real.
    const total = Number(this.newTotal.replace(/[^0-9]/g, ''));

    if (!cliente || !Number.isFinite(total) || total <= 0) {
      this.createError = 'Ingresa el nombre del cliente y un monto mayor a 0.';
      return;
    }

    this.isSubmitting = true;
    this.createError = '';

    this.ordersService.createOrder({
      cliente,
      total,
      estado: this.newEstado
    }).subscribe({
      next: (created) => {
        this.orders = [created, ...this.orders];
        this.status = 'success';
        this.selectedId = created.id;
        this.newCliente = '';
        this.newTotal = '';
        this.newEstado = 'PENDIENTE';
        this.showCreateForm = false;
        this.isSubmitting = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.isSubmitting = false;
        this.createError = 'No se pudo registrar el pedido. Verifica tu sesión y la conexión con la API.';
        this.cdr.detectChanges();
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