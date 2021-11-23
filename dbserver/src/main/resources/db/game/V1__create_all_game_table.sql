
CREATE TABLE `Account` (
  `id` bigint(20) NOT NULL,
  `IMEI` varchar(255) DEFAULT NULL,
  `adult` bit(1) NOT NULL,
  `channel` varchar(255) NOT NULL,
  `createIp` varchar(255) DEFAULT NULL,
  `createdOn` datetime(6) NOT NULL,
  `dayByContinuous` int(11) NOT NULL,
  `dayByTotal` int(11) NOT NULL,
  `device` varchar(255) DEFAULT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `loginByTotal` int(11) NOT NULL,
  `loginOn` datetime(6) DEFAULT NULL,
  `logoutOn` datetime(6) DEFAULT NULL,
  `nChannel` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `online` bit(1) NOT NULL,
  `state` int(11) NOT NULL,
  `timeByDay` bigint(20) NOT NULL,
  `timeByTotal` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_5ymbq1k3unixgcda1p89qnjv0` (`name`),
  KEY `Account_name` (`name`),
  KEY `IDX_ACCOUNT_CHANNEL` (`channel`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
