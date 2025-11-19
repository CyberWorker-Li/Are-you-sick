package com.hospital.dto;

import com.hospital.entity.ScheduleAdjustmentRequest;
import lombok.Data;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class ScheduleAdjustmentRequestDTO {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String reason;
    private ScheduleAdjustmentRequest.RequestStatus status;
    private String adminResponse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

