import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MsalService } from '@azure/msal-angular';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  styleUrl: './navbar.css',
  templateUrl: './navbar.html'
})
export class Navbar implements OnInit {
  private readonly msalService = inject(MsalService);
  private readonly authService = inject(AuthService);
  readonly today = new Intl.DateTimeFormat('es-CL', { dateStyle: 'medium' }).format(new Date());

  ngOnInit(): void {
    this.authService.acquireApiToken().subscribe();
  }

  get accountName(): string {
    return this.authService.getClaimsSummary().accountName;
  }

  get accessBadge(): string {
    const { roles, scopes } = this.authService.getClaimsSummary();
    if (roles.length > 0) {
      return `Rol: ${roles.join(', ')}`;
    }
    if (scopes.length > 0) {
      return `Scope: ${scopes.join(', ')}`;
    }
    return 'JWT activo';
  }

  logout(): void {
    const account = this.authService.getActiveAccount();
    this.msalService.logoutRedirect({
      account: account ?? undefined,
      postLogoutRedirectUri: environment.azure.postLogoutRedirectUri
    }).subscribe();
  }
}