package com.hospital.service;

import com.hospital.dto.AppointmentDTO;
import com.hospital.dto.AppointmentRequest;
import com.hospital.entity.Appointment;

import java.util.List;

public interface AppointmentService {
    
    // 创建预约
    Appointment createAppointment(AppointmentRequest request);
    
    // 根据ID获取预约
    Appointment getAppointmentById(Long id);
    
    // 获取患者的所有预约
    List<AppointmentDTO> getAppointmentsByPatientId(Long patientId);
    
    // 获取医生的所有预约
    List<AppointmentDTO> getAppointmentsByDoctorId(Long doctorId);
    
    // 取消预约
    Appointment cancelAppointment(Long id, String reason);
    
    // 确认预约
    Appointment confirmAppointment(Long id);
    
    // 完成预约
    Appointment completeAppointment(Long id);
    
    // 检查时间冲突
    boolean checkTimeConflict(Long doctorId, java.time.LocalDateTime appointmentTime);
    
    // 获取可用的预约时间段
    List<java.time.LocalDateTime> getAvailableTimeSlots(Long doctorId, java.time.LocalDate date);
}
