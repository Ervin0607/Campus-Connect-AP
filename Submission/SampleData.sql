-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: localhost    Database: auth_db
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `auth_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `auth_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `auth_db`;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `UserID` int NOT NULL,
  `UserName` varchar(100) NOT NULL,
  `Role` varchar(50) NOT NULL,
  `Password` varchar(100) NOT NULL,
  `Status` varchar(100) NOT NULL,
  PRIMARY KEY (`UserID`),
  UNIQUE KEY `UserName` (`UserName`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (11,'admin1','ADMIN','$2a$10$EJ8bS4meY9pmt.QbRsSEdelHUhAYK44QiHfLoXMiRqIcWyTGnm./e','ACTIVE'),(12,'inst1','INSTRUCTOR','$2a$10$Wu3AL3amfSnemsapFSDiKuE9YEEdHvUZJTjBpq/3Vdcj7J6uQHgmC','ACTIVE'),(13,'stu1','STUDENT','$2a$10$PhSVFjbEG4iWDpX.MyihvOFhu/eXQIP4lc2AvpPHSjN9w/s3W2oEe','ACTIVE'),(14,'stu2','STUDENT','$2a$10$I6gx4cFXovNPKk0IXu5OwO7OOsP63LYeSUixSnvA2p/apkaEwboFG','ACTIVE'),(18,'inst2','INSTRUCTOR','$2a$10$A2DaE2knrotA6EVrTGwQaeUPT6R7gEdxJfg653t3nl0465PU3kWNC','ACTIVE');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Current Database: `erp_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `erp_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `erp_db`;

--
-- Table structure for table `courses`
--

DROP TABLE IF EXISTS `courses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courses` (
  `CourseId` int NOT NULL,
  `Code` varchar(40) NOT NULL,
  `Title` varchar(100) NOT NULL,
  `Credits` int NOT NULL,
  PRIMARY KEY (`CourseId`),
  UNIQUE KEY `Code` (`Code`),
  UNIQUE KEY `Title` (`Title`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courses`
--

LOCK TABLES `courses` WRITE;
/*!40000 ALTER TABLE `courses` DISABLE KEYS */;
INSERT INTO `courses` VALUES (1,'CSE101','Intro to Programming',4),(2,'DES101','Intro to HCI',4),(3,'ECE101','Digital Circuits',4),(4,'COM101','Communication Skills',4),(5,'MTH101','Linear Algebra',4);
/*!40000 ALTER TABLE `courses` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `instructors`
--

DROP TABLE IF EXISTS `instructors`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `instructors` (
  `UserID` int NOT NULL,
  `Name` varchar(200) NOT NULL,
  `Email` varchar(100) NOT NULL,
  `Qualification` varchar(100) NOT NULL,
  `JoiningDate` date NOT NULL,
  `Department` varchar(100) NOT NULL,
  `InstructorID` int NOT NULL,
  PRIMARY KEY (`UserID`),
  UNIQUE KEY `Email` (`Email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `instructors`
--

LOCK TABLES `instructors` WRITE;
/*!40000 ALTER TABLE `instructors` DISABLE KEYS */;
INSERT INTO `instructors` VALUES (12,'inst1','inst1@gmail.com','PHD','2025-11-26','CSE',1),(18,'inst2','inst2@gmail.com','PHD','2025-11-27','CSE',2);
/*!40000 ALTER TABLE `instructors` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `offerings`
--

DROP TABLE IF EXISTS `offerings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `offerings` (
  `OfferingID` int NOT NULL,
  `CourseID` int NOT NULL,
  `InstructorID` int NOT NULL,
  `Semester` varchar(100) NOT NULL,
  `Year` int NOT NULL,
  `Capacity` int DEFAULT NULL,
  `CurrentEnrollment` int DEFAULT NULL,
  `GradeSlabs` json DEFAULT NULL,
  `GradeComponents` json DEFAULT NULL,
  `LectureSchedule` json DEFAULT NULL,
  `LabSchedule` json DEFAULT NULL,
  `Announcements` json DEFAULT NULL,
  PRIMARY KEY (`OfferingID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `offerings`
--

LOCK TABLES `offerings` WRITE;
/*!40000 ALTER TABLE `offerings` DISABLE KEYS */;
INSERT INTO `offerings` VALUES (6,1,1,'SPRING',2025,1,0,'{\"A+\": 90, \"A-\": 80, \"B+\": 70, \"B-\": 60, \"C+\": 50, \"C-\": 40, \"D+\": 30, \"D-\": 20}','{}','[{\"day\": \"Mon\", \"end\": \"09:00\", \"start\": \"08:00\"}]',NULL,'[]'),(7,2,1,'SPRING',2025,30,0,'{\"A+\": 90, \"A-\": 80, \"B+\": 70, \"B-\": 60, \"C+\": 50, \"C-\": 40, \"D+\": 30, \"D-\": 20}','{}','[{\"day\": \"Tue\", \"end\": \"10:00\", \"start\": \"09:00\"}]',NULL,'[]'),(8,3,1,'SPRING',2025,30,0,'{\"A+\": 90, \"A-\": 80, \"B+\": 70, \"B-\": 60, \"C+\": 50, \"C-\": 40, \"D+\": 30, \"D-\": 20}','{}','[{\"day\": \"Wed\", \"end\": \"11:00\", \"start\": \"10:00\"}]',NULL,'[]'),(9,4,1,'SPRING',2025,30,0,'{\"A+\": 90, \"A-\": 80, \"B+\": 70, \"B-\": 60, \"C+\": 50, \"C-\": 40, \"D+\": 30, \"D-\": 20}','{}','[{\"day\": \"Thu\", \"end\": \"12:00\", \"start\": \"11:00\"}]',NULL,'[]'),(10,5,1,'SPRING',2025,30,0,'{\"A+\": 90, \"A-\": 80, \"B+\": 70, \"B-\": 60, \"C+\": 50, \"C-\": 40, \"D+\": 30, \"D-\": 20}','{}','[{\"day\": \"Fri\", \"end\": \"13:00\", \"start\": \"12:00\"}]',NULL,'[]'),(11,1,2,'SPRING',2025,30,0,'{\"A+\": 90, \"A-\": 80, \"B+\": 70, \"B-\": 60, \"C+\": 50, \"C-\": 40, \"D+\": 30, \"D-\": 20}','{}',NULL,NULL,'[]');
/*!40000 ALTER TABLE `offerings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `registrations`
--

DROP TABLE IF EXISTS `registrations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `registrations` (
  `RegistrationNumber` int NOT NULL,
  `StudentRollNumber` int NOT NULL,
  `OfferingID` int NOT NULL,
  `Status` varchar(100) NOT NULL,
  PRIMARY KEY (`RegistrationNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `registrations`
--

LOCK TABLES `registrations` WRITE;
/*!40000 ALTER TABLE `registrations` DISABLE KEYS */;
/*!40000 ALTER TABLE `registrations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `semesters`
--

DROP TABLE IF EXISTS `semesters`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `semesters` (
  `SemData` json DEFAULT NULL,
  `Maintainence` int DEFAULT NULL,
  `CurrentSem` json DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `semesters`
--

LOCK TABLES `semesters` WRITE;
/*!40000 ALTER TABLE `semesters` DISABLE KEYS */;
INSERT INTO `semesters` VALUES ('[{\"Year\": 2025, \"EndDate\": \"2025-08-01\", \"Semester\": \"Spring\", \"StartDate\": \"2025-03-01\", \"RegistrationEndDate\": \"2025-03-08\", \"RegistrationStartDate\": \"2025-02-23\"}, {\"Year\": 2025, \"EndDate\": \"2026-02-01\", \"Semester\": \"Spring\", \"StartDate\": \"2025-12-01\", \"RegistrationEndDate\": \"2025-12-06\", \"RegistrationStartDate\": \"2025-11-23\"}]',0,'{\"Year\": 2025, \"EndDate\": \"2026-02-01\", \"Semester\": \"Spring\", \"StartDate\": \"2025-12-01\", \"RegistrationEndDate\": \"2025-12-06\", \"RegistrationStartDate\": \"2025-11-23\"}');
/*!40000 ALTER TABLE `semesters` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `studentgraderecord`
--

DROP TABLE IF EXISTS `studentgraderecord`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `studentgraderecord` (
  `OfferingID` int NOT NULL,
  `RollNumber` int DEFAULT NULL,
  `Grade` json DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `studentgraderecord`
--

LOCK TABLES `studentgraderecord` WRITE;
/*!40000 ALTER TABLE `studentgraderecord` DISABLE KEYS */;
/*!40000 ALTER TABLE `studentgraderecord` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `studentnotifications`
--

DROP TABLE IF EXISTS `studentnotifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `studentnotifications` (
  `StudentRollNumber` int NOT NULL,
  `NotificationData` json DEFAULT NULL,
  PRIMARY KEY (`StudentRollNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `studentnotifications`
--

LOCK TABLES `studentnotifications` WRITE;
/*!40000 ALTER TABLE `studentnotifications` DISABLE KEYS */;
/*!40000 ALTER TABLE `studentnotifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `students`
--

DROP TABLE IF EXISTS `students`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `students` (
  `Name` varchar(100) NOT NULL,
  `RollNumber` int NOT NULL,
  `Program` varchar(200) NOT NULL,
  `Year` int NOT NULL,
  `UserID` int NOT NULL,
  PRIMARY KEY (`RollNumber`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `students`
--

LOCK TABLES `students` WRITE;
/*!40000 ALTER TABLE `students` DISABLE KEYS */;
INSERT INTO `students` VALUES ('stu1',2025001,'CSE',1,13),('stu2',2025002,'CSAM',1,14);
/*!40000 ALTER TABLE `students` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-27 19:37:46
