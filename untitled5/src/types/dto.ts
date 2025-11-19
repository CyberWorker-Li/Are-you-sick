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
