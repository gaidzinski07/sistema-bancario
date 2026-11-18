import { Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { AdminApi } from '../../core/api/admin-api';
import { Notificacoes } from '../../core/erros/notificacoes';
import { aplicarErrosDeCampo, mensagemDeErro } from '../../core/erros/problemas';

@Component({
  selector: 'app-agencias',
  imports: [ReactiveFormsModule, MatTableModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatProgressBarModule],
  template: `
    <h1>Agências</h1>
    @if (agencias.isLoading()) {
      <mat-progress-bar mode="indeterminate" />
    } @else if (agencias.error()) {
      <p class="erro" role="alert">{{ erro() }}</p>
    }
    <table mat-table [dataSource]="agencias.value() ?? []">
      <ng-container matColumnDef="codigo">
        <th mat-header-cell *matHeaderCellDef>Código</th>
        <td mat-cell *matCellDef="let a">{{ a.codigo }}</td>
      </ng-container>
      <ng-container matColumnDef="nome">
        <th mat-header-cell *matHeaderCellDef>Nome</th>
        <td mat-cell *matCellDef="let a">{{ a.nome }}</td>
      </ng-container>
      <ng-container matColumnDef="cidade">
        <th mat-header-cell *matHeaderCellDef>Cidade</th>
        <td mat-cell *matCellDef="let a">{{ a.endereco?.cidade ?? '—' }}{{ a.endereco?.uf ? ' - ' + a.endereco.uf : '' }}</td>
      </ng-container>
      <tr mat-header-row *matHeaderRowDef="colunas"></tr>
      <tr mat-row *matRowDef="let linha; columns: colunas"></tr>
    </table>

    <h2>Nova agência</h2>
    <form [formGroup]="formulario" (ngSubmit)="criar()" class="formulario">
      <mat-form-field class="codigo">
        <mat-label>Código</mat-label>
        <input matInput formControlName="codigo" inputmode="numeric" maxlength="4" placeholder="0004" />
        @if (formulario.controls.codigo.invalid) { <mat-error>4 dígitos.</mat-error> }
      </mat-form-field>
      <mat-form-field class="nome">
        <mat-label>Nome</mat-label>
        <input matInput formControlName="nome" />
        @if (formulario.controls.nome.hasError('required')) { <mat-error>Informe o nome.</mat-error> }
      </mat-form-field>
      <mat-form-field>
        <mat-label>Cidade</mat-label>
        <input matInput formControlName="cidade" />
      </mat-form-field>
      <mat-form-field class="uf">
        <mat-label>UF</mat-label>
        <input matInput formControlName="uf" maxlength="2" />
      </mat-form-field>
      <button mat-flat-button type="submit" [disabled]="enviando()">Cadastrar</button>
    </form>
  `,
  styles: `
    table { width: 100%; max-width: 720px; }
    .formulario { display: flex; flex-wrap: wrap; align-items: baseline; gap: 12px; }
    .codigo { width: 110px; }
    .uf { width: 80px; }
    .nome { min-width: 240px; }
    .erro { color: var(--mat-sys-error); }
  `,
})
export class Agencias {
  private readonly api = inject(AdminApi);
  private readonly notificacoes = inject(Notificacoes);

  protected readonly colunas = ['codigo', 'nome', 'cidade'];
  protected readonly agencias = rxResource({ stream: () => this.api.agencias() });
  protected readonly erro = computed(() => mensagemDeErro(this.agencias.error()));
  protected readonly enviando = signal(false);

  protected readonly formulario = inject(NonNullableFormBuilder).group({
    codigo: ['', [Validators.required, Validators.pattern(/^\d{4}$/)]],
    nome: ['', [Validators.required, Validators.maxLength(100)]],
    cidade: [''],
    uf: ['', Validators.pattern(/^[A-Za-z]{2}$/)],
  });

  protected criar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const { codigo, nome, cidade, uf } = this.formulario.getRawValue();
    const endereco = cidade || uf ? { cidade: cidade || null, uf: uf || null } : null;
    this.enviando.set(true);
    this.api.criarAgencia(codigo, nome, endereco).subscribe({
      next: (agencia) => {
        this.enviando.set(false);
        this.formulario.reset();
        this.notificacoes.sucesso(`Agência ${agencia.codigo} cadastrada.`);
        this.agencias.reload();
      },
      error: (erro: unknown) => {
        this.enviando.set(false);
        if (!aplicarErrosDeCampo(this.formulario, erro)) {
          this.notificacoes.erro(erro);
        }
      },
    });
  }
}
