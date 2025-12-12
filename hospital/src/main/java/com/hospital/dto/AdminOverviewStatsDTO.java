package com.hospital.dto;

import lombok.Data;

@Data
public class AdminOverviewStatsDTO {
    private long totalPatients;
    private long totalDoctors;
    private long totalDepartments;
    private long todaysAppointments;
    private long weekAppointments;
    private long pendingAppointments;
}
