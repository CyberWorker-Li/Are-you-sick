// src/api/registration.ts
import http from './axios';
import type { Registration, RegistrationRequest } from '../types/dto';

export const registrationApi = {
    // 创建挂号记录
    create: async (payload: RegistrationRequest): Promise<Registration> => {
        return http.post<Registration>('/registrations', payload);
    },

    // 获取挂号记录列表
    getList: async (): Promise<Registration[]> => {
        return http.get<Registration[]>('/registrations');
    },

    // 根据科室ID获取挂号记录
    getByDepartment: async (departmentId: number): Promise<Registration[]> => {
        return http.get<Registration[]>(`/registrations/department/${departmentId}`);
    },

    // 根据医生ID获取挂号记录
    getByDoctor: async (doctorId: number): Promise<Registration[]> => {
        return http.get<Registration[]>(`/registrations/doctor/${doctorId}`);
    },

    // 导出Excel
    exportExcel: async (): Promise<Blob> => {
        return http.get<Blob>('/registrations/export', {
            responseType: 'blob'
        });
    },

    // 根据ID获取单个挂号记录
    getById: async (id: number): Promise<Registration> => {
        return http.get<Registration>(`/registrations/${id}`);
    },

    // 更新挂号记录状态
    updateStatus: async (id: number, status: string): Promise<Registration> => {
        return http.put<Registration>(`/registrations/${id}/status`, { status });
    },

    // 删除挂号记录
    delete: async (id: number): Promise<void> => {
        return http.delete<void>(`/registrations/${id}`);
    }
};