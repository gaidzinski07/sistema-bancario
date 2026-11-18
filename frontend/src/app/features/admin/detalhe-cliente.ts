import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, input, numberAttribute, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { AdminApi } from '../../core/api/admin-api';
import { TipoConta } from '../../core/api/modelos';
import { Notificacoes } from '../../core/erros/notificacoes';
import { mensagemDeErro } from '../../core/erros/problemas';
import { ContaPipe, TipoContaPipe } from '../../shared/formatos';

/** Cliente: dados, contas e abertura de conta (no legado a tela existia, mas não estava implementada). */
@Component({
  selector: 'app-detalhe-cliente',
  imports: [ReactiveFormsModule, RouterLink, DatePipe, CurrencyPipe, MatCardModule, MatTableModule, MatFormFieldModule,
    MatSelectModule, MatButtonModule, MatIconModule, MatProgressBarModule, ContaPipe, TipoContaPipe],
  template: `
    <a mat-button routerLink="/admin/clientes"><mat-icon>arrow_back</mat-icon> Clientes</a>
    @if (cliente.error()) {
      <p class="erro" role="alert">{{ erroCliente() }}</p>
    }
    @if (cliente.value(); as c) {
      <h1>{{ c.nome }}</h1>
      <mat-card appearance="outlined">
        <mat-card-content>
          <dl class="dados">
            <dt>CPF</dt><dd>{{ c.cpf }}</dd>
            <dt>E-mail</dt><dd>{{ c.email ?? '—' }}</dd>
            <dt>Nascimento</dt><dd>{{ c.dataNascimento ? (c.dataNascimento | date: 'dd/MM/yyyy') : '—' }}</dd>
            <dt>Segmento</dt><dd>{{ c.segmento === 'INVESTIDOR' ? 'Investidor' : 'Correntista' }}</dd>
            <dt>Endereço</dt><dd>{{ endereco() || '—' }}</dd>
            <dt>Cliente desde</dt><dd>{{ c.criadoEm | date: 'dd/MM/yyyy' }}</dd>
          </dl>
        </mat-card-content>
      </mat-card>

      <h2>Contas</h2>
      @if (contas.value(); as lista) {
        @if (lista.length) {
          <table mat-table [dataSource]="lista">
            <ng-container matColumnDef="conta">
              <th mat-header-cell *matHeaderCellDef>Conta</th>
              <td mat-cell *matCellDef="let conta">{{ conta | conta }}</td>
            </ng-container>
            <ng-container matColumnDef="tipo">
              <th mat-header-cell *matHeaderCellDef>Tipo</th>
              <td mat-cell *matCellDef="let conta">{{ conta.tipo | tipoConta }}</td>
            </ng-container>
            <ng-container matColumnDef="saldo">
              <th mat-header-cell *matHeaderCellDef class="numero">Saldo</th>
              <td mat-cell *matCellDef="let conta" class="numero">{{ conta.saldo | currency }}</td>
            </ng-container>
            <tr mat-header-row *matHeaderRowDef="colunas"></tr>
            <tr mat-row *matRowDef="let linha; columns: colunas"></tr>
          </table>
        } @else {
          <p>Nenhuma conta aberta.</p>
        }
      }

      <h3>Abrir conta</h3>
      @if (abrindo()) {
        <mat-progress-bar mode="indeterminate" />
      }
      <form [formGroup]="novaConta" (ngSubmit)="abrirConta()" class="nova-conta">
        <mat-form-field>
          <mat-label>Agência</mat-label>
          <mat-select formControlName="agenciaId">
            @for (agencia of agencias.value() ?? []; track agencia.id) {
              <mat-option [value]="agencia.id">{{ agencia.codigo }} · {{ agencia.nome }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        <mat-form-field>
          <mat-label>Tipo</mat-label>
          <mat-select formControlName="tipo">
            <mat-option value="CORRENTE">Conta corrente</mat-option>
            <mat-option value="POUPANCA">Poupança</mat-option>
          </mat-select>
        </mat-form-field>
        <button mat-flat-button type="submit" [disabled]="abrindo()">Abrir conta</button>
      </form>
    }
  `,
  styles: `
    .dados { display: grid; grid-template-columns: max-content 1fr; gap: 8px 24px; margin: 0; }
    dt { color: var(--mat-sys-on-surface-variant); }
    dd { margin: 0; }
    table { width: 100%; max-width: 640px; }
    .numero { text-align: right; }
    .nova-conta { display: flex; flex-wrap: wrap; align-items: baseline; gap: 12px; }
    .erro { color: var(--mat-sys-error); }
  `,
})
export class DetalheCliente {
  private readonly api = inject(AdminApi);
  private readonly notificacoes = inject(Notificacoes);

  /** Parâmetro da rota `admin/clientes/:clienteId`. */
  readonly clienteId = input.required({ transform: numberAttribute });

  protected readonly colunas = ['conta', 'tipo', 'saldo'];
  protected readonly cliente = rxResource({ params: () => this.clienteId(), stream: ({ params }) => this.api.cliente(params) });
  protected readonly contas = rxResource({ params: () => this.clienteId(), stream: ({ params }) => this.api.contasDoCliente(params) });
  protected readonly agencias = rxResource({ stream: () => this.api.agencias() });
  protected readonly erroCliente = computed(() => mensagemDeErro(this.cliente.error()));
  protected readonly endereco = computed(() => {
    const e = this.cliente.value()?.endereco;
    if (!e) {
      return '';
    }
    const rua = [e.logradouro, e.numero, e.complemento].filter(Boolean).join(', ');
    return [rua, e.bairro, [e.cidade, e.uf].filter(Boolean).join(' - '), e.cep].filter(Boolean).join(' · ');
  });

  protected readonly abrindo = signal(false);
  protected readonly novaConta = inject(NonNullableFormBuilder).group({
    agenciaId: [null as number | null, Validators.required],
    tipo: ['CORRENTE' as TipoConta, Validators.required],
  });

  protected abrirConta(): void {
    if (this.novaConta.invalid) {
      this.novaConta.markAllAsTouched();
      return;
    }
    const { agenciaId, tipo } = this.novaConta.getRawValue();
    this.abrindo.set(true);
    this.api.abrirConta(this.clienteId(), agenciaId!, tipo).subscribe({
      next: (conta) => {
        this.abrindo.set(false);
        this.notificacoes.sucesso(`Conta ${conta.numero}-${conta.digito} aberta na agência ${conta.agencia}.`);
        this.contas.reload();
      },
      error: (erro: unknown) => {
        this.abrindo.set(false);
        this.notificacoes.erro(erro);
      },
    });
  }
}
