INSERT INTO mysql.contas (tipo, descricao) 
	VALUES (1, 'CONTA CORRENTE');
INSERT INTO mysql.contas (tipo, descricao) 
	VALUES (2, 'CONTA POUPANÇA');
INSERT INTO mysql.contas (tipo, descricao) 
	VALUES (3, 'CONTA INVESTIMENTO');

DELETE FROM usuarios;
INSERT INTO mysql.usuarios (nome, cpf, senha, endereco) 
	VALUES ('Luiz Gustavo', '008580', '123', 'rua a');

DELETE FROM contasusuario;
INSERT INTO mysql.contasusuario (id_user, tipoconta, agencia, conta, saldo) 
	VALUES (1, 1, 2020, 1317, 1000.0);  --verificar

