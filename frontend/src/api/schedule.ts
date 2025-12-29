import api from './axios';

export interface TimeSlotDTO {
  startTime: string;
  endTime: string;
  currentPatients: number;
  maxPatients: number;
  available: boolean;
  isWorkingTime: boolean;
}

export interface AvailableDatesParams {
  departmentId: number;
  doctorId?: number;
  startDate: string;
  endDate: string;
}

export const scheduleApi = {
  getAvailableTimeSlots: async (doctorId: number, date: string) => {
    return await api.get(`/schedules/available-slots?doctorId=${doctorId}&date=${date}`);
  },

  getAvailableDates: async (params: AvailableDatesParams) => {
    const query = new URLSearchParams({
      departmentId: params.departmentId.toString(),
      startDate: params.startDate,
      endDate: params.endDate
    });
    if (typeof params.doctorId === 'number') {
      query.append('doctorId', params.doctorId.toString());
    }
    return await api.get(`/schedules/available-dates?${query.toString()}`);
  }
};

export default scheduleApi;

