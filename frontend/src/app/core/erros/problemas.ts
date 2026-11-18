import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, FormGroup } from '@angular/forms';
import { Problema } from '../api/modelos';

/** O ProblemDetail do backend, se o erro for uma resposta da API nesse formato. */
export function problemaDe(erro: unknown): Problema | null {
  if (erro instanceof HttpErrorResponse && erro.error && typeof erro.error === 'object' && 'status' in erro.error) {
    return erro.error as Problema;
  }
  return null;
}

/** Mensagem para o usuário: o `detail` do backend (já em português) ou uma genérica. */
export function mensagemDeErro(erro: unknown): string {
  const problema = problemaDe(erro);
  if (problema?.detail) {
    return problema.detail;
  }
  if (erro instanceof HttpErrorResponse && erro.status === 0) {
    return 'Não foi possível falar com o servidor. Verifique sua conexão e tente de novo.';
  }
  return 'Ocorreu um erro inesperado. Tente novamente.';
}

export function codigoDoErro(erro: unknown): string | undefined {
  return problemaDe(erro)?.codigo;
}

/**
 * Leva os erros por campo do backend (`erros: [{campo, mensagem}]`) para os controles do
 * formulário, que passam a exibi-los no `mat-error`. Devolve se algum foi aplicado.
 */
export function aplicarErrosDeCampo(formulario: FormGroup, erro: unknown): boolean {
  const erros = problemaDe(erro)?.erros ?? [];
  let aplicou = false;
  for (const { campo, mensagem } of erros) {
    const controle: AbstractControl | null = formulario.get(campo);
    if (controle) {
      controle.setErrors({ ...(controle.errors ?? {}), servidor: mensagem });
      controle.markAsTouched();
      aplicou = true;
    }
  }
  return aplicou;
}
