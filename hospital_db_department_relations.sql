-- Target database (must match hospital_db_perfect.sql)
USE `hospital_db`;

SET FOREIGN_KEY_CHECKS = 0;

-- Drop dependent table first to avoid FK conflicts
DROP TABLE IF EXISTS `doctor`;
DROP TABLE IF EXISTS `department`;

-- Recreate department table
CREATE TABLE `department` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '科室ID',
  `name` VARCHAR(50) NOT NULL COMMENT '科室名称',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '科室简介',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_department_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医院科室表';

-- Seed department records (based on existing doctor data)
INSERT INTO `department` (`id`, `name`, `description`, `created_at`, `updated_at`) VALUES
  (1, '内科', '负责内科常见病诊疗与慢病管理', NOW(), NOW()),
  (2, '外科', '提供外科手术、创伤与术后康复服务', NOW(), NOW()),
  (3, '儿科', '为儿童提供常见疾病诊疗与疫苗咨询', NOW(), NOW()),
  (4, '放射科', '承担影像检查、诊断支持等工作', NOW(), NOW());

-- Recreate doctor table with department foreign key
CREATE TABLE `doctor` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '医生ID',
  `user_id` BIGINT NOT NULL COMMENT '关联用户ID',
  `department_id` BIGINT NOT NULL COMMENT '所属科室ID',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `title` VARCHAR(50) DEFAULT NULL COMMENT '职称',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_doctor_user_id` (`user_id`),
  KEY `idx_doctor_department_id` (`department_id`),
  CONSTRAINT `fk_doctor_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_doctor_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='医生信息表';

-- Seed doctor records with department references (preserving original info)
INSERT INTO `doctor` (`id`, `user_id`, `department_id`, `name`, `phone`, `title`, `created_at`, `updated_at`) VALUES
  (1, 3, 1, '张医生', '13800000001', '主任医师', '2025-11-10 23:12:35', '2025-11-10 23:12:35'),
  (2, 4, 1, '李医生', '13800000002', '副主任医师', '2025-11-10 23:12:35', '2025-11-10 23:12:35'),
  (3, 5, 2, '王医生', '13800000003', '主任医师', '2025-11-10 23:12:35', '2025-11-10 23:12:35'),
  (4, 6, 2, '赵医生', '13800000004', '主治医师', '2025-11-10 23:12:35', '2025-11-10 23:12:35'),
  (5, 7, 3, '钱医生', '13800000005', '主任医师', '2025-11-10 23:12:35', '2025-11-10 23:12:35');

SET FOREIGN_KEY_CHECKS = 1;
