
INSERT INTO dbjava.Agencia (endereco) 
	VALUES ('Niterói');

DELETE FROM usuarios;
INSERT INTO usuarios (nome, cpf, senha) 
	VALUES ('LuizGustavo', '00858', '123');
INSERT INTO usuarios (nome, cpf, senha) 
	VALUES ('Joao', '00555', '123');
INSERT INTO usuarios (nome, cpf, senha) 
	VALUES ('Lucas', '00777', '123');
INSERT INTO usuarios (nome, cpf, senha) 
	VALUES ('usuario', '00111', '123');
INSERT INTO usuarios (nome, cpf, senha) 
	VALUES ('usuario', '00111', '123');

INSERT INTO administrador (id_usuario, tipo_administrador) 
	VALUES (1, 'cadastrador');
INSERT INTO administrador (id_usuario, tipo_administrador) 
	VALUES (2, 'cadastrador');
INSERT INTO administrador (id_usuario, tipo_administrador) 
	VALUES (3, 'cadastrador');

INSERT INTO cliente (id_usuario, tipo_cliente) 
	VALUES (1, 'Correntista');
INSERT INTO cliente (id_usuario, tipo_cliente) 
	VALUES (4, 'Correntista');