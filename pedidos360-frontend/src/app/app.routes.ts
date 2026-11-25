import { Routes } from '@angular/router';
import { MsalGuard } from '@azure/msal-angular';
import { Login } from './components/login/login';
import { Navbar } from './components/navbar/navbar';
import { Pedidos } from './components/pedidos/pedidos';
import { Productos } from './components/productos/productos';

export const routes: Routes = [
  {
    path: '',
    component: Login,
    title: 'Pedidos360 | Operación de tienda'
  },
  {
    path: 'workspace',
    component: Navbar,
    canActivate: [MsalGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'pedidos'
      },
      {
        path: 'pedidos',
        component: Pedidos,
        title: 'Pedidos | Pedidos360'
      },
      {
        path: 'pedidos/:id',
        component: Pedidos,
        title: 'Detalle de pedido | Pedidos360'
      },
      {
        path: 'catalogo',
        component: Productos,
        title: 'Catálogo | Pedidos360'
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
