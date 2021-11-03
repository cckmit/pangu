# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 127.0.0.1 (MySQL 5.7.29-log)
# Database: shennu
# Generation Time: 2021-11-03 02:49:05 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table Account
# ------------------------------------------------------------

CREATE TABLE `Account` (
  `id` bigint(20) NOT NULL,
  `IMEI` varchar(255) DEFAULT NULL,
  `adult` bit(1) NOT NULL,
  `channel` int(11) NOT NULL,
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



# Dump of table ActionPoint
# ------------------------------------------------------------

CREATE TABLE `ActionPoint` (
  `id` bigint(20) NOT NULL,
  `drawRecord` longtext,
  `points` longtext,
  `resetTime` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table ChallengeTimes
# ------------------------------------------------------------

CREATE TABLE `ChallengeTimes` (
  `id` varchar(255) NOT NULL,
  `buyTimes` int(11) NOT NULL,
  `challengeTimes` int(11) NOT NULL,
  `nextResetTimes` bigint(20) NOT NULL,
  `owner` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table ChargeLog
# ------------------------------------------------------------

CREATE TABLE `ChargeLog` (
  `id` bigint(20) NOT NULL,
  `account` varchar(255) DEFAULT NULL,
  `addition` varchar(255) DEFAULT NULL,
  `createAt` datetime(6) DEFAULT NULL,
  `gold` int(11) NOT NULL,
  `ip` varchar(255) DEFAULT NULL,
  `money` int(11) NOT NULL,
  `orderId` varchar(255) DEFAULT NULL,
  `player` varchar(255) DEFAULT NULL,
  `target` bigint(20) NOT NULL,
  `test` bit(1) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `IDX_CHARGELOG_TARGET` (`target`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table ChargeOrder
# ------------------------------------------------------------

CREATE TABLE `ChargeOrder` (
  `id` varchar(255) NOT NULL,
  `amount` int(11) NOT NULL,
  `completeAt` datetime(6) DEFAULT NULL,
  `completeIP` varchar(255) DEFAULT NULL,
  `createIP` varchar(255) DEFAULT NULL,
  `createdAt` datetime(6) DEFAULT NULL,
  `deal` bit(1) NOT NULL,
  `goods` varchar(255) DEFAULT NULL,
  `money` int(11) NOT NULL,
  `orderId` varchar(255) DEFAULT NULL,
  `serial` bigint(20) NOT NULL,
  `target` bigint(20) NOT NULL,
  `worth` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table ChargeRecord
# ------------------------------------------------------------

CREATE TABLE `ChargeRecord` (
  `id` bigint(20) NOT NULL,
  `chargeDetail` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table Count
# ------------------------------------------------------------

CREATE TABLE `Count` (
  `id` bigint(20) NOT NULL,
  `countRecords` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table EntityIdGenerator
# ------------------------------------------------------------

CREATE TABLE `EntityIdGenerator` (
  `id` varchar(255) NOT NULL,
  `idMaxes` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table Equip
# ------------------------------------------------------------

CREATE TABLE `Equip` (
  `id` bigint(20) NOT NULL,
  `baseId` int(11) NOT NULL,
  `exp` int(11) NOT NULL,
  `grade` int(11) NOT NULL,
  `level` int(11) NOT NULL,
  `owner` bigint(20) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  `wearer` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `Equip_owner` (`owner`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table GlobalDraw
# ------------------------------------------------------------

CREATE TABLE `GlobalDraw` (
  `id` bigint(20) NOT NULL,
  `content` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table GlobalGift
# ------------------------------------------------------------

CREATE TABLE `GlobalGift` (
  `id` varchar(255) NOT NULL,
  `agent` varchar(255) DEFAULT NULL,
  `conditionDraw` longtext,
  `conditionShow` longtext,
  `conditions` longtext,
  `info` longtext,
  `name` varchar(255) DEFAULT NULL,
  `onlineTimes` int(11) DEFAULT NULL,
  `onlineType` int(11) DEFAULT NULL,
  `prev` varchar(255) DEFAULT NULL,
  `disable` int(11) NOT NULL,
  `endTime` datetime(6) DEFAULT NULL,
  `giftType` int(11) DEFAULT NULL,
  `repeatable` bit(1) NOT NULL,
  `rewardContent` longtext,
  `rewardType` int(11) DEFAULT NULL,
  `startTime` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table GlobalInfo
# ------------------------------------------------------------

CREATE TABLE `GlobalInfo` (
  `id` varchar(255) NOT NULL,
  `content` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table GlobalPromotion
# ------------------------------------------------------------

CREATE TABLE `GlobalPromotion` (
  `id` varchar(255) NOT NULL,
  `cause` int(11) DEFAULT NULL,
  `configVersion` int(11) NOT NULL,
  `content` longtext,
  `destroy` datetime(6) DEFAULT NULL,
  `end` datetime(6) DEFAULT NULL,
  `period` int(11) NOT NULL,
  `revertVersion` int(11) NOT NULL,
  `start` datetime(6) DEFAULT NULL,
  `status` longtext,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table GlobalRecord
# ------------------------------------------------------------

CREATE TABLE `GlobalRecord` (
  `type` varchar(255) NOT NULL,
  `content` longtext,
  PRIMARY KEY (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table GroupMail
# ------------------------------------------------------------

CREATE TABLE `GroupMail` (
  `id` bigint(20) NOT NULL,
  `attachment` longtext,
  `content` longtext,
  `receiver` varchar(255) DEFAULT NULL,
  `sender` varchar(255) DEFAULT NULL,
  `template` int(11) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `createTime` datetime(6) DEFAULT NULL,
  `destoryTime` datetime(6) DEFAULT NULL,
  `senderId` bigint(20) DEFAULT NULL,
  `system` bit(1) NOT NULL,
  `state` int(11) NOT NULL,
  `target` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `GroupMail_target` (`target`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table Hero
# ------------------------------------------------------------

CREATE TABLE `Hero` (
  `id` bigint(20) NOT NULL,
  `baseId` int(11) NOT NULL,
  `equips` longtext,
  `grade` int(11) NOT NULL,
  `level` int(11) NOT NULL,
  `owner` bigint(20) NOT NULL,
  `star` int(11) NOT NULL,
  `talents` longtext,
  `validTime` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `Hero_Owner` (`owner`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table Item
# ------------------------------------------------------------

CREATE TABLE `Item` (
  `id` bigint(20) NOT NULL,
  `amount` int(11) NOT NULL,
  `baseId` int(11) NOT NULL,
  `content` longtext,
  `owner` bigint(20) NOT NULL,
  `timeLine` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `Item_owner` (`owner`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table Lottery
# ------------------------------------------------------------

CREATE TABLE `Lottery` (
  `id` varchar(255) NOT NULL,
  `additionInfo` longtext,
  `lotteryTimes` int(11) NOT NULL,
  `nextFreeTimes` longtext,
  `playerId` bigint(20) NOT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table LotteryRecord
# ------------------------------------------------------------

CREATE TABLE `LotteryRecord` (
  `id` varchar(255) NOT NULL,
  `records` longtext,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table MailBox
# ------------------------------------------------------------

CREATE TABLE `MailBox` (
  `id` bigint(20) NOT NULL,
  `groupStates` longtext,
  `lastSentTime` datetime(6) DEFAULT NULL,
  `receiverStates` longtext,
  `senderStates` longtext,
  `sentSize` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table Mall
# ------------------------------------------------------------

CREATE TABLE `Mall` (
  `id` bigint(20) NOT NULL,
  `mallRecords` longtext,
  `nextResetTimes` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table OffLineMessage
# ------------------------------------------------------------

CREATE TABLE `OffLineMessage` (
  `id` bigint(20) NOT NULL,
  `messages` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table PersonalRecord
# ------------------------------------------------------------

CREATE TABLE `PersonalRecord` (
  `id` bigint(20) NOT NULL,
  `content` longtext,
  `owner` bigint(20) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `PersonalRecord_Owner` (`owner`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table PictorialBook
# ------------------------------------------------------------

CREATE TABLE `PictorialBook` (
  `id` varchar(255) NOT NULL,
  `owner` bigint(20) NOT NULL,
  `pictorialBooksJson` longtext,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table Player
# ------------------------------------------------------------

CREATE TABLE `Player` (
  `id` bigint(20) NOT NULL,
  `block` bit(1) NOT NULL,
  `exp` bigint(20) NOT NULL,
  `fight` bigint(20) NOT NULL,
  `level` int(11) NOT NULL,
  `maxFight` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  `renamed` bit(1) NOT NULL,
  `sex` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_pa46tl9f0gkueon0sd2qamcsx` (`name`),
  KEY `Player_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table PlayerGift
# ------------------------------------------------------------

CREATE TABLE `PlayerGift` (
  `id` bigint(20) NOT NULL,
  `drawHistory` longtext,
  `timeHistory` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table PlayerInfo
# ------------------------------------------------------------

CREATE TABLE `PlayerInfo` (
  `id` bigint(20) NOT NULL,
  `clientInfo` longtext,
  `contents` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table Promotion
# ------------------------------------------------------------

CREATE TABLE `Promotion` (
  `id` bigint(20) NOT NULL,
  `finishedUserPromotions` longtext,
  `items` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table PromotionItemRecord
# ------------------------------------------------------------

CREATE TABLE `PromotionItemRecord` (
  `id` varchar(255) NOT NULL,
  `content` longtext,
  `drawn` longtext,
  `owner` bigint(20) NOT NULL,
  `promotionId` varchar(255) DEFAULT NULL,
  `revertTime` datetime(6) DEFAULT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table Rank
# ------------------------------------------------------------

CREATE TABLE `Rank` (
  `id` varchar(255) NOT NULL,
  `content` longtext,
  `history` longtext,
  `nextDrawTime` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table Sociality
# ------------------------------------------------------------

CREATE TABLE `Sociality` (
  `id` bigint(20) NOT NULL,
  `blacks` longtext,
  `friends` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table SocialityApply
# ------------------------------------------------------------

CREATE TABLE `SocialityApply` (
  `id` bigint(20) NOT NULL,
  `beApplyIds` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table Task
# ------------------------------------------------------------

CREATE TABLE `Task` (
  `id` bigint(20) NOT NULL,
  `contents` longtext,
  `end` datetime(6) DEFAULT NULL,
  `expire` datetime(6) DEFAULT NULL,
  `finished` datetime(6) DEFAULT NULL,
  `owner` bigint(20) NOT NULL,
  `start` datetime(6) DEFAULT NULL,
  `state` int(11) DEFAULT NULL,
  `taskId` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `Task_owner` (`owner`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table TaskLog
# ------------------------------------------------------------

CREATE TABLE `TaskLog` (
  `id` bigint(20) NOT NULL,
  `finishedInfos` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table UserCycle
# ------------------------------------------------------------

CREATE TABLE `UserCycle` (
  `id` bigint(20) NOT NULL,
  `hits` longtext,
  `content` longtext,
  `versions` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table UserGift
# ------------------------------------------------------------

CREATE TABLE `UserGift` (
  `id` bigint(20) NOT NULL,
  `info` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `owner` bigint(20) NOT NULL,
  `content` longtext,
  `type` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `UserGift_owner` (`owner`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table UserMail
# ------------------------------------------------------------

CREATE TABLE `UserMail` (
  `id` bigint(20) NOT NULL,
  `attachment` longtext,
  `content` longtext,
  `receiver` varchar(255) DEFAULT NULL,
  `sender` varchar(255) DEFAULT NULL,
  `template` int(11) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `createTime` datetime(6) DEFAULT NULL,
  `destoryTime` datetime(6) DEFAULT NULL,
  `senderId` bigint(20) DEFAULT NULL,
  `system` bit(1) NOT NULL,
  `receiverId` bigint(20) NOT NULL,
  `state` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table UserSeed
# ------------------------------------------------------------

CREATE TABLE `UserSeed` (
  `id` bigint(20) NOT NULL,
  `content` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table UserTimeSeed
# ------------------------------------------------------------

CREATE TABLE `UserTimeSeed` (
  `id` bigint(20) NOT NULL,
  `timeSeeds` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table Vip
# ------------------------------------------------------------

CREATE TABLE `Vip` (
  `id` bigint(20) NOT NULL,
  `charge` int(11) NOT NULL,
  `cost` int(11) NOT NULL,
  `dayCharge` int(11) NOT NULL,
  `dayCost` int(11) NOT NULL,
  `dayFirstMoney` int(11) NOT NULL,
  `dayGift` int(11) NOT NULL,
  `dayGoldCost` int(11) NOT NULL,
  `dayInter` int(11) NOT NULL,
  `dayMoney` int(11) NOT NULL,
  `exp` int(11) NOT NULL,
  `firstCharge` int(11) NOT NULL,
  `firstChargeTime` datetime(6) DEFAULT NULL,
  `firstCost` datetime(6) DEFAULT NULL,
  `firstGoods` varchar(255) DEFAULT NULL,
  `gift` bigint(20) NOT NULL,
  `goldCost` int(11) NOT NULL,
  `inter` int(11) NOT NULL,
  `lastChargeTime` datetime(6) DEFAULT NULL,
  `lastCost` datetime(6) DEFAULT NULL,
  `money` int(11) NOT NULL,
  `resetTime` datetime(6) DEFAULT NULL,
  `vip` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `Vip_firstChargeTime` (`firstChargeTime`),
  KEY `Vip_lastChargeTime` (`lastChargeTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table Wallet
# ------------------------------------------------------------

CREATE TABLE `Wallet` (
  `id` bigint(20) NOT NULL,
  `copper` int(11) NOT NULL,
  `currencyCount` longtext,
  `gift` int(11) NOT NULL,
  `gold` int(11) NOT NULL,
  `inter` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
