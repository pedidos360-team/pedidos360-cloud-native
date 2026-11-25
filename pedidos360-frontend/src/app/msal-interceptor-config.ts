import { MsalInterceptorConfiguration, ProtectedResourceScopes } from '@azure/msal-angular';
import { InteractionType } from '@azure/msal-browser';
import { environment } from '../environments/environment';

export function msalInterceptorConfigFactory(): MsalInterceptorConfiguration {
  // MSAL Angular v6 uses strict URL matching by default. The wildcard is
  // required so every endpoint below /api/ receives the access token.
  const protectedResourceMap = new Map<string, Array<string | ProtectedResourceScopes> | null>([
    // GET /api/productos es publico (vitrina en el login); el resto de
    // metodos sobre esa misma URL siguen cayendo en el wildcard de abajo
    // y exigiendo token. Debe ir ANTES del wildcard para tener prioridad.
    [`${environment.apiBaseUrl}/api/productos`, [{ httpMethod: 'GET', scopes: null }]],
    [`${environment.apiBaseUrl}/api/*`, environment.azure.apiScopes],
    [`${environment.ordersApiBaseUrl}/api/*`, environment.azure.apiScopes]
  ]);

  return {
    interactionType: InteractionType.Redirect,
    protectedResourceMap
  };
}
