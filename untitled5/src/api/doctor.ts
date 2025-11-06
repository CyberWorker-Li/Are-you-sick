// src/api/doctor.ts
import http from './axios';
import type { Doctor } from '../types/dto';

export const doctorApi = {
    // 获取所有医生
    getList: async (): Promise<Doctor[]> => {
        return http.get<Doctor[]>('/doctors');
    },

    // 根据科室ID获取医生列表
    getByDepartment: async (departmentId: number): Promise<Doctor[]> => {
        return http.get<Doctor[]>(`/doctors/department/${departmentId}`);
    },

    // 根据ID获取医生
    getById: async (id: number): Promise<Doctor> => {
        return http.get<Doctor>(`/doctors/${id}`);
    },

    // 创建医生（管理员功能）
    create: async (payload: Omit<Doctor, 'id' | 'createdAt' | 'updatedAt'>): Promise<Doctor> => {
        return http.post<Doctor>('/doctors', payload);
    },

    // 更新医生信息（管理员功能）
    update: async (id: number, payload: Partial<Doctor>): Promise<Doctor> => {
        return http.put<Doctor>(`/doctors/${id}`, payload);
    },

    // 删除医生（管理员功能）
    delete: async (id: number): Promise<void> => {
        return http.delete<void>(`/doctors/${id}`);
    }
};