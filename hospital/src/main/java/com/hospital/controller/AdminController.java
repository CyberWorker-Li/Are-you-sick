package com.hospital.controller;

import com.hospital.dto.*;
import com.hospital.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final AdminService adminService;

    // ========== 医生信息管理 ==========
    
    @PostMapping("/doctors")
    public ResponseEntity<ApiResponse<DoctorDTO>> createDoctor(@RequestBody CreateDoctorRequest request) {
        try {
            DoctorDTO doctor = adminService.createDoctor(request);
            return ResponseEntity.ok(ApiResponse.success("创建医生成功", doctor));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/doctors/{doctorId}")
    public ResponseEntity<ApiResponse<DoctorDTO>> updateDoctor(
            @PathVariable Long doctorId,
            @RequestBody CreateDoctorRequest request) {
        try {
            DoctorDTO doctor = adminService.updateDoctor(doctorId, request);
            return ResponseEntity.ok(ApiResponse.success("更新医生信息成功", doctor));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/doctors/{doctorId}")
    public ResponseEntity<ApiResponse<Void>> deleteDoctor(@PathVariable Long doctorId) {
        try {
            adminService.deleteDoctor(doctorId);
            return ResponseEntity.ok(ApiResponse.success("删除医生成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/doctors")
    public ResponseEntity<ApiResponse<List<DoctorDTO>>> getAllDoctors() {
        try {
            List<DoctorDTO> doctors = adminService.getAllDoctors();
            return ResponseEntity.ok(ApiResponse.success("获取医生列表成功", doctors));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ========== 排班管理 ==========
    
    @PostMapping("/schedules")
    public ResponseEntity<ApiResponse<ScheduleDTO>> createSchedule(@RequestBody CreateScheduleRequest request) {
        try {
            ScheduleDTO schedule = adminService.createSchedule(request);
            return ResponseEntity.ok(ApiResponse.success("创建排班成功", schedule));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PutMapping("/schedules/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleDTO>> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody CreateScheduleRequest request) {
        try {
            ScheduleDTO schedule = adminService.updateSchedule(scheduleId, request);
            return ResponseEntity.ok(ApiResponse.success("更新排班成功", schedule));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable Long scheduleId) {
        try {
            adminService.deleteSchedule(scheduleId);
            return ResponseEntity.ok(ApiResponse.success("删除排班成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/doctors/{doctorId}/schedules")
    public ResponseEntity<ApiResponse<List<ScheduleDTO>>> getDoctorSchedules(@PathVariable Long doctorId) {
        try {
            List<ScheduleDTO> schedules = adminService.getDoctorSchedules(doctorId);
            return ResponseEntity.ok(ApiResponse.success("获取医生排班成功", schedules));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ========== 排班调整申请管理 ==========
    
    @GetMapping("/adjustment-requests")
    public ResponseEntity<ApiResponse<List<ScheduleAdjustmentRequestDTO>>> getAllAdjustmentRequests() {
        try {
            List<ScheduleAdjustmentRequestDTO> requests = adminService.getAllAdjustmentRequests();
            return ResponseEntity.ok(ApiResponse.success("获取排班调整申请成功", requests));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/adjustment-requests/{requestId}/approve")
    public ResponseEntity<ApiResponse<ScheduleAdjustmentRequestDTO>> approveAdjustmentRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String response) {
        try {
            ScheduleAdjustmentRequestDTO request = adminService.approveAdjustmentRequest(
                    requestId, response != null ? response : "已批准");
            return ResponseEntity.ok(ApiResponse.success("批准申请成功", request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/adjustment-requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<ScheduleAdjustmentRequestDTO>> rejectAdjustmentRequest(
            @PathVariable Long requestId,
            @RequestParam(required = false) String response) {
        try {
            ScheduleAdjustmentRequestDTO request = adminService.rejectAdjustmentRequest(
                    requestId, response != null ? response : "已拒绝");
            return ResponseEntity.ok(ApiResponse.success("拒绝申请成功", request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ========== 统计信息 ========== 

    @GetMapping("/stats/overview")
    public ResponseEntity<ApiResponse<AdminOverviewStatsDTO>> getOverviewStats() {
        try {
            AdminOverviewStatsDTO stats = adminService.getOverviewStats();
            return ResponseEntity.ok(ApiResponse.success("获取统计信息成功", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/stats/departments")
    public ResponseEntity<ApiResponse<List<AdminDepartmentStatDTO>>> getDepartmentStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDate start = parseDate(startDate);
            LocalDate end = parseDate(endDate);
            List<AdminDepartmentStatDTO> stats = adminService.getDepartmentStats(start, end);
            return ResponseEntity.ok(ApiResponse.success("获取科室统计成功", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/stats/daily-appointments")
    public ResponseEntity<ApiResponse<List<AdminDailyAppointmentDTO>>> getDailyAppointmentStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDate start = parseDate(startDate);
            LocalDate end = parseDate(endDate);
            List<AdminDailyAppointmentDTO> stats = adminService.getDailyAppointmentStats(start, end);
            return ResponseEntity.ok(ApiResponse.success("获取每日预约统计成功", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/stats/doctor-workload")
    public ResponseEntity<ApiResponse<List<AdminDoctorWorkloadDTO>>> getDoctorWorkload(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDate start = parseDate(startDate);
            LocalDate end = parseDate(endDate);
            List<AdminDoctorWorkloadDTO> stats = adminService.getDoctorWorkload(start, end);
            return ResponseEntity.ok(ApiResponse.success("获取医生工作量统计成功", stats));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    private LocalDate parseDate(String value) {
        return value != null && !value.isBlank() ? LocalDate.parse(value) : null;
    }
}

