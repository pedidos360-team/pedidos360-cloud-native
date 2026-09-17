import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MsalService } from '@azure/msal-angular';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  styleUrl: './navbar.css',
  templateUrl: './navbar.html'
})
export class Navbar {
  private readonly msalService = inject(MsalService);
  readonly today = new Intl.DateTimeFormat('es-CL', { dateStyle: 'medium' }).format(new Date());

  get accountName(): string {
    const account = this.msalService.instance.getActiveAccount() ?? this.msalService.instance.getAllAccounts()[0];
    return account?.name ?? account?.username ?? 'Equipo Pedidos360';
  }

  logout(): void {
    const account = this.msalService.instance.getActiveAccount() ?? this.msalService.instance.getAllAccounts()[0];
    this.msalService.logoutRedirect({
      account,
      postLogoutRedirectUri: environment.azure.postLogoutRedirectUri
    }).subscribe();
  }
}
