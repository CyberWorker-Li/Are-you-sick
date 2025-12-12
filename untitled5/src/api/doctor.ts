import api from './axios';
import type { DoctorDTO } from '../types/dto';

export const doctorApi = {
  // 获取所有医生
  getAllDoctors: async () => {
    try {
      const response = await api.get('/doctors');
      return response;
    } catch (error) {
      console.error('获取所有医生失败:', error);
      throw error;
    }
  },

  // 根据ID获取医生
  getDoctorById: async (id: number) => {
    try {
      const response = await api.get(`/doctors/${id}`);
      return response;
    } catch (error) {
      console.error('根据ID获取医生失败:', error);
      throw error;
    }
  },

  // 根据科室获取医生
  getDoctorsByDepartment: async (departmentId: number) => {
    try {
      const response = await api.get(`/doctors/department/${departmentId}`);
      return response;
    } catch (error) {
      console.error('根据科室获取医生失败:', error);
      throw error;
    }
  },

  // 搜索医生
  searchDoctors: async (keyword: string) => {
    try {
      const response = await api.get(`/doctors/search?keyword=${encodeURIComponent(keyword)}`);
      return response;
    } catch (error) {
      console.error('搜索医生失败:', error);
      throw error;
    }
  }
};

export default doctorApi;