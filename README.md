-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: monster_tamer_db
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `enemy_action_patterns`
--

DROP TABLE IF EXISTS `enemy_action_patterns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enemy_action_patterns` (
  `id` int NOT NULL AUTO_INCREMENT,
  `monster_id` int NOT NULL COMMENT 'モンスターID',
  `priority` int NOT NULL DEFAULT '0' COMMENT '優先度(高い順に判定)',
  `condition_type` varchar(255) DEFAULT NULL,
  `condition_value` int NOT NULL DEFAULT '0' COMMENT '条件の閾値',
  `activation_rate` int NOT NULL DEFAULT '100' COMMENT '発動確率(%)',
  `action_skill_id` int NOT NULL COMMENT '実行スキルID',
  `target_policy` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `monster_id` (`monster_id`),
  KEY `action_skill_id` (`action_skill_id`),
  CONSTRAINT `fk_eap_monster` FOREIGN KEY (`monster_id`) REFERENCES `monsters_master` (`id`),
  CONSTRAINT `fk_eap_skill` FOREIGN KEY (`action_skill_id`) REFERENCES `skills_master` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='敵モンスターの思考パターン定義';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enemy_action_patterns`
--

LOCK TABLES `enemy_action_patterns` WRITE;
/*!40000 ALTER TABLE `enemy_action_patterns` DISABLE KEYS */;
INSERT INTO `enemy_action_patterns` VALUES (1,1,50,'TURN_MOD_3',0,80,1,'SELF'),(2,1,30,'ALWAYS',0,30,2,'HIGH_HP'),(3,1,20,'HP_UNDER',30,100,3,'SELF'),(4,1,0,'ALWAYS',0,100,901,'RANDOM'),(5,2,100,'HP_UNDER',50,80,6,'ALLY_LOW_HP'),(6,2,50,'TURN_EQ',1,100,5,'SELF'),(7,2,30,'ALWAYS',0,40,4,'HIGH_HP'),(8,2,0,'ALWAYS',0,100,901,'RANDOM'),(9,3,100,'TURN_EQ',4,50,9,'HIGH_HP'),(10,3,90,'TURN_EQ',4,100,902,'SELF'),(11,3,50,'TURN_LESS',4,60,8,'SELF'),(12,3,40,'TURN_LESS',4,100,7,'LOW_HP'),(13,3,0,'ALWAYS',0,100,7,'RANDOM'),(14,4,50,'TURN_EQ',1,80,11,'PLAYER'),(15,4,30,'ALWAYS',0,40,10,'RANDOM'),(16,4,30,'ALWAYS',0,40,12,'HIGH_HP'),(17,4,0,'ALWAYS',0,100,901,'RANDOM'),(18,5,60,'TURN_MOD_3',0,70,13,'HIGH_HP'),(19,5,50,'HP_UNDER',50,50,14,'RANDOM'),(20,5,30,'ALWAYS',0,40,15,'RANDOM'),(21,5,0,'ALWAYS',0,100,901,'RANDOM'),(22,6,40,'ALWAYS',0,30,16,'HIGH_HP'),(23,6,40,'ALWAYS',0,30,17,'RANDOM'),(24,6,40,'ALWAYS',0,30,18,'RANDOM'),(25,6,0,'ALWAYS',0,100,901,'RANDOM'),(26,7,80,'TURN_EQ',1,100,19,'SELF'),(27,7,50,'ALWAYS',0,50,20,'LOW_HP'),(28,7,30,'ALWAYS',0,30,21,'RANDOM'),(29,7,0,'ALWAYS',0,100,901,'RANDOM'),(30,8,100,'HP_UNDER',40,90,24,'SELF'),(31,8,50,'ALWAYS',0,40,22,'RANDOM'),(32,8,50,'ALWAYS',0,40,23,'HIGH_HP'),(33,8,0,'ALWAYS',0,100,901,'RANDOM'),(34,9,70,'TURN_EQ',1,100,27,'SELF'),(35,9,50,'ALWAYS',0,50,25,'HIGH_HP'),(36,9,50,'ALWAYS',0,30,26,'RANDOM'),(37,9,0,'ALWAYS',0,100,901,'RANDOM'),(38,10,80,'HP_UNDER',20,100,30,'SELF'),(39,10,50,'TURN_MOD_3',0,50,29,'SELF'),(40,10,40,'ALWAYS',0,50,28,'RANDOM'),(41,10,0,'ALWAYS',0,100,901,'RANDOM'),(42,11,60,'TURN_EQ',1,80,31,'HIGH_HP'),(43,11,50,'ALWAYS',0,40,32,'PLAYER'),(44,11,40,'ALWAYS',0,40,33,'RANDOM'),(45,11,0,'ALWAYS',0,100,901,'RANDOM'),(46,12,70,'TURN_MOD_3',0,100,35,'SELF'),(47,12,50,'ALWAYS',0,50,36,'HIGH_HP'),(48,12,50,'ALWAYS',0,30,34,'LOW_HP'),(49,12,0,'ALWAYS',0,100,901,'RANDOM'),(50,13,100,'HP_UNDER',30,90,39,'ALLY_LOW_HP'),(51,13,80,'TURN_EQ',1,100,37,'SELF'),(52,13,50,'ALWAYS',0,60,38,'RANDOM'),(53,13,0,'ALWAYS',0,100,901,'RANDOM'),(54,14,100,'HP_UNDER',50,80,42,'ALLY_LOW_HP'),(55,14,60,'TURN_MOD_3',0,70,40,'ALLY_LOW_HP'),(56,14,40,'ALWAYS',0,100,41,'RANDOM'),(57,14,0,'ALWAYS',0,100,901,'RANDOM'),(58,15,90,'HP_UNDER',40,100,45,'SELF'),(59,15,80,'TURN_EQ',1,100,43,'SELF'),(60,15,50,'ALWAYS',0,60,44,'HIGH_HP'),(61,15,0,'ALWAYS',0,100,901,'RANDOM'),(62,16,70,'TURN_EQ',1,100,48,'SELF'),(63,16,60,'ALWAYS',0,40,47,'LOW_HP'),(64,16,60,'ALWAYS',0,40,46,'RANDOM'),(65,16,0,'ALWAYS',0,100,901,'RANDOM'),(66,17,80,'TURN_MOD_4',0,100,49,'RANDOM'),(67,17,60,'HP_UNDER',50,50,51,'SELF'),(68,17,40,'ALWAYS',0,30,50,'SELF'),(69,17,0,'ALWAYS',0,100,901,'RANDOM'),(70,18,70,'TURN_EQ',1,100,53,'SELF'),(71,18,50,'ALWAYS',0,40,54,'RANDOM'),(72,18,50,'ALWAYS',0,30,52,'HIGH_HP'),(73,18,0,'ALWAYS',0,100,901,'RANDOM'),(74,19,100,'HP_UNDER',20,100,56,'ALLY_LOW_HP'),(75,19,70,'TURN_EQ',1,100,57,'SELF'),(76,19,50,'ALWAYS',0,60,55,'RANDOM'),(77,19,0,'ALWAYS',0,100,901,'RANDOM'),(78,20,50,'ALWAYS',0,60,58,'SELF'),(79,20,50,'ALWAYS',0,20,59,'RANDOM'),(80,20,50,'ALWAYS',0,10,60,'RANDOM'),(81,20,0,'ALWAYS',0,100,902,'SELF'),(82,21,100,'HP_UNDER',10,100,63,'HIGH_HP'),(83,21,90,'TURN_MOD_3',0,80,61,'RANDOM'),(84,21,50,'ALWAYS',0,50,62,'RANDOM'),(85,21,0,'ALWAYS',0,100,901,'RANDOM'),(86,22,100,'TURN_EQ',1,100,64,'RANDOM'),(87,22,90,'HP_UNDER',30,70,66,'HIGH_HP'),(88,22,50,'ALWAYS',0,40,65,'RANDOM'),(89,22,0,'ALWAYS',0,100,61,'RANDOM'),(90,99,50,'ALWAYS',0,50,64,'RANDOM'),(91,99,0,'ALWAYS',0,100,901,'RANDOM');
/*!40000 ALTER TABLE `enemy_action_patterns` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `enemy_parties`
--

DROP TABLE IF EXISTS `enemy_parties`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `enemy_parties` (
  `id` int NOT NULL AUTO_INCREMENT,
  `floor` int NOT NULL,
  `monster_left_id` int DEFAULT NULL,
  `monster_center_id` int NOT NULL,
  `monster_right_id` int DEFAULT NULL,
  `weight` int DEFAULT '10',
  PRIMARY KEY (`id`),
  KEY `monster_left_id` (`monster_left_id`),
  KEY `monster_center_id` (`monster_center_id`),
  KEY `monster_right_id` (`monster_right_id`),
  CONSTRAINT `enemy_parties_ibfk_1` FOREIGN KEY (`monster_left_id`) REFERENCES `monsters_master` (`id`),
  CONSTRAINT `enemy_parties_ibfk_2` FOREIGN KEY (`monster_center_id`) REFERENCES `monsters_master` (`id`),
  CONSTRAINT `enemy_parties_ibfk_3` FOREIGN KEY (`monster_right_id`) REFERENCES `monsters_master` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `enemy_parties`
--

LOCK TABLES `enemy_parties` WRITE;
/*!40000 ALTER TABLE `enemy_parties` DISABLE KEYS */;
INSERT INTO `enemy_parties` VALUES (1,1,NULL,2,NULL,40),(2,1,4,4,NULL,30),(3,1,2,6,2,20),(4,1,NULL,1,NULL,5),(5,1,NULL,3,NULL,5),(6,2,5,5,NULL,35),(7,2,9,8,9,35),(8,2,7,7,7,20),(9,2,NULL,5,NULL,10),(10,3,10,11,10,40),(11,3,12,12,NULL,30),(12,3,13,15,NULL,20),(13,3,NULL,14,NULL,10),(14,4,NULL,17,NULL,30),(15,4,NULL,19,NULL,30),(16,4,16,18,16,30),(17,4,NULL,21,NULL,10),(18,9,NULL,22,NULL,100);
/*!40000 ALTER TABLE `enemy_parties` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monsters_master`
--

DROP TABLE IF EXISTS `monsters_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monsters_master` (
  `id` int NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `hp` int NOT NULL,
  `armor` int DEFAULT '0',
  `attack` int NOT NULL,
  `speed` int NOT NULL,
  `skill_1` int DEFAULT NULL,
  `skill_2` int DEFAULT NULL,
  `skill_3` int DEFAULT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monsters_master`
--

LOCK TABLES `monsters_master` WRITE;
/*!40000 ALTER TABLE `monsters_master` DISABLE KEYS */;
INSERT INTO `monsters_master` VALUES (1,'ラッキー',10,800,1,100,1,2,3,'/images/monsters/fairy_experience.png'),(2,'ハッピー',10,800,1,100,4,5,6,'/images/monsters/fairy_gold.png'),(3,'森本君',10,5,1,5,7,8,9,'/images/monsters/bomb.png'),(4,'マジックキャット',10,5,5,5,10,11,12,'/images/monsters/cat.png'),(5,'オーガ',10,10,10,5,13,14,15,'/images/monsters/demon.png'),(6,'ビッグヘッド',10,10,10,5,16,17,18,'/images/monsters/doll_guy.png'),(7,'ブリッツェン',10,5,5,5,19,20,21,'/images/monsters/reindeer.png'),(8,'ハナ・サキ',100,20,20,5,22,23,24,'/images/monsters/doll_girl.png'),(9,'アトラク・ナクア',100,20,20,5,25,26,27,'/images/monsters/spider.png'),(10,'ハイ・スケルトン',100,20,20,5,28,29,30,'/images/monsters/skeleton.png'),(11,'ブラックウィッチ',100,15,15,5,31,32,33,'/images/monsters/witch.png'),(12,'シャドウウルフ',100,20,20,5,34,35,36,'/images/monsters/wolf.png'),(13,'人面樹',500,50,25,5,37,38,39,'/images/monsters/tree.png'),(14,'ホーリースライム',500,50,25,5,40,41,42,'/images/monsters/saint.png'),(15,'アーマードスケルトン',500,100,30,5,43,44,45,'/images/monsters/skeleton_armored.png'),(16,'ワイバーン',1000,150,50,5,46,47,48,'/images/monsters/wyvern.png'),(17,'レッドドラゴン',1000,150,50,5,49,50,51,'/images/monsters/dragon_red.png'),(18,'アースドレイク',1000,150,50,5,52,53,54,'/images/monsters/dragon_brown.png'),(19,'アジュールドラゴン',1000,150,50,5,55,56,57,'/images/monsters/dragon_white.png'),(20,'？？？の卵',1,100,1,5,58,59,60,'/images/monsters/dragon_egg.png'),(21,'黒龍ニーズヘッグ',5000,500,100,5,61,62,63,'/images/monsters/dragon_black.png'),(22,'クトゥルー',8000,1000,100,5,64,65,66,'/images/monsters/cthulhu.png'),(99,'デバッグ勇者',9999,9999,999,999,64,56,17,'/images/monsters/cthulhu.png');
/*!40000 ALTER TABLE `monsters_master` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `skills_master`
--

DROP TABLE IF EXISTS `skills_master`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `skills_master` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `effect_type` varchar(255) DEFAULT NULL,
  `damage_value` int DEFAULT '0',
  `damage_multiplier` int DEFAULT '0',
  `change_atk` int DEFAULT '100',
  `change_speed` int DEFAULT '100',
  `change_armor` int DEFAULT '0',
  `target_type` varchar(255) DEFAULT NULL,
  `max_uses` int NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=916 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `skills_master`
--

LOCK TABLES `skills_master` WRITE;
/*!40000 ALTER TABLE `skills_master` DISABLE KEYS */;
INSERT INTO `skills_master` VALUES (1,'幸運の風','BUFF',0,0,100,150,0,'ALLY_ALL',5,'味方全体の素早さを1.5倍にする'),(2,'光の粉','DEBUFF',0,0,100,70,0,'ENEMY_ALL',5,'敵全体の素早さを0.7倍にする'),(3,'逃走の構え','BUFF',0,0,100,200,0,'SELF',3,'自身の素早さを2倍にする'),(4,'黄金の輝き','DEBUFF',0,0,70,100,0,'ENEMY_ALL',5,'敵全体の攻撃力を0.7倍にする'),(5,'祝杯','BUFF',0,0,130,100,0,'ALLY_ALL',5,'味方全体の攻撃力を1.3倍にする'),(6,'ハッピーダンス','HEAL',20,0,100,100,0,'ALLY_ALL',3,'味方全体のHPを回復する'),(7,'体当たり','ATK',15,0,100,100,0,'ENEMY',10,'敵に15ダメージを与える'),(8,'硬くなる','ARMOR_ADD',0,0,100,100,15,'SELF',5,'自身のアーマーを15追加する'),(9,'大爆発','SUICIDE_ATTACK',0,3,100,100,0,'ENEMY_ALL',1,'HP+アーマーの3倍ダメを与え自身は死亡する'),(10,'ひっかき','ATK_MULTI',5,2,100,100,0,'ENEMY',10,'5ダメージを2回与える'),(11,'猫だまし','DEBUFF',0,0,100,50,0,'ENEMY',5,'敵の素早さを半分にする'),(12,'魔力の眼','DEBUFF',0,0,50,100,0,'ENEMY',5,'敵の攻撃力を半分にする'),(13,'重い一撃','ATK',30,0,100,100,0,'ENEMY',5,'敵に30ダメージを与える'),(14,'咆哮','BUFF',0,0,150,100,0,'SELF',3,'自身の攻撃力を1.5倍にする'),(15,'棍棒振り回し','ATK',15,0,100,100,0,'ENEMY_ALL',5,'敵全体に15ダメージを与える'),(16,'睨みつける','DEBUFF',0,0,100,80,0,'ENEMY',10,'敵の素早さを0.8倍にする'),(17,'ヘッドバット','ATK',20,0,100,100,0,'ENEMY',10,'敵に20ダメージを与える'),(18,'不気味な笑い','DEBUFF',0,0,80,100,0,'ENEMY',5,'敵の攻撃力を0.8倍にする'),(19,'高速移動','BUFF',0,0,100,150,0,'SELF',5,'自身の素早さを1.5倍にする'),(20,'電光石火','ATK_RATIO_SPD',0,1,100,100,0,'ENEMY',10,'自身の素早さと同じダメージを与える'),(21,'角突き','ATK',25,0,100,100,0,'ENEMY',10,'敵に25ダメージを与える'),(22,'花粉','DEBUFF',0,0,100,70,0,'ENEMY_ALL',3,'敵全体の素早さを0.7倍にする'),(23,'吸い取り','ATK_HEAL',30,0,100,100,0,'ENEMY',5,'30ダメを与え自身のHPを回復する'),(24,'光合成','HEAL',50,0,100,100,0,'SELF',3,'自身のHPを50回復する'),(25,'粘着糸','DEBUFF',0,0,100,50,0,'ENEMY',5,'敵の素早さを半分にする'),(26,'猛毒の牙','ATK',40,0,100,100,0,'ENEMY',5,'敵に40ダメージを与える'),(27,'巣を張る','ARMOR_ADD',0,0,100,100,50,'SELF',3,'自身のアーマーを50追加する'),(28,'骨投げ','ATK_MULTI',15,2,100,100,0,'ENEMY',10,'15ダメージを2回与える'),(29,'死の舞','BUFF',0,0,150,100,0,'SELF',3,'自身の攻撃力を1.5倍にする'),(30,'執念','ARMOR_ADD',0,0,100,100,100,'SELF',1,'自身のアーマーを100追加する'),(31,'闇の魔力','DEBUFF',0,0,60,100,0,'ENEMY_ALL',3,'敵全体の攻撃力を0.6倍にする'),(32,'呪い','DEBUFF',0,0,100,60,0,'ENEMY_ALL',3,'敵全体の素早さを0.6倍にする'),(33,'シャドウボール','ATK',60,0,100,100,0,'ENEMY',5,'敵に60ダメージを与える'),(34,'闇討ち','ATK_RATIO_SPD',0,2,100,100,0,'ENEMY',5,'素早さの2倍のダメージを与える'),(35,'遠吠え','BUFF',0,0,130,100,0,'ALLY_ALL',3,'味方全体の攻撃力を1.3倍にする'),(36,'影縫い','DEBUFF',0,0,100,40,0,'ENEMY',5,'敵の素早さを0.4倍にする'),(37,'根を張る','ARMOR_BUFF',0,0,100,100,2,'SELF',3,'自身のアーマーを2倍にする'),(38,'枝の鞭','ATK',40,0,100,100,0,'ENEMY_ALL',5,'敵全体に40ダメージを与える'),(39,'癒しの葉','HEAL',100,0,100,100,0,'ALLY_ALL',2,'味方全体を100回復する'),(40,'聖なる液','ARMOR_ADD',0,0,100,100,150,'ALLY_SINGLE',5,'味方のアーマーを150追加する'),(41,'天罰','ATK',150,0,100,100,0,'ENEMY',3,'敵に150ダメージを与える'),(42,'再生','HEAL',200,0,100,100,0,'SELF',2,'自身のHPを200回復する'),(43,'盾を構える','ARMOR_ADD',0,0,100,100,300,'SELF',5,'自身のアーマーを300追加する'),(44,'重斬','ATK',120,0,100,100,0,'ENEMY',5,'敵に120ダメージを与える'),(45,'鉄壁','ARMOR_BUFF',0,0,100,100,2,'SELF',2,'自身のアーマーを2倍にする'),(46,'エアスラッシュ','ATK_RATIO_SPD',0,3,100,100,0,'ENEMY',5,'素早さの3倍のダメージを与える'),(47,'急降下','BUFF',0,0,200,100,0,'SELF',3,'自身の攻撃力を2倍にする'),(48,'翼の守り','BUFF',0,0,100,150,0,'SELF',5,'自身の素早さを1.5倍にする'),(49,'ブレス','ATK',200,0,100,100,0,'ENEMY_ALL',3,'敵全体に200ダメージを与える'),(50,'火焔の鎧','ARMOR_ADD',0,0,100,100,500,'SELF',3,'自身のアーマーを500追加する'),(51,'憤怒','BUFF',0,0,200,100,0,'SELF',2,'自身の攻撃力を2倍にする'),(52,'グランドプレス','ATK_RATIO_ARMOR',0,1,100,100,0,'ENEMY',5,'自身のアーマーと同じダメージを与える'),(53,'岩壁','ARMOR_ADD',0,0,100,100,800,'SELF',3,'自身のアーマーを800追加する'),(54,'地震','ATK',150,0,100,100,0,'ENEMY_ALL',3,'敵全体に150ダメージを与える'),(55,'蒼天の雷','ATK_RATIO_SPD',0,5,100,100,0,'ENEMY',5,'素早さの5倍のダメージを与える'),(56,'天の加護','HEAL',999,0,100,100,0,'ALLY_ALL',1,'味方全体のHPを全回復する'),(57,'龍脈','BUFF',0,0,150,150,0,'SELF',3,'自身の攻・速を1.5倍にする'),(58,'震える','ARMOR_ADD',0,0,100,100,50,'SELF',10,'アーマーを50追加する'),(59,'転がる','ATK',10,0,100,100,0,'ENEMY',10,'10ダメージを与える'),(60,'未知の力','BUFF',0,0,200,200,0,'SELF',1,'自身の全ステータスを2倍にする'),(61,'滅びの息吹','ATK',500,0,100,100,0,'ENEMY_ALL',3,'敵全体に500ダメージを与える'),(62,'深淵の絶望','DEBUFF',0,0,50,50,0,'ENEMY_ALL',3,'敵全体の全ステータスを半分にする'),(63,'終焉の代償','SUICIDE_ATTACK',0,10,100,100,0,'ENEMY_ALL',1,'HP+アーマーの10倍ダメを与え自身は死亡する'),(64,'星の呼び声','ATK',1000,0,100,100,0,'ENEMY_ALL',5,'敵全体に1000ダメージを与える'),(65,'精神崩壊','DEBUFF',0,0,10,10,0,'ENEMY_ALL',5,'敵全体の全ステータスを0.1倍にする'),(66,'深淵の目醒め','ATK_RESERVE_HP',9999,0,100,100,0,'ENEMY',1,'敵を必ずHP1にする'),(901,'通常攻撃','BASIC_ATTACK',0,1,100,100,0,'ENEMY',-1,'敵1体に通常の攻撃を行う。回数制限なし。'),(902,'防御','BASIC_DEFENSE',0,0,100,100,0,'SELF',-1,'次の行動まで受けるダメージを半分にする。回数制限なし。'),(911,'みね打ち','ATK',1,1,100,100,0,'ENEMY',5,'この技で倒した敵は仲間になる。'),(912,'鎧砕き','ATK_ARMOR',100,1,100,100,0,'ENEMY',10,'アーマーのダメージ効率2倍'),(913,'鼓舞','BUFF',0,0,150,100,0,'SELF',5,'攻撃力1.5倍'),(914,'応急手当','HEAL',0,0,100,100,0,'SELF',3,'味方を回復'),(915,'秘薬','HEAL_USE',0,0,100,100,0,'SELF',1,'技使用回数を回復する。');
/*!40000 ALTER TABLE `skills_master` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'monster_tamer_db'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-02-16 23:51:43
