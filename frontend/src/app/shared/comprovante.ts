import { Component, input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

export interface LinhaDeComprovante {
  rotulo: string;
  valor: string;
}

/** Comprovante de operação concluída (depósito, saque, transferência, aplicação, resgate). */
@Component({
  selector: 'app-comprovante',
  imports: [MatCardModule, MatIconModule],
  template: `
    <mat-card appearance="outlined" class="comprovante" role="status">
      <mat-card-header>
        <mat-icon mat-card-avatar class="ok">check_circle</mat-icon>
        <mat-card-title>{{ titulo() }}</mat-card-title>
        @if (subtitulo()) {
          <mat-card-subtitle>{{ subtitulo() }}</mat-card-subtitle>
        }
      </mat-card-header>
      <mat-card-content>
        <dl>
          @for (linha of linhas(); track linha.rotulo) {
            <div class="linha">
              <dt>{{ linha.rotulo }}</dt>
              <dd>{{ linha.valor }}</dd>
            </div>
          }
        </dl>
      </mat-card-content>
      <mat-card-actions>
        <ng-content />
      </mat-card-actions>
    </mat-card>
  `,
  styles: `
    .comprovante { max-width: 560px; }
    .ok { color: #1b6e2e; font-size: 40px; width: 40px; height: 40px; }
    dl { margin: 8px 0 0; }
    .linha { display: flex; justify-content: space-between; gap: 16px; padding: 8px 0;
             border-bottom: 1px solid var(--mat-sys-outline-variant); }
    dt { color: var(--mat-sys-on-surface-variant); }
    dd { margin: 0; text-align: right; font-weight: 500; }
  `,
})
export class Comprovante {
  readonly titulo = input.required<string>();
  readonly subtitulo = input<string>();
  readonly linhas = input.required<LinhaDeComprovante[]>();
}
