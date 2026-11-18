import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router, RouterLink } from '@angular/router';
import { ContasApi } from '../../core/api/contas-api';
import { InvestimentosApi } from '../../core/api/investimentos-api';
import { ComprovanteInvestimento, Fundo } from '../../core/api/modelos';
import { Notificacoes } from '../../core/erros/notificacoes';
import { mensagemDeErro } from '../../core/erros/problemas';
import { AplicarDialogo, DadosAplicacao } from './aplicar-dialogo';

@Component({
  selector: 'app-fundos',
  imports: [CurrencyPipe, DecimalPipe, RouterLink, MatCardModule, MatButtonModule, MatProgressBarModule],
  providers: [CurrencyPipe],
  template: `
    <h1>Fundos de investimento</h1>
    <p><a routerLink="/investimentos">Ver minha carteira</a></p>
    @if (fundos.isLoading()) {
      <mat-progress-bar mode="indeterminate" />
    } @else if (fundos.error()) {
      <p class="erro" role="alert">{{ erro() }}</p>
    }
    <div class="grade">
      @for (fundo of fundos.value() ?? []; track fundo.id) {
        <mat-card appearance="outlined">
          <mat-card-header>
            <mat-card-title>{{ fundo.nome }}</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <p>Cota atual: <strong>{{ fundo.cotaAtual | number: '1.2-8' }}</strong></p>
            <p>Aplicação mínima: {{ fundo.aplicacaoMinima | currency }}</p>
          </mat-card-content>
          <mat-card-actions>
            <button mat-flat-button type="button" (click)="aplicar(fundo)" [disabled]="!contasCorrentes().length">Aplicar</button>
          </mat-card-actions>
        </mat-card>
      } @empty {
        @if (!fundos.isLoading()) {
          <p>Nenhum fundo disponível no momento.</p>
        }
      }
    </div>
    @if (contas.hasValue() && !contasCorrentes().length) {
      <p class="erro">Você precisa de uma conta corrente para investir.</p>
    }
  `,
  styles: `
    .grade { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
    .erro { color: var(--mat-sys-error); }
  `,
})
export class Fundos {
  private readonly investimentos = inject(InvestimentosApi);
  private readonly contasApi = inject(ContasApi);
  private readonly dialogos = inject(MatDialog);
  private readonly notificacoes = inject(Notificacoes);
  private readonly router = inject(Router);
  private readonly moeda = inject(CurrencyPipe);

  protected readonly fundos = rxResource({ stream: () => this.investimentos.fundos() });
  protected readonly contas = rxResource({ stream: () => this.contasApi.minhas() });
  protected readonly contasCorrentes = computed(() => (this.contas.value() ?? []).filter((conta) => conta.tipo === 'CORRENTE'));
  protected readonly erro = computed(() => mensagemDeErro(this.fundos.error()));

  protected aplicar(fundo: Fundo): void {
    this.dialogos
      .open<AplicarDialogo, DadosAplicacao, ComprovanteInvestimento>(AplicarDialogo, {
        data: { fundo, contas: this.contasCorrentes() },
      })
      .afterClosed()
      .subscribe((comprovante) => {
        if (comprovante) {
          this.notificacoes.sucesso(
            `Aplicação de ${this.moeda.transform(comprovante.movimento.valor)} em ${comprovante.fundo.nome} realizada.`,
          );
          void this.router.navigate(['/investimentos/posicoes', comprovante.posicao.id]);
        }
      });
  }
}
