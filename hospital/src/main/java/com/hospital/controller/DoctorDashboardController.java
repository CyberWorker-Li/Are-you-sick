package com.hospital.controller;

import com.hospital.dto.*;
import com.hospital.service.DoctorDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorDashboardController {
    
    private final DoctorDashboardService doctorDashboardService;

    // 获取本周排班
    @GetMapping("/schedules")
    public ResponseEntity<ApiResponse<List<ScheduleDTO>>> getMySchedules(
            @RequestParam Long doctorId) {
        try {
            List<ScheduleDTO> schedules = doctorDashboardService.getMySchedulesForWeek(doctorId);
            return ResponseEntity.ok(ApiResponse.success("获取排班成功", schedules));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 获取就诊队列
    @GetMapping("/appointments/queue")
    public ResponseEntity<ApiResponse<List<AppointmentQueueDTO>>> getAppointmentQueue(
            @RequestParam Long doctorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime appointmentTime) {
        try {
            List<AppointmentQueueDTO> queue = doctorDashboardService.getAppointmentQueue(doctorId, appointmentTime);
            return ResponseEntity.ok(ApiResponse.success("获取就诊队列成功", queue));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 发送就诊提醒
    @PostMapping("/appointments/{appointmentId}/remind")
    public ResponseEntity<ApiResponse<Void>> sendAppointmentReminder(@PathVariable Long appointmentId) {
        try {
            doctorDashboardService.sendAppointmentReminder(appointmentId);
            return ResponseEntity.ok(ApiResponse.success("发送提醒成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 申请排班调整
    @PostMapping("/schedule-adjustment")
    public ResponseEntity<ApiResponse<ScheduleAdjustmentRequestDTO>> requestScheduleAdjustment(
            @RequestParam Long doctorId,
            @RequestParam DayOfWeek dayOfWeek,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) String reason) {
        try {
            ScheduleAdjustmentRequestDTO request = doctorDashboardService.requestScheduleAdjustment(
                    doctorId, dayOfWeek, startTime, endTime, reason);
            return ResponseEntity.ok(ApiResponse.success("申请提交成功", request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // 获取我的排班调整申请
    @GetMapping("/schedule-adjustment")
    public ResponseEntity<ApiResponse<List<ScheduleAdjustmentRequestDTO>>> getMyAdjustmentRequests(
            @RequestParam Long doctorId) {
        try {
            List<ScheduleAdjustmentRequestDTO> requests = doctorDashboardService.getMyAdjustmentRequests(doctorId);
            return ResponseEntity.ok(ApiResponse.success("获取申请列表成功", requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

