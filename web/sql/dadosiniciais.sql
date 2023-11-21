INSERT INTO mysql.Agencia (endereco) 
	VALUES ('Niterói');

--DELETE FROM usuarios;
INSERT INTO mysql.usuarios (nome, cpf, senha) 
	VALUES ('LuizGustavo', '00858', '123');
INSERT INTO mysql.usuarios (nome, cpf, senha) 
	VALUES ('Joao', '00555', '123');
INSERT INTO mysql.usuarios (nome, cpf, senha) 
	VALUES ('Lucas', '00777', '123');
INSERT INTO mysql.usuarios (nome, cpf, senha) 
	VALUES ('usuario', '00111', '123');
INSERT INTO mysql.usuarios (nome, cpf, senha) 
	VALUES ('usuario', '00111', '123');

INSERT INTO mysql.administrador (id_usuario, tipo_administrador) 
	VALUES (1, 'cadastrador');
INSERT INTO mysql.administrador (id_usuario, tipo_administrador) 
	VALUES (2, 'cadastrador');
INSERT INTO mysql.administrador (id_usuario, tipo_administrador) 
	VALUES (3, 'cadastrador');

INSERT INTO mysql.cliente (id_usuario, tipo_cliente) 
	VALUES (1, 'Correntista');
INSERT INTO mysql.cliente (id_usuario, tipo_cliente) 
	VALUES (4, 'Correntista');


INSERT INTO mysql.agencia (endereco) 
	VALUES ('Rua Presidente Backer 172');


INSERT INTO mysql.contasbancaria (conta_corrente, conta_poupanca, id_cliente, saldo_atual, id_agencia) 
	VALUES (100, 101, 1, 500.35, 1);


INSERT INTO mysql.fundo (id_fundo, valor_cota, valor_minimo) 
VALUES (1, 10.50, 1000.00);

INSERT INTO mysql.fundo (id_fundo, valor_cota, valor_minimo) 
VALUES (2, 8.75, 800.00);

INSERT INTO mysql.fundo(id_fundo, valor_cota, valor_minimo) 
VALUES (3, 15.25, 1500.00);

INSERT INTO mysql.fundo (id_fundo, valor_cota, valor_minimo) 
VALUES (4, 12.00, 1200.00);

