import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { rxResource } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { AdminApi } from '../../core/api/admin-api';
import { mensagemDeErro } from '../../core/erros/problemas';

@Component({
  selector: 'app-clientes',
  imports: [DatePipe, RouterLink, MatTableModule, MatPaginatorModule, MatButtonModule, MatIconModule, MatProgressBarModule],
  template: `
    <div class="cabecalho">
      <h1>Clientes</h1>
      <a mat-flat-button routerLink="/admin/clientes/novo"><mat-icon>person_add</mat-icon> Novo cliente</a>
    </div>
    @if (clientes.isLoading()) {
      <mat-progress-bar mode="indeterminate" />
    }
    @if (clientes.error()) {
      <p class="erro" role="alert">{{ erro() }}</p>
    }
    @if (clientes.value(); as pagina) {
      <div class="tabela">
        <table mat-table [dataSource]="pagina.conteudo">
          <ng-container matColumnDef="nome">
            <th mat-header-cell *matHeaderCellDef>Nome</th>
            <td mat-cell *matCellDef="let c"><a [routerLink]="['/admin/clientes', c.id]">{{ c.nome }}</a></td>
          </ng-container>
          <ng-container matColumnDef="cpf">
            <th mat-header-cell *matHeaderCellDef>CPF</th>
            <td mat-cell *matCellDef="let c">{{ c.cpf }}</td>
          </ng-container>
          <ng-container matColumnDef="segmento">
            <th mat-header-cell *matHeaderCellDef>Segmento</th>
            <td mat-cell *matCellDef="let c">{{ c.segmento === 'INVESTIDOR' ? 'Investidor' : 'Correntista' }}</td>
          </ng-container>
          <ng-container matColumnDef="desde">
            <th mat-header-cell *matHeaderCellDef>Cliente desde</th>
            <td mat-cell *matCellDef="let c">{{ c.criadoEm | date: 'dd/MM/yyyy' }}</td>
          </ng-container>
          <tr mat-header-row *matHeaderRowDef="colunas"></tr>
          <tr mat-row *matRowDef="let linha; columns: colunas"></tr>
        </table>
      </div>
      <mat-paginator [length]="pagina.totalElementos" [pageIndex]="pagina.pagina" [pageSize]="pagina.tamanho"
                     [pageSizeOptions]="[10, 20, 50]" (page)="mudarPagina($event)" aria-label="Páginas de clientes" />
    }
  `,
  styles: `
    .cabecalho { display: flex; align-items: center; justify-content: space-between; gap: 16px; flex-wrap: wrap; }
    .tabela { overflow-x: auto; }
    table { width: 100%; }
    .mat-column-cpf, .mat-column-desde { white-space: nowrap; }
    .erro { color: var(--mat-sys-error); }
  `,
})
export class Clientes {
  private readonly api = inject(AdminApi);
  private readonly pagina = signal(0);
  private readonly tamanho = signal(20);

  protected readonly colunas = ['nome', 'cpf', 'segmento', 'desde'];
  protected readonly clientes = rxResource({
    params: () => ({ pagina: this.pagina(), tamanho: this.tamanho() }),
    stream: ({ params }) => this.api.clientes(params.pagina, params.tamanho),
  });
  protected readonly erro = computed(() => mensagemDeErro(this.clientes.error()));

  protected mudarPagina(evento: PageEvent): void {
    this.tamanho.set(evento.pageSize);
    this.pagina.set(evento.pageIndex);
  }
}
