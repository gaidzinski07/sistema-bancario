import { HttpErrorResponse } from '@angular/common/http';
import { FormControl, FormGroup } from '@angular/forms';
import { aplicarErrosDeCampo, codigoDoErro, mensagemDeErro } from './problemas';

function erroDaApi(status: number, corpo: object): HttpErrorResponse {
  return new HttpErrorResponse({ status, error: { status, ...corpo } });
}

describe('problemas', () => {
  it('usa o detail do ProblemDetail do backend', () => {
    const erro = erroDaApi(422, { codigo: 'saldo-insuficiente', detail: 'Saldo insuficiente para esta operação.' });
    expect(mensagemDeErro(erro)).toBe('Saldo insuficiente para esta operação.');
    expect(codigoDoErro(erro)).toBe('saldo-insuficiente');
  });

  it('erro de rede tem mensagem própria', () => {
    expect(mensagemDeErro(new HttpErrorResponse({ status: 0 }))).toContain('servidor');
  });

  it('qualquer outro erro vira mensagem genérica', () => {
    expect(mensagemDeErro(new Error('x'))).toContain('inesperado');
  });

  it('leva os erros por campo para o formulário', () => {
    const formulario = new FormGroup({ valor: new FormControl(1), cpf: new FormControl('') });
    const erro = erroDaApi(400, { codigo: 'dados-invalidos', erros: [{ campo: 'valor', mensagem: 'deve ser positivo' }] });

    expect(aplicarErrosDeCampo(formulario, erro)).toBe(true);
    expect(formulario.controls.valor.getError('servidor')).toBe('deve ser positivo');
    expect(formulario.controls.cpf.valid).toBe(true);
  });

  it('sem erros por campo, devolve false', () => {
    const formulario = new FormGroup({ valor: new FormControl(1) });
    expect(aplicarErrosDeCampo(formulario, erroDaApi(422, { codigo: 'x' }))).toBe(false);
  });
});
