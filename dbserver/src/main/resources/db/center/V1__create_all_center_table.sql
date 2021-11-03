# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 127.0.0.1 (MySQL 5.7.29-log)
# Database: center
# Generation Time: 2021-11-03 02:46:55 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table CenterCrossFight
# ------------------------------------------------------------

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



# Dump of table CenterGlobalHistory
# ------------------------------------------------------------

CREATE TABLE `CenterGlobalHistory` (
  `id` varchar(255) NOT NULL,
  `ids` longtext,
  `timespace` int(11) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table CenterGlobalInfo
# ------------------------------------------------------------

CREATE TABLE `CenterGlobalInfo` (
  `id` varchar(255) NOT NULL,
  `content` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table CenterGlobalRecord
# ------------------------------------------------------------

CREATE TABLE `CenterGlobalRecord` (
  `id` varchar(255) NOT NULL,
  `content` longtext,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table CenterMessaging
# ------------------------------------------------------------

CREATE TABLE `CenterMessaging` (
  `id` bigint(20) NOT NULL,
  `addresses` longtext,
  `content` longtext,
  `createTime` datetime DEFAULT NULL,
  `eventName` varchar(255) DEFAULT NULL,
  `lastErrorMessage` varchar(255) DEFAULT NULL,
  `lastTryTime` datetime DEFAULT NULL,
  `nextTryTime` datetime DEFAULT NULL,
  `originServerIds` longtext,
  `success` bit(1) NOT NULL,
  `times` int(11) NOT NULL,
  `waitForSend` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table CenterMutation
# ------------------------------------------------------------

CREATE TABLE `CenterMutation` (
  `id` varchar(255) NOT NULL,
  `maxFight` bigint(20) NOT NULL,
  `nextResetTime` bigint(20) NOT NULL,
  `rank` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table CenterMYSD
# ------------------------------------------------------------

CREATE TABLE `CenterMYSD` (
  `id` int(11) NOT NULL,
  `bosses` longtext,
  `owners` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table CenterPeakArena
# ------------------------------------------------------------

CREATE TABLE `CenterPeakArena` (
  `id` int(11) NOT NULL,
  `end` bit(1) NOT NULL,
  `gamerFormation` longblob,
  `guess` longtext,
  `guessResult` longtext,
  `playerTeam` longtext,
  `players` longtext,
  `season` int(11) NOT NULL,
  `stage` varchar(255) DEFAULT NULL,
  `start` bit(1) NOT NULL,
  `startTime` bigint(20) NOT NULL,
  `timeSpaceSet` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table CenterPeakArenaChampion
# ------------------------------------------------------------

CREATE TABLE `CenterPeakArenaChampion` (
  `id` int(11) NOT NULL,
  `consumeBattle` longtext,
  `gamerGroup` longtext,
  `rank` longtext,
  `season` int(11) NOT NULL,
  `stageBattle` longtext,
  `stageType` varchar(255) DEFAULT NULL,
  `start` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table CenterPeakArenaTeamFight
# ------------------------------------------------------------

CREATE TABLE `CenterPeakArenaTeamFight` (
  `id` int(11) NOT NULL,
  `fight` bit(1) NOT NULL,
  `gamerBattle` longtext,
  `rank` longtext,
  `season` int(11) NOT NULL,
  `stageType` varchar(255) DEFAULT NULL,
  `start` bit(1) NOT NULL,
  `teamGameBattle` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table CenterPeakChampionBattle
# ------------------------------------------------------------

CREATE TABLE `CenterPeakChampionBattle` (
  `id` bigint(20) NOT NULL,
  `battleInfos` longtext,
  `gameIndex` int(11) NOT NULL,
  `reportUrl` longtext,
  `result` varchar(255) DEFAULT NULL,
  `resultList` longtext,
  `summaryList` longtext,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table CenterPeakTeamBattle
# ------------------------------------------------------------

CREATE TABLE `CenterPeakTeamBattle` (
  `id` bigint(20) NOT NULL,
  `battleInfos` longtext,
  `reportUrl` longtext,
  `result` varchar(255) DEFAULT NULL,
  `resultList` longtext,
  `summaryList` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table CenterRank
# ------------------------------------------------------------

CREATE TABLE `CenterRank` (
  `id` varchar(255) NOT NULL,
  `content` longtext,
  `history` longtext,
  `nextDrawTime` datetime DEFAULT NULL,
  `timeSpace` int(11) NOT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table CenterRankMap
# ------------------------------------------------------------

CREATE TABLE `CenterRankMap` (
  `id` int(11) NOT NULL,
  `rankMap` longtext,
  `resetTime` datetime DEFAULT NULL,
  `version` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table CenterSingelBattleTimes
# ------------------------------------------------------------

CREATE TABLE `CenterSingelBattleTimes` (
  `id` bigint(20) NOT NULL,
  `date` datetime DEFAULT NULL,
  `ok` int(11) NOT NULL,
  `play` int(11) NOT NULL,
  `reference` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table CenterSingleBattle
# ------------------------------------------------------------

CREATE TABLE `CenterSingleBattle` (
  `id` bigint(20) NOT NULL,
  `addition` varchar(255) DEFAULT NULL,
  `allResult` longtext,
  `battleInfos` longtext,
  `path` varchar(255) DEFAULT NULL,
  `reportId` varchar(255) DEFAULT NULL,
  `reportSummary` longtext,
  `result` varchar(255) DEFAULT NULL,
  `time` datetime DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table ChallengeBoss
# ------------------------------------------------------------

CREATE TABLE `ChallengeBoss` (
  `id` int(11) NOT NULL,
  `damageRecord` longtext,
  `open` bit(1) NOT NULL,
  `season` int(11) NOT NULL,
  `stageType` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table Corps
# ------------------------------------------------------------

CREATE TABLE `Corps` (
  `id` bigint(20) NOT NULL,
  `active` int(11) NOT NULL,
  `activeRecord` longtext,
  `applyLevelLimit` int(11) NOT NULL,
  `applyList` longtext,
  `autoApprove` bit(1) NOT NULL,
  `createdTime` bigint(20) NOT NULL,
  `creator` bigint(20) NOT NULL,
  `donateList` longtext,
  `exp` int(11) NOT NULL,
  `houses` longtext,
  `icon` int(11) NOT NULL,
  `jobs` longtext,
  `lastDailyResetTime` bigint(20) NOT NULL,
  `level` int(11) NOT NULL,
  `logoutRecord` longtext,
  `memberNameCache` longtext,
  `members` longtext,
  `name` varchar(255) DEFAULT NULL,
  `nextRefreshTime` bigint(20) NOT NULL,
  `post` varchar(255) DEFAULT NULL,
  `server` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table CorpsRecord
# ------------------------------------------------------------

CREATE TABLE `CorpsRecord` (
  `id` bigint(20) NOT NULL,
  `records` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table CrossFightFirstHalf
# ------------------------------------------------------------

CREATE TABLE `CrossFightFirstHalf` (
  `id` int(11) NOT NULL,
  `gameTypeMap` longtext,
  `rank` longtext,
  `season` int(11) NOT NULL,
  `stageType` varchar(255) DEFAULT NULL,
  `start` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table CrossFightSecondHalf
# ------------------------------------------------------------

CREATE TABLE `CrossFightSecondHalf` (
  `id` int(11) NOT NULL,
  `holdBreakTime` longtext,
  `holdInfo` longblob,
  `openFight` bit(1) NOT NULL,
  `openFormation` bit(1) NOT NULL,
  `playerScore` longtext,
  `rankMap` longtext,
  `season` int(11) NOT NULL,
  `signUpInfo` longblob,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table GlobalHeroComment
# ------------------------------------------------------------

CREATE TABLE `GlobalHeroComment` (
  `id` int(11) NOT NULL,
  `hotCommentRank` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table HeroComment
# ------------------------------------------------------------

CREATE TABLE `HeroComment` (
  `id` int(11) NOT NULL,
  `hotRank` longtext,
  `idGenerator` longtext,
  `latestRank` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table HotFormation
# ------------------------------------------------------------

CREATE TABLE `HotFormation` (
  `id` int(11) NOT NULL,
  `hotFormations` longtext,
  `nextResetTime` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table HotHero
# ------------------------------------------------------------

CREATE TABLE `HotHero` (
  `id` varchar(255) NOT NULL,
  `hotHeroes` longtext,
  `latestUpdateTime` bigint(20) NOT NULL,
  `totalTimes` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table Mineral
# ------------------------------------------------------------

CREATE TABLE `Mineral` (
  `id` int(11) NOT NULL,
  `end` bit(1) NOT NULL,
  `mineralInfoMap` longtext,
  `mineralPlayerInfos` longtext,
  `season` int(11) NOT NULL,
  `stageType` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table SequenceIdGenerator
# ------------------------------------------------------------

CREATE TABLE `SequenceIdGenerator` (
  `id` varchar(255) NOT NULL,
  `idGenerator` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table Stronghold
# ------------------------------------------------------------

CREATE TABLE `Stronghold` (
  `id` int(11) NOT NULL,
  `enemies` longblob,
  `open` bit(1) NOT NULL,
  `season` int(11) NOT NULL,
  `stageType` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table SweepEnemy
# ------------------------------------------------------------

CREATE TABLE `SweepEnemy` (
  `id` int(11) NOT NULL,
  `enemies` longblob,
  `open` bit(1) NOT NULL,
  `season` int(11) NOT NULL,
  `stageType` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table TimeSpace
# ------------------------------------------------------------

CREATE TABLE `TimeSpace` (
  `id` int(11) NOT NULL,
  `lastServerOpenTime` datetime DEFAULT NULL,
  `servers` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



# Dump of table TimeSpaceServer
# ------------------------------------------------------------

CREATE TABLE `TimeSpaceServer` (
  `id` varchar(255) NOT NULL,
  `openTime` datetime DEFAULT NULL,
  `serverIds` longtext,
  `timeSpace` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
