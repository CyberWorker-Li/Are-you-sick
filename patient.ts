// src/api/patient.ts
import http from './axios';

const patientApi = {
  // 获取患者信息
  getPatientInfo() {
    return http.get('/auth/me');
  },

  // 更新患者信息
  updatePatientProfile(data: any) {
    return http.put('/auth/update-patient-profile', data);  // 注意路径
  }
};

export default patientApi;