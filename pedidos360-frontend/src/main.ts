import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app';
import { HTTP_INTERCEPTORS, provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { routes } from './app/app.routes';
import {
  MSAL_INSTANCE,
  MSAL_INTERCEPTOR_CONFIG,
  MsalInterceptor,
  MsalGuard,
  MsalService,
  MsalBroadcastService
} from '@azure/msal-angular';
import { msalInstanceFactory } from './app/msal-config';
import { msalInterceptorConfigFactory } from './app/msal-interceptor-config';

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    { provide: MSAL_INSTANCE, useFactory: msalInstanceFactory },
    { provide: MSAL_INTERCEPTOR_CONFIG, useFactory: msalInterceptorConfigFactory },
    { provide: HTTP_INTERCEPTORS, useClass: MsalInterceptor, multi: true },
    MsalGuard,
    MsalService,
    MsalBroadcastService
  ]
}).catch(err => console.error(err));
