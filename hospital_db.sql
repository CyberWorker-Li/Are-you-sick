-- Consolidated database initialization script for Are-you-sick

SET NAMES utf8mb4;

DROP DATABASE IF EXISTS `hospital_db`;
CREATE DATABASE `hospital_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `hospital_db`;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- Core tables
-- =========================

CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱(登录账号)',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(加密)',
  `role` ENUM('PATIENT','DOCTOR','ADMIN') NOT NULL COMMENT '用户角色',
  `status` TINYINT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_email` (`email`),
  KEY `idx_user_email` (`email`),
  KEY `idx_user_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE `department` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '科室ID',
  `name` VARCHAR(50) NOT NULL COMMENT '科室名称',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '科室简介',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_department_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医院科室表';

CREATE TABLE `admin` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_user_id` (`user_id`),
  CONSTRAINT `fk_admin_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员信息表';

CREATE TABLE `patient` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '患者ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `gender` ENUM('MALE','FEMALE','OTHER') DEFAULT NULL COMMENT '性别',
  `birth_date` DATE DEFAULT NULL COMMENT '出生日期',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_patient_user_id` (`user_id`),
  CONSTRAINT `fk_patient_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='患者信息表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生信息表';

CREATE TABLE `appointment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `patient_id` BIGINT NOT NULL,
  `doctor_id` BIGINT NOT NULL,
  `appointment_time` DATETIME(6) NOT NULL,
  `status` ENUM('CANCELLED','COMPLETED','CONFIRMED','EXPIRED','PENDING') NOT NULL,
  `notes` VARCHAR(500) DEFAULT NULL,
  `created_at` DATETIME(6) DEFAULT NULL,
  `updated_at` DATETIME(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_appointment_doctor_id` (`doctor_id`),
  KEY `idx_appointment_patient_id` (`patient_id`),
  KEY `idx_appointment_time` (`appointment_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `email_verification` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `code` VARCHAR(6) NOT NULL COMMENT '验证码',
  `type` ENUM('REGISTER','RESET_PASSWORD') NOT NULL COMMENT '验证码类型',
  `expire_time` DATETIME NOT NULL COMMENT '过期时间',
  `used` TINYINT DEFAULT 0,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_email_code` (`email`,`code`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱验证码表';

-- =========================
-- Schedule related tables
-- =========================

CREATE TABLE `schedule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '排班ID',
  `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
  `day_of_week` VARCHAR(20) NOT NULL COMMENT '星期几',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `max_patients` INT NOT NULL DEFAULT 10 COMMENT '最大就诊人数',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_schedule_doctor_id` (`doctor_id`),
  KEY `idx_schedule_day_of_week` (`day_of_week`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生排班表';

CREATE TABLE `schedule_adjustment_request` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
  `day_of_week` VARCHAR(20) NOT NULL COMMENT '星期几',
  `start_time` TIME NOT NULL COMMENT '开始时间',
  `end_time` TIME NOT NULL COMMENT '结束时间',
  `reason` VARCHAR(500) DEFAULT NULL COMMENT '申请原因',
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING, APPROVED, REJECTED',
  `admin_response` VARCHAR(500) DEFAULT NULL COMMENT '管理员回复',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_adjustment_doctor_id` (`doctor_id`),
  KEY `idx_adjustment_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排班调整申请表';

-- =========================
-- Seed data (optional)
-- =========================

INSERT INTO `department` (`id`, `name`, `description`) VALUES
  (1, '内科', '负责内科常见病诊疗与慢病管理'),
  (2, '外科', '提供外科手术、创伤与术后康复服务'),
  (3, '儿科', '为儿童提供常见疾病诊疗与疫苗咨询'),
  (4, '放射科', '承担影像检查、诊断支持等工作'),
  (5, '皮肤科', '皮肤疾病诊疗与健康管理'),
  (6, '眼科', '眼部疾病诊疗与视力健康管理'),
  (7, '口腔科', '口腔健康检查、治疗与修复'),
  (8, '妇科', '女性常见疾病诊疗与健康管理'),
  (9, '耳鼻喉科', '耳鼻喉相关疾病诊疗与健康管理');

-- Default users (all passwords use unified BCrypt hash, real password: Abc123456)
INSERT INTO `user` (`id`, `email`, `password`, `role`, `status`) VALUES
  (1, 'admin@hospital.com',       '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'ADMIN',   1),
  (2, 'patient1@hospital.com',    '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (3, 'patient2@hospital.com',    '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (4, 'patient3@hospital.com',    '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (5, 'patient4@hospital.com',    '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (6, 'patient5@hospital.com',    '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (7, 'patient6@hospital.com',    '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (8, 'patient7@hospital.com',    '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (9, 'patient8@hospital.com',    '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (10,'patient9@hospital.com',    '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (11,'patient10@hospital.com',   '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (12,'patient11@hospital.com',   '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (13,'patient12@hospital.com',   '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (14,'patient13@hospital.com',   '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (15,'patient14@hospital.com',   '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (16,'patient15@hospital.com',   '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (17,'patient16@hospital.com',   '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (18,'patient17@hospital.com',   '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (19,'patient18@hospital.com',   '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (20,'patient19@hospital.com',   '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (21,'13530305314@163.com',      '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'PATIENT', 1),
  (22, 'wrhang@hospital.com',      '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (23, 'shli@hospital.com',        '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (24, 'jlwang@hospital.com',      '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (25, 'xyzhao@hospital.com',      '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (26, 'jnqian@hospital.com',      '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (27, 'yyxiao@hospital.com',      '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (28, 'zmchen@hospital.com',      '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (29, 'rxliu@hospital.com',       '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (30, 'ytyang@hospital.com',      '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (31, 'jjhuang@hospital.com',     '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (32, 'zhzhou@hospital.com',      '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (33, 'tywu@hospital.com',        '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (34, 'zxxu@hospital.com',        '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (35, 'hrsun@hospital.com',       '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (36, 'yczhu@hospital.com',       '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (37, 'jlma@hospital.com',        '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (38, 'syhu@hospital.com',        '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (39, 'jxguo@hospital.com',       '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (40, 'yqhe@hospital.com',        '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (41, 'zxgao@hospital.com',       '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (42, 'wqlin@hospital.com',       '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (43, 'jyzheng@hospital.com',     '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (44, 'yhcao@hospital.com',       '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (45, 'sypeng@hospital.com',      '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (46, 'zayuan@hospital.com',      '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (47, 'hyfeng@hospital.com',      '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1),
  (48, 'yzxie@hospital.com',       '$2a$10$tVE/EJiN7blX0dDCpcJy7.1n8Gq43HEbCJwtEflB4dUzsuxgvzZZi', 'DOCTOR',  1);

INSERT INTO `admin` (`id`, `user_id`, `name`, `phone`) VALUES
  (1, 1, '系统管理员', '13530305314');

INSERT INTO `patient` (`id`, `user_id`, `name`, `phone`, `gender`, `birth_date`) VALUES
  (1, 2,  NULL, NULL, NULL, NULL),
  (2, 3,  NULL, NULL, NULL, NULL),
  (3, 4,  NULL, NULL, NULL, NULL),
  (4, 5,  NULL, NULL, NULL, NULL),
  (5, 6,  NULL, NULL, NULL, NULL),
  (6, 7,  NULL, NULL, NULL, NULL),
  (7, 8,  NULL, NULL, NULL, NULL),
  (8, 9,  NULL, NULL, NULL, NULL),
  (9, 10, NULL, NULL, NULL, NULL),
  (10,11, NULL, NULL, NULL, NULL),
  (11,12, NULL, NULL, NULL, NULL),
  (12,13, NULL, NULL, NULL, NULL),
  (13,14, NULL, NULL, NULL, NULL),
  (14,15, NULL, NULL, NULL, NULL),
  (15,16, NULL, NULL, NULL, NULL),
  (16,17, NULL, NULL, NULL, NULL),
  (17,18, NULL, NULL, NULL, NULL),
  (18,19, NULL, NULL, NULL, NULL),
  (19,20, NULL, NULL, NULL, NULL),
  (20,21, NULL, NULL, NULL, NULL);

 INSERT INTO `doctor` (`id`, `user_id`, `department_id`, `name`, `phone`, `title`) VALUES
  (1,  22, 1, '张伟然', '13800000001', '主任医师'),
  (2,  23, 1, '李思涵', '13800000002', '副主任医师'),
  (3,  24, 2, '王俊霖', '13800000003', '主任医师'),
  (4,  25, 2, '赵欣怡', '13800000004', '主治医师'),
  (5,  26, 3, '钱嘉宁', '13800000005', '主任医师'),
  (6,  27, 1, '肖易元', '13800000006', '主任医师'),
  (7,  28, 4, '陈子墨', '13800000007', '主治医师'),
  (8,  29, 4, '刘若曦', '13800000008', '副主任医师'),
  (9,  30, 5, '杨雨桐', '13800000009', '主治医师'),
  (10, 31, 5, '黄俊杰', '13800000010', '主任医师'),
  (11, 32, 6, '周子涵', '13800000011', '主治医师'),
  (12, 33, 6, '吴天宇', '13800000012', '副主任医师'),
  (13, 34, 7, '徐梓萱', '13800000013', '主治医师'),
  (14, 35, 7, '孙浩然', '13800000014', '主任医师'),
  (15, 36, 1, '朱雨辰', '13800000015', '主治医师'),
  (16, 37, 2, '马嘉乐', '13800000016', '主任医师'),
  (17, 38, 3, '胡思远', '13800000017', '主治医师'),
  (18, 39, 4, '郭景行', '13800000018', '主任医师'),
  (19, 40, 5, '何雨晴', '13800000019', '副主任医师'),
  (20, 41, 6, '高子轩', '13800000020', '主治医师'),
  (21, 42, 8, '林婉清', '13800000021', '主任医师'),
  (22, 43, 8, '郑嘉怡', '13800000022', '副主任医师'),
  (23, 44, 9, '曹宇航', '13800000023', '主治医师'),
  (24, 45, 9, '彭思妍', '13800000024', '主任医师'),
  (25, 46, 8, '袁子昂', '13800000025', '主治医师'),
  (26, 47, 9, '冯浩宇', '13800000026', '副主任医师'),
  (27, 48, 7, '谢雨泽', '13800000027', '主任医师');

INSERT INTO `appointment` (`id`, `appointment_time`, `created_at`, `doctor_id`, `notes`, `patient_id`, `status`, `updated_at`) VALUES
  (1, '2025-12-10 09:00:00.000000',NULL, 1,'既往复诊',                 1,'CONFIRMED',NULL),
  (2, '2025-12-12 14:30:00.000000',NULL, 2,'慢病随访',                 2,'PENDING',  NULL),
  (3, '2025-12-15 10:15:00.000000',NULL, 3,'外科术前咨询',             3,'CONFIRMED',NULL),
  (4, '2025-12-18 11:00:00.000000',NULL, 4,'复查',                     4,'CANCELLED',NULL),
  (5, '2025-12-20 15:45:00.000000',NULL, 5,'儿科咨询',                 5,'COMPLETED',NULL),
  (6, '2025-12-29 09:00:00.000000',NULL, 6,'内科初诊',                 6,'CONFIRMED',NULL),
  (7, '2025-12-29 09:30:00.000000',NULL, 7,'影像报告解读',             7,'CONFIRMED',NULL),
  (8, '2025-12-29 10:00:00.000000',NULL, 8,'放射检查咨询',             8,'PENDING',  NULL),
  (9, '2025-12-29 10:30:00.000000',NULL, 9,'皮肤过敏',                 9,'CONFIRMED',NULL),
  (10,'2025-12-29 11:00:00.000000',NULL,10,'皮肤复诊',                 10,'COMPLETED',NULL),
  (11,'2025-12-29 14:00:00.000000',NULL,11,'视力检查',                 11,'CONFIRMED',NULL),
  (12,'2025-12-29 14:30:00.000000',NULL,12,'眼干症状',                 12,'PENDING',  NULL),
  (13,'2025-12-29 15:00:00.000000',NULL,13,'口腔疼痛',                 13,'CONFIRMED',NULL),
  (14,'2025-12-29 15:30:00.000000',NULL,14,'牙龈出血',                 14,'CONFIRMED',NULL),
  (15,'2025-12-29 16:00:00.000000',NULL,15,'内科复诊',                 15,'COMPLETED',NULL),
  (16,'2025-12-30 09:00:00.000000',NULL,16,'外科复查',                 16,'CONFIRMED',NULL),
  (17,'2025-12-30 09:30:00.000000',NULL,17,'儿科发热咨询',             17,'CONFIRMED',NULL),
  (18,'2025-12-30 10:00:00.000000',NULL,18,'影像检查复核',             18,'CONFIRMED',NULL),
  (19,'2025-12-30 10:30:00.000000',NULL,19,'皮肤瘙痒',                 19,'PENDING',  NULL),
  (20,'2025-12-30 11:00:00.000000',NULL,20,'眼科复查',                 20,'CONFIRMED',NULL),
  (21,'2025-12-30 14:00:00.000000',NULL,21,'妇科咨询',                 1,'CONFIRMED',NULL),
  (22,'2025-12-30 14:30:00.000000',NULL,22,'妇科复诊',                 2,'COMPLETED',NULL),
  (23,'2025-12-30 15:00:00.000000',NULL,23,'咽喉不适',                 3,'CONFIRMED',NULL),
  (24,'2025-12-30 15:30:00.000000',NULL,24,'耳鸣咨询',                 4,'CONFIRMED',NULL),
  (25,'2025-12-30 16:00:00.000000',NULL,25,'妇科随访',                 5,'PENDING',  NULL),
  (26,'2025-12-31 09:00:00.000000',NULL,26,'鼻炎咨询',                 6,'CONFIRMED',NULL),
  (27,'2025-12-31 09:30:00.000000',NULL,27,'口腔检查',                 7,'CONFIRMED',NULL),
  (28,'2025-12-31 10:00:00.000000',NULL, 1,'复诊',                     8,'CONFIRMED',NULL),
  (29,'2025-12-31 10:30:00.000000',NULL, 2,'咨询',                     9,'COMPLETED',NULL),
  (30,'2025-12-31 11:00:00.000000',NULL, 3,'检查结果解读',             10,'CONFIRMED',NULL),
  (31,'2025-12-31 14:00:00.000000',NULL, 4,'术后复查',                 11,'CONFIRMED',NULL),
  (32,'2025-12-31 14:30:00.000000',NULL, 5,'儿科复诊',                 12,'CONFIRMED',NULL),
  (33,'2025-12-31 15:00:00.000000',NULL, 6,'内科随访',                 13,'PENDING',  NULL),
  (34,'2025-12-31 15:30:00.000000',NULL, 7,'影像咨询',                 14,'CONFIRMED',NULL),
  (35,'2025-12-31 16:00:00.000000',NULL, 8,'检查安排',                 15,'CONFIRMED',NULL),
  (36,'2026-01-01 09:00:00.000000',NULL, 9,'皮肤复诊',                 16,'COMPLETED',NULL),
  (37,'2026-01-01 09:30:00.000000',NULL,10,'皮肤咨询',                 17,'CONFIRMED',NULL),
  (38,'2026-01-01 10:00:00.000000',NULL,11,'视力复查',                 18,'CONFIRMED',NULL),
  (39,'2026-01-01 10:30:00.000000',NULL,12,'干眼症复诊',               19,'PENDING',  NULL),
  (40,'2026-01-01 11:00:00.000000',NULL,13,'口腔复诊',                 20,'CONFIRMED',NULL),
  (41,'2026-01-01 14:00:00.000000',NULL,14,'牙周咨询',                 1,'CONFIRMED',NULL),
  (42,'2026-01-01 14:30:00.000000',NULL,15,'内科复诊',                 2,'COMPLETED',NULL),
  (43,'2026-01-01 15:00:00.000000',NULL,16,'外科复诊',                 3,'CONFIRMED',NULL),
  (44,'2026-01-01 15:30:00.000000',NULL,17,'儿科复诊',                 4,'CONFIRMED',NULL),
  (45,'2026-01-02 09:00:00.000000',NULL,18,'影像复核',                 5,'CONFIRMED',NULL),
  (46,'2026-01-02 09:30:00.000000',NULL,19,'皮肤检查',                 6,'COMPLETED',NULL),
  (47,'2026-01-02 10:00:00.000000',NULL,20,'眼科咨询',                 7,'CONFIRMED',NULL),
  (48,'2026-01-02 10:30:00.000000',NULL,21,'妇科检查',                 8,'CONFIRMED',NULL),
  (49,'2026-01-02 11:00:00.000000',NULL,22,'妇科随访',                 9,'PENDING',  NULL),
  (50,'2026-01-02 14:00:00.000000',NULL,23,'鼻炎复诊',                 10,'CONFIRMED',NULL),
  (51,'2026-01-02 14:30:00.000000',NULL,24,'咽喉检查',                 11,'COMPLETED',NULL),
  (52,'2026-01-02 15:00:00.000000',NULL,25,'妇科复诊',                 12,'CONFIRMED',NULL),
  (53,'2026-01-03 09:00:00.000000',NULL,26,'耳鼻喉复诊',               13,'CONFIRMED',NULL),
  (54,'2026-01-03 09:30:00.000000',NULL,27,'口腔检查',                 14,'CONFIRMED',NULL),
  (55,'2026-01-03 10:00:00.000000',NULL, 1,'内科咨询',                 15,'CONFIRMED',NULL),
  (56,'2026-01-03 10:30:00.000000',NULL, 2,'慢病随访',                 16,'COMPLETED',NULL),
  (57,'2026-01-03 11:00:00.000000',NULL, 3,'外科咨询',                 17,'CONFIRMED',NULL),
  (58,'2026-01-03 14:00:00.000000',NULL, 4,'外科复诊',                 18,'CONFIRMED',NULL),
  (59,'2026-01-04 09:00:00.000000',NULL, 5,'儿科咨询',                 19,'CONFIRMED',NULL),
  (60,'2026-01-04 09:30:00.000000',NULL, 6,'内科复诊',                 20,'COMPLETED',NULL),
  (61,'2026-01-04 10:00:00.000000',NULL, 7,'影像检查咨询',             1,'PENDING',  NULL),
  (62,'2026-01-04 10:30:00.000000',NULL, 8,'放射复查',                 2,'CONFIRMED',NULL),
  (63,'2026-01-04 11:00:00.000000',NULL, 9,'皮肤复查',                 3,'CONFIRMED',NULL),
  (64,'2026-01-04 14:00:00.000000',NULL,10,'皮肤复诊',                 4,'COMPLETED',NULL),
  (65,'2026-01-04 14:30:00.000000',NULL,11,'眼科复查',                 5,'CONFIRMED',NULL),
  (66,'2026-01-04 15:00:00.000000',NULL,12,'眼科咨询',                 6,'CONFIRMED',NULL);

INSERT INTO `schedule` (`id`, `doctor_id`, `day_of_week`, `start_time`, `end_time`, `max_patients`) VALUES
  (1,  1,  'MONDAY',    '09:00:00', '12:00:00', 10),
  (2,  2,  'TUESDAY',   '09:00:00', '12:00:00', 10),
  (3,  3,  'WEDNESDAY', '14:00:00', '17:00:00', 10),
  (4,  4,  'THURSDAY',  '09:00:00', '12:00:00', 10),
  (5,  5,  'FRIDAY',    '14:00:00', '17:00:00', 10),
  (6,  6,  'MONDAY',    '14:00:00', '17:00:00', 10),
  (7,  7,  'TUESDAY',   '14:00:00', '17:00:00', 10),
  (8,  8,  'WEDNESDAY', '09:00:00', '12:00:00', 10),
  (9,  9,  'THURSDAY',  '14:00:00', '17:00:00', 10),
  (10, 10, 'FRIDAY',    '09:00:00', '12:00:00', 10),
  (11, 11, 'MONDAY',    '09:00:00', '12:00:00', 10),
  (12, 12, 'TUESDAY',   '09:00:00', '12:00:00', 10),
  (13, 13, 'WEDNESDAY', '14:00:00', '17:00:00', 10),
  (14, 14, 'THURSDAY',  '09:00:00', '12:00:00', 10),
  (15, 15, 'FRIDAY',    '14:00:00', '17:00:00', 10),
  (16, 16, 'MONDAY',    '14:00:00', '17:00:00', 10),
  (17, 17, 'TUESDAY',   '14:00:00', '17:00:00', 10),
  (18, 18, 'WEDNESDAY', '09:00:00', '12:00:00', 10),
  (19, 19, 'THURSDAY',  '14:00:00', '17:00:00', 10),
  (20, 20, 'FRIDAY',    '09:00:00', '12:00:00', 10),
  (21, 21, 'MONDAY',    '09:00:00', '12:00:00', 10),
  (22, 22, 'TUESDAY',   '09:00:00', '12:00:00', 10),
  (23, 23, 'WEDNESDAY', '14:00:00', '17:00:00', 10),
  (24, 24, 'THURSDAY',  '09:00:00', '12:00:00', 10),
  (25, 25, 'FRIDAY',    '14:00:00', '17:00:00', 10),
  (26, 26, 'MONDAY',    '14:00:00', '17:00:00', 10),
  (27, 27, 'TUESDAY',   '14:00:00', '17:00:00', 10);

SET FOREIGN_KEY_CHECKS = 1;
