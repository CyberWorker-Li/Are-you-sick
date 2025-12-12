// src/types/dto.ts
export type ApiResponse<T> = {
    code: number;
    message: string;
    data: T | null;
};

export type UserInfo = {
    id: number;
    name?: string;
    phone?: string;
    department?: string;
    title?: string;
};

export type LoginResponse = {
    token: string;
    userId: number;
    email: string;
    role?: string;
    userInfo?: UserInfo;
};

export type LoginRequestPayload = {
    email: string;
    password: string;
};

export type RegisterRequestPayload = {
    email: string;
    password: string;
    verificationCode: string;
};

export type ResetPasswordRequestPayload = {
    email: string;
    verificationCode: string;
    newPassword: string;
};

export type SendCodeRequestPayload = {
    email: string;
    type: 'REGISTER' | 'RESET_PASSWORD' | string;
};

// 挂号相关类型
export type AppointmentStatus = 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED' | 'EXPIRED';

export type AppointmentDTO = {
    id: number;
    patientId: number;
    doctorId: number;
    appointmentTime: string;
    status: AppointmentStatus;
    notes?: string;
    createdAt: string;
    doctorName?: string;
    doctorDepartment?: string;
    doctorTitle?: string;
    patientName?: string;
    patientPhone?: string;
};

export type AppointmentRequest = {
    patientId: number;
    doctorId: number;
    appointmentTime: string;
    notes?: string;
    patientName?: string;
    patientPhone?: string;
};

export type DoctorDTO = {
    id: number;
    userId: number;
    name: string;
    phone?: string;
    department?: string;
    title?: string;
    createdAt: string;
};
// src/types/dto.ts - 只添加患者相关的类型

// 性别枚举
export type Gender = 'MALE' | 'FEMALE' | 'OTHER';

// 患者个人信息
export type PatientProfileDTO = {
  id: number;
  userId: number;
  email: string;
  name: string;
  phone: string;
  gender: Gender;
  birthDate: string; // 格式：YYYY-MM-DD
  createdAt: string;
  updatedAt: string;
};

// 更新患者信息请求
export type UpdatePatientRequest = {
  name?: string;
  phone?: string;
  gender?: Gender;
  birthDate?: string;
};

// 修改密码请求
export type ChangePasswordRequest = {
  oldPassword: string;
  newPassword: string;
};