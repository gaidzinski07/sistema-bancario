import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, input, numberAttribute, signal } from '@angular/core';
import { rxResource, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatRadioModule } from '@angular/material/radio';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { InvestimentosApi, PedidoResgate } from '../../core/api/investimentos-api';
import { ComprovanteInvestimento } from '../../core/api/modelos';
import { aplicarErrosDeCampo, mensagemDeErro } from '../../core/erros/problemas';
import { Comprovante, LinhaDeComprovante } from '../../shared/comprovante';
import { ContaPipe } from '../../shared/formatos';

/** Posição num fundo: resumo, histórico de aplicações/resgates e resgate parcial ou total. */
@Component({
  selector: 'app-detalhe-posicao',
  imports: [ReactiveFormsModule, RouterLink, CurrencyPipe, DatePipe, DecimalPipe, MatCardModule, MatTableModule,
    MatFormFieldModule, MatInputModule, MatRadioModule, MatButtonModule, MatIconModule, MatProgressBarModule, Comprovante, ContaPipe],
  providers: [CurrencyPipe, DatePipe, DecimalPipe],
  template: `
    <a mat-button routerLink="/investimentos"><mat-icon>arrow_back</mat-icon> Carteira</a>
    @if (detalhe.isLoading() && !detalhe.hasValue()) {
      <mat-progress-bar mode="indeterminate" />
    } @else if (detalhe.error()) {
      <p class="erro" role="alert">{{ erroCarga() }}</p>
    }
    @if (detalhe.value(); as d) {
      <h1>{{ d.posicao.fundo.nome }}</h1>
      <p class="conta">{{ d.posicao.conta | conta }}</p>

      <div class="resumo">
        <mat-card appearance="outlined"><mat-card-content>
          <span class="rotulo">Cotas</span><strong>{{ d.posicao.quantidadeCotas | number: '1.2-8' }}</strong>
        </mat-card-content></mat-card>
        <mat-card appearance="outlined"><mat-card-content>
          <span class="rotulo">Cota atual</span><strong>{{ d.posicao.cotaAtual | number: '1.2-8' }}</strong>
        </mat-card-content></mat-card>
        <mat-card appearance="outlined"><mat-card-content>
          <span class="rotulo">Aplicado</span><strong>{{ d.posicao.valorAplicado | currency }}</strong>
        </mat-card-content></mat-card>
        <mat-card appearance="outlined"><mat-card-content>
          <span class="rotulo">Valor atual</span><strong>{{ d.posicao.valorAtual | currency }}</strong>
        </mat-card-content></mat-card>
        <mat-card appearance="outlined"><mat-card-content>
          <span class="rotulo">Rendimento</span>
          <strong [class.positivo]="d.posicao.rendimento > 0" [class.negativo]="d.posicao.rendimento < 0">
            {{ d.posicao.rendimento | currency }}
          </strong>
        </mat-card-content></mat-card>
      </div>

      <h2>Resgatar</h2>
      @if (comprovante(); as feito) {
        <app-comprovante titulo="Resgate realizado" [linhas]="linhas(feito)">
          <a mat-button [routerLink]="['/contas', feito.conta.id, 'extrato']">Ver extrato</a>
          <button mat-flat-button type="button" (click)="comprovante.set(null)">OK</button>
        </app-comprovante>
      } @else if (d.posicao.quantidadeCotas > 0) {
        @if (resgatando()) {
          <mat-progress-bar mode="indeterminate" />
        }
        <form [formGroup]="formulario" (ngSubmit)="resgatar()" class="formulario">
          <mat-radio-group formControlName="modo" aria-label="Tipo de resgate" class="modo">
            <mat-radio-button value="parcial">Parcial</mat-radio-button>
            <mat-radio-button value="total">Total ({{ d.posicao.valorAtual | currency }})</mat-radio-button>
          </mat-radio-group>
          @if (formulario.controls.modo.value === 'parcial') {
            <mat-form-field>
              <mat-label>Valor</mat-label>
              <span matTextPrefix>R$&nbsp;</span>
              <input matInput type="number" formControlName="valor" min="0.01" [max]="d.posicao.valorAtual" step="0.01" inputmode="decimal" />
              <mat-hint>Disponível: {{ d.posicao.valorAtual | currency }}</mat-hint>
              @if (formulario.controls.valor.hasError('required')) {
                <mat-error>Informe o valor.</mat-error>
              } @else if (formulario.controls.valor.hasError('min')) {
                <mat-error>O valor deve ser maior que zero.</mat-error>
              }
            </mat-form-field>
          }
          <mat-form-field>
            <mat-label>Senha de transação</mat-label>
            <input matInput type="password" formControlName="senhaTransacao" inputmode="numeric" maxlength="6" autocomplete="off" />
            @if (formulario.controls.senhaTransacao.hasError('required')) {
              <mat-error>Informe a senha de transação.</mat-error>
            }
          </mat-form-field>
          @if (erro()) {
            <p class="erro" role="alert">{{ erro() }}</p>
          }
          <div class="acoes">
            <button mat-flat-button type="submit" [disabled]="resgatando()">Resgatar para a conta corrente</button>
          </div>
        </form>
      } @else {
        <p>Esta posição foi totalmente resgatada.</p>
      }

      <h2>Histórico</h2>
      <div class="tabela">
        <table mat-table [dataSource]="d.movimentos">
          <ng-container matColumnDef="data">
            <th mat-header-cell *matHeaderCellDef>Data</th>
            <td mat-cell *matCellDef="let m">{{ m.ocorridoEm | date: 'dd/MM/yyyy HH:mm' }}</td>
          </ng-container>
          <ng-container matColumnDef="tipo">
            <th mat-header-cell *matHeaderCellDef>Tipo</th>
            <td mat-cell *matCellDef="let m">{{ m.tipo === 'APLICACAO' ? 'Aplicação' : 'Resgate' }}</td>
          </ng-container>
          <ng-container matColumnDef="cotas">
            <th mat-header-cell *matHeaderCellDef class="numero">Cotas</th>
            <td mat-cell *matCellDef="let m" class="numero">{{ m.quantidadeCotas | number: '1.2-8' }}</td>
          </ng-container>
          <ng-container matColumnDef="cota">
            <th mat-header-cell *matHeaderCellDef class="numero">Valor da cota</th>
            <td mat-cell *matCellDef="let m" class="numero">{{ m.valorCota | number: '1.2-8' }}</td>
          </ng-container>
          <ng-container matColumnDef="valor">
            <th mat-header-cell *matHeaderCellDef class="numero">Valor</th>
            <td mat-cell *matCellDef="let m" class="numero">{{ m.valor | currency }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="colunas"></tr>
          <tr mat-row *matRowDef="let linha; columns: colunas"></tr>
        </table>
      </div>
    }
  `,
  styles: `
    .conta { color: var(--mat-sys-on-surface-variant); margin-top: -8px; }
    .resumo { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 12px; }
    .resumo mat-card-content { display: flex; flex-direction: column; gap: 4px; }
    .rotulo { font: var(--mat-sys-label-medium); color: var(--mat-sys-on-surface-variant); }
    .resumo strong { font: var(--mat-sys-title-medium); }
    .formulario { display: flex; flex-direction: column; gap: 12px; max-width: 420px; }
    .modo { display: flex; gap: 16px; }
    .acoes { display: flex; justify-content: flex-end; }
    .tabela { overflow-x: auto; }
    table { width: 100%; }
    .numero { text-align: right; white-space: nowrap; }
    .positivo { color: #1b6e2e; }
    .negativo, .erro { color: var(--mat-sys-error); }
  `,
})
export class DetalhePosicao {
  private readonly api = inject(InvestimentosApi);
  private readonly moeda = inject(CurrencyPipe);
  private readonly data = inject(DatePipe);
  private readonly numero = inject(DecimalPipe);

  /** Parâmetro da rota `investimentos/posicoes/:posicaoId`. */
  readonly posicaoId = input.required({ transform: numberAttribute });

  protected readonly colunas = ['data', 'tipo', 'cotas', 'cota', 'valor'];
  protected readonly detalhe = rxResource({ params: () => this.posicaoId(), stream: ({ params }) => this.api.posicao(params) });
  protected readonly erroCarga = computed(() => mensagemDeErro(this.detalhe.error()));

  protected readonly resgatando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly comprovante = signal<ComprovanteInvestimento | null>(null);

  protected readonly formulario = inject(NonNullableFormBuilder).group({
    modo: ['parcial' as 'parcial' | 'total'],
    valor: [null as number | null, [Validators.required, Validators.min(0.01)]],
    senhaTransacao: ['', Validators.required],
  });

  constructor() {
    // No resgate total o valor não é pedido.
    this.formulario.controls.modo.valueChanges.pipe(takeUntilDestroyed()).subscribe((modo) => {
      const valor = this.formulario.controls.valor;
      if (modo === 'total') {
        valor.disable();
      } else {
        valor.enable();
      }
    });
  }

  protected resgatar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const { modo, valor, senhaTransacao } = this.formulario.getRawValue();
    const pedido: PedidoResgate = modo === 'total' ? { total: true } : { valor: valor! };
    this.resgatando.set(true);
    this.erro.set(null);
    this.api.resgatar(this.posicaoId(), pedido, senhaTransacao).subscribe({
      next: (comprovante) => {
        this.resgatando.set(false);
        this.comprovante.set(comprovante);
        this.formulario.reset();
        this.detalhe.reload();
      },
      error: (erro: unknown) => {
        this.resgatando.set(false);
        this.formulario.controls.senhaTransacao.reset();
        if (!aplicarErrosDeCampo(this.formulario, erro)) {
          this.erro.set(mensagemDeErro(erro));
        }
      },
    });
  }

  protected linhas(c: ComprovanteInvestimento): LinhaDeComprovante[] {
    return [
      { rotulo: 'Valor creditado', valor: this.moeda.transform(c.movimento.valor) ?? '' },
      { rotulo: 'Cotas resgatadas', valor: this.numero.transform(c.movimento.quantidadeCotas, '1.2-8') ?? '' },
      { rotulo: 'Valor da cota', valor: this.numero.transform(c.movimento.valorCota, '1.2-8') ?? '' },
      { rotulo: 'Saldo da conta após', valor: this.moeda.transform(c.saldoContaApos) ?? '' },
      { rotulo: 'Data', valor: this.data.transform(c.movimento.ocorridoEm, 'dd/MM/yyyy HH:mm:ss') ?? '' },
    ];
  }
}
