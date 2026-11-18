import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { map } from 'rxjs';
import { Sessao } from '../auth/sessao';

interface ItemDeMenu {
  rota: string;
  rotulo: string;
  icone: string;
}

const MENU_CLIENTE: ItemDeMenu[] = [
  { rota: '/inicio', rotulo: 'Início', icone: 'home' },
  { rota: '/operacoes/transferencia', rotulo: 'Transferir', icone: 'swap_horiz' },
  { rota: '/operacoes/deposito', rotulo: 'Depositar', icone: 'savings' },
  { rota: '/operacoes/saque', rotulo: 'Sacar', icone: 'payments' },
  { rota: '/investimentos', rotulo: 'Investimentos', icone: 'trending_up' },
  { rota: '/senha-transacao', rotulo: 'Senha de transação', icone: 'password' },
];

const MENU_ADMIN: ItemDeMenu[] = [
  { rota: '/admin/clientes', rotulo: 'Clientes', icone: 'group' },
  { rota: '/admin/agencias', rotulo: 'Agências', icone: 'account_balance' },
  { rota: '/admin/fundos', rotulo: 'Fundos', icone: 'query_stats' },
  { rota: '/admin/administradores/novo', rotulo: 'Novo administrador', icone: 'admin_panel_settings' },
];

@Component({
  selector: 'app-shell',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
  template: `
    <mat-toolbar class="barra">
      @if (telaPequena()) {
        <button mat-icon-button aria-label="Abrir menu" (click)="menu.toggle()">
          <mat-icon>menu</mat-icon>
        </button>
      }
      <a class="marca" routerLink="/inicio">Banco UFF</a>
      <span class="espaco"></span>
      @if (sessao.usuario(); as usuario) {
        <span class="usuario">{{ usuario.nome }}</span>
      }
      <button mat-icon-button matTooltip="Sair" aria-label="Sair" (click)="sessao.sair()">
        <mat-icon>logout</mat-icon>
      </button>
    </mat-toolbar>

    <mat-sidenav-container class="conteudo">
      <mat-sidenav #menu [mode]="telaPequena() ? 'over' : 'side'" [opened]="!telaPequena()">
        <mat-nav-list>
          @if (itensCliente().length) {
            <h3 matSubheader>Minha conta</h3>
            @for (item of itensCliente(); track item.rota) {
              <a mat-list-item [routerLink]="item.rota" routerLinkActive #ativo="routerLinkActive"
                 [activated]="ativo.isActive" (click)="fecharSeMovel(menu)">
                <mat-icon matListItemIcon>{{ item.icone }}</mat-icon>
                <span matListItemTitle>{{ item.rotulo }}</span>
              </a>
            }
          }
          @if (itensAdmin().length) {
            <h3 matSubheader>Administração</h3>
            @for (item of itensAdmin(); track item.rota) {
              <a mat-list-item [routerLink]="item.rota" routerLinkActive #ativo="routerLinkActive"
                 [activated]="ativo.isActive" (click)="fecharSeMovel(menu)">
                <mat-icon matListItemIcon>{{ item.icone }}</mat-icon>
                <span matListItemTitle>{{ item.rotulo }}</span>
              </a>
            }
          }
        </mat-nav-list>
      </mat-sidenav>
      <mat-sidenav-content>
        <main class="pagina">
          <router-outlet />
        </main>
      </mat-sidenav-content>
    </mat-sidenav-container>
  `,
  styles: `
    :host { display: flex; flex-direction: column; height: 100%; }
    .barra { position: sticky; top: 0; z-index: 2; background: var(--mat-sys-primary); color: var(--mat-sys-on-primary); }
    .barra button { color: inherit; }
    .marca { color: inherit; text-decoration: none; font-weight: 500; }
    .espaco { flex: 1; }
    .usuario { font: var(--mat-sys-body-medium); margin-right: 8px; }
    .conteudo { flex: 1; }
    mat-sidenav { width: 248px; }
    .pagina { padding: 24px; max-width: 1100px; margin: 0 auto; }
    @media (max-width: 599px) {
      .usuario { display: none; }
      .pagina { padding: 16px; }
    }
  `,
})
export class Shell {
  protected readonly sessao = inject(Sessao);

  protected readonly telaPequena = toSignal(
    inject(BreakpointObserver).observe([Breakpoints.XSmall, Breakpoints.Small]).pipe(map((estado) => estado.matches)),
    { initialValue: false },
  );
  protected readonly itensCliente = computed(() => (this.sessao.ehCliente() ? MENU_CLIENTE : []));
  protected readonly itensAdmin = computed(() => (this.sessao.ehAdmin() ? MENU_ADMIN : []));

  protected fecharSeMovel(menu: { close: () => void }): void {
    if (this.telaPequena()) {
      menu.close();
    }
  }
}
