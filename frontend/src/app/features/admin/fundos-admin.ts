import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AdminApi } from '../../core/api/admin-api';
import { Fundo } from '../../core/api/modelos';
import { Notificacoes } from '../../core/erros/notificacoes';
import { mensagemDeErro } from '../../core/erros/problemas';

/**
 * Fundos: cadastro, cota do dia e desativação. A cota informada vale na hora para aplicações,
 * resgates e carteiras, e vai para o histórico do fundo (decisão 8 do plano).
 */
@Component({
  selector: 'app-fundos-admin',
  imports: [ReactiveFormsModule, FormsModule, CurrencyPipe, DecimalPipe, MatTableModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatTooltipModule, MatProgressBarModule],
  template: `
    <h1>Fundos</h1>
    @if (fundos.isLoading()) {
      <mat-progress-bar mode="indeterminate" />
    } @else if (fundos.error()) {
      <p class="erro" role="alert">{{ erro() }}</p>
    }
    <div class="tabela">
      <table mat-table [dataSource]="fundos.value() ?? []">
        <ng-container matColumnDef="nome">
          <th mat-header-cell *matHeaderCellDef>Fundo</th>
          <td mat-cell *matCellDef="let f" [class.inativo]="!f.ativo">{{ f.nome }}{{ f.ativo ? '' : ' (inativo)' }}</td>
        </ng-container>
        <ng-container matColumnDef="minimo">
          <th mat-header-cell *matHeaderCellDef class="numero">Aplicação mínima</th>
          <td mat-cell *matCellDef="let f" class="numero">{{ f.aplicacaoMinima | currency }}</td>
        </ng-container>
        <ng-container matColumnDef="cota">
          <th mat-header-cell *matHeaderCellDef class="numero">Cota atual</th>
          <td mat-cell *matCellDef="let f" class="numero">{{ f.cotaAtual | number: '1.2-8' }}</td>
        </ng-container>
        <ng-container matColumnDef="novaCota">
          <th mat-header-cell *matHeaderCellDef>Cota de hoje</th>
          <td mat-cell *matCellDef="let f">
            <div class="nova-cota">
              <input class="campo-cota" type="number" step="0.00000001" min="0.00000001" [attr.aria-label]="'Nova cota de ' + f.nome"
                     [(ngModel)]="novasCotas[f.id]" [ngModelOptions]="{ standalone: true }" />
              <button mat-icon-button type="button" matTooltip="Salvar cota" [attr.aria-label]="'Salvar cota de ' + f.nome"
                      [disabled]="!novasCotas[f.id] || salvando() === f.id" (click)="atualizarCota(f)">
                <mat-icon>save</mat-icon>
              </button>
            </div>
          </td>
        </ng-container>
        <ng-container matColumnDef="acoes">
          <th mat-header-cell *matHeaderCellDef></th>
          <td mat-cell *matCellDef="let f">
            @if (f.ativo) {
              <button mat-button type="button" (click)="desativar(f)">Desativar</button>
            }
          </td>
        </ng-container>
        <tr mat-header-row *matHeaderRowDef="colunas"></tr>
        <tr mat-row *matRowDef="let linha; columns: colunas"></tr>
      </table>
    </div>

    <h2>Novo fundo</h2>
    <form [formGroup]="formulario" (ngSubmit)="criar()" class="formulario">
      <mat-form-field class="nome">
        <mat-label>Nome</mat-label>
        <input matInput formControlName="nome" />
        @if (formulario.controls.nome.hasError('required')) { <mat-error>Informe o nome.</mat-error> }
      </mat-form-field>
      <mat-form-field>
        <mat-label>Cota inicial</mat-label>
        <input matInput type="number" formControlName="cotaInicial" step="0.00000001" min="0.00000001" />
        @if (formulario.controls.cotaInicial.invalid) { <mat-error>Maior que zero.</mat-error> }
      </mat-form-field>
      <mat-form-field>
        <mat-label>Aplicação mínima</mat-label>
        <span matTextPrefix>R$&nbsp;</span>
        <input matInput type="number" formControlName="aplicacaoMinima" step="0.01" min="0" />
      </mat-form-field>
      <button mat-flat-button type="submit" [disabled]="criando()">Cadastrar</button>
    </form>
  `,
  styles: `
    .tabela { overflow-x: auto; }
    table { width: 100%; }
    .numero { text-align: right; white-space: nowrap; }
    .inativo { color: var(--mat-sys-on-surface-variant); }
    .nova-cota { display: flex; align-items: center; gap: 4px; }
    .campo-cota { width: 120px; padding: 6px 8px; font: inherit; border: 1px solid var(--mat-sys-outline); border-radius: 4px;
                  background: transparent; color: inherit; }
    .formulario { display: flex; flex-wrap: wrap; align-items: baseline; gap: 12px; }
    .nome { min-width: 260px; }
    .erro { color: var(--mat-sys-error); }
  `,
})
export class FundosAdmin {
  private readonly api = inject(AdminApi);
  private readonly notificacoes = inject(Notificacoes);

  protected readonly colunas = ['nome', 'minimo', 'cota', 'novaCota', 'acoes'];
  protected readonly fundos = rxResource({ stream: () => this.api.fundos() });
  protected readonly erro = computed(() => mensagemDeErro(this.fundos.error()));
  protected readonly salvando = signal<number | null>(null);
  protected readonly criando = signal(false);
  protected novasCotas: Record<number, number | null> = {};

  protected readonly formulario = inject(NonNullableFormBuilder).group({
    nome: ['', [Validators.required, Validators.maxLength(100)]],
    cotaInicial: [null as number | null, [Validators.required, Validators.min(0.00000001)]],
    aplicacaoMinima: [0, [Validators.required, Validators.min(0)]],
  });

  protected atualizarCota(fundo: Fundo): void {
    const cota = this.novasCotas[fundo.id];
    if (!cota) {
      return;
    }
    this.salvando.set(fundo.id);
    this.api.atualizarCota(fundo.id, cota).subscribe({
      next: () => {
        this.salvando.set(null);
        this.novasCotas[fundo.id] = null;
        this.notificacoes.sucesso(`Cota de ${fundo.nome} atualizada.`);
        this.fundos.reload();
      },
      error: (erro: unknown) => {
        this.salvando.set(null);
        this.notificacoes.erro(erro);
      },
    });
  }

  protected desativar(fundo: Fundo): void {
    if (!confirm(`Desativar ${fundo.nome}? Novas aplicações serão bloqueadas; resgates continuam permitidos.`)) {
      return;
    }
    this.api.desativarFundo(fundo.id).subscribe({
      next: () => {
        this.notificacoes.sucesso(`${fundo.nome} desativado.`);
        this.fundos.reload();
      },
      error: (erro: unknown) => this.notificacoes.erro(erro),
    });
  }

  protected criar(): void {
    if (this.formulario.invalid) {
      this.formulario.markAllAsTouched();
      return;
    }
    const { nome, cotaInicial, aplicacaoMinima } = this.formulario.getRawValue();
    this.criando.set(true);
    this.api.criarFundo(nome.trim(), cotaInicial!, aplicacaoMinima).subscribe({
      next: (fundo) => {
        this.criando.set(false);
        this.formulario.reset();
        this.notificacoes.sucesso(`Fundo ${fundo.nome} cadastrado.`);
        this.fundos.reload();
      },
      error: (erro: unknown) => {
        this.criando.set(false);
        this.notificacoes.erro(erro);
      },
    });
  }
}
