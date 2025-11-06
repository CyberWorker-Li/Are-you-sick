// src/api/department.ts
import http from './axios';
import type { Department } from '../types/dto';

export const departmentApi = {
    // 获取所有科室
    getList: async (): Promise<Department[]> => {
        return http.get<Department[]>('/departments');
    },

    // 根据ID获取科室
    getById: async (id: number): Promise<Department> => {
        return http.get<Department>(`/departments/${id}`);
    },

    // 创建科室（管理员功能）
    create: async (payload: Omit<Department, 'id' | 'createdAt' | 'updatedAt'>): Promise<Department> => {
        return http.post<Department>('/departments', payload);
    },

    // 更新科室（管理员功能）
    update: async (id: number, payload: Partial<Department>): Promise<Department> => {
        return http.put<Department>(`/departments/${id}`, payload);
    },

    // 删除科室（管理员功能）
    delete: async (id: number): Promise<void> => {
        return http.delete<void>(`/departments/${id}`);
    }
};