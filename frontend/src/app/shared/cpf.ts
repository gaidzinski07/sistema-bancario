import { Pipe, PipeTransform } from '@angular/core';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Só os dígitos de um CPF digitado com ou sem máscara. */
export function digitosDoCpf(valor: string | null | undefined): string {
  return (valor ?? '').replace(/\D/g, '');
}

/** Mesma regra do `Cpf` do backend: 11 dígitos, não todos iguais, dígitos verificadores corretos. */
export function cpfValido(valor: string | null | undefined): boolean {
  const cpf = digitosDoCpf(valor);
  if (cpf.length !== 11 || /^(\d)\1{10}$/.test(cpf)) {
    return false;
  }
  const digito = (quantidade: number): number => {
    let soma = 0;
    for (let i = 0; i < quantidade; i++) {
      soma += Number(cpf[i]) * (quantidade + 1 - i);
    }
    const resto = (soma * 10) % 11;
    return resto === 10 ? 0 : resto;
  };
  return digito(9) === Number(cpf[9]) && digito(10) === Number(cpf[10]);
}

/** Validador de formulário. Vazio passa (use `Validators.required` junto quando for obrigatório). */
export const validadorCpf: ValidatorFn = (controle: AbstractControl): ValidationErrors | null =>
  !controle.value || cpfValido(controle.value) ? null : { cpf: true };

/** `52998224725` → `529.982.247-25`. */
@Pipe({ name: 'cpf' })
export class CpfPipe implements PipeTransform {
  transform(valor: string | null | undefined): string {
    const cpf = digitosDoCpf(valor);
    return cpf.length === 11 ? `${cpf.slice(0, 3)}.${cpf.slice(3, 6)}.${cpf.slice(6, 9)}-${cpf.slice(9)}` : (valor ?? '');
  }
}
