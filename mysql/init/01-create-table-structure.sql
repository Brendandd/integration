CREATE USER IF NOT EXISTS 'admin'@'%' IDENTIFIED BY 'admin';
GRANT ALL PRIVILEGES ON integration.* TO 'admin'@'%';
FLUSH PRIVILEGES;

CREATE TABLE `camel_messageprocessed` (
  `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,
  `processor_name` varchar(255) DEFAULT NULL,
  `message_id` varchar(100) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `camel_messageprocessed_seq` (
  `next_val` int NOT NULL,
  `sequence_name` varchar(45) NOT NULL,
  PRIMARY KEY (`next_val`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `component` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `type` varchar(45) DEFAULT NULL,
  `category` varchar(45) DEFAULT NULL,
  `owner` varchar(45) DEFAULT NULL,
  `route_id` int DEFAULT NULL,
  `inbound_state` varchar(45) DEFAULT NULL,
  `outbound_state` varchar(45) DEFAULT NULL,
  `created_by_user_id` varchar(45) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `communication_point_id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



CREATE TABLE `event` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` varchar(45) DEFAULT NULL,
  `payload` blob,
  `event_date_time` datetime DEFAULT NULL,
  `created_by_user_id` varchar(45) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `message` (
  `id` int NOT NULL AUTO_INCREMENT,
  `content` blob,
  `content_type` varchar(45) DEFAULT NULL,
  `created_by_user_id` varchar(45) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `message_id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
SELECT * FROM integration.component;

CREATE TABLE `message_flow_event` (
  `id` int NOT NULL AUTO_INCREMENT,
  `type` varchar(45) DEFAULT NULL,
  `event_date_time` datetime DEFAULT NULL,
  `message_flow_id` int DEFAULT NULL,
  `component_id` int DEFAULT NULL,
  `retry_count` int DEFAULT NULL,
  `retry_after` datetime DEFAULT NULL,
  `created_by_user_id` varchar(45) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=225 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `message_flow_group` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_by_user_id` varchar(45) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `message_flow` (
  `id` int NOT NULL AUTO_INCREMENT,
  `component_id` int DEFAULT NULL,
  `group_id` int DEFAULT NULL,
  `message_id` int DEFAULT NULL,
  `parent_message_flow_id` int DEFAULT NULL,
  `action` varchar(100) DEFAULT NULL,
  `created_by_user_id` varchar(45) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=260 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `message_flow_filtered` (
  `id` int NOT NULL AUTO_INCREMENT,
  `message_flow_id` int DEFAULT NULL,
  `name` varchar(45) DEFAULT NULL,
  `reason` varchar(100) DEFAULT NULL,
  `created_by_user_id` varchar(45) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `message_flow_error` (
  `id` int NOT NULL AUTO_INCREMENT,
  `message_flow_id` int DEFAULT NULL,
  `details` blob DEFAULT NULL,
  `created_by_user_id` varchar(45) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `route` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  `owner` varchar(45) DEFAULT NULL,
  `created_by_user_id` varchar(45) DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shedlock` (
  `name` varchar(64) NOT NULL,
  `lock_until` timestamp(3) NULL DEFAULT NULL,
  `locked_at` timestamp(3) NULL DEFAULT NULL,
  `locked_by` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `message_flow_property` (
  `id` int NOT NULL AUTO_INCREMENT,
  `property_key` varchar(100) DEFAULT NULL,
  `value` varchar(100) DEFAULT NULL,
  `message_flow_id` int DEFAULT NULL,
  `created_date` datetime DEFAULT NULL,
  `created_by_user_id` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
