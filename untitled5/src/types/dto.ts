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
    departmentId?: number;
    departmentName?: string;
    title?: string;
};

export type CreateDoctorRequest = {
    name: string;
    email: string;
    password?: string;
    phone?: string;
    departmentId?: number;
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
    doctorDepartmentId?: number;
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
    departmentId?: number;
    departmentName?: string;
    title?: string;
    createdAt: string;
};

export type DepartmentDTO = {
    id: number;
    name: string;
    description?: string;
    createdAt?: string;
    updatedAt?: string;
    doctorCount?: number;
};

export type AdminOverviewStatsDTO = {
    totalPatients: number;
    totalDoctors: number;
    totalDepartments: number;
    todaysAppointments: number;
    weekAppointments: number;
    pendingAppointments: number;
};

export type AdminDepartmentStatDTO = {
    departmentId: number;
    departmentName: string;
    description?: string;
    doctorCount: number;
    appointmentCount: number;
};

export type AdminDailyAppointmentDTO = {
    date: string;
    total: number;
    pending: number;
    confirmed: number;
    completed: number;
    cancelled: number;
};

export type AdminDoctorWorkloadDTO = {
    doctorId: number;
    doctorName: string;
    departmentId?: number;
    departmentName?: string;
    totalAppointments: number;
    completedAppointments: number;
    pendingAppointments: number;
};

export type ScheduleAdjustmentRequestDTO = {
    id: number;
    doctorId: number;
    doctorName?: string;
    dayOfWeek: string;
    startTime: string;
    endTime: string;
    reason?: string;
    status: string;
    adminResponse?: string;
    createdAt?: string;
    updatedAt?: string;
};
