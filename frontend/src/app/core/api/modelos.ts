/**
 * Tipos das respostas da API (espelham os records de `application` no backend).
 * Valores monetários chegam como `number` (o JSON do BigDecimal) e datas/instantes como string ISO-8601.
 */

export type Papel = 'ADMIN' | 'CLIENTE';
export type PerfilAdministrador = 'GERENTE' | 'OPERADOR';
export type SegmentoCliente = 'CORRENTISTA' | 'INVESTIDOR';
export type TipoConta = 'CORRENTE' | 'POUPANCA';
export type StatusConta = 'ATIVA' | 'BLOQUEADA' | 'ENCERRADA';
export type FormaDeposito = 'DINHEIRO' | 'CHEQUE';
export type Natureza = 'CREDITO' | 'DEBITO';
export type TipoLancamento =
  | 'DEPOSITO'
  | 'SAQUE'
  | 'TRANSFERENCIA_ENVIADA'
  | 'TRANSFERENCIA_RECEBIDA'
  | 'APLICACAO'
  | 'RESGATE';

/** Erro da API no formato ProblemDetail (RFC 9457) com o `codigo` estável do backend. */
export interface Problema {
  type?: string;
  title?: string;
  status: number;
  detail?: string;
  instance?: string;
  codigo?: string;
  erros?: { campo: string; mensagem: string }[];
}

export interface Pagina<T> {
  conteudo: T[];
  pagina: number;
  tamanho: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface Endereco {
  logradouro?: string | null;
  numero?: string | null;
  complemento?: string | null;
  bairro?: string | null;
  cidade?: string | null;
  uf?: string | null;
  cep?: string | null;
}

// --- identidade -------------------------------------------------------------

export interface Tokens {
  accessToken: string;
  tipo: 'Bearer';
  expiraEmSegundos: number;
  refreshToken: string;
}

export interface Usuario {
  id: number;
  nome: string;
  cpf: string;
  email: string | null;
  dataNascimento: string | null;
  papeis: Papel[];
  perfilAdministrador: PerfilAdministrador | null;
  ativo: boolean;
}

// --- contas -----------------------------------------------------------------

export interface Conta {
  id: number;
  agencia: string;
  numero: string;
  digito: string;
  tipo: TipoConta;
  saldo: number;
  status: StatusConta;
  titularId: number;
}

export interface ContaResumo {
  id: number;
  agencia: string;
  conta: string;
  tipo: TipoConta;
}

export interface ComprovanteOperacao {
  operacaoId: number;
  tipo: TipoLancamento;
  descricao: string;
  valor: number;
  saldoApos: number;
  ocorridoEm: string;
  conta: ContaResumo;
}

export interface ComprovanteTransferencia {
  id: number;
  valor: number;
  ocorridoEm: string;
  origem: ContaResumo;
  destino: ContaResumo;
  favorecido: string;
  saldoOrigemApos: number;
}

export interface Lancamento {
  id: number;
  ocorridoEm: string;
  tipo: TipoLancamento;
  natureza: Natureza;
  descricao: string;
  contraparte: string | null;
  valor: number;
  saldoApos: number;
}

export interface Extrato {
  conta: ContaResumo;
  de: string;
  ate: string;
  fusoHorario: string;
  saldoInicial: number;
  totalCreditos: number;
  totalDebitos: number;
  saldoFinal: number;
  lancamentos: Pagina<Lancamento>;
}

// --- investimentos ----------------------------------------------------------

export interface Fundo {
  id: number;
  nome: string;
  cotaAtual: number;
  aplicacaoMinima: number;
  ativo: boolean;
}

export interface Cotacao {
  data: string;
  valorCota: number;
}

export interface Posicao {
  id: number;
  fundo: { id: number; nome: string };
  conta: ContaResumo;
  quantidadeCotas: number;
  cotaAtual: number;
  valorAplicado: number;
  valorAtual: number;
  rendimento: number;
  rentabilidadePercentual: number | null;
}

export interface MovimentoInvestimento {
  id: number;
  tipo: 'APLICACAO' | 'RESGATE';
  quantidadeCotas: number;
  valorCota: number;
  valor: number;
  ocorridoEm: string;
}

export interface DetalhePosicao {
  posicao: Posicao;
  movimentos: MovimentoInvestimento[];
}

export interface ComprovanteInvestimento {
  movimento: MovimentoInvestimento;
  fundo: { id: number; nome: string };
  conta: ContaResumo;
  saldoContaApos: number;
  posicao: Posicao;
}

// --- administração ----------------------------------------------------------

export interface Cliente {
  id: number;
  nome: string;
  cpf: string;
  email: string | null;
  dataNascimento: string | null;
  segmento: SegmentoCliente;
  endereco: Endereco | null;
  ativo: boolean;
  criadoEm: string;
}

export interface Agencia {
  id: number;
  codigo: string;
  nome: string;
  endereco: Endereco | null;
}
