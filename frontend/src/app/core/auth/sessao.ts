import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, finalize, firstValueFrom, map, of, shareReplay, switchMap, tap, throwError } from 'rxjs';
import { AuthApi } from '../api/auth-api';
import { Papel, Tokens, Usuario } from '../api/modelos';

/** Chave do refresh token no sessionStorage (ver {@link Sessao}). */
export const CHAVE_REFRESH = 'banco.refreshToken';

/**
 * Sessão do usuário no navegador (autenticação JWT do backend).
 *
 * - **Access token**: só em memória (signal). Some ao recarregar a página — por isso o refresh.
 * - **Refresh token**: no `sessionStorage`, para sobreviver a um F5 na mesma aba e sumir ao
 *   fechá-la. É acessível a JavaScript (risco em caso de XSS); a alternativa mais segura seria um
 *   cookie HttpOnly, que a decisão de usar JWT sem cookies descartou. O impacto é limitado porque o
 *   backend rotaciona o token a cada uso e revoga a sessão inteira se um token usado reaparecer.
 * - **Renovação compartilhada**: o refresh token é de uso único; se dois pedidos receberem 401 ao
 *   mesmo tempo e cada um renovasse por conta própria, o segundo reapresentaria um token já usado e
 *   o backend derrubaria a sessão. Por isso todas as renovações simultâneas esperam a mesma chamada.
 */
@Injectable({ providedIn: 'root' })
export class Sessao {
  private readonly api = inject(AuthApi);
  private readonly router = inject(Router);

  private readonly token = signal<string | null>(null);
  private readonly usuarioAtual = signal<Usuario | null>(null);
  private renovacaoEmAndamento: Observable<string> | null = null;

  readonly usuario = this.usuarioAtual.asReadonly();
  readonly autenticado = computed(() => this.usuarioAtual() !== null);
  readonly ehAdmin = computed(() => this.possui('ADMIN'));
  readonly ehCliente = computed(() => this.possui('CLIENTE'));

  accessToken(): string | null {
    return this.token();
  }

  possui(papel: Papel): boolean {
    return this.usuarioAtual()?.papeis.includes(papel) ?? false;
  }

  login(cpf: string, senha: string): Observable<Usuario> {
    return this.api.login(cpf, senha).pipe(
      tap((tokens) => this.guardar(tokens)),
      switchMap(() => this.api.eu()),
      tap((usuario) => this.usuarioAtual.set(usuario)),
    );
  }

  /**
   * Na inicialização do app: se houver refresh token da aba, tenta reabrir a sessão sem pedir
   * login de novo. Falha silenciosamente (o usuário cai na tela de login).
   */
  restaurar(): Promise<void> {
    if (!lerRefresh()) {
      return Promise.resolve();
    }
    return firstValueFrom(
      this.renovar().pipe(
        switchMap(() => this.api.eu()),
        tap((usuario) => this.usuarioAtual.set(usuario)),
        map(() => undefined),
        catchError(() => of(undefined)),
      ),
    );
  }

  /** Troca o refresh token por um novo par e devolve o novo access token. Chamadas simultâneas compartilham a mesma. */
  renovar(): Observable<string> {
    if (!this.renovacaoEmAndamento) {
      const refresh = lerRefresh();
      if (!refresh) {
        return throwError(() => new Error('Sem refresh token'));
      }
      this.renovacaoEmAndamento = this.api.renovar(refresh).pipe(
        tap((tokens) => this.guardar(tokens)),
        map((tokens) => tokens.accessToken),
        catchError((erro) => {
          this.limpar();
          return throwError(() => erro);
        }),
        finalize(() => (this.renovacaoEmAndamento = null)),
        shareReplay(1),
      );
    }
    return this.renovacaoEmAndamento;
  }

  /** Logout explícito: revoga o refresh token no backend (melhor esforço) e volta ao login. */
  sair(): void {
    const refresh = lerRefresh();
    this.limpar();
    if (refresh) {
      this.api.logout(refresh).pipe(catchError(() => of(undefined))).subscribe();
    }
    void this.router.navigate(['/login']);
  }

  /** Sessão expirou/foi revogada no meio do uso. */
  expirar(): void {
    this.limpar();
    void this.router.navigate(['/login'], { queryParams: { sessao: 'expirada' } });
  }

  private guardar(tokens: Tokens): void {
    this.token.set(tokens.accessToken);
    gravarRefresh(tokens.refreshToken);
  }

  private limpar(): void {
    this.token.set(null);
    this.usuarioAtual.set(null);
    gravarRefresh(null);
  }
}

/* O sessionStorage pode não existir ou lançar erro (modo privado, testes): a sessão continua só em memória. */
function lerRefresh(): string | null {
  try {
    return sessionStorage.getItem(CHAVE_REFRESH);
  } catch {
    return null;
  }
}

function gravarRefresh(valor: string | null): void {
  try {
    if (valor) {
      sessionStorage.setItem(CHAVE_REFRESH, valor);
    } else {
      sessionStorage.removeItem(CHAVE_REFRESH);
    }
  } catch {
    // sem persistência: o usuário terá de logar de novo após recarregar a página
  }
}
