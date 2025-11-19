package com.hospital.dto;

import com.hospital.entity.Appointment;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentDTO {
    private Long id;
    private Long patientId;
    private Long doctorId;
    private LocalDateTime appointmentTime;
    private Appointment.AppointmentStatus status;
    private String notes;
    private LocalDateTime createdAt;
    
    // 医生信息（前端显示用）
    private String doctorName;
    private String doctorDepartment;
    private String doctorTitle;
    
    // 患者信息（医生端显示用）
    private String patientName;
    private String patientPhone;
}
