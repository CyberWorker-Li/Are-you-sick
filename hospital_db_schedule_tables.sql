-- 排班表和排班调整申请表
-- 这些表会在JPA自动创建，但这里提供SQL脚本供参考

-- 排班表
CREATE TABLE IF NOT EXISTS `schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '排班ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `day_of_week` varchar(20) NOT NULL COMMENT '星期几',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `max_patients` int NOT NULL DEFAULT 10 COMMENT '最大就诊人数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_doctor_id` (`doctor_id`),
  KEY `idx_day_of_week` (`day_of_week`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='医生排班表';

-- 排班调整申请表
CREATE TABLE IF NOT EXISTS `schedule_adjustment_request` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `day_of_week` varchar(20) NOT NULL COMMENT '星期几',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `reason` varchar(500) DEFAULT NULL COMMENT '申请原因',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING, APPROVED, REJECTED',
  `admin_response` varchar(500) DEFAULT NULL COMMENT '管理员回复',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_doctor_id` (`doctor_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='排班调整申请表';

