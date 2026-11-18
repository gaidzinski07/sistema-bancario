import { Routes } from '@angular/router';

/** Área do cliente (papel CLIENTE). */
export const rotasDoCliente: Routes = [
  {
    path: 'contas/:contaId/extrato',
    loadComponent: () => import('./contas/extrato').then((m) => m.Extrato),
    title: 'Extrato · Banco UFF',
  },
  {
    path: 'operacoes/deposito',
    loadComponent: () => import('./operacoes/operacao-de-conta').then((m) => m.OperacaoDeConta),
    data: { tipo: 'deposito' },
    title: 'Depósito · Banco UFF',
  },
  {
    path: 'operacoes/saque',
    loadComponent: () => import('./operacoes/operacao-de-conta').then((m) => m.OperacaoDeConta),
    data: { tipo: 'saque' },
    title: 'Saque · Banco UFF',
  },
  {
    path: 'operacoes/transferencia',
    loadComponent: () => import('./operacoes/transferencia').then((m) => m.Transferencia),
    title: 'Transferência · Banco UFF',
  },
  {
    path: 'senha-transacao',
    loadComponent: () => import('./operacoes/senha-transacao').then((m) => m.SenhaTransacao),
    title: 'Senha de transação · Banco UFF',
  },
  {
    path: 'investimentos',
    loadComponent: () => import('./investimentos/carteira').then((m) => m.Carteira),
    title: 'Investimentos · Banco UFF',
  },
  {
    path: 'investimentos/fundos',
    loadComponent: () => import('./investimentos/fundos').then((m) => m.Fundos),
    title: 'Fundos · Banco UFF',
  },
  {
    path: 'investimentos/posicoes/:posicaoId',
    loadComponent: () => import('./investimentos/detalhe-posicao').then((m) => m.DetalhePosicao),
    title: 'Posição · Banco UFF',
  },
];
