package com.hospital.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RegistrationResponse {
    private Long id;
    private String departmentName;
    private String doctorName;
    private String patientName;
    private String phone;
    private String idCard;
    private LocalDateTime registrationTime;
    private String status;
    private LocalDateTime createdAt;
}