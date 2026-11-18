import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, input, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';
import { ContasApi } from '../../core/api/contas-api';
import { ComprovanteOperacao, FormaDeposito } from '../../core/api/modelos';
import { aplicarErrosDeCampo, mensagemDeErro } from '../../core/erros/problemas';
import { Comprovante, LinhaDeComprovante } from '../../shared/comprovante';
import { ContaPipe, TipoContaPipe } from '../../shared/formatos';

type TipoOperacao = 'deposito' | 'saque';

/**
 * Depósito e saque (o tipo vem da rota). Os dois formulários do legado (realizardeposito.jsp e
 * realizarsaque.jsp) só mudavam o texto; aqui é um componente só.
 */
@Component({
  selector: 'app-operacao-de-conta',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatRadioModule,
    MatButtonModule,
    MatProgressBarModule,
    Comprovante,
    ContaPipe,
    TipoContaPipe,
    CurrencyPipe,
  ],
  providers: [CurrencyPipe, DatePipe, ContaPipe],
  template: `
    <h1>{{ ehDeposito() ? 'Depósito' : 'Saque' }}</h1>

    @if (comprovante(); as feito) {
      <app-comprovante [titulo]="ehDeposito() ? 'Depósito realizado' : 'Saque realizado'" [linhas]="linhas(feito)">
        <a mat-button routerLink="/inicio">Início</a>
        <a mat-button [routerLink]="['/contas', feito.conta.id, 'extrato']">Ver extrato</a>
        <button mat-flat-button type="button" (click)="novaOperacao()">Nova operação</button>
      </app-comprovante>
    } @else {
      @if (enviando()) {
        <mat-progress-bar mode="indeterminate" />
      }
      <form [formGroup]="formulario" (ngSubmit)="enviar()" class="formulario">
        <mat-form-field>
          <mat-label>Conta</mat-label>
          <mat-select formControlName="contaId">
            @for (conta of contas.value() ?? []; track conta.id) {
              <mat-option [value]="conta.id">{{ conta.tipo | tipoConta }} · {{ conta | conta }} · {{ conta.saldo | currency }}</mat-option>
            }
          </mat-select>
          @if (formulario.controls.contaId.hasError('required')) {
            <mat-error>Escolha a conta.</mat-error>
          }
        </mat-form-field>

        @if (ehDeposito()) {
          <mat-radio-group formControlName="forma" aria-label="Forma do depósito" class="forma">
            <mat-radio-button value="DINHEIRO">Dinheiro</mat-radio-button>
            <mat-radio-button value="CHEQUE">Cheque</mat-radio-button>
          </mat-radio-group>
        }

        <mat-form-field>
          <mat-label>Valor</mat-label>
          <span matTextPrefix>R$&nbsp;</span>
          <input matInput type="number" formControlName="valor" min="0.01" step="0.01" inputmode="decimal" />
          @if (formulario.controls.valor.hasError('required')) {
            <mat-error>Informe o valor.</mat-error>
          } @else if (formulario.controls.valor.hasError('min')) {
            <mat-error>O valor deve ser maior que zero.</mat-error>
          } @else if (formulario.controls.valor.hasError('servidor')) {
            <mat-error>{{ formulario.controls.valor.getError('servidor') }}</mat-error>
          }
        </mat-form-field>

        <mat-form-field>
          <mat-label>Senha de transação</mat-label>
          <input matInput type="password" formControlName="senhaTransacao" inputmode="numeric" maxlength="6" autocomplete="off" />
          <mat-hint>Não tem? <a routerLink="/senha-transacao">Defina sua senha de transação</a>.</mat-hint>
          @if (formulario.controls.senhaTransacao.hasError('required')) {
            <mat-error>Informe a senha de transação.</mat-error>
          }
        </mat-form-field>

        @if (erro()) {
          <p class="erro" role="alert">{{ erro() }}</p>
        }
        <div class="acoes">
          <a mat-button routerLink="/inicio">Cancelar</a>
          <button mat-flat-button type="submit" [disabled]="enviando()">{{ ehDeposito() ? 'Depositar' : 'Sacar' }}</button>
        </div>
      </form>
    }
  `,
  styles: `
    .formulario { display: flex; flex-direction: column; gap: 12px; max-width: 480px; }
    .forma { display: flex; gap: 16px; }
    .acoes { display: flex; justify-content: flex-end; gap: 8px; }
    .erro { color: var(--mat-sys-error); margin: 0; }
  `,
})
export class OperacaoDeConta {
  private readonly api = inject(ContasApi);
  private readonly moeda = inject(CurrencyPipe);
  private readonly data = inject(DatePipe);
  private readonly formatoConta = inject(ContaPipe);

  /** Definido nos `data` da rota. */
  readonly tipo = input.required<TipoOperacao>();

  protected readonly ehDeposito = computed(() => this.tipo() === 'deposito');
  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly comprovante = signal<ComprovanteOperacao | null>(null);

  protected readonly contas = rxResource({ stream: () => this.api.minhas() });

  protected readonly formulario = inject(NonNullableFormBuilder).group({
    contaId: [null as number | null, Validators.required],
    forma: ['DINHEIRO' as FormaDeposito],
    valor: [null as number | null, [Validators.required, Validators.min(0.01)]],
    senhaTransacao: ['', Validators.required],
  });

  protected enviar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const { contaId, forma, valor, senhaTransacao } = this.formulario.getRawValue();
    const chamada: Observable<ComprovanteOperacao> = this.ehDeposito()
      ? this.api.depositar(contaId!, valor!, forma, senhaTransacao)
      : this.api.sacar(contaId!, valor!, senhaTransacao);

    this.enviando.set(true);
    this.erro.set(null);
    chamada.subscribe({
      next: (comprovante) => {
        this.comprovante.set(comprovante);
        this.enviando.set(false);
        this.contas.reload();
      },
      error: (erro: unknown) => {
        if (!aplicarErrosDeCampo(this.formulario, erro)) {
          this.erro.set(mensagemDeErro(erro));
        }
        this.formulario.controls.senhaTransacao.reset();
        this.enviando.set(false);
      },
    });
  }

  protected novaOperacao(): void {
    this.comprovante.set(null);
    this.formulario.reset();
  }

  protected linhas(c: ComprovanteOperacao): LinhaDeComprovante[] {
    return [
      { rotulo: 'Conta', valor: this.formatoConta.transform(c.conta) },
      { rotulo: 'Operação', valor: c.descricao },
      { rotulo: 'Valor', valor: this.moeda.transform(c.valor) ?? '' },
      { rotulo: 'Saldo após', valor: this.moeda.transform(c.saldoApos) ?? '' },
      { rotulo: 'Data', valor: this.data.transform(c.ocorridoEm, 'dd/MM/yyyy HH:mm:ss') ?? '' },
      { rotulo: 'Número da operação', valor: String(c.operacaoId) },
    ];
  }
}
