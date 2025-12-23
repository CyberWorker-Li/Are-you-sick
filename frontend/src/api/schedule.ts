import api from './axios';

export interface TimeSlotDTO {
  startTime: string;
  endTime: string;
  currentPatients: number;
  maxPatients: number;
  available: boolean;
  isWorkingTime: boolean;
}

export const scheduleApi = {
  getAvailableTimeSlots: async (doctorId: number, date: string) => {
    return await api.get(`/schedules/available-slots?doctorId=${doctorId}&date=${date}`);
  }
};

export default scheduleApi;

