package com.hospital.service;

import com.hospital.dto.AppointmentQueueDTO;
import com.hospital.dto.ScheduleAdjustmentRequestDTO;
import com.hospital.dto.ScheduleDTO;
import com.hospital.entity.ScheduleAdjustmentRequest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface DoctorDashboardService {
    
    // 获取医生本周的排班
    List<ScheduleDTO> getMySchedulesForWeek(Long doctorId);
    
    // 获取医生指定时间段的就诊患者队列
    List<AppointmentQueueDTO> getAppointmentQueue(Long doctorId, java.time.LocalDateTime appointmentTime);
    
    // 发送就诊提醒邮件
    void sendAppointmentReminder(Long appointmentId);
    
    // 申请排班调整
    ScheduleAdjustmentRequestDTO requestScheduleAdjustment(
        Long doctorId, 
        DayOfWeek dayOfWeek, 
        LocalTime startTime, 
        LocalTime endTime, 
        String reason);
    
    // 获取医生的排班调整申请
    List<ScheduleAdjustmentRequestDTO> getMyAdjustmentRequests(Long doctorId);
}

