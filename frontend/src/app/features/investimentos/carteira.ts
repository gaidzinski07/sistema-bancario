import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { InvestimentosApi } from '../../core/api/investimentos-api';
import { mensagemDeErro } from '../../core/erros/problemas';

/** Carteira: posições com valor atual e rendimento pela cota de agora. */
@Component({
  selector: 'app-carteira',
  imports: [CurrencyPipe, DecimalPipe, RouterLink, MatTableModule, MatButtonModule, MatProgressBarModule],
  template: `
    <div class="cabecalho">
      <h1>Investimentos</h1>
      <a mat-flat-button routerLink="/investimentos/fundos">Investir</a>
    </div>
    @if (carteira.isLoading()) {
      <mat-progress-bar mode="indeterminate" />
    } @else if (carteira.error()) {
      <p class="erro" role="alert">{{ erro() }}</p>
    } @else if (carteira.value(); as posicoes) {
      @if (posicoes.length) {
        <p class="total">Valor atual total: <strong>{{ total() | currency }}</strong></p>
        <div class="tabela">
          <table mat-table [dataSource]="posicoes">
            <ng-container matColumnDef="fundo">
              <th mat-header-cell *matHeaderCellDef>Fundo</th>
              <td mat-cell *matCellDef="let p"><a [routerLink]="['/investimentos/posicoes', p.id]">{{ p.fundo.nome }}</a></td>
            </ng-container>
            <ng-container matColumnDef="cotas">
              <th mat-header-cell *matHeaderCellDef class="numero">Cotas</th>
              <td mat-cell *matCellDef="let p" class="numero">{{ p.quantidadeCotas | number: '1.2-8' }}</td>
            </ng-container>
            <ng-container matColumnDef="aplicado">
              <th mat-header-cell *matHeaderCellDef class="numero">Aplicado</th>
              <td mat-cell *matCellDef="let p" class="numero">{{ p.valorAplicado | currency }}</td>
            </ng-container>
            <ng-container matColumnDef="atual">
              <th mat-header-cell *matHeaderCellDef class="numero">Valor atual</th>
              <td mat-cell *matCellDef="let p" class="numero">{{ p.valorAtual | currency }}</td>
            </ng-container>
            <ng-container matColumnDef="rendimento">
              <th mat-header-cell *matHeaderCellDef class="numero">Rendimento</th>
              <td mat-cell *matCellDef="let p" class="numero" [class.positivo]="p.rendimento > 0" [class.negativo]="p.rendimento < 0">
                {{ p.rendimento | currency }}
                @if (p.rentabilidadePercentual !== null) {
                  ({{ p.rentabilidadePercentual | number: '1.2-2' }}%)
                }
              </td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="colunas"></tr>
            <tr mat-row *matRowDef="let linha; columns: colunas"></tr>
          </table>
        </div>
      } @else {
        <p>Você ainda não tem investimentos. <a routerLink="/investimentos/fundos">Conheça os fundos</a>.</p>
      }
    }
  `,
  styles: `
    .cabecalho { display: flex; align-items: center; justify-content: space-between; gap: 16px; }
    .tabela { overflow-x: auto; }
    table { width: 100%; }
    .numero { text-align: right; white-space: nowrap; }
    .positivo { color: #1b6e2e; }
    .negativo, .erro { color: var(--mat-sys-error); }
  `,
})
export class Carteira {
  private readonly api = inject(InvestimentosApi);

  protected readonly colunas = ['fundo', 'cotas', 'aplicado', 'atual', 'rendimento'];
  protected readonly carteira = rxResource({ stream: () => this.api.carteira() });
  protected readonly total = computed(() => (this.carteira.value() ?? []).reduce((soma, p) => soma + p.valorAtual, 0));
  protected readonly erro = computed(() => mensagemDeErro(this.carteira.error()));
}
