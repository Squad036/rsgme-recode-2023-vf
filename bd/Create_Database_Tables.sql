create DATABASE bd_sgme;

CREATE TABLE `usuarios` (
  `id` varchar(255) NOT NULL,
  `login` varchar(255) NOT NULL,
  `nome` varchar(255) NOT NULL,
  `role` tinyint DEFAULT NULL,
  `senha` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_r8oo98o39ykr4hi57md9nibmw` (`login`),
  CONSTRAINT `usuarios_chk_1` CHECK ((`role` between 0 and 1))
);

CREATE TABLE `clientes` (
  `id` varchar(255) NOT NULL,
  `cpf` varchar(14) NOT NULL,
  `data_nascimento` date NOT NULL,
  `nome` varchar(80) NOT NULL,
  `telefone` varchar(15) NOT NULL,
  `usuario_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk6iwsq3kts1bblivkjy6epajx` (`usuario_id`),
  CONSTRAINT `FKk6iwsq3kts1bblivkjy6epajx` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
);

CREATE TABLE `fornecedores` (
  `id` varchar(255) NOT NULL,
  `cnpj` varchar(14) NOT NULL,
  `nome` varchar(80) NOT NULL,
  `usuario_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKvh3gk655mbt747bpsw7mb3r7` (`usuario_id`),
  CONSTRAINT `FKvh3gk655mbt747bpsw7mb3r7` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
);


CREATE TABLE `despesas` (
  `id` varchar(255) NOT NULL,
  `data_vencimento` date NOT NULL,
  `observacao` varchar(255) DEFAULT NULL,
  `pagamento` enum('CARTAO','DINHEIRO','PIX','BOLETO') NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  `valor` double NOT NULL,
  `fornecedor_id` varchar(255) DEFAULT NULL,
  `usuario_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlvct2h8ujsplqy8braquc8oa9` (`fornecedor_id`),
  KEY `FK81p6vcta42utt15r5xt04vo84` (`usuario_id`),
  CONSTRAINT `FK81p6vcta42utt15r5xt04vo84` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FKlvct2h8ujsplqy8braquc8oa9` FOREIGN KEY (`fornecedor_id`) REFERENCES `fornecedores` (`id`)
);


CREATE TABLE `receitas` (
  `id` varchar(255) NOT NULL,
  `data_vencimento` date NOT NULL,
  `observacao` varchar(255) DEFAULT NULL,
  `pagamento` enum('CARTAO','DINHEIRO','PIX','BOLETO') NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  `valor` double NOT NULL,
  `cliente_id` varchar(255) DEFAULT NULL,
  `usuario_id` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKj1vsikbl6daix53vj7wfresao` (`cliente_id`),
  KEY `FKilaslr3e78y3tvl2l2hq6rwrp` (`usuario_id`),
  CONSTRAINT `FKilaslr3e78y3tvl2l2hq6rwrp` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `FKj1vsikbl6daix53vj7wfresao` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`)
);

