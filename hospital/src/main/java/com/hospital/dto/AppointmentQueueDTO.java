package com.hospital.dto;

import com.hospital.entity.Appointment;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentQueueDTO {
    private Long appointmentId;
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private String patientEmail;
    private LocalDateTime appointmentTime;
    private Appointment.AppointmentStatus status;
    private Integer queuePosition; // 排队位置（前面还有几人）
    private String notes;
}

