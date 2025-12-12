package com.hospital.service;

import com.hospital.dto.ScheduleDTO;
import com.hospital.dto.CreateScheduleRequest;
import com.hospital.dto.TimeSlotDTO;
import com.hospital.entity.Schedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleService {
    
    // 创建排班
    Schedule createSchedule(CreateScheduleRequest request);
    
    // 获取医生的所有排班
    List<ScheduleDTO> getSchedulesByDoctorId(Long doctorId);
    
    // 获取医生本周的排班
    List<ScheduleDTO> getDoctorSchedulesForWeek(Long doctorId);
    
    // 获取医生指定日期的可用时间段
    List<TimeSlotDTO> getAvailableTimeSlots(Long doctorId, LocalDate date);
    
    // 检查时间段是否在医生的工作时间内
    boolean isWithinWorkingHours(Long doctorId, LocalDateTime appointmentTime);
    
    // 检查时间段是否已满员
    boolean isTimeSlotFull(Long doctorId, LocalDateTime appointmentTime);
    
    // 获取时间段当前预约人数
    int getCurrentAppointmentCount(Long doctorId, LocalDateTime appointmentTime);
    
    // 删除排班
    void deleteSchedule(Long scheduleId);
    
    // 更新排班
    Schedule updateSchedule(Long scheduleId, CreateScheduleRequest request);
    
    // 验证医生一周内是否有两个工作时段
    boolean validateDoctorWeeklySchedule(Long doctorId);
}

