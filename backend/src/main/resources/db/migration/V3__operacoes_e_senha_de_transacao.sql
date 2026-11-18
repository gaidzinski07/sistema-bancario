-- =============================================================================
-- V3 — Fase 3: limite de tentativas da senha de transação.
--
-- A senha de transação tem 4 a 6 dígitos (no máximo 1 milhão de combinações).
-- Sem limite de tentativas ela cai por força bruta; após 3 erros seguidos fica
-- bloqueada por 15 minutos.
-- =============================================================================

ALTER TABLE usuario
    ADD COLUMN tentativas_senha_transacao    INT          NOT NULL DEFAULT 0,
    ADD COLUMN senha_transacao_bloqueada_ate TIMESTAMP(6) NULL;

-- Consultas de idempotência e de comprovante: lançamento de uma operação específica.
CREATE INDEX ix_lancamento_operacao ON lancamento (tipo, operacao_id);
