import api from './axios';

export interface DepartmentRequest {
  name: string;
  description?: string;
}

export const departmentApi = {
  getAll: async () => {
    return await api.get('/departments');
  },
  getById: async (id: number) => {
    return await api.get(`/departments/${id}`);
  },
  create: async (payload: DepartmentRequest) => {
    return await api.post('/admin/departments', payload);
  },
  update: async (id: number, payload: DepartmentRequest) => {
    return await api.put(`/admin/departments/${id}`, payload);
  },
  remove: async (id: number) => {
    return await api.delete(`/admin/departments/${id}`);
  }
};

export default departmentApi;
