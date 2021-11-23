CREATE TABLE `CenterCrossFight` (
  `id` int(11) NOT NULL,
  `end` bit(1) NOT NULL,
  `players` longtext,
  `season` int(11) NOT NULL,
  `stage` varchar(255) DEFAULT NULL,
  `start` bit(1) NOT NULL,
  `startTime` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;