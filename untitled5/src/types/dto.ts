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
    role?: string;
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

// 科室相关类型
export type Department = {
    id: number;
    name: string;
    description?: string;
    createdAt?: string;
    updatedAt?: string;
};

// 医生相关类型
export type Doctor = {
    id: number;
    name: string;
    title?: string;
    departmentId: number;
    departmentName?: string;
    phone?: string;
    email?: string;
    createdAt?: string;
    updatedAt?: string;
};

// 挂号记录类型
export type Registration = {
    id: number;
    departmentName: string;
    doctorName: string;
    patientName: string;
    phone: string;
    idCard: string;
    registrationTime: string;
    status: string;
    createdAt: string;
};

// 挂号请求类型
export type RegistrationRequest = {
    departmentId: number;
    doctorId: number;
    patientName: string;
    phone: string;
    idCard: string;
    registrationTime: string;
};

// 患者信息类型
export type Patient = {
    id: number;
    name: string;
    phone: string;
    idCard: string;
    gender?: 'MALE' | 'FEMALE' | 'OTHER';
    birthDate?: string;
    address?: string;
    emergencyContact?: string;
    emergencyPhone?: string;
    createdAt?: string;
    updatedAt?: string;
};
