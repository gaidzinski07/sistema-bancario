import { Pipe, PipeTransform } from '@angular/core';
import { Conta, ContaResumo, Natureza, TipoConta, TipoLancamento } from '../core/api/modelos';

const NOMES_TIPO_CONTA: Record<TipoConta, string> = {
  CORRENTE: 'Conta corrente',
  POUPANCA: 'Poupança',
};

const NOMES_LANCAMENTO: Record<TipoLancamento, string> = {
  DEPOSITO: 'Depósito',
  SAQUE: 'Saque',
  TRANSFERENCIA_ENVIADA: 'Transferência enviada',
  TRANSFERENCIA_RECEBIDA: 'Transferência recebida',
  APLICACAO: 'Aplicação',
  RESGATE: 'Resgate',
};

/** `CORRENTE` → `Conta corrente`. */
@Pipe({ name: 'tipoConta' })
export class TipoContaPipe implements PipeTransform {
  transform(tipo: TipoConta | string | null | undefined): string {
    return tipo ? (NOMES_TIPO_CONTA[tipo as TipoConta] ?? tipo) : '';
  }
}

/** `TRANSFERENCIA_ENVIADA` → `Transferência enviada`. */
@Pipe({ name: 'tipoLancamento' })
export class TipoLancamentoPipe implements PipeTransform {
  transform(tipo: TipoLancamento | string | null | undefined): string {
    return tipo ? (NOMES_LANCAMENTO[tipo as TipoLancamento] ?? tipo) : '';
  }
}

/** Identificação de conta: `Ag 0001 · CC 00001-9`. Aceita `Conta` (número + dígito) ou `ContaResumo`. */
@Pipe({ name: 'conta' })
export class ContaPipe implements PipeTransform {
  transform(conta: Conta | ContaResumo | null | undefined): string {
    if (!conta) {
      return '';
    }
    const numero = 'numero' in conta ? `${conta.numero}-${conta.digito}` : conta.conta;
    const sigla = conta.tipo === 'POUPANCA' ? 'PP' : 'CC';
    return `Ag ${conta.agencia} · ${sigla} ${numero}`;
  }
}

/** Valor com o sinal da natureza: débito vira negativo para exibição. */
export function valorComSinal(valor: number, natureza: Natureza): number {
  return natureza === 'DEBITO' ? -valor : valor;
}

/** Data local no formato da API (`AAAA-MM-DD`), sem passar por UTC. */
export function dataIso(data: Date): string {
  const mes = String(data.getMonth() + 1).padStart(2, '0');
  const dia = String(data.getDate()).padStart(2, '0');
  return `${data.getFullYear()}-${mes}-${dia}`;
}
