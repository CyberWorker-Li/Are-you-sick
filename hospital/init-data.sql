-- 医院挂号系统初始化数据

-- 清理现有数据（可选）
-- DELETE FROM registrations;
-- DELETE FROM doctors;
-- DELETE FROM departments;
-- DELETE FROM patients;
-- DELETE FROM admins;
-- DELETE FROM users;

-- 插入科室数据
INSERT INTO departments (name, description, created_at, updated_at) VALUES
('内科', '内科疾病诊疗', NOW(), NOW()),
('外科', '外科手术治疗', NOW(), NOW()),
('儿科', '儿童疾病专科', NOW(), NOW()),
('妇产科', '妇科和产科疾病', NOW(), NOW()),
('眼科', '眼部疾病治疗', NOW(), NOW()),
('耳鼻喉科', '耳鼻喉疾病专科', NOW(), NOW()),
('皮肤科', '皮肤疾病治疗', NOW(), NOW()),
('骨科', '骨骼疾病和外伤', NOW(), NOW());

-- 插入用户数据（医生）
INSERT INTO users (email, password, role, created_at, updated_at) VALUES
('doctor1@hospital.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKk1o2pS0z6rqe6.5o8Eo0Oj6', 'DOCTOR', NOW(), NOW()), -- password: doctor123
('doctor2@hospital.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKk1o2pS0z6rqe6.5o8Eo0Oj6', 'DOCTOR', NOW(), NOW()),
('doctor3@hospital.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKk1o2pS0z6rqe6.5o8Eo0Oj6', 'DOCTOR', NOW(), NOW()),
('doctor4@hospital.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKk1o2pS0z6rqe6.5o8Eo0Oj6', 'DOCTOR', NOW(), NOW()),
('doctor5@hospital.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKk1o2pS0z6rqe6.5o8Eo0Oj6', 'DOCTOR', NOW(), NOW());

-- 插入用户数据（管理员）
INSERT INTO users (email, password, role, created_at, updated_at) VALUES
('admin@hospital.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKk1o2pS0z6rqe6.5o8Eo0Oj6', 'ADMIN', NOW(), NOW()); -- password: admin123

-- 插入用户数据（患者）
INSERT INTO users (email, password, role, created_at, updated_at) VALUES
('patient1@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKk1o2pS0z6rqe6.5o8Eo0Oj6', 'PATIENT', NOW(), NOW()), -- password: patient123
('patient2@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKk1o2pS0z6rqe6.5o8Eo0Oj6', 'PATIENT', NOW(), NOW());

-- 插入医生数据
INSERT INTO doctors (user_id, name, title, department_id, phone, email, created_at, updated_at) VALUES
(1, '张医生', '主任医师', 1, '13800001001', 'doctor1@hospital.com', NOW(), NOW()),
(2, '李医生', '副主任医师', 1, '13800001002', 'doctor2@hospital.com', NOW(), NOW()),
(3, '王医生', '主治医师', 2, '13800001003', 'doctor3@hospital.com', NOW(), NOW()),
(4, '赵医生', '主任医师', 3, '13800001004', 'doctor4@hospital.com', NOW(), NOW()),
(5, '刘医生', '副主任医师', 4, '13800001005', 'doctor5@hospital.com', NOW(), NOW());

-- 插入管理员数据
INSERT INTO admins (user_id, name, phone, created_at, updated_at) VALUES
(6, '系统管理员', '13800000000', NOW(), NOW());

-- 插入患者数据
INSERT INTO patients (user_id, name, phone, id_card, gender, birth_date, address, emergency_contact, emergency_phone, created_at, updated_at) VALUES
(7, '张三', '13900001001', '110101199001011234', 'MALE', '1990-01-01', '北京市朝阳区', '李四', '13900001002', NOW(), NOW()),
(8, '李梅', '13900001003', '110101199202022345', 'FEMALE', '1992-02-02', '北京市海淀区', '王五', '13900001004', NOW(), NOW());

-- 插入一些示例挂号记录
INSERT INTO registrations (department_id, doctor_id, patient_id, patient_name, phone, id_card, registration_time, status, created_at, updated_at) VALUES
(1, 1, 1, '张三', '13900001001', '110101199001011234', '2024-01-15 09:00:00', 'PENDING', NOW(), NOW()),
(2, 3, 2, '李梅', '13900001003', '110101199202022345', '2024-01-15 10:30:00', 'COMPLETED', NOW(), NOW()),
(3, 4, 1, '张三', '13900001001', '110101199001011234', '2024-01-16 14:00:00', 'PENDING', NOW(), NOW());

-- 查看插入的数据
SELECT 'Departments:' as info;
SELECT * FROM departments;

SELECT 'Users:' as info;
SELECT id, email, role FROM users;

SELECT 'Doctors:' as info;
SELECT d.id, d.name, d.title, dept.name as department_name FROM doctors d 
JOIN departments dept ON d.department_id = dept.id;

SELECT 'Registrations:' as info;
SELECT r.id, r.patient_name, dept.name as department_name, d.name as doctor_name, r.status 
FROM registrations r
JOIN departments dept ON r.department_id = dept.id
JOIN doctors d ON r.doctor_id = d.id;