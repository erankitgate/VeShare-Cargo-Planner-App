CREATE DATABASE  IF NOT EXISTS `generaldbw` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `generaldbw`;
-- MySQL dump 10.13  Distrib 8.0.16, for Win64 (x86_64)
--
-- Host: localhost    Database: generaldbw
-- ------------------------------------------------------
-- Server version	8.0.16

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `addresses`
--

DROP TABLE IF EXISTS `addresses`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `addresses` (
  `Unique_address_id` int(11) NOT NULL,
  `Address_line1` varchar(45) DEFAULT NULL,
  `Address_line2` varchar(45) DEFAULT NULL,
  `Address_line3` varchar(45) DEFAULT NULL,
  `Landmark` varchar(45) DEFAULT NULL,
  `Locatlity` varchar(45) DEFAULT NULL,
  `City/town` varchar(45) DEFAULT NULL,
  `State` varchar(45) NOT NULL,
  `Country` varchar(45) NOT NULL,
  `Pincode` varchar(45) NOT NULL,
  `Coordinate_id` int(11) DEFAULT NULL,
  `Assoicated_with` enum('Consignor','Consignee','Operator','Depot',' Driver') DEFAULT NULL COMMENT 'Consignor Consignee Operator Depot Driver',
  PRIMARY KEY (`Unique_address_id`),
  KEY `addresses_coordinateIDFK_idx` (`Coordinate_id`),
  CONSTRAINT `addresses_coordinateIDFK` FOREIGN KEY (`Coordinate_id`) REFERENCES `coordinate_ids` (`Unique_coordinate_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `carrier_issues`
--

DROP TABLE IF EXISTS `carrier_issues`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `carrier_issues` (
  `Unique_issue_id` int(11) NOT NULL,
  `Carrier_id` int(11) DEFAULT NULL,
  `Issue_type` varchar(45) DEFAULT NULL,
  `Timestamp` timestamp NULL DEFAULT NULL,
  `Description_of_issue` varchar(200) DEFAULT NULL,
  `Path_to_image` varchar(45) DEFAULT NULL,
  `Date_issue_resolution` date DEFAULT NULL,
  PRIMARY KEY (`Unique_issue_id`),
  KEY `carrierIssues_carrierIDFK_idx` (`Carrier_id`),
  CONSTRAINT `carrierIssues_carrierIDFK` FOREIGN KEY (`Carrier_id`) REFERENCES `carriers_on_platform` (`Unique_carrier_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `carrier_live_locations`
--

DROP TABLE IF EXISTS `carrier_live_locations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `carrier_live_locations` (
  `Unique_carrier_id` int(11) NOT NULL,
  `Timestamp` datetime DEFAULT NULL,
  `Latitude` float DEFAULT NULL,
  `Longitude` float DEFAULT NULL,
  `Fuel_level` int(11) DEFAULT NULL COMMENT 'in percentage',
  PRIMARY KEY (`Unique_carrier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `carriers_on_platform`
--

DROP TABLE IF EXISTS `carriers_on_platform`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `carriers_on_platform` (
  `Unique_carrier_id` int(11) NOT NULL,
  `Plate_number` varchar(45) DEFAULT NULL,
  `Operator_id` int(11) NOT NULL,
  `Depot_id` int(11) DEFAULT NULL,
  `Name` varchar(45) DEFAULT NULL COMMENT 'This is for the name given to the carrier by the manufacturer like Tata Ace',
  `Operate_on` enum('ROADWAY','RAILWAY','AIRWAY','WATERWAY') DEFAULT 'ROADWAY' COMMENT 'Road / Railway /Air /Water',
  `Type` enum('FLATBED','ATTACHED') DEFAULT NULL COMMENT 'Flatbed or Attached',
  `Category` varchar(45) DEFAULT NULL COMMENT 'Select among these\\nVery light\\nLight duty \\nMedium duty \\nHeavy duty\\nVery heavy',
  `Number_of_wheels` int(11) DEFAULT NULL,
  `Fuel_type` varchar(45) DEFAULT NULL,
  `Number_of_container` int(11) NOT NULL DEFAULT '1',
  `Total_capacity` float NOT NULL COMMENT 'Unit: Tonnes ',
  `Manufacturer_company` varchar(45) NOT NULL,
  `Model` varchar(45) DEFAULT NULL,
  `Launch_Year` year(4) DEFAULT NULL,
  `Manufacturing_discontinued` year(4) DEFAULT NULL,
  `Energy_consumption_pkm` double DEFAULT NULL COMMENT 'Unit:  kWh',
  `Emission_CO2_pkm` double DEFAULT NULL COMMENT 'Unit: g/km',
  PRIMARY KEY (`Unique_carrier_id`),
  KEY `operatorIDFK_idx` (`Operator_id`),
  KEY `carriers_depotIDFK_idx` (`Depot_id`),
  CONSTRAINT `carriers_depotIDFK` FOREIGN KEY (`Depot_id`) REFERENCES `depots_on_platform` (`Unique_depot_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `carriers_operatorIDFK` FOREIGN KEY (`Operator_id`) REFERENCES `operators` (`Unique_operator_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `consignment_movement`
--

DROP TABLE IF EXISTS `consignment_movement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `consignment_movement` (
  `Unique_consignment_id` int(11) NOT NULL,
  `Sequence_hop` int(11) DEFAULT NULL COMMENT ' this is relevant when consignment moves between containers, and in case of multi-modal shipment. In case of movement within the container, we’ll have multiple row entires in table.\n',
  `Start_coordinate_id` int(11) DEFAULT NULL,
  `End_coordinate_id` int(11) DEFAULT NULL,
  `Start_address_id` int(11) DEFAULT NULL,
  `End_address_id` int(11) DEFAULT NULL,
  `X` int(11) unsigned DEFAULT NULL COMMENT 'Id in location table pointing out position of cargo in container',
  `Y` int(11) unsigned DEFAULT NULL,
  `Z` int(11) unsigned DEFAULT NULL,
  `Status` int(11) DEFAULT NULL COMMENT '[-1 (not yet picked), 0 (in container), 1 (completed the journey and delivered -- drop-off done)]',
  `Pickup_timestamp` datetime DEFAULT NULL,
  `Dropoff_timestamp` datetime DEFAULT NULL,
  `Wall` int(11) unsigned DEFAULT NULL COMMENT 'First wall starts from container origin i.e. lower left corner of container (opposite end of container door)',
  `Layer` int(11) unsigned DEFAULT NULL,
  `Position_in_layer` int(11) unsigned DEFAULT NULL,
  `Container_id` int(11) DEFAULT NULL,
  `Priority` int(11) DEFAULT NULL,
  PRIMARY KEY (`Unique_consignment_id`),
  KEY `consignmentMovement_startAddressIDFK_idx` (`Start_address_id`),
  KEY `consignmentMovement_endAddressIDFK_idx` (`End_address_id`),
  KEY `consignmentMovement_startCoordinateIDFK_idx` (`Start_coordinate_id`),
  KEY `consignmentMovement_endCoordinateIDFK_idx` (`End_coordinate_id`),
  KEY `consignmentMovement_containerIDFK_idx` (`Container_id`),
  CONSTRAINT `consignmentMovement_containerIDFK` FOREIGN KEY (`Container_id`) REFERENCES `containers_on_platform` (`Unique_container_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `consignmentMovement_endAddressIDFK` FOREIGN KEY (`End_address_id`) REFERENCES `addresses` (`Unique_address_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `consignmentMovement_endCoordinateIDFK` FOREIGN KEY (`End_coordinate_id`) REFERENCES `coordinate_ids` (`Unique_coordinate_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `consignmentMovement_startAddressIDFK` FOREIGN KEY (`Start_address_id`) REFERENCES `addresses` (`Unique_address_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `consignmentMovement_startCoordinateIDFK` FOREIGN KEY (`Start_coordinate_id`) REFERENCES `coordinate_ids` (`Unique_coordinate_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `consignmentMovement_uniqueConsignementId` FOREIGN KEY (`Unique_consignment_id`) REFERENCES `consignments` (`Unique_consignment_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `consignments`
--

DROP TABLE IF EXISTS `consignments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `consignments` (
  `Unique_consignment_id` int(11) NOT NULL,
  `Date_added` date DEFAULT NULL,
  `Delivery_deadline_date` date DEFAULT NULL,
  `Goods_type` varchar(45) DEFAULT NULL,
  `Goods_quantity` int(11) DEFAULT NULL,
  `Shipment_charges` double DEFAULT NULL,
  `Max_temperature` double DEFAULT NULL COMMENT 'Unit: Degree Celcius',
  `Min_temperature` double DEFAULT NULL COMMENT 'Unit: Degree Celcius',
  `Special_handling` varchar(45) DEFAULT NULL,
  `Packaging_type` varchar(45) DEFAULT NULL,
  `Pickup_time_window_start` time DEFAULT NULL,
  `Pickup_time_window_end` time DEFAULT NULL,
  `Dropoff_time_window_start` time DEFAULT NULL,
  `Dropoff_time_window_end` time DEFAULT NULL,
  `Pickup_coordinate_id` int(11) DEFAULT NULL,
  `Dropoff_coordinate_id` int(11) DEFAULT NULL,
  `Pickup_address_id` int(11) DEFAULT NULL,
  `Dropoff_address_id` int(11) DEFAULT NULL,
  `Current_operator_id` int(11) DEFAULT NULL COMMENT 'either this consignment is in this container or is to be picked by the carrier moving this container next. If delivered, we’ll keep the last container it was in and move the row entry to another table.',
  `Status` varchar(45) DEFAULT NULL COMMENT 'To be allocated\nTo be picked\nIn the container\nDelivered\nDelivery Failed\nReturning to sender depot',
  `Pickup_timestamp` datetime DEFAULT NULL,
  `Delivered_timestamp` datetime DEFAULT NULL,
  `Box_description` varchar(45) DEFAULT NULL,
  `length` int(11) unsigned NOT NULL DEFAULT '35' COMMENT 'Unit is cm',
  `width` int(11) unsigned NOT NULL DEFAULT '27' COMMENT 'Unit is cm',
  `height` int(11) unsigned NOT NULL DEFAULT '2' COMMENT 'Unit is cm',
  `weight` int(11) unsigned NOT NULL DEFAULT '0' COMMENT 'Unit is gram',
  `is_floor_only` tinyint(1) unsigned DEFAULT '0' COMMENT 'Default is false',
  `length_upright_allowed` tinyint(1) unsigned DEFAULT '1' COMMENT 'Default is true',
  `width_upright_allowed` tinyint(1) unsigned DEFAULT '1' COMMENT 'Default is true',
  `height_upright_allowed` tinyint(1) unsigned DEFAULT '1' COMMENT 'Default is true',
  `is_stackable` tinyint(1) unsigned DEFAULT '1' COMMENT 'Default is true',
  `stack_weight_length_upright` int(11) unsigned DEFAULT NULL COMMENT 'Unit is gram',
  `stack_weight_width_upright` int(11) unsigned DEFAULT NULL COMMENT 'Unit is gram',
  `stack_weight_height_upright` int(11) unsigned DEFAULT NULL COMMENT 'Unit is gram',
  `color` varchar(45) DEFAULT 'navy',
  PRIMARY KEY (`Unique_consignment_id`),
  KEY `consignments_currentOperatorIDFK_idx` (`Current_operator_id`),
  CONSTRAINT `consignments_currentOperatorIDFK` FOREIGN KEY (`Current_operator_id`) REFERENCES `operators` (`Unique_operator_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `container_issues`
--

DROP TABLE IF EXISTS `container_issues`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `container_issues` (
  `Unique_issue_id` int(11) NOT NULL,
  `Container_id` int(11) DEFAULT NULL,
  `Issue_type` varchar(45) DEFAULT NULL,
  `Timestamp` timestamp NULL DEFAULT NULL,
  `Description_of_issue` varchar(200) DEFAULT NULL,
  `Path_to_image` varchar(45) DEFAULT NULL,
  `Date_issue_resolution` date DEFAULT NULL,
  PRIMARY KEY (`Unique_issue_id`),
  KEY `containerIssues_containerIDFK_idx` (`Container_id`),
  CONSTRAINT `containerIssues_containerIDFK` FOREIGN KEY (`Container_id`) REFERENCES `containers_on_platform` (`Unique_container_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `containers_on_platform`
--

DROP TABLE IF EXISTS `containers_on_platform`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `containers_on_platform` (
  `Unique_container_id` int(11) NOT NULL,
  `Operator_id` int(11) DEFAULT NULL,
  `Carrier_id` int(11) DEFAULT NULL,
  `Roof` enum('OPEN','CLOSED') DEFAULT 'CLOSED' COMMENT 'Open or Closed',
  `Roof_material` varchar(45) DEFAULT NULL,
  `Type_of_cargo_supported` enum('Dry','Insulated','AirConditioned','Frozen','TemperatureControlled') DEFAULT 'Dry' COMMENT 'Dry Insulated AirConditioned Frozen TemperatureControlled',
  `External_length` int(11) DEFAULT NULL COMMENT 'Unit: cm',
  `External_width` int(11) DEFAULT NULL COMMENT 'Unit: cm',
  `External_height` int(11) DEFAULT NULL COMMENT 'Unit: cm',
  `Internal_length` int(11) unsigned NOT NULL COMMENT 'Unit: cm',
  `Internal_width` int(11) unsigned NOT NULL COMMENT 'Unit: cm',
  `Internal_height` int(11) unsigned NOT NULL COMMENT 'Unit: cm',
  `Overhang_length` int(11) DEFAULT NULL COMMENT 'Unit: cm',
  `Overhang_width` int(11) DEFAULT NULL COMMENT 'Unit: cm',
  `Overhang_height` int(11) DEFAULT NULL COMMENT 'Unit: cm',
  `Material` varchar(45) DEFAULT NULL,
  `Tare_weight` int(11) unsigned DEFAULT NULL COMMENT 'Empty container weight\\\\nUnit: kg',
  `Tonnage` int(11) unsigned NOT NULL COMMENT 'Load carrying capacity weight\\\\\\\\nUnit: kg',
  `Container_description` varchar(45) DEFAULT NULL COMMENT 'Comma separated list of use cases',
  PRIMARY KEY (`Unique_container_id`),
  KEY `operatorIDFK_idx` (`Operator_id`),
  KEY `carrierIDFK_idx` (`Carrier_id`),
  CONSTRAINT `containers_carrierIDFK` FOREIGN KEY (`Carrier_id`) REFERENCES `carriers_on_platform` (`Unique_carrier_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `containers_operatorIDFK` FOREIGN KEY (`Operator_id`) REFERENCES `operators` (`Unique_operator_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `coordinate_ids`
--

DROP TABLE IF EXISTS `coordinate_ids`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `coordinate_ids` (
  `Unique_coordinate_id` int(11) NOT NULL,
  `Latitude` float NOT NULL,
  `Longitude` float NOT NULL,
  PRIMARY KEY (`Unique_coordinate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `depots_on_platform`
--

DROP TABLE IF EXISTS `depots_on_platform`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `depots_on_platform` (
  `Unique_depot_id` int(11) NOT NULL,
  `Type` varchar(45) DEFAULT NULL COMMENT 'Warehouse \nPod\nGodown\nRetail shop\nprivate location\n',
  `Address_id` int(11) DEFAULT NULL,
  `Contact_number` varchar(45) DEFAULT NULL,
  `Contact_email` varchar(45) DEFAULT NULL,
  `Goods_type` varchar(45) DEFAULT NULL,
  `Coordinates_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`Unique_depot_id`),
  KEY `depots_addressIDFK_idx` (`Address_id`),
  KEY `depots_coordinateIDFK_idx` (`Coordinates_id`),
  CONSTRAINT `depots_addressIDFK` FOREIGN KEY (`Address_id`) REFERENCES `addresses` (`Unique_address_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `depots_coordinateIDFK` FOREIGN KEY (`Coordinates_id`) REFERENCES `coordinate_ids` (`Unique_coordinate_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `drivers_on_platform`
--

DROP TABLE IF EXISTS `drivers_on_platform`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `drivers_on_platform` (
  `Unique_diver_id` int(11) NOT NULL,
  `Operator_id` int(11) NOT NULL,
  `Contact_number` varchar(45) NOT NULL,
  `Name` varchar(45) DEFAULT NULL,
  `Age` int(11) DEFAULT NULL,
  `Gender` enum('Male','Female','Other') DEFAULT NULL,
  `Type_of_vehicles_drivable` varchar(45) DEFAULT NULL,
  `Driver_license_id` varchar(45) DEFAULT NULL,
  `Address_id` int(11) DEFAULT NULL,
  `Nationality` varchar(45) DEFAULT NULL,
  `Driver_license_image_path` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`Unique_diver_id`),
  KEY `drivers_operatorIDFK_idx` (`Operator_id`),
  KEY `drivers_addressIDFK_idx` (`Address_id`),
  CONSTRAINT `drivers_addressIDFK` FOREIGN KEY (`Address_id`) REFERENCES `addresses` (`Unique_address_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `drivers_operatorIDFK` FOREIGN KEY (`Operator_id`) REFERENCES `operators` (`Unique_operator_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `operators`
--

DROP TABLE IF EXISTS `operators`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `operators` (
  `Unique_operator_id` int(11) NOT NULL,
  `First_name` varchar(45) DEFAULT NULL,
  `Last_name` varchar(45) DEFAULT NULL,
  `Registered_company` varchar(45) DEFAULT NULL COMMENT 'YES or NO',
  `Company_name` varchar(45) DEFAULT NULL,
  `GST number` varchar(45) DEFAULT NULL,
  `Address_id` int(11) DEFAULT NULL,
  `Nationality` varchar(45) DEFAULT NULL,
  `Number_of_vehicles` int(11) DEFAULT NULL,
  `Type_of_operation` varchar(45) DEFAULT NULL,
  `Number_of_drivers` int(11) DEFAULT NULL,
  PRIMARY KEY (`Unique_operator_id`),
  KEY `operators_addressIDFK_idx` (`Address_id`),
  CONSTRAINT `operators_addressIDFK` FOREIGN KEY (`Address_id`) REFERENCES `addresses` (`Unique_address_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `passwords`
--

DROP TABLE IF EXISTS `passwords`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `passwords` (
  `password_id` int(11) NOT NULL,
  `password` varchar(45) NOT NULL,
  PRIMARY KEY (`password_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `routes_current`
--

DROP TABLE IF EXISTS `routes_current`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `routes_current` (
  `Unique_route_id` int(11) NOT NULL,
  `List_of_coordinate_ids` varchar(45) DEFAULT NULL COMMENT 'List of drop location coordinates seperated by commas in order of delivery',
  `Time_of_route_generation` datetime DEFAULT NULL,
  `Carrier_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`Unique_route_id`),
  KEY `routesCurrent_carrierIDFK_idx` (`Carrier_id`),
  CONSTRAINT `routesCurrent_carrierIDFK` FOREIGN KEY (`Carrier_id`) REFERENCES `carriers_on_platform` (`Unique_carrier_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `routes_history`
--

DROP TABLE IF EXISTS `routes_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `routes_history` (
  `Unique_route_id` int(11) NOT NULL,
  `List_of_coordinate_ids` varchar(45) DEFAULT NULL COMMENT 'List of drop location coordinates seperated by commas in order of delivery',
  `Time_of_generation` datetime DEFAULT NULL,
  `Carrier_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`Unique_route_id`),
  KEY `routesHistory_carrierIDFK_idx` (`Carrier_id`),
  CONSTRAINT `routesHistory_carrierIDFK` FOREIGN KEY (`Carrier_id`) REFERENCES `carriers_on_platform` (`Unique_carrier_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sequence`
--

DROP TABLE IF EXISTS `sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `sequence` (
  `SEQ_NAME` varchar(50) NOT NULL,
  `SEQ_COUNT` decimal(38,0) DEFAULT NULL,
  PRIMARY KEY (`SEQ_NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_accounts`
--

DROP TABLE IF EXISTS `user_accounts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `user_accounts` (
  `Unique_user_account_id` int(11) unsigned NOT NULL,
  `Username` varchar(45) DEFAULT NULL,
  `Email` varchar(45) DEFAULT NULL,
  `Phone_number` varchar(45) DEFAULT NULL,
  `Password_id` int(11) DEFAULT NULL,
  `Role` enum('Operator','Admin','Driver') NOT NULL,
  `Account_creation_date` date NOT NULL,
  `Device_ids` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`Unique_user_account_id`),
  UNIQUE KEY `Unique_user_account_id_UNIQUE` (`Unique_user_account_id`),
  UNIQUE KEY `Phone_number_UNIQUE` (`Phone_number`),
  UNIQUE KEY `Email_UNIQUE` (`Email`),
  KEY `passwordFK_idx` (`Password_id`),
  CONSTRAINT `passwordFK` FOREIGN KEY (`Password_id`) REFERENCES `passwords` (`password_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping routines for database 'generaldbw'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-06-04 19:30:24
