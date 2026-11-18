import { FormControl } from '@angular/forms';
import { CpfPipe, cpfValido, validadorCpf } from './cpf';

describe('CPF', () => {
  it.each(['529.982.247-25', '52998224725', '390.533.447-05'])('aceita %s', (cpf) => {
    expect(cpfValido(cpf)).toBe(true);
  });

  it.each(['111', '52998224724', '111.111.111-11', '', '5299822472a'])('recusa "%s"', (cpf) => {
    expect(cpfValido(cpf)).toBe(false);
  });

  it('validador deixa vazio para o required e marca CPF inválido', () => {
    expect(validadorCpf(new FormControl(''))).toBeNull();
    expect(validadorCpf(new FormControl('123'))).toEqual({ cpf: true });
    expect(validadorCpf(new FormControl('529.982.247-25'))).toBeNull();
  });

  it('pipe formata com máscara', () => {
    expect(new CpfPipe().transform('52998224725')).toBe('529.982.247-25');
    expect(new CpfPipe().transform('123')).toBe('123');
  });
});
