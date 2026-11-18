import { Routes } from '@angular/router';
import { anonimo, autenticado, comPapel } from './core/auth/guards';
import { Shell } from './core/layout/shell';

/**
 * Rotas. Cada área é carregada sob demanda (lazy) e protegida por papel: o cliente não baixa o
 * código das telas de administração e vice-versa. A autorização de verdade é do backend.
 */
export const routes: Routes = [
  {
    path: 'login',
    canMatch: [anonimo],
    loadComponent: () => import('./features/auth/login').then((m) => m.Login),
    title: 'Entrar · Banco UFF',
  },
  {
    path: '',
    component: Shell,
    canMatch: [autenticado],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'inicio' },
      {
        path: 'inicio',
        loadComponent: () => import('./features/inicio/inicio').then((m) => m.Inicio),
        title: 'Início · Banco UFF',
      },
      {
        path: '',
        canMatch: [comPapel('CLIENTE')],
        loadChildren: () => import('./features/cliente.routes').then((m) => m.rotasDoCliente),
      },
      {
        path: 'admin',
        canMatch: [comPapel('ADMIN')],
        loadChildren: () => import('./features/admin/admin.routes').then((m) => m.rotasDeAdmin),
      },
      {
        path: '**',
        loadComponent: () => import('./features/nao-encontrada').then((m) => m.NaoEncontrada),
        title: 'Página não encontrada · Banco UFF',
      },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
