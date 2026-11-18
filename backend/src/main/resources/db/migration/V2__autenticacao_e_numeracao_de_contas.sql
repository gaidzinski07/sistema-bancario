-- =============================================================================
-- V2 — Fase 2: refresh tokens (JWT) e numeração de contas por agência.
-- =============================================================================

-- Refresh tokens opacos para renovar o access token (JWT). Só o hash SHA-256 é
-- guardado; o token em si só existe na resposta do login/refresh.
CREATE TABLE refresh_token (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    usuario_id  BIGINT       NOT NULL,
    token_hash  VARCHAR(64)  NOT NULL,                    -- SHA-256 em hexadecimal
    familia     VARCHAR(36)  NOT NULL,                    -- UUID compartilhado pelos tokens de uma mesma sessão de login
    expira_em   TIMESTAMP(6) NOT NULL,
    revogado_em TIMESTAMP(6) NULL,
    criado_em   TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX ix_refresh_token_familia ON refresh_token (familia);

-- Próximo número de conta de cada agência. A reserva é feita com a linha da
-- agência travada (SELECT ... FOR UPDATE), evitando números duplicados.
ALTER TABLE agencia ADD COLUMN proximo_numero_conta BIGINT NOT NULL DEFAULT 1;

-- Continua a numeração das contas que já existirem (ex.: seed de desenvolvimento).
UPDATE agencia a
   SET proximo_numero_conta = COALESCE(
           (SELECT MAX(CAST(c.numero AS UNSIGNED)) FROM conta c WHERE c.agencia_id = a.id), 0) + 1;
