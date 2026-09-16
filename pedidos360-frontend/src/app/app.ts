import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { MsalService, MsalModule } from '@azure/msal-angular';
import { AuthenticationResult } from '@azure/msal-browser';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, MsalModule],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class AppComponent implements OnInit {
  private msalService = inject(MsalService);
  private cdr = inject(ChangeDetectorRef); // Permite forzar la actualización de la vista

  ngOnInit(): void {
    this.msalService.instance.initialize().then(() => {
      return this.msalService.instance.handleRedirectPromise();
    }).then((response: AuthenticationResult | null) => {
      if (response) {
        this.msalService.instance.setActiveAccount(response.account);
      } else {
        const accounts = this.msalService.instance.getAllAccounts();
        if (accounts.length > 0) {
          this.msalService.instance.setActiveAccount(accounts[0]);
        }
      }
      // Forzamos a Angular a detectar el cambio de estado de la sesión
      this.cdr.detectChanges();
    }).catch(error => {
      console.error('Error al inicializar MSAL:', error);
    });
  }

  isLoggedIn(): boolean {
    return this.msalService.instance.getActiveAccount() !== null || 
           this.msalService.instance.getAllAccounts().length > 0;
  }

  login(): void {
    this.msalService.loginRedirect();
  }

logout(): void {
  const activeAccount = this.msalService.instance.getActiveAccount();
  
  // 1. Limpiamos la cuenta activa localmente
  if (activeAccount) {
    this.msalService.instance.setActiveAccount(null);
  }

  // 2. Limpiamos el caché explícitamente de la memoria del navegador
  window.localStorage.clear();

  // 3. Redirigimos a Microsoft indicando la URL de retorno
  this.msalService.logoutRedirect({
    account: activeAccount || undefined,
    postLogoutRedirectUri: 'http://localhost:4200'
  });
}
}