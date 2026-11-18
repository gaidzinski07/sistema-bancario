import { CurrencyPipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterLink } from '@angular/router';
import { of } from 'rxjs';
import { ContasApi } from '../../core/api/contas-api';
import { InvestimentosApi } from '../../core/api/investimentos-api';
import { Sessao } from '../../core/auth/sessao';
import { mensagemDeErro } from '../../core/erros/problemas';
import { ContaPipe, TipoContaPipe } from '../../shared/formatos';

/**
 * Página inicial. Cliente: contas com saldo real (no legado, a tela de saldo mostrava R$ 85,00
 * fixos) e resumo dos investimentos. Administrador: atalhos para os cadastros.
 */
@Component({
  selector: 'app-inicio',
  imports: [CurrencyPipe, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatProgressBarModule, ContaPipe, TipoContaPipe],
  template: `
    <h1>Olá, {{ primeiroNome() }}</h1>

    @if (sessao.ehCliente()) {
      <section>
        <h2>Minhas contas</h2>
        @if (contas.isLoading()) {
          <mat-progress-bar mode="indeterminate" />
        } @else if (contas.error()) {
          <p class="erro" role="alert">{{ erroContas() }}</p>
        } @else {
          <div class="grade">
            @for (conta of contas.value() ?? []; track conta.id) {
              <mat-card appearance="outlined">
                <mat-card-header>
                  <mat-card-title>{{ conta.tipo | tipoConta }}</mat-card-title>
                  <mat-card-subtitle>{{ conta | conta }}</mat-card-subtitle>
                </mat-card-header>
                <mat-card-content>
                  <p class="saldo" [attr.aria-label]="'Saldo: ' + (conta.saldo | currency)">{{ conta.saldo | currency }}</p>
                </mat-card-content>
                <mat-card-actions>
                  <a mat-button [routerLink]="['/contas', conta.id, 'extrato']">Extrato</a>
                </mat-card-actions>
              </mat-card>
            } @empty {
              <p>Você ainda não tem contas. Procure sua agência.</p>
            }
          </div>
        }
      </section>

      <section>
        <h2>Atalhos</h2>
        <div class="atalhos">
          <a mat-stroked-button routerLink="/operacoes/transferencia"><mat-icon>swap_horiz</mat-icon> Transferir</a>
          <a mat-stroked-button routerLink="/operacoes/deposito"><mat-icon>savings</mat-icon> Depositar</a>
          <a mat-stroked-button routerLink="/operacoes/saque"><mat-icon>payments</mat-icon> Sacar</a>
          <a mat-stroked-button routerLink="/investimentos/fundos"><mat-icon>trending_up</mat-icon> Investir</a>
        </div>
      </section>

      <section>
        <h2>Investimentos</h2>
        @if (carteira.value(); as posicoes) {
          @if (posicoes.length) {
            <p>
              {{ posicoes.length }} {{ posicoes.length === 1 ? 'posição' : 'posições' }} ·
              valor atual <strong>{{ totalInvestido() | currency }}</strong>
            </p>
            <a mat-button routerLink="/investimentos">Ver carteira</a>
          } @else {
            <p>Você ainda não investe. <a routerLink="/investimentos/fundos">Conheça os fundos</a>.</p>
          }
        }
      </section>
    }

    @if (sessao.ehAdmin()) {
      <section>
        <h2>Administração</h2>
        <div class="atalhos">
          <a mat-stroked-button routerLink="/admin/clientes"><mat-icon>group</mat-icon> Clientes</a>
          <a mat-stroked-button routerLink="/admin/clientes/novo"><mat-icon>person_add</mat-icon> Novo cliente</a>
          <a mat-stroked-button routerLink="/admin/agencias"><mat-icon>account_balance</mat-icon> Agências</a>
          <a mat-stroked-button routerLink="/admin/fundos"><mat-icon>query_stats</mat-icon> Fundos e cotas</a>
        </div>
      </section>
    }
  `,
  styles: `
    section { margin-bottom: 32px; }
    .grade { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
    .saldo { font: var(--mat-sys-headline-medium); margin: 8px 0 0; }
    .atalhos { display: flex; flex-wrap: wrap; gap: 12px; }
    .erro { color: var(--mat-sys-error); }
  `,
})
export class Inicio {
  protected readonly sessao = inject(Sessao);
  private readonly contasApi = inject(ContasApi);
  private readonly investimentosApi = inject(InvestimentosApi);

  protected readonly contas = rxResource({
    stream: () => (this.sessao.ehCliente() ? this.contasApi.minhas() : of([])),
  });
  protected readonly carteira = rxResource({
    stream: () => (this.sessao.ehCliente() ? this.investimentosApi.carteira() : of([])),
  });

  protected readonly primeiroNome = computed(() => this.sessao.usuario()?.nome.split(' ')[0] ?? '');
  protected readonly totalInvestido = computed(() =>
    (this.carteira.value() ?? []).reduce((total, posicao) => total + posicao.valorAtual, 0),
  );
  protected readonly erroContas = computed(() => mensagemDeErro(this.contas.error()));
}
