import api from './axios';
import type { AppointmentDTO, AppointmentRequest } from '../types/dto';

export const appointmentApi = {
  // 创建预约
  createAppointment: async (request: AppointmentRequest) => {
    try {
      const response = await api.post('/appointments', request);
      return response;
    } catch (error) {
      console.error('创建预约失败:', error);
      throw error;
    }
  },

  // 获取患者预约
  getPatientAppointments: async (patientId: number) => {
    try {
      const response = await api.get(`/appointments/patient/${patientId}`);
      return response;
    } catch (error) {
      console.error('获取患者预约失败:', error);
      throw error;
    }
  },

  // 获取医生预约
  getDoctorAppointments: async (doctorId: number) => {
    const response = await api.get(`/appointments/doctor/${doctorId}`);
    return response.data;
  },

  // 取消预约
  cancelAppointment: async (id: number, reason: string) => {
    const response = await api.post(`/appointments/${id}/cancel?reason=${encodeURIComponent(reason)}`);
    return response;
  },

  // 确认预约
  confirmAppointment: async (id: number) => {
    const response = await api.post(`/appointments/${id}/confirm`);
    return response.data;
  },

  // 完成预约
  completeAppointment: async (id: number) => {
    const response = await api.post(`/appointments/${id}/complete`);
    return response.data;
  },

  // 获取可用时间段
  getAvailableTimeSlots: async (doctorId: number, date: string) => {
    const response = await api.get(`/appointments/available-slots?doctorId=${doctorId}&date=${date}`);
    return response.data;
  },

  // 检查时间冲突
  checkTimeConflict: async (doctorId: number, appointmentTime: string) => {
    const response = await api.get(`/appointments/check-conflict?doctorId=${doctorId}&appointmentTime=${appointmentTime}`);
    return response.data;
  }
};

export default appointmentApi;