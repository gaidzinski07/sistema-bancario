import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, input, numberAttribute, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { ContasApi } from '../../core/api/contas-api';
import { mensagemDeErro } from '../../core/erros/problemas';
import { ContaPipe, TipoLancamentoPipe, dataIso, valorComSinal } from '../../shared/formatos';

interface Periodo {
  de: Date;
  ate: Date;
}

const ATALHOS_DE_PERIODO = [7, 30, 90] as const;

/**
 * Extrato da conta por período. Saldos e totais vêm do backend e se referem ao período inteiro;
 * os lançamentos são paginados no servidor (a tela não carrega o histórico todo).
 */
@Component({
  selector: 'app-extrato',
  imports: [
    CurrencyPipe,
    DatePipe,
    RouterLink,
    ReactiveFormsModule,
    MatCardModule,
    MatTableModule,
    MatPaginatorModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatIconModule,
    MatProgressBarModule,
    ContaPipe,
    TipoLancamentoPipe,
  ],
  template: `
    <a mat-button routerLink="/inicio"><mat-icon>arrow_back</mat-icon> Início</a>
    <h1>Extrato</h1>
    @if (extrato.value(); as dados) {
      <p class="conta">{{ dados.conta | conta }}</p>
    }

    <div class="filtros">
      <mat-button-toggle-group [value]="atalhoAtivo()" (change)="usarAtalho($event.value)" aria-label="Período rápido">
        @for (dias of atalhos; track dias) {
          <mat-button-toggle [value]="dias">{{ dias }} dias</mat-button-toggle>
        }
      </mat-button-toggle-group>

      <mat-form-field class="periodo">
        <mat-label>Período</mat-label>
        <mat-date-range-input [formGroup]="filtro" [rangePicker]="seletor" [max]="hoje">
          <input matStartDate formControlName="de" placeholder="Início" />
          <input matEndDate formControlName="ate" placeholder="Fim" (dateChange)="aplicarPeriodo()" />
        </mat-date-range-input>
        <mat-datepicker-toggle matIconSuffix [for]="seletor" />
        <mat-date-range-picker #seletor />
      </mat-form-field>
    </div>

    @if (extrato.isLoading()) {
      <mat-progress-bar mode="indeterminate" />
    }
    @if (extrato.error()) {
      <p class="erro" role="alert">{{ erro() }}</p>
    }

    @if (extrato.value(); as dados) {
      <div class="resumo">
        <mat-card appearance="outlined"><mat-card-content>
          <span class="rotulo">Saldo em {{ dados.de | date: 'dd/MM/yyyy' }}</span>
          <strong>{{ dados.saldoInicial | currency }}</strong>
        </mat-card-content></mat-card>
        <mat-card appearance="outlined"><mat-card-content>
          <span class="rotulo">Entradas</span>
          <strong class="credito">{{ dados.totalCreditos | currency }}</strong>
        </mat-card-content></mat-card>
        <mat-card appearance="outlined"><mat-card-content>
          <span class="rotulo">Saídas</span>
          <strong class="debito">{{ -dados.totalDebitos | currency }}</strong>
        </mat-card-content></mat-card>
        <mat-card appearance="outlined"><mat-card-content>
          <span class="rotulo">Saldo em {{ dados.ate | date: 'dd/MM/yyyy' }}</span>
          <strong>{{ dados.saldoFinal | currency }}</strong>
        </mat-card-content></mat-card>
      </div>

      <div class="tabela">
        <table mat-table [dataSource]="dados.lancamentos.conteudo">
          <ng-container matColumnDef="data">
            <th mat-header-cell *matHeaderCellDef>Data</th>
            <td mat-cell *matCellDef="let l">{{ l.ocorridoEm | date: 'dd/MM/yyyy HH:mm' }}</td>
          </ng-container>
          <ng-container matColumnDef="descricao">
            <th mat-header-cell *matHeaderCellDef>Descrição</th>
            <td mat-cell *matCellDef="let l">
              <div>{{ l.tipo | tipoLancamento }}</div>
              @if (l.contraparte) {
                <div class="contraparte">{{ l.contraparte }}</div>
              }
            </td>
          </ng-container>
          <ng-container matColumnDef="valor">
            <th mat-header-cell *matHeaderCellDef class="numero">Valor</th>
            <td mat-cell *matCellDef="let l" class="numero" [class.credito]="l.natureza === 'CREDITO'" [class.debito]="l.natureza === 'DEBITO'">
              {{ comSinal(l.valor, l.natureza) | currency }}
            </td>
          </ng-container>
          <ng-container matColumnDef="saldo">
            <th mat-header-cell *matHeaderCellDef class="numero">Saldo</th>
            <td mat-cell *matCellDef="let l" class="numero">{{ l.saldoApos | currency }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="colunas"></tr>
          <tr mat-row *matRowDef="let linha; columns: colunas"></tr>
          <tr class="mat-row" *matNoDataRow>
            <td class="mat-cell vazio" [attr.colspan]="colunas.length">Nenhum lançamento no período.</td>
          </tr>
        </table>
      </div>
      <mat-paginator
        [length]="dados.lancamentos.totalElementos"
        [pageIndex]="dados.lancamentos.pagina"
        [pageSize]="dados.lancamentos.tamanho"
        [pageSizeOptions]="[10, 25, 50]"
        (page)="mudarPagina($event)"
        aria-label="Páginas do extrato" />
    }
  `,
  styles: `
    .conta { color: var(--mat-sys-on-surface-variant); margin-top: -8px; }
    .filtros { display: flex; flex-wrap: wrap; align-items: center; gap: 16px; margin: 16px 0; }
    .periodo { min-width: 280px; }
    .resumo { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 12px; margin-bottom: 16px; }
    .resumo mat-card-content { display: flex; flex-direction: column; gap: 4px; }
    .rotulo { font: var(--mat-sys-label-medium); color: var(--mat-sys-on-surface-variant); }
    .resumo strong { font: var(--mat-sys-title-large); }
    .tabela { overflow-x: auto; }
    table { width: 100%; }
    .numero { text-align: right; white-space: nowrap; }
    .credito { color: #1b6e2e; }
    .debito { color: var(--mat-sys-error); }
    .contraparte { font: var(--mat-sys-body-small); color: var(--mat-sys-on-surface-variant); }
    .vazio { padding: 16px; text-align: center; }
    .erro { color: var(--mat-sys-error); }
  `,
})
export class Extrato {
  private readonly api = inject(ContasApi);

  /** Parâmetro da rota `contas/:contaId/extrato`. */
  readonly contaId = input.required({ transform: numberAttribute });

  protected readonly atalhos = ATALHOS_DE_PERIODO;
  protected readonly colunas = ['data', 'descricao', 'valor', 'saldo'];
  protected readonly hoje = new Date();

  private readonly periodo = signal<Periodo>(ultimosDias(30));
  private readonly pagina = signal(0);
  private readonly tamanho = signal(25);
  protected readonly atalhoAtivo = signal<number | null>(30);

  protected readonly filtro = new FormGroup({
    de: new FormControl<Date | null>(this.periodo().de),
    ate: new FormControl<Date | null>(this.periodo().ate),
  });

  protected readonly extrato = rxResource({
    params: () => ({ conta: this.contaId(), periodo: this.periodo(), pagina: this.pagina(), tamanho: this.tamanho() }),
    stream: ({ params }) =>
      this.api.extrato(params.conta, {
        de: dataIso(params.periodo.de),
        ate: dataIso(params.periodo.ate),
        pagina: params.pagina,
        tamanho: params.tamanho,
      }),
  });

  protected readonly erro = computed(() => mensagemDeErro(this.extrato.error()));
  protected readonly comSinal = valorComSinal;

  protected usarAtalho(dias: number): void {
    const periodo = ultimosDias(dias);
    this.atalhoAtivo.set(dias);
    this.filtro.setValue({ de: periodo.de, ate: periodo.ate }, { emitEvent: false });
    this.pagina.set(0);
    this.periodo.set(periodo);
  }

  protected aplicarPeriodo(): void {
    const { de, ate } = this.filtro.getRawValue();
    if (de && ate) {
      this.atalhoAtivo.set(null);
      this.pagina.set(0);
      this.periodo.set({ de, ate });
    }
  }

  protected mudarPagina(evento: PageEvent): void {
    this.tamanho.set(evento.pageSize);
    this.pagina.set(evento.pageIndex);
  }
}

function ultimosDias(dias: number): Periodo {
  const ate = new Date();
  const de = new Date();
  de.setDate(ate.getDate() - (dias - 1));
  return { de, ate };
}
