
-- Create the 'admin' user (if not exists) and grant permissions
CREATE USER IF NOT EXISTS 'admin'@'%' IDENTIFIED BY 'admin';
GRANT ALL PRIVILEGES ON integration.* TO 'admin'@'%';
FLUSH PRIVILEGES;-- MySQL dump 10.13  Distrib 8.0.33, for Win64 (x86_64)
--
-- Host: localhost    Database: integration
-- ------------------------------------------------------
-- Server version	8.0.33

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
-- Table structure for table `component_route`
--

DROP TABLE IF EXISTS `component_route`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `component_route` (
  `id` int NOT NULL AUTO_INCREMENT,
  `component_id` int DEFAULT NULL,
  `route_id` int DEFAULT NULL,
  `inbound_state` varchar(45) DEFAULT NULL,
  `outbound_state` varchar(45) DEFAULT NULL,
  `external_system_id` int DEFAULT NULL,
  `created_by_user_id` varchar(45) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `communication_point_id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `component_route`
--

LOCK TABLES `component_route` WRITE;
/*!40000 ALTER TABLE `component_route` DISABLE KEYS */;
INSERT INTO `component_route` VALUES (16,3,1,'RUNNING','RUNNING',1,NULL,NULL),(17,13,1,'RUNNING','RUNNING',1,NULL,NULL),(29,5,2,'RUNNING','RUNNING',1,NULL,NULL),(30,6,2,'RUNNING','RUNNING',1,NULL,NULL),(31,9,2,'RUNNING','RUNNING',1,NULL,NULL),(32,11,2,'RUNNING','RUNNING',1,NULL,NULL),(33,14,2,'RUNNING','RUNNING',1,NULL,NULL),(34,4,2,'RUNNING','RUNNING',1,NULL,NULL),(36,16,2,'RUNNING','RUNNING',1,NULL,NULL),(37,1,4,'RUNNING','RUNNING',1,NULL,NULL),(38,16,4,'RUNNING','RUNNING',1,NULL,NULL),(39,2,5,'RUNNING','RUNNING',1,NULL,NULL),(40,17,5,'RUNNING','RUNNING',1,NULL,NULL),(41,17,2,'RUNNING','RUNNING',1,NULL,NULL);
/*!40000 ALTER TABLE `component_route` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-04-19 18:38:07
