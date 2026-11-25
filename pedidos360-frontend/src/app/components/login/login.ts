import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { MsalService } from '@azure/msal-angular';
import { environment } from '../../../environments/environment';
import { CatalogService } from '../../core/catalog.service';
import { formatCurrency } from '../../core/formatters';
import { Order, Product } from '../../core/models';
import { productImage, productImageAlt, productSku } from '../../core/catalog-presentation';
import { AUTH_ERROR_STORAGE_KEY } from '../../msal-initializer';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  styleUrl: './login.css',
  templateUrl: './login.html'
})
export class Login implements OnInit {
  private readonly msalService = inject(MsalService);
  private readonly router = inject(Router);
  private readonly catalogService = inject(CatalogService);

  isConnecting = false;
  loginError = '';
  showcaseProducts: Product[] = [];
  readonly formatCurrency = formatCurrency;
  readonly productImage = productImage;
  readonly productImageAlt = productImageAlt;
  readonly productSku = productSku;

  ngOnInit(): void {
    this.loginError = this.readStoredAuthError();

    if (this.isLoggedIn()) {
      void this.router.navigateByUrl('/workspace/pedidos');
      return;
    }

    this.catalogService.getProducts().subscribe({
      next: (products) => {
        this.showcaseProducts = products;
      },
      error: () => {
        // Si la API no responde, la vitrina simplemente queda vacia.
      }
    });
  }

  readonly sampleOrders: Order[] = [
    { id: 1048, cliente: 'Ana Pérez', total: 23300, estado: 'EN_PREPARACION', requestedTime: '10:30', fulfillment: 'RECOGIDA' },
    { id: 1049, cliente: 'Daniel Ruiz', total: 18400, estado: 'NUEVO', requestedTime: '11:00', fulfillment: 'ENTREGA' },
    { id: 1044, cliente: 'Marta León', total: 31800, estado: 'LISTO', requestedTime: '10:15', fulfillment: 'RECOGIDA' },
    { id: 1041, cliente: 'Laura Gil', total: 12200, estado: 'CANCELADO', requestedTime: '09:45', fulfillment: 'ENTREGA' }
  ];

  isLoggedIn(): boolean {
    return this.msalService.instance.getAllAccounts().length > 0 && !this.loginError;
  }

  login(): void {
    this.isConnecting = true;
    this.loginError = '';

    this.msalService.loginRedirect({
      scopes: [...environment.azure.loginScopes, ...environment.azure.apiScopes],
      redirectStartPage: `${window.location.origin}/workspace/pedidos`
    }).subscribe({
      error: () => {
        this.isConnecting = false;
        this.loginError = 'No pudimos iniciar sesión con Microsoft. Comprueba tu cuenta e inténtalo de nuevo.';
      }
    });
  }

  openWorkspace(): void {
    void this.router.navigateByUrl('/workspace/pedidos');
  }

  logout(): void {
    const account = this.msalService.instance.getActiveAccount() ?? this.msalService.instance.getAllAccounts()[0];
    this.msalService.logoutRedirect({
      account,
      postLogoutRedirectUri: environment.azure.postLogoutRedirectUri
    }).subscribe();
  }

  orderStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      NUEVO: 'Nuevo',
      EN_PREPARACION: 'En preparación',
      LISTO: 'Listo',
      CANCELADO: 'Cancelado'
    };
    return labels[status] ?? status;
  }

  orderStatusClass(status: string): string {
    if (status === 'LISTO') return 'status--ready';
    if (status === 'CANCELADO') return 'status--error';
    if (status === 'EN_PREPARACION') return 'status--preparing';
    return 'status--new';
  }

  fulfillmentLabel(value?: string): string {
    return value === 'ENTREGA' ? 'Entrega' : 'Recogida';
  }

  private readStoredAuthError(): string {
    try {
      const message = window.sessionStorage.getItem(AUTH_ERROR_STORAGE_KEY) ?? '';
      window.sessionStorage.removeItem(AUTH_ERROR_STORAGE_KEY);
      return message;
    } catch {
      return '';
    }
  }
}