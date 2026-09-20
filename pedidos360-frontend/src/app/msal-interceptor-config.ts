import { MsalInterceptorConfiguration } from '@azure/msal-angular';
import { InteractionType } from '@azure/msal-browser';
import { environment } from '../environments/environment';

export function msalInterceptorConfigFactory(): MsalInterceptorConfiguration {
  // MSAL Angular v6 uses strict URL matching by default. The wildcard is
  // required so every endpoint below /api/ receives the access token.
  const protectedResourceMap = new Map<string, string[]>([
    [`${environment.apiBaseUrl}/api/*`, environment.azure.apiScopes],
    [`${environment.ordersApiBaseUrl}/api/*`, environment.azure.apiScopes]
  ]);

  return {
    interactionType: InteractionType.Redirect,
    protectedResourceMap
  };
}
