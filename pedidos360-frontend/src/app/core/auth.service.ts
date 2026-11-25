import { Injectable, inject } from '@angular/core';
import { AccountInfo, AuthenticationResult } from '@azure/msal-browser';
import { MsalService } from '@azure/msal-angular';
import { Observable, catchError, map, of, tap } from 'rxjs';
import { environment } from '../../environments/environment';

export interface TokenClaimsSummary {
  accountName: string;
  username: string;
  roles: string[];
  scopes: string[];
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly msalService = inject(MsalService);
  private cachedAccessToken: string | null = null;

  getActiveAccount(): AccountInfo | null {
    return (
      this.msalService.instance.getActiveAccount() ??
      this.msalService.instance.getAllAccounts()[0] ??
      null
    );
  }

  acquireApiToken(): Observable<string | null> {
    const account = this.getActiveAccount();
    if (!account) {
      return of(null);
    }

    return this.msalService
      .acquireTokenSilent({
        scopes: environment.azure.apiScopes,
        account
      })
      .pipe(
        tap((result: AuthenticationResult) => {
          this.cachedAccessToken = result.accessToken;
        }),
        map((result: AuthenticationResult) => result.accessToken),
        catchError((error) => {
          console.warn('No se pudo renovar el token silenciosamente:', error);
          return of(null);
        })
      );
  }

  getClaimsSummary(): TokenClaimsSummary {
    const account = this.getActiveAccount();
    const idClaims = (account?.idTokenClaims ?? {}) as Record<string, unknown>;
    const accessClaims = this.cachedAccessToken
      ? this.decodeJwtPayload(this.cachedAccessToken)
      : {};

    const rawRoles = accessClaims['roles'] ?? idClaims['roles'];
    const roles = Array.isArray(rawRoles)
      ? rawRoles.map((r) => String(r))
      : [];

    const rawScopes = accessClaims['scp'] ?? idClaims['scp'];
    const scopes =
      typeof rawScopes === 'string' && rawScopes.trim().length > 0
        ? rawScopes.trim().split(/\s+/)
        : [];

    return {
      accountName: account?.name ?? account?.username ?? 'Equipo Pedidos360',
      username: account?.username ?? '',
      roles,
      scopes
    };
  }

  hasRole(role: string): boolean {
    return this.getClaimsSummary().roles.includes(role);
  }

  hasScope(scope: string): boolean {
    return this.getClaimsSummary().scopes.includes(scope);
  }

  private decodeJwtPayload(token: string): Record<string, unknown> {
    try {
      const payloadPart = token.split('.')[1];
      if (!payloadPart) return {};
      const base64 = payloadPart.replace(/-/g, '+').replace(/_/g, '/');
      const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=');
      const json = decodeURIComponent(
        atob(padded)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(json) as Record<string, unknown>;
    } catch {
      return {};
    }
  }
}