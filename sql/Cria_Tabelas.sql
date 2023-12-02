DROP TABLE IF EXISTS `Administrador`;
DROP TABLE IF EXISTS `Contasbancaria`;
--DROP TABLE IF EXISTS `Conta_bancaria`;
--DROP TABLE IF EXISTS `Fundo`;
DROP TABLE IF EXISTS `Saque`;
DROP TABLE IF EXISTS `Deposito`;
DROP TABLE IF EXISTS `Transferencia`;
DROP TABLE IF EXISTS `Investimento`;
DROP TABLE IF EXISTS `Fundo`;
--ALTER TABLE Usuarios NOCHECK CONSTRAINT MyConstraint;
--ALTER TABLE `cliente` DROP FOREIGN KEY `FK_user_cli`; 
DROP TABLE IF EXISTS `Cliente`;
DROP TABLE IF EXISTS `Conta_bancaria`;
DROP TABLE IF EXISTS `Usuarios`;
DROP TABLE IF EXISTS `Usuario`;
DROP TABLE IF EXISTS `Agencia`;

create table AGENCIA(
	id int not null auto_increment primary key,
	endereco varchar(100)
);

create table CONTA_BANCARIA(
	conta_corrente int auto_increment,
	id_agencia int not null,
	saldo float not null default 0,
        saldoPP float not null default 0,
	FOREIGN KEY (id_agencia) REFERENCES AGENCIA(id),
	primary key (conta_corrente, id_agencia)
);

create table USUARIO(
	id int not null auto_increment primary key,
	cpf varchar(11) unique not null,
	nome varchar(100) not null,
	endereco varchar(100),
	data_nascimento Timestamp,
	senha varchar(100) not null        
);

create table ADMINISTRADOR(
	id_usuario int primary key,
	tipo_administrador varchar(20),
	FOREIGN KEY (id_usuario) REFERENCES USUARIO(id)
);
                                                                                               
create table CLIENTE(
	id_usuario int primary key,
	tipo_cliente varchar(20),
	conta_bancaria int not null unique,
	FOREIGN KEY (id_usuario) REFERENCES USUARIO(id),
	foreign key (conta_bancaria) references CONTA_BANCARIA(conta_corrente)
);

create table FUNDO(
	id int auto_increment primary key,
	nome varchar(100) not null,
	vr_cota float not null default 0,
	vr_minimo float not null default 0
);

create table INVESTIMENTO(
	id int auto_increment primary key,
	conta_bancaria int not null,
	id_fundo int not null,
	vr_cota_inicio float not null,
	vr_investido float not null,
	qtd_cotas float not null,
	FOREIGN KEY (conta_bancaria) REFERENCES CONTA_BANCARIA(conta_corrente),
	FOREIGN KEY (id_fundo) REFERENCES FUNDO(id)
);

create table SAQUE(
	id int auto_increment primary key,
	conta_bancaria int not null,
	id_agencia int not null,
	ts_saque Timestamp not null default CURRENT_TIMESTAMP,
	valor float not null,
	FOREIGN KEY (conta_bancaria) REFERENCES CONTA_BANCARIA(conta_corrente),
	FOREIGN KEY (id_agencia) REFERENCES AGENCIA(id)
);

create table DEPOSITO(
	id int auto_increment primary key,
	conta_bancaria int not null,
	id_agencia int not null,
	ts_deposito Timestamp not null default CURRENT_TIMESTAMP,
	valor float not null,
	FOREIGN KEY (conta_bancaria) REFERENCES CONTA_BANCARIA(conta_corrente),
	FOREIGN KEY (id_agencia) REFERENCES AGENCIA(id)
);

create table TRANSFERENCIA(
	id int auto_increment primary key,
	conta_bancaria_origem int not null,
	conta_bancaria_destino int not null,
	ts_transferencia Timestamp not null default CURRENT_TIMESTAMP,
	valor float not null,
	FOREIGN KEY (conta_bancaria_origem) REFERENCES CONTA_BANCARIA(conta_corrente),
	FOREIGN KEY (conta_bancaria_destino) REFERENCES CONTA_BANCARIA(conta_corrente)
);


INSERT INTO usuario (cpf, nome, endereco, data_nascimento, senha) values ('11111111111', 'João Vicente', 'Rua A, 1, Bairro A, Niterói - RJ', CURRENT_TIMESTAMP, '123'),
('22222222222', 'Lucas Magalhães', 'Rua B, 2, Bairro B, Niterói - RJ', CURRENT_TIMESTAMP, '123'),
('33333333333', 'Luiz Gustavo', 'Rua C, 3, Bairro C, Niterói - RJ', CURRENT_TIMESTAMP, '123'),
('44444444444', 'Antônio Augusto', 'Rua D, 4, Bairro D, Niterói - RJ', CURRENT_TIMESTAMP, '123');

insert into agencia (endereco) values ('Rua E, 5, Bairro E, Niterói - RJ'),
('Rua F, 6, Bairro F, Niterói - RJ'),
('Rua G, 7, Bairro G, Niterói - RJ');

insert into conta_bancaria (id_agencia) values (1), (3); 

insert into cliente (id_usuario, tipo_cliente, conta_bancaria) values (1, 'Correntista', 1),
(2, 'Investidor', 2);

insert into administrador (id_usuario, tipo_administrador) values (1, 'Gerente'), (2, 'Operador');

insert into fundo (nome, vr_cota, vr_minimo) values ('Lojas Americanas', 3.8, 1.9), ('777 Partners', 7.77, 7.77), ('Magalu', 5.2, 10.4);

insert into investimento (conta_bancaria, id_fundo, vr_cota_inicio, vr_investido, qtd_cotas) values (2, 2, 7.77, 7770, 100), (2, 1, 3.8, 11.4, 3), (1, 2, 7.77, 777, 10);

insert into deposito(conta_bancaria, id_agencia, valor) values (1, 1, 2000), (2, 3, 10000);

insert into saque(conta_bancaria, id_agencia, valor) values (1, 1, 100), (2, 2, 200);

insert into transferencia(conta_bancaria_origem, conta_bancaria_destino, valor) values (1, 2, 100), (2, 1, 200);


update conta_bancaria set saldo = 10 where conta_corrente =1; 
update conta_bancaria set saldo = 20 where conta_corrente =2;
update conta_bancaria set saldoPP = 100 where conta_corrente =1; 
update conta_bancaria set saldoPP = 200 where conta_corrente =2; 








