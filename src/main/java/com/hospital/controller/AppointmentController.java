package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.AppointmentDTO;
import com.hospital.dto.AppointmentRequest;
import com.hospital.entity.Appointment;
import com.hospital.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    
    private final AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<Appointment>> createAppointment(@RequestBody AppointmentRequest request) {
        try {
            Appointment appointment = appointmentService.createAppointment(request);
            return ResponseEntity.ok(ApiResponse.success("预约创建成功", appointment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Appointment>> getAppointment(@PathVariable Long id) {
        try {
            Appointment appointment = appointmentService.getAppointmentById(id);
            return ResponseEntity.ok(ApiResponse.success("获取预约成功", appointment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getPatientAppointments(@PathVariable Long patientId) {
        try {
            List<AppointmentDTO> appointments = appointmentService.getAppointmentsByPatientId(patientId);
            return ResponseEntity.ok(ApiResponse.success("获取患者预约成功", appointments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<List<AppointmentDTO>>> getDoctorAppointments(@PathVariable Long doctorId) {
        try {
            List<AppointmentDTO> appointments = appointmentService.getAppointmentsByDoctorId(doctorId);
            return ResponseEntity.ok(ApiResponse.success("获取医生预约成功", appointments));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Appointment>> cancelAppointment(
            @PathVariable Long id, 
            @RequestParam String reason) {
        try {
            Appointment appointment = appointmentService.cancelAppointment(id, reason);
            return ResponseEntity.ok(ApiResponse.success("预约取消成功", appointment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<Appointment>> confirmAppointment(@PathVariable Long id) {
        try {
            Appointment appointment = appointmentService.confirmAppointment(id);
            return ResponseEntity.ok(ApiResponse.success("预约确认成功", appointment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<Appointment>> completeAppointment(@PathVariable Long id) {
        try {
            Appointment appointment = appointmentService.completeAppointment(id);
            return ResponseEntity.ok(ApiResponse.success("预约完成成功", appointment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/available-slots")
    public ResponseEntity<ApiResponse<List<LocalDateTime>>> getAvailableTimeSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<LocalDateTime> timeSlots = appointmentService.getAvailableTimeSlots(doctorId, date);
            return ResponseEntity.ok(ApiResponse.success("获取可用时间段成功", timeSlots));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/check-conflict")
    public ResponseEntity<ApiResponse<Boolean>> checkTimeConflict(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime appointmentTime) {
        try {
            boolean hasConflict = appointmentService.checkTimeConflict(doctorId, appointmentTime);
            return ResponseEntity.ok(ApiResponse.success("检查时间冲突成功", hasConflict));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
