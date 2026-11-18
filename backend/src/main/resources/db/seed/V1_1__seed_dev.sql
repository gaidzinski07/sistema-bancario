-- =============================================================================
-- Dados de exemplo — carregados SOMENTE no profile `dev` (ver application-dev.yml).
--
-- Credenciais de teste (todas as contas):
--   senha de login .......... Senha@123
--   senha de transação ...... 1234
--
-- Os saldos são coerentes com o extrato: cada saldo inicial entra como um depósito
-- com o lançamento correspondente (no legado os saldos eram setados com UPDATE e
-- não batiam com as movimentações).
-- Os CPFs são fictícios, mas válidos (passam nos dígitos verificadores).
-- =============================================================================

SET @agora = '2026-01-05 12:00:00.000000';
SET @senha = '{bcrypt}$2a$10$Ezg9cD6UKFaVRRlqhll1z.FBij0MU2xj7GeOYUkC82dfZGBzPsC6G';
SET @senha_transacao = '{bcrypt}$2a$10$WluNmkYMwrPhZ.1kndrAQe/W0lRF5OlnW5YKvWZHR6zqZPSgh83x6';

INSERT INTO agencia (id, codigo, nome, logradouro, numero, bairro, cidade, uf, cep, criado_em, atualizado_em) VALUES
    (1, '0001', 'Agência Centro',    'Rua E', '5', 'Centro',    'Niterói', 'RJ', '24020000', @agora, @agora),
    (2, '0002', 'Agência Icaraí',    'Rua F', '6', 'Icaraí',    'Niterói', 'RJ', '24220000', @agora, @agora),
    (3, '0003', 'Agência São Domingos', 'Rua G', '7', 'São Domingos', 'Niterói', 'RJ', '24210000', @agora, @agora);

INSERT INTO usuario (id, cpf, nome, email, data_nascimento, senha_hash, senha_transacao_hash, perfil_administrador, ativo, criado_em, atualizado_em) VALUES
    (1, '52998224725', 'João Vicente',    'joao@banco.test',    '2000-01-10', @senha, @senha_transacao, 'GERENTE', TRUE, @agora, @agora),
    (2, '11144477735', 'Lucas Magalhães', 'lucas@banco.test',   '2000-02-20', @senha, @senha_transacao, NULL,      TRUE, @agora, @agora),
    (3, '39053344705', 'Luiz Gustavo',    'luiz@banco.test',    '2000-03-30', @senha, @senha_transacao, NULL,      TRUE, @agora, @agora),
    (4, '12345678909', 'Antônio Augusto', 'antonio@banco.test', '1990-04-15', @senha, NULL,             'OPERADOR', TRUE, @agora, @agora);

-- 1: admin + cliente | 2, 3: só cliente | 4: só admin
INSERT INTO usuario_papel (usuario_id, papel) VALUES
    (1, 'ADMIN'), (1, 'CLIENTE'),
    (2, 'CLIENTE'),
    (3, 'CLIENTE'),
    (4, 'ADMIN');

INSERT INTO cliente (usuario_id, segmento, logradouro, numero, bairro, cidade, uf, cep, criado_em, atualizado_em) VALUES
    (1, 'CORRENTISTA', 'Rua A', '1', 'Bairro A', 'Niterói', 'RJ', '24000001', @agora, @agora),
    (2, 'INVESTIDOR',  'Rua B', '2', 'Bairro B', 'Niterói', 'RJ', '24000002', @agora, @agora),
    (3, 'CORRENTISTA', 'Rua C', '3', 'Bairro C', 'Niterói', 'RJ', '24000003', @agora, @agora);

-- Dígito: módulo 11 sobre o número (pesos 2..9 da direita para a esquerda; resto >= 10 vira 0).
INSERT INTO conta (id, agencia_id, numero, digito, titular_id, tipo, saldo, status, version, criado_em, atualizado_em) VALUES
    (1, 1, '00001', '9', 1, 'CORRENTE', 2000.00,  'ATIVA', 0, @agora, @agora),
    (2, 1, '00002', '7', 1, 'POUPANCA', 100.00,   'ATIVA', 0, @agora, @agora),
    (3, 3, '00003', '5', 2, 'CORRENTE', 10000.00, 'ATIVA', 0, @agora, @agora),
    (4, 3, '00004', '3', 2, 'POUPANCA', 200.00,   'ATIVA', 0, @agora, @agora),
    (5, 2, '00005', '1', 3, 'CORRENTE', 500.00,   'ATIVA', 0, @agora, @agora);

INSERT INTO deposito (id, conta_id, agencia_id, forma, valor, ocorrido_em) VALUES
    (1, 1, 1, 'DINHEIRO', 2000.00,  @agora),
    (2, 2, 1, 'DINHEIRO', 100.00,   @agora),
    (3, 3, 3, 'DINHEIRO', 10000.00, @agora),
    (4, 4, 3, 'DINHEIRO', 200.00,   @agora),
    (5, 5, 2, 'DINHEIRO', 500.00,   @agora);

INSERT INTO lancamento (conta_id, tipo, natureza, valor, saldo_apos, descricao, contraparte, operacao_id, ocorrido_em) VALUES
    (1, 'DEPOSITO', 'CREDITO', 2000.00,  2000.00,  'Depósito em dinheiro', NULL, 1, @agora),
    (2, 'DEPOSITO', 'CREDITO', 100.00,   100.00,   'Depósito em dinheiro', NULL, 2, @agora),
    (3, 'DEPOSITO', 'CREDITO', 10000.00, 10000.00, 'Depósito em dinheiro', NULL, 3, @agora),
    (4, 'DEPOSITO', 'CREDITO', 200.00,   200.00,   'Depósito em dinheiro', NULL, 4, @agora),
    (5, 'DEPOSITO', 'CREDITO', 500.00,   500.00,   'Depósito em dinheiro', NULL, 5, @agora);

INSERT INTO fundo (id, nome, cota_atual, aplicacao_minima, ativo, criado_em, atualizado_em) VALUES
    (1, 'Fundo Renda Fixa DI',  3.80000000, 100.00,  TRUE, @agora, @agora),
    (2, 'Fundo Multimercado',   7.77000000, 500.00,  TRUE, @agora, @agora),
    (3, 'Fundo de Ações Brasil', 5.20000000, 1000.00, TRUE, @agora, @agora);

INSERT INTO cotacao_fundo (fundo_id, data, valor_cota) VALUES
    (1, '2026-01-05', 3.80000000),
    (2, '2026-01-05', 7.77000000),
    (3, '2026-01-05', 5.20000000);
