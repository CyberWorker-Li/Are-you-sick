package com.hospital.service;

import com.hospital.dto.*;
import java.time.LocalDate;
import java.util.List;

public interface AdminService {
    
    // 创建医生（包括创建用户账号）
    DoctorDTO createDoctor(CreateDoctorRequest request);
    
    // 更新医生信息
    DoctorDTO updateDoctor(Long doctorId, UpdateDoctorRequest request);
    
    // 删除医生
    void deleteDoctor(Long doctorId);
    
    // 获取所有医生
    List<DoctorDTO> getAllDoctors();
    
    // 创建排班
    ScheduleDTO createSchedule(CreateScheduleRequest request);
    
    // 更新排班
    ScheduleDTO updateSchedule(Long scheduleId, CreateScheduleRequest request);
    
    // 删除排班
    void deleteSchedule(Long scheduleId);
    
    // 获取医生的排班
    List<ScheduleDTO> getDoctorSchedules(Long doctorId);
    
    // 获取所有排班调整申请
    List<ScheduleAdjustmentRequestDTO> getAllAdjustmentRequests();
    
    // 批准排班调整申请
    ScheduleAdjustmentRequestDTO approveAdjustmentRequest(Long requestId, String response);
    
    // 拒绝排班调整申请
    ScheduleAdjustmentRequestDTO rejectAdjustmentRequest(Long requestId, String response);

    // 管理员统计
    AdminOverviewStatsDTO getOverviewStats();

    List<AdminDepartmentStatDTO> getDepartmentStats(LocalDate startDate, LocalDate endDate);

    List<AdminDailyAppointmentDTO> getDailyAppointmentStats(LocalDate startDate, LocalDate endDate);

    List<AdminDoctorWorkloadDTO> getDoctorWorkload(LocalDate startDate, LocalDate endDate);
}
