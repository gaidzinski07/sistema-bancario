import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { Papel } from '../api/modelos';
import { Sessao } from './sessao';

/** Rotas internas: exige sessão; sem ela, vai para o login guardando o destino. */
export const autenticado: CanMatchFn = (_rota, segmentos) => {
  const sessao = inject(Sessao);
  if (sessao.autenticado()) {
    return true;
  }
  const destino = '/' + segmentos.map((s) => s.path).join('/');
  return inject(Router).createUrlTree(['/login'], { queryParams: destino !== '/' ? { destino } : {} });
};

/**
 * Exige um papel. Devolve `false` (e não um redirecionamento) para o roteador seguir tentando as
 * próximas rotas: a área do cliente fica em `''`, e redirecionar ali barraria também `/admin/...`.
 * Sem papel, a URL cai na página "não encontrada".
 *
 * Esconder a rota é só conveniência de interface — quem garante a regra é o backend (403).
 */
export function comPapel(papel: Papel): CanMatchFn {
  return () => inject(Sessao).possui(papel);
}

/** Tela de login: quem já está logado vai direto para o início. */
export const anonimo: CanMatchFn = () => !inject(Sessao).autenticado() || inject(Router).createUrlTree(['/inicio']);
