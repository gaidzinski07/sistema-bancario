import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LOCALE_ID } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormGroup } from '@angular/forms';
import { provideRouter } from '@angular/router';
import { Conta } from '../../core/api/modelos';
import { Transferencia } from './transferencia';

registerLocaleData(localePt);

const CONTA: Conta = { id: 5, agencia: '0002', numero: '00005', digito: '1', tipo: 'CORRENTE', saldo: 500, status: 'ATIVA', titularId: 3 };

/** Acesso aos membros protegidos que o template usa. */
interface Interno {
  dados: FormGroup<any>;
  confirmacao: FormGroup<any>;
  confirmar(): void;
  chave(): string | null;
}

describe('Transferencia', () => {
  let fixture: ComponentFixture<Transferencia>;
  let http: HttpTestingController;
  let tela: Interno;

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [Transferencia],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([]), { provide: LOCALE_ID, useValue: 'pt-BR' }],
    });
    fixture = TestBed.createComponent(Transferencia);
    http = TestBed.inject(HttpTestingController);
    tela = fixture.componentInstance as unknown as Interno;
    fixture.detectChanges();
    http.expectOne('/api/contas').flush([CONTA]);
    await fixture.whenStable();

    tela.dados.setValue({ contaOrigemId: 5, agenciaDestino: '0001', contaDestino: '00001-9', cpfDestino: '529.982.247-25', valor: 10 });
    tela.confirmacao.setValue({ senhaTransacao: '1234' });
  });

  afterEach(() => http.verify());

  it('manda o CPF sem máscara e uma Idempotency-Key', () => {
    tela.confirmar();

    const requisicao = http.expectOne('/api/transferencias');
    expect(requisicao.request.body.cpfDestino).toBe('52998224725');
    expect(requisicao.request.headers.get('Idempotency-Key')).toMatch(/^[0-9a-f-]{36}$/);
    requisicao.flush({ id: 1, valor: 10, ocorridoEm: '2026-11-18T12:00:00Z', origem: {}, destino: {}, favorecido: 'João', saldoOrigemApos: 490 });
  });

  it('ao tentar de novo depois de erro de rede, reaproveita a mesma chave (não duplica a transferência)', () => {
    tela.confirmar();
    const primeira = http.expectOne('/api/transferencias');
    const chave = primeira.request.headers.get('Idempotency-Key');
    primeira.error(new ProgressEvent('error'), { status: 0 });

    tela.confirmacao.setValue({ senhaTransacao: '1234' });
    tela.confirmar();
    const segunda = http.expectOne('/api/transferencias');
    expect(segunda.request.headers.get('Idempotency-Key')).toBe(chave);
    segunda.flush({ id: 1, valor: 10, ocorridoEm: '2026-11-18T12:00:00Z', origem: {}, destino: {}, favorecido: 'João', saldoOrigemApos: 490 });
  });

  it('erro de regra de negócio não gravou nada: a próxima tentativa usa chave nova', () => {
    tela.confirmar();
    const primeira = http.expectOne('/api/transferencias');
    const chave = primeira.request.headers.get('Idempotency-Key');
    primeira.flush({ status: 422, codigo: 'saldo-insuficiente', detail: 'Saldo insuficiente.' }, { status: 422, statusText: 'Unprocessable' });

    tela.confirmacao.setValue({ senhaTransacao: '1234' });
    tela.confirmar();
    const segunda = http.expectOne('/api/transferencias');
    expect(segunda.request.headers.get('Idempotency-Key')).not.toBe(chave);
    segunda.flush({ id: 2, valor: 10, ocorridoEm: '2026-11-18T12:00:00Z', origem: {}, destino: {}, favorecido: 'João', saldoOrigemApos: 490 });
  });

  it('mudar os dados da transferência descarta a chave', () => {
    tela.confirmar();
    http.expectOne('/api/transferencias').error(new ProgressEvent('error'), { status: 0 });
    expect(tela.chave()).not.toBeNull();

    tela.dados.get('valor')!.setValue(20);

    expect(tela.chave()).toBeNull();
  });
});
