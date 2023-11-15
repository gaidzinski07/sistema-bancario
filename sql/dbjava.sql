
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

DROP TABLE IF EXISTS `transacoes`;
DROP TABLE IF EXISTS `contas`;
DROP TABLE IF EXISTS `contasusuario`;
DROP TABLE IF EXISTS `usuarios`;
--
-- Estrutura da tabela `contasusuario`
--
CREATE TABLE IF NOT EXISTS `contasusuario` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_user` int NOT NULL,
  `tipoconta` int NOT NULL,
  `agencia` int NOT NULL,
  `conta` int NOT NULL,
  `saldo` real not NULL,
  PRIMARY KEY (`id`),
  KEY `FK_user` (`id_user`),
  KEY `FK_tipo` (`tipoconta`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estrutura da tabela `contas`
--
CREATE TABLE IF NOT EXISTS `contas` (
  --`id` int NOT NULL AUTO_INCREMENT,
  `tipo` int NOT NULL AUTO_INCREMENT,
  `descricao` varchar (255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`tipo`)  
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Estrutura da tabela `usuarios`
--
CREATE TABLE IF NOT EXISTS `usuarios` (
  `id` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `cpf` varchar(14) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `senha` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `endereco` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`)  
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Estrutura da tabela `transacao`
--


CREATE TABLE IF NOT EXISTS `transacoes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `id_user` int NOT NULL,
  `agenciain` int NOT NULL,
  `agenciaout` int NOT NULL,
  `contain` int NOT NULL,
  `contatipoin` int NOT NULL,
  `contaout` int NOT NULL,
  `contatipoout` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_usuario` (`id_user`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


--
-- Restrições para despejos de tabelas
--
--
-- Limitadores para a tabelas`
--

ALTER TABLE `contasusuario`
  ADD CONSTRAINT `FK_user` FOREIGN KEY (`id_user`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `FK_tipo` FOREIGN KEY (`tipoconta`) REFERENCES `contas` (`tipo`);
COMMIT;
ALTER TABLE `transacoes`
  ADD CONSTRAINT `FK_usuario` FOREIGN KEY (`id_user`) REFERENCES `usuarios` (`id`);  
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
