import { TestBed } from '@angular/core/testing';
import { CanMatchFn, Route, Router, UrlSegment, UrlTree, provideRouter } from '@angular/router';
import { anonimo, autenticado, comPapel } from './guards';
import { Sessao } from './sessao';

describe('guards', () => {
  let sessao: { autenticado: () => boolean; possui: (papel: string) => boolean };

  function executar(guard: typeof autenticado, caminho: string[] = []): ReturnType<typeof autenticado> {
    return TestBed.runInInjectionContext(() => guard({} as Route, caminho.map((p) => new UrlSegment(p, {})), {} as Parameters<CanMatchFn>[2]));
  }

  beforeEach(() => {
    sessao = { autenticado: () => false, possui: () => false };
    TestBed.configureTestingModule({ providers: [provideRouter([]), { provide: Sessao, useValue: sessao }] });
  });

  it('sem sessão, manda para o login guardando o destino', () => {
    const resultado = executar(autenticado, ['contas', '5', 'extrato']) as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(resultado)).toBe('/login?destino=%2Fcontas%2F5%2Fextrato');
  });

  it('com sessão, deixa passar', () => {
    sessao.autenticado = () => true;
    expect(executar(autenticado)).toBe(true);
  });

  it('comPapel não redireciona: devolve false para o roteador tentar as próximas rotas', () => {
    sessao.possui = (papel) => papel === 'CLIENTE';
    expect(executar(comPapel('CLIENTE'))).toBe(true);
    expect(executar(comPapel('ADMIN'))).toBe(false);
  });

  it('quem já está logado não vê a tela de login', () => {
    sessao.autenticado = () => true;
    expect(TestBed.inject(Router).serializeUrl(executar(anonimo) as UrlTree)).toBe('/inicio');
  });
});
