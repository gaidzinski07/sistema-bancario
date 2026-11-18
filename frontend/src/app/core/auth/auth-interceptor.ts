import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { Sessao } from './sessao';

/** Rotas de autenticação: não levam Bearer e um 401 nelas não dispara renovação. */
const ROTAS_DE_AUTENTICACAO = ['/api/auth/login', '/api/auth/refresh', '/api/auth/logout'];

/**
 * Coloca o `Authorization: Bearer` nas chamadas à API e, se o access token expirou (401),
 * renova uma vez pelo refresh token e repete a chamada. Se a renovação falhar, a sessão acabou.
 */
export const authInterceptor: HttpInterceptorFn = (requisicao, proximo) => {
  if (!requisicao.url.startsWith('/api/') || ROTAS_DE_AUTENTICACAO.includes(requisicao.url)) {
    return proximo(requisicao);
  }
  const sessao = inject(Sessao);

  return proximo(comToken(requisicao, sessao.accessToken())).pipe(
    catchError((erro: unknown) => {
      if (!(erro instanceof HttpErrorResponse) || erro.status !== 401) {
        return throwError(() => erro);
      }
      return sessao.renovar().pipe(
        catchError((falhaNaRenovacao: unknown) => {
          sessao.expirar();
          return throwError(() => falhaNaRenovacao);
        }),
        switchMap((novoToken) => proximo(comToken(requisicao, novoToken))),
      );
    }),
  );
};

function comToken(requisicao: HttpRequest<unknown>, token: string | null): HttpRequest<unknown> {
  return token ? requisicao.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : requisicao;
}
