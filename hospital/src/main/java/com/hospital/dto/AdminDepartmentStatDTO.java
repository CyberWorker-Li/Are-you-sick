package com.hospital.dto;

import lombok.Data;

@Data
public class AdminDepartmentStatDTO {
    private Long departmentId;
    private String departmentName;
    private String description;
    private long doctorCount;
    private long appointmentCount;
}
