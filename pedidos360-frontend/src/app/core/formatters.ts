import { OrderStatus } from './models';

const currencyFormatter = new Intl.NumberFormat('es-CL', {
  style: 'currency',
  currency: 'CLP',
  maximumFractionDigits: 0
});

const dateFormatter = new Intl.DateTimeFormat('es-CL', {
  dateStyle: 'medium'
});

export function formatCurrency(value: number): string {
  return currencyFormatter.format(value);
}

export function formatDate(value: string | Date): string {
  return dateFormatter.format(new Date(value));
}

export function orderStatusLabel(status: OrderStatus): string {
  const labels: Record<string, string> = {
    NUEVO: 'Nuevo',
    EN_PREPARACION: 'En preparación',
    LISTO: 'Listo',
    ENTREGADO: 'Entregado',
    CANCELADO: 'Cancelado',
    PENDIENTE: 'Pendiente',
    COMPLETADO: 'Completado'
  };

  return labels[status] ?? status;
}

export function orderStatusClass(status: OrderStatus): string {
  switch (status) {
    case 'LISTO':
    case 'COMPLETADO':
      return 'status--ready';
    case 'CANCELADO':
      return 'status--error';
    case 'EN_PREPARACION':
    case 'PENDIENTE':
      return 'status--preparing';
    default:
      return 'status--new';
  }
}
