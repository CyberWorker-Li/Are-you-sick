import api from './axios';

export interface CreateDoctorRequest {
  name: string;
  phone?: string;
  department?: string;
  title?: string;
  email: string;
  password: string;
}

export interface CreateScheduleRequest {
  doctorId: number;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  maxPatients?: number;
}

export const adminApi = {
  // 医生管理
  createDoctor: async (request: CreateDoctorRequest) => {
    return await api.post('/admin/doctors', request);
  },

  updateDoctor: async (doctorId: number, request: CreateDoctorRequest) => {
    return await api.put(`/admin/doctors/${doctorId}`, request);
  },

  deleteDoctor: async (doctorId: number) => {
    return await api.delete(`/admin/doctors/${doctorId}`);
  },

  getAllDoctors: async () => {
    return await api.get('/admin/doctors');
  },

  // 排班管理
  createSchedule: async (request: CreateScheduleRequest) => {
    return await api.post('/admin/schedules', request);
  },

  updateSchedule: async (scheduleId: number, request: CreateScheduleRequest) => {
    return await api.put(`/admin/schedules/${scheduleId}`, request);
  },

  deleteSchedule: async (scheduleId: number) => {
    return await api.delete(`/admin/schedules/${scheduleId}`);
  },

  getDoctorSchedules: async (doctorId: number) => {
    return await api.get(`/admin/doctors/${doctorId}/schedules`);
  },

  // 排班调整申请管理
  getAllAdjustmentRequests: async () => {
    return await api.get('/admin/adjustment-requests');
  },

  approveAdjustmentRequest: async (requestId: number, response?: string) => {
    return await api.post(`/admin/adjustment-requests/${requestId}/approve`, null, {
      params: { response }
    });
  },

  rejectAdjustmentRequest: async (requestId: number, response?: string) => {
    return await api.post(`/admin/adjustment-requests/${requestId}/reject`, null, {
      params: { response }
    });
  }
};

export default adminApi;

