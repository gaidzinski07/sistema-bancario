
-- Host: 127.0.0.1:3306
-- Tempo de geração: 15-NOV-2023 às 13:35


SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Banco de dados: `dbjava`
--

-- --------------------------------------------------------
--DROP TABLE IF EXISTS

--To Disable a Constraint at DB level

--EXEC sp_MSForEachTable 'ALTER TABLE ? NOCHECK CONSTRAINT ALL'

DROP TABLE IF EXISTS `Administrador`;
DROP TABLE IF EXISTS `Contasbancaria`;
DROP TABLE IF EXISTS `Fundo`;
DROP TABLE IF EXISTS `Saque`;
DROP TABLE IF EXISTS `Deposito`;
DROP TABLE IF EXISTS `Transferencia`;
DROP TABLE IF EXISTS `Investimento`;
--ALTER TABLE Usuarios NOCHECK CONSTRAINT MyConstraint;
ALTER TABLE `cliente` DROP FOREIGN KEY `FK_user_cli`; 
DROP TABLE IF EXISTS `Cliente`;
DROP TABLE IF EXISTS `Usuarios`;
DROP TABLE IF EXISTS `Agencia`;

--To Enable a Constraint at DB level

--EXEC sp_MSForEachTable 'ALTER TABLE ? CHECK CONSTRAINT ALL'
--
-- Estrutura da tabela `Usuarios`
--
CREATE TABLE IF NOT EXISTS `Usuarios` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `cpf` varchar(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `senha` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`)  
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
--
-- Estrutura da tabela `Administrador`
--
CREATE TABLE IF NOT EXISTS `Administrador` (
    `id_usuario` int NOT NULL,
    `tipo_administrador` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  KEY `FK_user_adm` (`id_usuario`)
)ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
--
-- Estrutura da tabela `Cliente`
--
CREATE TABLE IF NOT EXISTS `Cliente` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_usuario` int NOT NULL,
  `tipo_cliente` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_user_cli` (`id_usuario`)
  ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
--
-- Estrutura da tabela `Agencia`
--
CREATE TABLE IF NOT EXISTS `Agencia` (
  `id` int NOT NULL AUTO_INCREMENT,
  `endereco` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`)
  ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
--
-- Estrutura da tabela `ContaBancaria`
--
CREATE TABLE IF NOT EXISTS `Contasbancaria` (
  --`id` int NOT NULL AUTO_INCREMENT,
  `conta_corrente` int NOT NULL,
  `conta_poupanca` int NOT NULL,
  `id_cliente`  int NOT NULL,
  `saldo_atual` float NOT NULL,
  `id_agencia` int NOT NULL,
  PRIMARY KEY (`conta_corrente`), 
  KEY `FK_c` (`id_cliente`),
  KEY `FK_g` (id_agencia)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
--
-- Estrutura da tabela `Fundo`
--
CREATE TABLE IF NOT EXISTS `Fundo` (
  `id_fundo` int NOT NULL,
  `valor_cota` float NOT NULL,
  `valor_minimo` float NOT NULL,
  PRIMARY KEY (`id_fundo`)  
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
--
-- Estrutura da tabela `Saque`
--
CREATE TABLE IF NOT EXISTS `Saque` (
  `id_saque` int NOT NULL,
  `id_usuario` int NOT NULL,
  `id_agencia` int NOT NULL,
  `data` timestamp NOT NULL,
  `local` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `valor` float NOT NULL,
   PRIMARY KEY (`id_saque`),
   KEY `FK_user_saq` (`id_usuario`),
   KEY `FK_sg` (id_agencia)
  ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
--
-- Estrutura da tabela `Deposito`
--
CREATE TABLE IF NOT EXISTS `Deposito` (
  `id_deposito` int NOT NULL,
  `id_usuario_destino` int NOT NULL,
  `data` timestamp NOT NULL,
  `local` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `id_agencia` int NOT NULL,
  `valor` float NOT NULL,
   PRIMARY KEY (`id_deposito`),
   KEY `FK_user_dep` (`id_usuario_destino`),
   KEY `FK_sg` (id_agencia) 
  ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
--
-- Estrutura da tabela `Transferencia`
--
CREATE TABLE IF NOT EXISTS `Transferencia` (
  `id_transferencia` int NOT NULL,
  `id_usuario_origem` int NOT NULL,
  `id_usuario_destino` int NOT NULL,
  `data` timestamp NOT NULL,
  `valor` float NOT NULL,
   PRIMARY KEY (`id_transferencia`),
   KEY `FK_user_o` (`id_usuario_origem`), 
   KEY `FK_user_d` (`id_usuario_destino`) 
  ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
--
-- Estrutura da tabela `Investimento`
--
CREATE TABLE IF NOT EXISTS `Investimento` (
  `id_investimento` int NOT NULL,
  `id_cliente` int NOT NULL,
  `id_fundo` int NOT NULL,
  `vr_cota_inicio` float NOT NULL,
  `vr_investido` float NOT NULL,
  `qtd_cotas` float NOT NULL,
   PRIMARY KEY (`id_investimento`),
   KEY `FK_user_inv` (`id_cliente`), 
   KEY `FK_fundo` (id_fundo)
   ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
--
-- Restrições para despejos de tabelas
--
--
-- Limitadores para a tabelas
--
ALTER TABLE `Administrador`
  ADD CONSTRAINT `FK_user_adm` FOREIGN KEY (`id_usuario`) REFERENCES `Usuarios` (`id`);
COMMIT;
ALTER TABLE `Cliente`
  ADD CONSTRAINT `FK_user_cli` FOREIGN KEY (`id_usuario`) REFERENCES `Usuarios` (`id`);
COMMIT;
ALTER TABLE `Contasbancaria`
  ADD CONSTRAINT `FK_c` FOREIGN KEY (`id_cliente`) REFERENCES `Cliente` (`id`),
  ADD CONSTRAINT `FK_g` FOREIGN KEY (`id_agencia`) REFERENCES `Agencia` (`id`);
COMMIT;
ALTER TABLE `Saque`
  ADD CONSTRAINT `FK_user_saq` FOREIGN KEY (`id_usuario`) REFERENCES `Usuarios` (`id`),
  ADD CONSTRAINT `FK_sg` FOREIGN KEY (`id_agencia`) REFERENCES `Agencia` (`id`);
COMMIT;
ALTER TABLE `Deposito`
  ADD CONSTRAINT `FK_user_dep` FOREIGN KEY (`id_usuario_destino`) REFERENCES `Usuarios` (`id`),
  ADD CONSTRAINT `FK_dg` FOREIGN KEY (`id_agencia`) REFERENCES `Agencia` (`id`);
COMMIT;
ALTER TABLE `Transferencia`
  ADD CONSTRAINT `FK_user_o` FOREIGN KEY (`id_usuario_origem`) REFERENCES `Usuarios` (`id`),  
  ADD CONSTRAINT `FK_user_d` FOREIGN KEY (`id_usuario_destino`) REFERENCES `Usuarios` (`id`);
COMMIT;
ALTER TABLE `Investimento`
  ADD CONSTRAINT `FK_user_inv` FOREIGN KEY (`id_cliente`) REFERENCES `Usuarios` (`id`) ,
  ADD CONSTRAINT `FK_fundo` FOREIGN KEY (`id_fundo`) REFERENCES `Fundo` (`id_fundo`) ;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
