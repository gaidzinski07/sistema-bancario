import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, signal, viewChild } from '@angular/core';
import { rxResource, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatStepper, MatStepperModule } from '@angular/material/stepper';
import { RouterLink } from '@angular/router';
import { ContasApi } from '../../core/api/contas-api';
import { ComprovanteTransferencia, Conta } from '../../core/api/modelos';
import { codigoDoErro, mensagemDeErro } from '../../core/erros/problemas';
import { Comprovante, LinhaDeComprovante } from '../../shared/comprovante';
import { CpfPipe, digitosDoCpf, validadorCpf } from '../../shared/cpf';
import { ContaPipe, TipoContaPipe } from '../../shared/formatos';

/**
 * Transferência em três passos: dados → confirmação com senha de transação → comprovante.
 *
 * Idempotência: a `Idempotency-Key` é criada quando o usuário confirma e reaproveitada se ele
 * precisar tentar de novo (erro de rede, timeout). Se a primeira tentativa tiver sido gravada, o
 * backend devolve o comprovante original em vez de transferir de novo. Mudar qualquer dado da
 * transferência descarta a chave.
 *
 * No legado: origem fixa na conta 1, agência e CPF do destino ignorados, e a senha digitada era
 * exibida na própria página.
 */
@Component({
  selector: 'app-transferencia',
  imports: [
    ReactiveFormsModule,
    RouterLink,
    MatStepperModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressBarModule,
    Comprovante,
    ContaPipe,
    TipoContaPipe,
    CpfPipe,
    CurrencyPipe,
  ],
  providers: [CurrencyPipe, DatePipe, ContaPipe],
  template: `
    <h1>Transferência</h1>
    <mat-stepper linear #passos [orientation]="'horizontal'">
      <mat-step [stepControl]="dados" label="Dados" [editable]="!comprovante()">
        <form [formGroup]="dados" class="formulario">
          <mat-form-field>
            <mat-label>Conta de origem</mat-label>
            <mat-select formControlName="contaOrigemId">
              @for (conta of contas.value() ?? []; track conta.id) {
                <mat-option [value]="conta.id">{{ conta.tipo | tipoConta }} · {{ conta | conta }} · {{ conta.saldo | currency }}</mat-option>
              }
            </mat-select>
            @if (dados.controls.contaOrigemId.hasError('required')) {
              <mat-error>Escolha a conta de origem.</mat-error>
            }
          </mat-form-field>

          <fieldset>
            <legend>Destino</legend>
            <div class="linha">
              <mat-form-field class="agencia">
                <mat-label>Agência</mat-label>
                <input matInput formControlName="agenciaDestino" inputmode="numeric" maxlength="4" placeholder="0001" />
                @if (dados.controls.agenciaDestino.invalid) {
                  <mat-error>4 dígitos.</mat-error>
                }
              </mat-form-field>
              <mat-form-field class="numero-conta">
                <mat-label>Conta com dígito</mat-label>
                <input matInput formControlName="contaDestino" placeholder="00001-9" />
                @if (dados.controls.contaDestino.invalid) {
                  <mat-error>Formato 00000-0.</mat-error>
                }
              </mat-form-field>
            </div>
            <mat-form-field>
              <mat-label>CPF do titular</mat-label>
              <input matInput formControlName="cpfDestino" inputmode="numeric" placeholder="000.000.000-00" />
              @if (dados.controls.cpfDestino.hasError('required')) {
                <mat-error>Informe o CPF do titular.</mat-error>
              } @else if (dados.controls.cpfDestino.hasError('cpf')) {
                <mat-error>CPF inválido.</mat-error>
              }
            </mat-form-field>
          </fieldset>

          <mat-form-field>
            <mat-label>Valor</mat-label>
            <span matTextPrefix>R$&nbsp;</span>
            <input matInput type="number" formControlName="valor" min="0.01" step="0.01" inputmode="decimal" />
            @if (dados.controls.valor.hasError('required')) {
              <mat-error>Informe o valor.</mat-error>
            } @else if (dados.controls.valor.hasError('min')) {
              <mat-error>O valor deve ser maior que zero.</mat-error>
            } @else if (origem() && (dados.controls.valor.value ?? 0) > origem()!.saldo) {
              <mat-hint class="alerta">Acima do saldo da conta ({{ origem()!.saldo | currency }}).</mat-hint>
            }
          </mat-form-field>

          <div class="acoes">
            <a mat-button routerLink="/inicio">Cancelar</a>
            <button mat-flat-button matStepperNext type="button">Continuar</button>
          </div>
        </form>
      </mat-step>

      <mat-step [stepControl]="confirmacao" label="Confirmação" [editable]="!comprovante()">
        <div class="resumo">
          <p>Transferir <strong>{{ dados.controls.valor.value | currency }}</strong></p>
          <p>de {{ origem() | conta }}</p>
          <p>para Ag {{ dados.controls.agenciaDestino.value }} · conta {{ dados.controls.contaDestino.value }}
            (CPF {{ dados.controls.cpfDestino.value | cpf }})</p>
        </div>
        <form [formGroup]="confirmacao" (ngSubmit)="confirmar()" class="formulario">
          <mat-form-field>
            <mat-label>Senha de transação</mat-label>
            <input matInput type="password" formControlName="senhaTransacao" inputmode="numeric" maxlength="6" autocomplete="off" />
            @if (confirmacao.controls.senhaTransacao.hasError('required')) {
              <mat-error>Informe a senha de transação.</mat-error>
            }
          </mat-form-field>
          @if (enviando()) {
            <mat-progress-bar mode="indeterminate" />
          }
          @if (erro()) {
            <p class="erro" role="alert">{{ erro() }}</p>
          }
          <div class="acoes">
            <button mat-button matStepperPrevious type="button" [disabled]="enviando()">Voltar</button>
            <button mat-flat-button type="submit" [disabled]="enviando()">{{ chave() ? 'Tentar de novo' : 'Confirmar transferência' }}</button>
          </div>
        </form>
      </mat-step>

      <mat-step label="Comprovante">
        @if (comprovante(); as feito) {
          <app-comprovante titulo="Transferência realizada" [subtitulo]="'Favorecido: ' + feito.favorecido" [linhas]="linhas(feito)">
            <a mat-button [routerLink]="['/contas', feito.origem.id, 'extrato']">Ver extrato</a>
            <button mat-flat-button type="button" (click)="novaTransferencia()">Nova transferência</button>
          </app-comprovante>
        }
      </mat-step>
    </mat-stepper>
  `,
  styles: `
    .formulario { display: flex; flex-direction: column; gap: 12px; max-width: 520px; padding-top: 8px; }
    fieldset { border: 1px solid var(--mat-sys-outline-variant); border-radius: 8px; padding: 12px 16px; display: flex; flex-direction: column; gap: 12px; }
    legend { padding: 0 4px; color: var(--mat-sys-on-surface-variant); }
    .linha { display: flex; gap: 12px; flex-wrap: wrap; }
    .agencia { width: 120px; }
    .numero-conta { flex: 1; min-width: 160px; }
    .resumo p { margin: 4px 0; }
    .acoes { display: flex; justify-content: flex-end; gap: 8px; }
    .erro { color: var(--mat-sys-error); margin: 0; }
    .alerta { color: var(--mat-sys-error); }
  `,
})
export class Transferencia {
  private readonly api = inject(ContasApi);
  private readonly moeda = inject(CurrencyPipe);
  private readonly data = inject(DatePipe);
  private readonly formatoConta = inject(ContaPipe);
  private readonly formularios = inject(NonNullableFormBuilder);
  private readonly passos = viewChild.required<MatStepper>('passos');

  protected readonly contas = rxResource({ stream: () => this.api.minhas() });

  protected readonly dados = this.formularios.group({
    contaOrigemId: [null as number | null, Validators.required],
    agenciaDestino: ['', [Validators.required, Validators.pattern(/^\d{4}$/)]],
    contaDestino: ['', [Validators.required, Validators.pattern(/^\d{1,10}-\d$/)]],
    cpfDestino: ['', [Validators.required, validadorCpf]],
    valor: [null as number | null, [Validators.required, Validators.min(0.01)]],
  });
  protected readonly confirmacao = this.formularios.group({
    senhaTransacao: ['', Validators.required],
  });

  /** Chave de idempotência da transferência em andamento (nula até a primeira confirmação). */
  protected readonly chave = signal<string | null>(null);
  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);
  protected readonly comprovante = signal<ComprovanteTransferencia | null>(null);

  private readonly origemId = signal<number | null>(null);
  protected readonly origem = computed<Conta | undefined>(() =>
    (this.contas.value() ?? []).find((conta) => conta.id === this.origemId()),
  );

  constructor() {
    // Qualquer mudança nos dados é outra transferência: descarta a chave de idempotência.
    this.dados.valueChanges.pipe(takeUntilDestroyed()).subscribe((valor) => {
      this.chave.set(null);
      this.origemId.set(valor.contaOrigemId ?? null);
    });
  }

  protected confirmar(): void {
    if (this.confirmacao.invalid || this.dados.invalid) {
      this.confirmacao.markAllAsTouched();
      return;
    }
    const chave = this.chave() ?? crypto.randomUUID();
    this.chave.set(chave);
    const d = this.dados.getRawValue();

    this.enviando.set(true);
    this.erro.set(null);
    this.api
      .transferir(
        {
          contaOrigemId: d.contaOrigemId!,
          agenciaDestino: d.agenciaDestino,
          contaDestino: d.contaDestino,
          cpfDestino: digitosDoCpf(d.cpfDestino),
          valor: d.valor!,
          senhaTransacao: this.confirmacao.getRawValue().senhaTransacao,
        },
        chave,
      )
      .subscribe({
        next: (comprovante) => {
          this.comprovante.set(comprovante);
          this.enviando.set(false);
          this.contas.reload();
          this.passos().next();
        },
        error: (erro: unknown) => {
          this.enviando.set(false);
          this.confirmacao.reset();
          this.erro.set(mensagemDeErro(erro));
          // Erro de regra (destino, saldo...) não gravou nada: a chave pode ir embora.
          // Erro de rede/servidor fica com a chave, para o "Tentar de novo" não duplicar.
          if (codigoDoErro(erro)) {
            this.chave.set(null);
          }
        },
      });
  }

  protected novaTransferencia(): void {
    this.comprovante.set(null);
    this.chave.set(null);
    this.erro.set(null);
    this.dados.reset();
    this.confirmacao.reset();
    this.passos().reset();
  }

  protected linhas(c: ComprovanteTransferencia): LinhaDeComprovante[] {
    return [
      { rotulo: 'Valor', valor: this.moeda.transform(c.valor) ?? '' },
      { rotulo: 'Origem', valor: this.formatoConta.transform(c.origem) },
      { rotulo: 'Destino', valor: this.formatoConta.transform(c.destino) },
      { rotulo: 'Saldo da origem após', valor: this.moeda.transform(c.saldoOrigemApos) ?? '' },
      { rotulo: 'Data', valor: this.data.transform(c.ocorridoEm, 'dd/MM/yyyy HH:mm:ss') ?? '' },
      { rotulo: 'Número da transferência', valor: String(c.id) },
    ];
  }
}
