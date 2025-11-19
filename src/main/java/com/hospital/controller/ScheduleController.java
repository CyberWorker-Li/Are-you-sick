package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.ScheduleDTO;
import com.hospital.dto.TimeSlotDTO;
import com.hospital.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/schedules")
@RequiredArgsConstructor
public class ScheduleController {
    
    private final ScheduleService scheduleService;

    @GetMapping("/available-slots")
    public ResponseEntity<ApiResponse<List<TimeSlotDTO>>> getAvailableTimeSlots(
            @RequestParam Long doctorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            List<TimeSlotDTO> timeSlots = scheduleService.getAvailableTimeSlots(doctorId, date);
            return ResponseEntity.ok(ApiResponse.success("获取可用时间段成功", timeSlots));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

