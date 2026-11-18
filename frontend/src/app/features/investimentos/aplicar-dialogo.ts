import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { InvestimentosApi } from '../../core/api/investimentos-api';
import { ComprovanteInvestimento, Conta, Fundo } from '../../core/api/modelos';
import { aplicarErrosDeCampo, mensagemDeErro } from '../../core/erros/problemas';
import { ContaPipe } from '../../shared/formatos';

export interface DadosAplicacao {
  fundo: Fundo;
  /** Só contas correntes: investimentos saem e voltam para a corrente. */
  contas: Conta[];
}

/** Aplicação num fundo. Fecha devolvendo o comprovante. */
@Component({
  selector: 'app-aplicar-dialogo',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatButtonModule,
    MatProgressBarModule, CurrencyPipe, DecimalPipe, ContaPipe],
  template: `
    <h2 mat-dialog-title>Aplicar em {{ dados.fundo.nome }}</h2>
    <mat-dialog-content>
      <p class="info">
        Cota atual {{ dados.fundo.cotaAtual | number: '1.2-8' }} · aplicação mínima {{ dados.fundo.aplicacaoMinima | currency }}
      </p>
      @if (enviando()) {
        <mat-progress-bar mode="indeterminate" />
      }
      <form [formGroup]="formulario" id="form-aplicar" (ngSubmit)="aplicar()" class="formulario">
        <mat-form-field>
          <mat-label>Conta corrente</mat-label>
          <mat-select formControlName="contaId">
            @for (conta of dados.contas; track conta.id) {
              <mat-option [value]="conta.id">{{ conta | conta }} · {{ conta.saldo | currency }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field>
          <mat-label>Valor</mat-label>
          <span matTextPrefix>R$&nbsp;</span>
          <input matInput type="number" formControlName="valor" [min]="dados.fundo.aplicacaoMinima" step="0.01" inputmode="decimal" />
          @if (formulario.controls.valor.hasError('min')) {
            <mat-error>Mínimo de {{ dados.fundo.aplicacaoMinima | currency }}.</mat-error>
          } @else if (formulario.controls.valor.hasError('required')) {
            <mat-error>Informe o valor.</mat-error>
          }
        </mat-form-field>
        <mat-form-field>
          <mat-label>Senha de transação</mat-label>
          <input matInput type="password" formControlName="senhaTransacao" inputmode="numeric" maxlength="6" autocomplete="off" />
          @if (formulario.controls.senhaTransacao.hasError('required')) {
            <mat-error>Informe a senha de transação.</mat-error>
          }
        </mat-form-field>
      </form>
      @if (erro()) {
        <p class="erro" role="alert">{{ erro() }}</p>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close type="button">Cancelar</button>
      <button mat-flat-button type="submit" form="form-aplicar" [disabled]="enviando()">Aplicar</button>
    </mat-dialog-actions>
  `,
  styles: `
    .info { color: var(--mat-sys-on-surface-variant); margin-top: 0; }
    .formulario { display: flex; flex-direction: column; gap: 12px; min-width: min(380px, 80vw); }
    .erro { color: var(--mat-sys-error); }
  `,
})
export class AplicarDialogo {
  protected readonly dados = inject<DadosAplicacao>(MAT_DIALOG_DATA);
  private readonly dialogo = inject(MatDialogRef<AplicarDialogo, ComprovanteInvestimento>);
  private readonly api = inject(InvestimentosApi);

  protected readonly enviando = signal(false);
  protected readonly erro = signal<string | null>(null);

  protected readonly formulario = inject(NonNullableFormBuilder).group({
    contaId: [this.dados.contas.length === 1 ? this.dados.contas[0].id : (null as number | null), Validators.required],
    valor: [null as number | null, [Validators.required, Validators.min(Math.max(this.dados.fundo.aplicacaoMinima, 0.01))]],
    senhaTransacao: ['', Validators.required],
  });

  protected aplicar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const { contaId, valor, senhaTransacao } = this.formulario.getRawValue();
    this.enviando.set(true);
    this.erro.set(null);
    this.api.aplicar(contaId!, this.dados.fundo.id, valor!, senhaTransacao).subscribe({
      next: (comprovante) => this.dialogo.close(comprovante),
      error: (erro: unknown) => {
        this.enviando.set(false);
        this.formulario.controls.senhaTransacao.reset();
        if (!aplicarErrosDeCampo(this.formulario, erro)) {
          this.erro.set(mensagemDeErro(erro));
        }
      },
    });
  }
}
