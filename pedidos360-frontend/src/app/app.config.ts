import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { MSAL_INSTANCE, MsalService, MsalGuard, MsalBroadcastService } from '@azure/msal-angular';
import { PublicClientApplication, BrowserCacheLocation } from '@azure/msal-browser';
import { environment } from '../environments/environment';

export function MSALInstanceFactory() {
  return new PublicClientApplication({
    auth: {
      clientId: environment.azure.clientId,
      authority: environment.azure.authority,
      redirectUri: environment.azure.redirectUri,
      postLogoutRedirectUri: environment.azure.redirectUri // Asegura la redirección limpia al salir
    },
    cache: {
      cacheLocation: BrowserCacheLocation.LocalStorage
    }
  });
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    {
      provide: MSAL_INSTANCE,
      useFactory: MSALInstanceFactory
    },
    MsalService,
    MsalGuard,
    MsalBroadcastService
  ]
};