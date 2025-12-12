package com.hospital.dto;

import lombok.Data;

@Data
public class AdminDoctorWorkloadDTO {
    private Long doctorId;
    private String doctorName;
    private Long departmentId;
    private String departmentName;
    private long totalAppointments;
    private long completedAppointments;
    private long pendingAppointments;
}
