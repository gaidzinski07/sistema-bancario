package br.uff.banco.identidade.domain;

/**
 * Papel de autorização. Vira a authority {@code ROLE_<nome>} no Spring Security.
 * <p>
 * Substitui o uso das tabelas ADMINISTRADOR/CLIENTE do legado como mecanismo de
 * autorização (lá os dois tipos de login gravavam o mesmo atributo na sessão).
 */
public enum Papel {
    ADMIN,
    CLIENTE
}
