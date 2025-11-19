import api from './axios';

export const doctorDashboardApi = {
  // 获取我的排班
  getMySchedules: async (doctorId: number) => {
    return await api.get(`/doctor/schedules?doctorId=${doctorId}`);
  },

  // 获取就诊队列
  getAppointmentQueue: async (doctorId: number, appointmentTime: string) => {
    return await api.get(`/doctor/appointments/queue?doctorId=${doctorId}&appointmentTime=${appointmentTime}`);
  },

  // 发送就诊提醒
  sendAppointmentReminder: async (appointmentId: number) => {
    return await api.post(`/doctor/appointments/${appointmentId}/remind`);
  },

  // 申请排班调整
  requestScheduleAdjustment: async (
    doctorId: number,
    dayOfWeek: string,
    startTime: string,
    endTime: string,
    reason?: string
  ) => {
    return await api.post('/doctor/schedule-adjustment', null, {
      params: { doctorId, dayOfWeek, startTime, endTime, reason }
    });
  },

  // 获取我的排班调整申请
  getMyAdjustmentRequests: async (doctorId: number) => {
    return await api.get(`/doctor/schedule-adjustment?doctorId=${doctorId}`);
  }
};

export default doctorDashboardApi;

