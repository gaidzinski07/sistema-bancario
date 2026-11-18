import { Routes } from '@angular/router';

/** Área de administração (papel ADMIN), sob `/admin`. */
export const rotasDeAdmin: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'clientes' },
  {
    path: 'clientes',
    loadComponent: () => import('./clientes').then((m) => m.Clientes),
    title: 'Clientes · Banco UFF',
  },
  {
    path: 'clientes/novo',
    loadComponent: () => import('./novo-cliente').then((m) => m.NovoCliente),
    title: 'Novo cliente · Banco UFF',
  },
  {
    path: 'clientes/:clienteId',
    loadComponent: () => import('./detalhe-cliente').then((m) => m.DetalheCliente),
    title: 'Cliente · Banco UFF',
  },
  {
    path: 'agencias',
    loadComponent: () => import('./agencias').then((m) => m.Agencias),
    title: 'Agências · Banco UFF',
  },
  {
    path: 'fundos',
    loadComponent: () => import('./fundos-admin').then((m) => m.FundosAdmin),
    title: 'Fundos · Banco UFF',
  },
  {
    path: 'administradores/novo',
    loadComponent: () => import('./novo-administrador').then((m) => m.NovoAdministrador),
    title: 'Novo administrador · Banco UFF',
  },
];
