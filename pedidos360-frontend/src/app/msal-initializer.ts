import { inject } from '@angular/core';
import { AuthenticationResult } from '@azure/msal-browser';
import { MsalService } from '@azure/msal-angular';
import { firstValueFrom } from 'rxjs';

export const AUTH_ERROR_STORAGE_KEY = 'pedidos360.auth.error';

/**
 * Restores a redirect response before Angular evaluates the protected routes.
 * Keeping this in an app initializer prevents MsalGuard from racing the redirect
 * response on a full-page return from Microsoft Entra ID.
 */
export function initializeMsal(): Promise<void> {
  const msalService = inject(MsalService);

  return firstValueFrom(msalService.handleRedirectObservable())
    .then((response: AuthenticationResult | null) => {
      const account = response?.account ?? msalService.instance.getAllAccounts()[0] ?? null;
      msalService.instance.setActiveAccount(account);
      removeStoredAuthError();
    })
    .catch((error: unknown) => {
      console.error('Error al completar el inicio de sesión con Microsoft Entra ID:', error);
      storeAuthError(error);
    });
}

function storeAuthError(error: unknown): void {
  if (typeof window === 'undefined') {
    return;
  }

  const errorCode = getErrorCode(error);
  const message = errorCode
    ? `Microsoft no pudo completar el acceso (${errorCode}). Verifica que tu cuenta pertenezca al tenant configurado y que http://localhost:4200 esté registrado como redirección SPA.`
    : 'Microsoft no pudo completar el acceso. Verifica tu cuenta institucional y vuelve a intentarlo.';

  try {
    window.sessionStorage.setItem(AUTH_ERROR_STORAGE_KEY, message);
  } catch {
    // Storage can be unavailable in restricted browser contexts; the console still has the detail.
  }
}

function removeStoredAuthError(): void {
  if (typeof window === 'undefined') {
    return;
  }

  try {
    window.sessionStorage.removeItem(AUTH_ERROR_STORAGE_KEY);
  } catch {
    // Ignore storage failures; authentication itself has already completed.
  }
}

function getErrorCode(error: unknown): string | null {
  if (typeof error !== 'object' || error === null || !('errorCode' in error)) {
    return null;
  }

  const errorCode = (error as { errorCode?: unknown }).errorCode;
  return typeof errorCode === 'string' && errorCode.length > 0 ? errorCode : null;
}
