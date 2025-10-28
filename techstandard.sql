# SQL Manager 2007 for MySQL 4.2.1.1
# ---------------------------------------
# Host     : localhost
# Port     : 3306
# Database : TechStandard


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

SET FOREIGN_KEY_CHECKS=0;

USE `TechStandard`;

#
# Structure for the `accessgroups` table : 
#

DROP TABLE IF EXISTS `accessgroups`;

CREATE TABLE `accessgroups` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `grp_name` char(50) DEFAULT '',
  `description` text,
  `task_creator` tinyint(4) DEFAULT '0',
  `task_approver` tinyint(4) DEFAULT '0',
  `need_approval` tinyint(4) DEFAULT '0',
  `delete_confirmer` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

#
# Structure for the `accessrights` table : 
#

DROP TABLE IF EXISTS `accessrights`;

CREATE TABLE `accessrights` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_id` int(11) DEFAULT NULL,
  `acts` tinyint(4) DEFAULT NULL,
  `contracts` tinyint(11) DEFAULT NULL,
  `guides` tinyint(4) DEFAULT NULL,
  `requests` tinyint(4) DEFAULT NULL,
  `devices` tinyint(4) DEFAULT NULL,
  `evaluations` tinyint(4) DEFAULT NULL,
  `dictionaries` tinyint(4) DEFAULT NULL,
  `employees` tinyint(4) DEFAULT NULL,
  `rights` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

#
# Structure for the `acts` table : 
#

DROP TABLE IF EXISTS `acts`;

CREATE TABLE `acts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deleted` tinyint(4) DEFAULT '0',
  `deleted_by` int(11) DEFAULT NULL,
  `contract_id` int(11) DEFAULT NULL,
  `work_num` char(20) NOT NULL DEFAULT '' COMMENT '# of expertise, act',
  `work_date` date DEFAULT NULL,
  `obj_type` int(11) DEFAULT NULL,
  `obj_name` char(200) DEFAULT NULL,
  `obj_fnum` char(20) DEFAULT NULL,
  `obj_rnum` char(20) DEFAULT NULL,
  `work_next_date` date DEFAULT NULL,
  `completed` tinyint(1) DEFAULT NULL,
  `notes` text,
  PRIMARY KEY (`id`),
  KEY `obj_type` (`obj_type`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8 ROW_FORMAT=FIXED;

#
# Structure for the `attachments` table : 
#

DROP TABLE IF EXISTS `attachments`;

CREATE TABLE `attachments` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deleted` tinyint(4) DEFAULT '0',
  `parent_id` int(11) DEFAULT NULL,
  `parent_type` tinyint(4) DEFAULT NULL COMMENT '1-contract,2-device,3-task',
  `title` char(255) DEFAULT NULL,
  `attach_type` int(11) DEFAULT NULL,
  `filename` char(255) DEFAULT NULL,
  `saved_as` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`),
  KEY `type` (`attach_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

#
# Structure for the `chatlogs` table : 
#

DROP TABLE IF EXISTS `chatlogs`;

CREATE TABLE `chatlogs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `timestamp` bigint(20) DEFAULT NULL,
  `author` char(50) DEFAULT NULL,
  `room` char(100) DEFAULT NULL,
  `message` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8;

#
# Structure for the `clients` table : 
#

DROP TABLE IF EXISTS `clients`;

CREATE TABLE `clients` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deleted` tinyint(4) DEFAULT '0',
  `deleted_by` int(11) DEFAULT NULL,
  `name` char(200) NOT NULL DEFAULT '',
  `full_name` char(100) DEFAULT NULL,
  `boss` char(100) DEFAULT NULL,
  `address` char(255) DEFAULT NULL,
  `address2` char(255) DEFAULT NULL,
  `phone` char(30) DEFAULT NULL,
  `fax` char(30) DEFAULT NULL,
  `inn` char(20) DEFAULT NULL,
  `kpp` char(20) DEFAULT NULL,
  `email` char(50) DEFAULT NULL,
  `actual` tinyint(4) DEFAULT '0',
  `bank_name` char(100) DEFAULT NULL,
  `rsch` char(20) DEFAULT NULL,
  `ksch` char(20) DEFAULT NULL,
  `okpo` char(20) DEFAULT NULL,
  `okato` char(20) DEFAULT NULL,
  `ogrn` char(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8 ROW_FORMAT=FIXED;

#
# Structure for the `contracts` table : 
#

DROP TABLE IF EXISTS `contracts`;

CREATE TABLE `contracts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deleted` tinyint(4) DEFAULT '0',
  `deleted_by` int(11) DEFAULT NULL,
  `client_id` int(11) NOT NULL,
  `subj_id` int(11) DEFAULT NULL,
  `responsible_id` int(11) DEFAULT NULL,
  `num` char(20) DEFAULT NULL,
  `signed` date DEFAULT NULL,
  `expires` date DEFAULT NULL,
  `closed` tinyint(4) DEFAULT NULL,
  `notes` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

#
# Structure for the `departments` table : 
#

DROP TABLE IF EXISTS `departments`;

CREATE TABLE `departments` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `parent_id` int(11) DEFAULT '0',
  `name` char(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

#
# Structure for the `devices` table : 
#

DROP TABLE IF EXISTS `devices`;

CREATE TABLE `devices` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deleted` tinyint(4) DEFAULT '0',
  `deleted_by` int(11) DEFAULT NULL,
  `title` char(200) DEFAULT NULL,
  `device_type` char(50) DEFAULT NULL,
  `precision` char(20) DEFAULT NULL,
  `range` char(20) DEFAULT NULL,
  `num` int(11) DEFAULT NULL,
  `fnum` char(20) DEFAULT NULL,
  `check_cert` char(20) DEFAULT NULL,
  `check_period` int(11) DEFAULT NULL,
  `last_checked` date DEFAULT NULL,
  `checker_id` int(11) DEFAULT NULL,
  `next_check` date DEFAULT NULL,
  `groen` int(11) DEFAULT NULL,
  `notes` text,
  `responsible_id` int(11) DEFAULT NULL,
  `task_created` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;

#
# Structure for the `dictionaries` table : 
#

DROP TABLE IF EXISTS `dictionaries`;

CREATE TABLE `dictionaries` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deleted` tinyint(4) DEFAULT NULL,
  `deleted_by` int(11) DEFAULT NULL,
  `type` tinyint(1) DEFAULT NULL,
  `name` char(200) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8 ROW_FORMAT=FIXED;

#
# Structure for the `employees` table : 
#

DROP TABLE IF EXISTS `employees`;

CREATE TABLE `employees` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` char(100) DEFAULT '',
  `login` char(50) DEFAULT '',
  `password` char(50) DEFAULT NULL,
  `email` char(50) DEFAULT NULL,
  `grp` int(11) unsigned DEFAULT NULL,
  `position_id` int(11) DEFAULT NULL,
  `department_id` int(11) DEFAULT NULL,
  `boss` tinyint(1) DEFAULT NULL,
  `fired` tinyint(4) DEFAULT NULL,
  `supervisor` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `login` (`login`),
  UNIQUE KEY `login_2` (`login`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8 ROW_FORMAT=FIXED;

#
# Structure for the `evaluations` table : 
#

DROP TABLE IF EXISTS `evaluations`;

CREATE TABLE `evaluations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deleted` tinyint(4) DEFAULT '0',
  `employee_id` int(11) DEFAULT NULL,
  `field_id` int(11) DEFAULT NULL,
  `cert_num` char(50) DEFAULT NULL,
  `last_eval_date` date DEFAULT NULL,
  `next_eval_date` date DEFAULT NULL,
  `notification_sent` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;

#
# Structure for the `events` table : 
#

DROP TABLE IF EXISTS `events`;

CREATE TABLE `events` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created` datetime DEFAULT NULL,
  `recepient_id` int(11) DEFAULT NULL,
  `title` char(100) DEFAULT NULL,
  `description` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8;

#
# Structure for the `guides` table : 
#

DROP TABLE IF EXISTS `guides`;

CREATE TABLE `guides` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deleted` tinyint(4) DEFAULT '0',
  `deleted_by` int(11) DEFAULT NULL,
  `obj_type_id` int(11) DEFAULT NULL,
  `obj_name` char(200) DEFAULT NULL,
  `rnum` char(50) DEFAULT NULL,
  `fnum` char(50) DEFAULT NULL,
  `client_id` int(11) DEFAULT NULL,
  `contract_id` int(11) DEFAULT NULL,
  `act_id` int(11) DEFAULT NULL,
  `responsible_id` int(11) DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `notes` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

#
# Structure for the `requests` table : 
#

DROP TABLE IF EXISTS `requests`;

CREATE TABLE `requests` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `deleted` tinyint(4) DEFAULT '0',
  `deleted_by` int(11) DEFAULT NULL,
  `description` text,
  `client_id` int(11) DEFAULT NULL,
  `responsible_id` int(11) DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `notes` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

#
# Structure for the `tasks` table : 
#

DROP TABLE IF EXISTS `tasks`;

CREATE TABLE `tasks` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created_by` int(11) DEFAULT NULL,
  `type_id` int(11) DEFAULT NULL,
  `executor_id` int(11) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  `completed_date` date DEFAULT NULL,
  `description` text,
  `status` tinyint(1) DEFAULT NULL,
  `notes` text,
  `follower_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8;

#
# Structure for the `templates` table : 
#

DROP TABLE IF EXISTS `templates`;

CREATE TABLE `templates` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `client_id` int(11) DEFAULT NULL,
  `num` char(20) DEFAULT NULL,
  `signer` char(255) DEFAULT NULL,
  `signed` date DEFAULT NULL,
  `foundation` char(255) DEFAULT NULL,
  `subject` text,
  `duration` int(11) DEFAULT NULL,
  `prepay` int(11) DEFAULT NULL,
  `unit_name` char(100) DEFAULT NULL,
  `unit_price` double(15,3) DEFAULT NULL,
  `total_price` double(15,3) DEFAULT NULL,
  `multiple_items` tinyint(1) DEFAULT NULL,
  `due_date` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

#
# Data for the `accessgroups` table  (LIMIT 0,500)
#

INSERT INTO `accessgroups` (`id`, `grp_name`, `description`, `task_creator`, `task_approver`, `need_approval`, `delete_confirmer`) VALUES 
  (1,'Администратор-1','Полный доступ к системе',1,1,0,1),
  (2,'Администратор-2','Полный доступ к системе без права подтверждать выполнение заданий',1,0,1,0),
  (3,'Пользователь','Чтение всех данных',0,0,0,0),
  (5,'Начальник отдела','Создание и изменение всех данных',1,0,0,0);

COMMIT;

#
# Data for the `accessrights` table  (LIMIT 0,500)
#

INSERT INTO `accessrights` (`id`, `group_id`, `acts`, `contracts`, `guides`, `requests`, `devices`, `evaluations`, `dictionaries`, `employees`, `rights`) VALUES 
  (1,1,31,31,31,31,31,31,31,31,31),
  (2,2,31,31,31,31,31,31,31,0,0),
  (3,3,17,17,17,17,17,0,17,0,0),
  (5,5,31,31,31,31,31,0,0,0,31);

COMMIT;

#
# Data for the `acts` table  (LIMIT 0,500)
#

INSERT INTO `acts` (`id`, `deleted`, `deleted_by`, `contract_id`, `work_num`, `work_date`, `obj_type`, `obj_name`, `obj_fnum`, `obj_rnum`, `work_next_date`, `completed`, `notes`) VALUES 
  (9,0,NULL,0,'',NULL,0,'','','',NULL,0,''),
  (11,0,NULL,0,'',NULL,0,'','','',NULL,0,''),
  (12,0,NULL,0,'',NULL,0,'','','',NULL,0,''),
  (13,0,NULL,0,'',NULL,0,'','','',NULL,0,'');

COMMIT;

#
# Data for the `chatlogs` table  (LIMIT 0,500)
#

INSERT INTO `chatlogs` (`id`, `timestamp`, `author`, `room`, `message`) VALUES 
  (5,1412993843877,'Журавлев Е.А.',NULL,'>>Старцев С.В.: серега привет'),
  (6,1412993876617,'Старцев С.В.',NULL,'врунукнку'),
  (7,1412993891360,'Журавлев Е.А.',NULL,'фингня'),
  (8,1412993891529,'Старцев С.В.',NULL,'я в чате'),
  (9,1412993905186,'Журавлев Е.А.',NULL,'эй'),
  (10,1412994005811,'Малых А.В.',NULL,'>>Старцев С.В.: ghbdtn'),
  (11,1412994017830,'Малых А.В.',NULL,'эй ты где'),
  (12,1412994049082,'Старцев С.В.',NULL,'апапапапап');

COMMIT;

#
# Data for the `clients` table  (LIMIT 0,500)
#

INSERT INTO `clients` (`id`, `deleted`, `deleted_by`, `name`, `full_name`, `boss`, `address`, `address2`, `phone`, `fax`, `inn`, `kpp`, `email`, `actual`, `bank_name`, `rsch`, `ksch`, `okpo`, `okato`, `ogrn`) VALUES 
  (1,0,2,'ООО \"Рога и копыта\"','Общество с ограниченной ответственностью \"Рога и копыта\"','Имярек Бесфамильный','г. Город','г. Город','8-123-456-78-90',NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,NULL,NULL),
  (2,0,2,'ООО \"Фирма\"','Общество с ограниченной ответственностью \"Фирма\"','Главный А.А.','г. Поселок','г. Поселок','1',NULL,'2703','2701',NULL,1,'Сбербанк','23','23',NULL,NULL,NULL),

COMMIT;

#
# Data for the `contracts` table  (LIMIT 0,500)
#

INSERT INTO `contracts` (`id`, `deleted`, `deleted_by`, `client_id`, `subj_id`, `responsible_id`, `num`, `signed`, `expires`, `closed`, `notes`) VALUES 
  (2,0,NULL,13,1,4,'q1','2014-10-01','2014-10-31',0,'');

COMMIT;

#
# Data for the `departments` table  (LIMIT 0,500)
#

INSERT INTO `departments` (`id`, `parent_id`, `name`) VALUES 
  (1,0,'Директорат'),
  (2,1,'Сотрудники'),
  (3,1,'Экспертный отдел');

COMMIT;

#
# Data for the `devices` table  (LIMIT 0,500)
#

INSERT INTO `devices` (`id`, `deleted`, `deleted_by`, `title`, `device_type`, `precision`, `range`, `num`, `fnum`, `check_cert`, `check_period`, `last_checked`, `checker_id`, `next_check`, `groen`, `notes`, `responsible_id`, `task_created`) VALUES 
  (1,0,NULL,'Комплект для визуального и измерительного контроля',NULL,NULL,'-',1,'359','В.ДЖО.М – 1517-14',10,'2014-02-21',6,'2015-02-21',6,NULL,6,NULL),
  (2,0,NULL,'Тахеометр Электронный','СХ-105L',NULL,'-',1,'HK 0112','0535087',12,'2014-05-15',7,'2015-05-15',11,NULL,6,NULL),
  (3,0,NULL,'Лазерный дальномер','Disto D510',NULL,'-',1,'1031050629','2615',12,'2014-05-15',8,'2015-06-16',6,NULL,6,NULL),
  (4,0,NULL,'Дефектоскоп ультразвуковой','А1212 MASTER',NULL,'-',1,'3121670','2194C',12,'2015-05-15',9,'2015-05-15',6,NULL,6,NULL),
  (5,0,NULL,'Молоток Шмидта',NULL,NULL,'-',1,'SH-01-005-0106','1512C',12,'2014-03-27',9,'2015-03-27',6,NULL,6,NULL),
  (6,0,NULL,'Толщиномер ультразвуковой','А1207',NULL,'-',1,'1004766','0976С',12,'2014-02-27',9,'2015-02-27',6,NULL,6,NULL),
  (7,0,NULL,'Твердомер динамический','Equotip Bambino 2',NULL,'-',1,'EP04-005-0407','2616С',12,'2014-06-16',9,'2015-06-16',6,NULL,6,NULL),
  (8,0,NULL,'Стандартный образец','СО-2',NULL,NULL,0,'125','0305С',24,'2014-03-26',9,'2016-03-26',NULL,NULL,6,NULL),
  (9,0,3,'Стандартный образец','СО-3',NULL,NULL,0,'425','0306С',24,'2014-03-26',9,'2016-03-26',NULL,NULL,6,NULL);

COMMIT;

#
# Data for the `dictionaries` table  (LIMIT 0,500)
#

INSERT INTO `dictionaries` (`id`, `deleted`, `deleted_by`, `type`, `name`) VALUES 
  (1,0,0,1,'Обследование кранового пути'),
  (2,0,0,1,'Экспертиза крана'),
  (3,0,0,2,'Крановый наземный путь'),
  (4,0,0,2,'Портальный кран'),
  (5,0,0,2,'Стреловой кран'),
  (6,0,0,3,'г.Москва, ВНИИМС'),
  (15,0,0,6,'Специальные требования ПБ'),
  (16,0,0,6,'Подъемные сооружения'),
  (17,0,0,6,'Нефтехимия'),
  (18,0,0,6,'Котлонадзор'),
  (19,0,0,6,'Газоснабжение'),
  (20,0,0,6,'Пожарно-технический минимум'),
  (21,0,0,6,'Охрана труда'),
  (22,0,0,6,'УК, МК, ВИК, АЭ'),
  (23,0,0,6,'РК'),
  (24,0,0,6,'УК ВИК'),
  (25,0,0,7,'Директор'),
  (26,0,0,7,'Эксперт'),
  (27,0,0,7,'Специалист НК'),
  (35,0,0,8,'Закрыться с контрагентом'),
  (36,0,0,7,'Заместитель директора'),
  (37,0,0,7,'Экономист'),
  (38,0,0,7,'Бухгалтер'),
  (39,0,0,7,'Сметчик'),
  (40,0,0,7,'Геодезист'),
  (41,NULL,NULL,8,'Экспертиза промышленной безопасности мостового крана');

COMMIT;

#
# Data for the `employees` table  (LIMIT 0,500)
#

INSERT INTO `employees` (`id`, `name`, `login`, `password`, `email`, `grp`, `position_id`, `department_id`, `boss`, `fired`, `supervisor`) VALUES 
  (1,'Администратор системы','administrator','admin',NULL,0,0,0,0,0,1),
  (2,'Жуков А.С.','aszhukov','asz123','avbogatyrev@gmail.com',2,25,1,1,0,0),
  (3,'Старцев С.В.','svstartsev','svs123','avbogatyrev@gmail.com',5,26,3,1,0,0),
  (4,'Залесский К.В.','kvzalesskij','kvz123','avbogatyrev@gmail.com',3,27,3,0,0,0),
  (5,'Кочермин С.Г.','sgkochermin','sgk123','avbogatyrev@gmail.com',3,27,3,0,0,0),
  (6,'Журавлев Е.А.','eazhuravlev','eaz123','avbogatyrev@gmail.com',1,36,1,0,0,0),
  (7,'Малых А.В.','avmalyh','avm123','avbogatyrev@gmail.com',3,37,2,0,0,0),
  (8,'Темникова Г.М.','gmtemnikova','gmt123','avbogatyrev@gmail.com',3,38,2,0,0,0),
  (9,'Пучкова А.В.','avpuchkova','avp123','avbogatyrev@gmail.com',3,39,2,0,0,0),
  (10,'Малюгин С.Н.','snmalyugin','snm123','avbogatyrev@gmail.com',3,40,3,0,0,0),
  (11,'Рудьков П.М.','pmrudkov','pmr123','avbogatyrev@gmail.com',3,40,3,0,0,0);

COMMIT;

#
# Data for the `evaluations` table  (LIMIT 0,500)
#

INSERT INTO `evaluations` (`id`, `deleted`, `employee_id`, `field_id`, `cert_num`, `last_eval_date`, `next_eval_date`, `notification_sent`) VALUES 
  (1,0,2,15,'71-11-1939-01 А','2011-10-21','2014-10-21','2014-10-06'),
  (2,0,3,15,'71-11-1941-01 Б9','2011-10-21','2014-10-21','2014-10-06'),
  (4,0,3,15,'71-11-1940-02 А, Б1, Б8','2011-10-21','2014-10-21','2014-10-06'),
  (5,0,3,16,'НОА-0067-П1011-01','2011-10-28','2014-10-28','2014-10-06'),
  (6,0,3,17,'НОА-0067-Н1011-01','2011-10-28','2014-10-28','2014-10-06'),
  (7,0,3,18,'НОА-0067-К1011-01','2011-10-28','2014-10-28','2014-10-06'),
  (8,0,3,19,'НОА-0067-0446-С12','2013-12-12','2016-12-12',NULL),
  (9,0,3,20,'982','2013-06-27','2016-06-27',NULL),
  (10,0,3,21,'979','2013-06-27','2016-06-27',NULL),
  (11,0,4,15,'2014-01-01','2014-05-23','2019-05-23',NULL),
  (12,0,4,22,'0001-39820-14','2014-04-01','2017-04-01',NULL),
  (13,0,4,23,'0001-39061-14','2014-05-01','2017-05-01',NULL),
  (14,0,4,20,'539','2014-05-07','2017-05-07',NULL),
  (15,0,4,21,'537','2014-05-07','2017-05-07',NULL),
  (16,0,5,15,'2014-01-02','2014-05-23','2019-05-23',NULL),
  (17,0,5,24,'0001-39819-14','2014-04-01','2017-04-01',NULL),
  (18,0,5,20,'540','2014-05-07','2017-05-07',NULL),
  (19,0,5,21,'538','2014-05-07','2014-05-07','2014-10-06');

COMMIT;

#
# Data for the `events` table  (LIMIT 0,500)
#

INSERT INTO `events` (`id`, `created`, `recepient_id`, `title`, `description`) VALUES 
  (13,'2014-10-11 13:24:22',7,'Новое задание','Вы назначены исполнителем по заданию \"подготовить договор\".'),
  (16,'2014-10-11 13:26:42',7,'Изменение статуса задания','Ваше задание \"подготовить договор\" утверждено в статусе \"Выполненное\".'),
  (21,'2014-10-13 14:19:36',5,'Новое задание','Вы назначены исполнителем по заданию \"task\".');

COMMIT;

#
# Data for the `requests` table  (LIMIT 0,500)
#

INSERT INTO `requests` (`id`, `deleted`, `deleted_by`, `description`, `client_id`, `responsible_id`, `due_date`, `notes`) VALUES 
  (2,0,NULL,'task',13,4,'2014-10-17',''),
  (3,0,NULL,'taask2',7,6,'2014-10-10','');

COMMIT;

#
# Data for the `tasks` table  (LIMIT 0,500)
#

INSERT INTO `tasks` (`id`, `created_by`, `type_id`, `executor_id`, `start_date`, `due_date`, `completed_date`, `description`, `status`, `notes`, `follower_id`) VALUES 
  (3,6,41,3,'2014-10-13','2014-10-16','2014-10-11','Выдача заключения',1,'Выполнено. ЭПБ №456-2014',0),
  (4,6,41,3,'2014-10-17','2014-10-18',NULL,'dfggfg',0,'',0),
  (7,6,41,7,'2014-10-13','2014-10-13','2014-10-11','подготовить договор',1,'цена 125000 с ндс',0),
  (8,6,41,3,'2014-10-13','2014-10-14',NULL,'прверить пртоколы',0,'',0),
  (10,3,35,6,'2014-10-01','2014-10-22',NULL,'task',0,'',0),
  (13,6,41,6,'2014-10-08','2014-10-27',NULL,'du task',0,NULL,0),
  (20,6,41,4,'2014-10-14','2014-10-16',NULL,'ttt',0,NULL,0);

COMMIT;



/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;