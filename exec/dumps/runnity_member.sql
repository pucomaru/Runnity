-- MySQL dump 10.13  Distrib 8.0.43, for Win64 (x86_64)
--
-- Host: 13.209.89.196    Database: runnity
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
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT,
  `average_pace` int DEFAULT NULL,
  `birth` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `gender` varchar(255) DEFAULT NULL,
  `height` float DEFAULT NULL,
  `nickname` varchar(255) DEFAULT NULL,
  `profile_image` varchar(255) DEFAULT NULL,
  `social_type` varchar(255) DEFAULT NULL,
  `social_uid` varchar(255) NOT NULL,
  `weight` float DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `UK6iv7u70vy1re0ebbwqkhwt17a` (`social_uid`)
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member`
--

LOCK TABLES `member` WRITE;
/*!40000 ALTER TABLE `member` DISABLE KEYS */;
INSERT INTO `member` VALUES (1,NULL,'2025-11-11','myannnng@gmail.com','MALE',160,'마루',NULL,'GOOGLE','103637718457299303064',48,'2025-11-10 17:27:11.121188',_binary '\0','2025-11-12 10:27:10.989902'),(3,292,'1997-03-23','g2vssw@gmail.com','MALE',190,'g2v','https://runnity-s3-bucket.s3.ap-northeast-2.amazonaws.com/profile-photos/3/2667d08d-85a6-481f-89d7-e3e3ff93eebb.jpg','GOOGLE','111326314914400802992',70,'2025-11-11 11:50:18.506516',_binary '\0','2025-11-17 16:27:52.670008'),(60,NULL,'2025-11-11','','MALE',175.41,'abc1','https://runnity-s3-bucket.s3.ap-northeast-2.amazonaws.com/profile-photos/60/619e73ed-890a-497b-8aee-a6aba582653e.jpg','KAKAO','4532521692',68.21,'2025-11-12 13:15:32.557779',_binary '\0','2025-11-12 14:24:31.430451'),(61,NULL,'1998-09-21','','FEMALE',160,'마루마루','https://runnity-s3-bucket.s3.ap-northeast-2.amazonaws.com/profile-photos/61/b96ca672-3889-4aa8-8cf2-d52d0958751e.png','KAKAO','4541086109',48,'2025-11-12 13:15:45.078993',_binary '\0','2025-11-17 14:41:40.032822'),(63,NULL,NULL,'chosarah70@gmail.com',NULL,NULL,NULL,NULL,'GOOGLE','111386880482821729941',NULL,'2025-11-12 17:59:47.032057',_binary '\0','2025-11-12 17:59:47.032057'),(64,390,NULL,'rlatkdrud123@gmail.com',NULL,NULL,'sang','https://runnity-s3-bucket.s3.ap-northeast-2.amazonaws.com/profile-photos/3/d1739f36-d0b7-4540-ab91-92bbba533514.gif','GOOGLE','103329728794569999890',NULL,'2025-11-13 01:13:03.143028',_binary '\0','2025-11-13 01:13:03.143028'),(66,NULL,'1997-08-04','','MALE',181,'TToy',NULL,'KAKAO','4542895912',65,'2025-11-13 12:54:51.700935',_binary '\0','2025-11-13 12:56:35.584162'),(67,NULL,'1997-03-23','','MALE',190,'기사','https://runnity-s3-bucket.s3.ap-northeast-2.amazonaws.com/profile-photos/67/07cfbc70-7dcd-4a4b-bbe9-1055f2044fe8.gif','KAKAO','4541253332',70,'2025-11-17 10:03:54.848635',_binary '\0','2025-11-17 14:41:21.535958'),(68,NULL,'1997-03-23','','MALE',190,'카카오',NULL,'KAKAO','4539726548',80,'2025-11-17 17:48:23.558194',_binary '\0','2025-11-17 17:48:54.823173');
/*!40000 ALTER TABLE `member` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-17 19:25:22
