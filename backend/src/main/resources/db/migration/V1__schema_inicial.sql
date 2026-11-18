-- =============================================================================
-- V1 — Schema inicial do sistema bancário (substitui sql/Cria_Tabelas.sql).
--
-- Convenções:
--   * PK surrogate BIGINT em todas as tabelas (exceto cliente, que compartilha a PK do usuário).
--   * Dinheiro em DECIMAL(19,2); quantidade/valor de cota em DECIMAL(19,8). Nada de FLOAT.
--   * Datas/horas em TIMESTAMP(6), sempre UTC (hibernate.jdbc.time_zone=UTC).
--   * Enums gravados como VARCHAR com o nome da constante Java (EnumType.STRING).
--   * Tabelas mutáveis têm criado_em/atualizado_em; operações (imutáveis) têm ocorrido_em.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Identidade
-- -----------------------------------------------------------------------------
CREATE TABLE usuario (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    cpf                   CHAR(11)     NOT NULL,
    nome                  VARCHAR(100) NOT NULL,
    email                 VARCHAR(150) NULL,
    data_nascimento       DATE         NULL,
    senha_hash            VARCHAR(100) NOT NULL,
    senha_transacao_hash  VARCHAR(100) NULL,
    perfil_administrador  VARCHAR(20)  NULL,              -- GERENTE | OPERADOR (só para quem tem papel ADMIN)
    ativo                 BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em             TIMESTAMP(6) NOT NULL,
    atualizado_em         TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_cpf UNIQUE (cpf),
    CONSTRAINT uk_usuario_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE usuario_papel (
    usuario_id  BIGINT      NOT NULL,
    papel       VARCHAR(20) NOT NULL,                      -- ADMIN | CLIENTE
    PRIMARY KEY (usuario_id, papel),
    CONSTRAINT fk_usuario_papel_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Clientes e agências
-- -----------------------------------------------------------------------------
CREATE TABLE cliente (
    usuario_id     BIGINT       NOT NULL,                  -- @OneToOne @MapsId com usuario
    segmento       VARCHAR(20)  NOT NULL,                  -- CORRENTISTA | INVESTIDOR
    logradouro     VARCHAR(150) NULL,
    numero         VARCHAR(10)  NULL,
    complemento    VARCHAR(60)  NULL,
    bairro         VARCHAR(80)  NULL,
    cidade         VARCHAR(80)  NULL,
    uf             CHAR(2)      NULL,
    cep            CHAR(8)      NULL,
    criado_em      TIMESTAMP(6) NOT NULL,
    atualizado_em  TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (usuario_id),
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE agencia (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    codigo         CHAR(4)      NOT NULL,
    nome           VARCHAR(100) NOT NULL,
    logradouro     VARCHAR(150) NULL,
    numero         VARCHAR(10)  NULL,
    complemento    VARCHAR(60)  NULL,
    bairro         VARCHAR(80)  NULL,
    cidade         VARCHAR(80)  NULL,
    uf             CHAR(2)      NULL,
    cep            CHAR(8)      NULL,
    criado_em      TIMESTAMP(6) NOT NULL,
    atualizado_em  TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_agencia_codigo UNIQUE (codigo)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Contas e operações
-- -----------------------------------------------------------------------------
CREATE TABLE conta (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    agencia_id     BIGINT        NOT NULL,
    numero         VARCHAR(10)   NOT NULL,
    digito         CHAR(1)       NOT NULL,
    titular_id     BIGINT        NOT NULL,
    tipo           VARCHAR(20)   NOT NULL,                 -- CORRENTE | POUPANCA
    saldo          DECIMAL(19,2) NOT NULL DEFAULT 0,
    status         VARCHAR(20)   NOT NULL,                 -- ATIVA | BLOQUEADA | ENCERRADA
    version        BIGINT        NOT NULL DEFAULT 0,       -- @Version (lock otimista)
    criado_em      TIMESTAMP(6)  NOT NULL,
    atualizado_em  TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_conta_agencia_numero UNIQUE (agencia_id, numero),
    CONSTRAINT fk_conta_agencia FOREIGN KEY (agencia_id) REFERENCES agencia (id),
    CONSTRAINT fk_conta_titular FOREIGN KEY (titular_id) REFERENCES cliente (usuario_id),
    -- Defesa em profundidade: a regra de saldo vive no domínio, mas o banco também recusa saldo negativo.
    CONSTRAINT ck_conta_saldo_nao_negativo CHECK (saldo >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX ix_conta_titular ON conta (titular_id);

CREATE TABLE deposito (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    conta_id     BIGINT        NOT NULL,
    agencia_id   BIGINT        NULL,                       -- agência onde o depósito foi feito
    forma        VARCHAR(20)   NOT NULL,                   -- CHEQUE | DINHEIRO
    valor        DECIMAL(19,2) NOT NULL,
    ocorrido_em  TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_deposito_conta FOREIGN KEY (conta_id) REFERENCES conta (id),
    CONSTRAINT fk_deposito_agencia FOREIGN KEY (agencia_id) REFERENCES agencia (id),
    CONSTRAINT ck_deposito_valor_positivo CHECK (valor > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE saque (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    conta_id     BIGINT        NOT NULL,
    agencia_id   BIGINT        NULL,
    valor        DECIMAL(19,2) NOT NULL,
    ocorrido_em  TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_saque_conta FOREIGN KEY (conta_id) REFERENCES conta (id),
    CONSTRAINT fk_saque_agencia FOREIGN KEY (agencia_id) REFERENCES agencia (id),
    CONSTRAINT ck_saque_valor_positivo CHECK (valor > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE transferencia (
    id                  BIGINT        NOT NULL AUTO_INCREMENT,
    conta_origem_id     BIGINT        NOT NULL,
    conta_destino_id    BIGINT        NOT NULL,
    valor               DECIMAL(19,2) NOT NULL,
    chave_idempotencia  VARCHAR(64)   NOT NULL,
    ocorrido_em         TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_transferencia_idempotencia UNIQUE (chave_idempotencia),
    CONSTRAINT fk_transferencia_origem FOREIGN KEY (conta_origem_id) REFERENCES conta (id),
    CONSTRAINT fk_transferencia_destino FOREIGN KEY (conta_destino_id) REFERENCES conta (id),
    CONSTRAINT ck_transferencia_valor_positivo CHECK (valor > 0),
    CONSTRAINT ck_transferencia_contas_distintas CHECK (conta_origem_id <> conta_destino_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Extrato (ledger). Toda operação que movimenta saldo gera um ou mais lançamentos
-- na mesma transação; a soma dos lançamentos de uma conta é igual ao seu saldo.
CREATE TABLE lancamento (
    id           BIGINT        NOT NULL AUTO_INCREMENT,
    conta_id     BIGINT        NOT NULL,
    tipo         VARCHAR(30)   NOT NULL,                   -- DEPOSITO | SAQUE | TRANSFERENCIA_ENVIADA | TRANSFERENCIA_RECEBIDA | APLICACAO | RESGATE
    natureza     VARCHAR(10)   NOT NULL,                   -- CREDITO | DEBITO
    valor        DECIMAL(19,2) NOT NULL,                   -- sempre positivo; o sinal vem da natureza
    saldo_apos   DECIMAL(19,2) NOT NULL,
    descricao    VARCHAR(200)  NOT NULL,
    contraparte  VARCHAR(150)  NULL,                       -- ex.: nome do favorecido, nome do fundo
    operacao_id  BIGINT        NULL,                       -- id em deposito/saque/transferencia/movimento_investimento
    ocorrido_em  TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_lancamento_conta FOREIGN KEY (conta_id) REFERENCES conta (id),
    CONSTRAINT ck_lancamento_valor_positivo CHECK (valor > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX ix_lancamento_conta_data ON lancamento (conta_id, ocorrido_em);

-- -----------------------------------------------------------------------------
-- Investimentos
-- -----------------------------------------------------------------------------
CREATE TABLE fundo (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    nome              VARCHAR(100)  NOT NULL,
    cota_atual        DECIMAL(19,8) NOT NULL,
    aplicacao_minima  DECIMAL(19,2) NOT NULL DEFAULT 0,
    ativo             BOOLEAN       NOT NULL DEFAULT TRUE,
    criado_em         TIMESTAMP(6)  NOT NULL,
    atualizado_em     TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_fundo_nome UNIQUE (nome),
    CONSTRAINT ck_fundo_cota_positiva CHECK (cota_atual > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE cotacao_fundo (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    fundo_id    BIGINT        NOT NULL,
    data        DATE          NOT NULL,
    valor_cota  DECIMAL(19,8) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_cotacao_fundo_data UNIQUE (fundo_id, data),
    CONSTRAINT fk_cotacao_fundo_fundo FOREIGN KEY (fundo_id) REFERENCES fundo (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Posição consolidada de uma conta em um fundo (substitui a tabela investimento,
-- que tinha uma linha por aplicação e só permitia resgate total).
CREATE TABLE posicao (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    conta_id          BIGINT        NOT NULL,
    fundo_id          BIGINT        NOT NULL,
    quantidade_cotas  DECIMAL(19,8) NOT NULL DEFAULT 0,
    valor_aplicado    DECIMAL(19,2) NOT NULL DEFAULT 0,    -- custo de aquisição das cotas em carteira
    version           BIGINT        NOT NULL DEFAULT 0,
    criado_em         TIMESTAMP(6)  NOT NULL,
    atualizado_em     TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_posicao_conta_fundo UNIQUE (conta_id, fundo_id),
    CONSTRAINT fk_posicao_conta FOREIGN KEY (conta_id) REFERENCES conta (id),
    CONSTRAINT fk_posicao_fundo FOREIGN KEY (fundo_id) REFERENCES fundo (id),
    CONSTRAINT ck_posicao_cotas_nao_negativas CHECK (quantidade_cotas >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE movimento_investimento (
    id                BIGINT        NOT NULL AUTO_INCREMENT,
    posicao_id        BIGINT        NOT NULL,
    tipo              VARCHAR(20)   NOT NULL,              -- APLICACAO | RESGATE
    quantidade_cotas  DECIMAL(19,8) NOT NULL,
    valor_cota        DECIMAL(19,8) NOT NULL,
    valor             DECIMAL(19,2) NOT NULL,
    ocorrido_em       TIMESTAMP(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_movimento_investimento_posicao FOREIGN KEY (posicao_id) REFERENCES posicao (id),
    CONSTRAINT ck_movimento_investimento_valor_positivo CHECK (valor > 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
