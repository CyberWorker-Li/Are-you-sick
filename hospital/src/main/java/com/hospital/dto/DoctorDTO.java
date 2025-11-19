package com.hospital.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DoctorDTO {
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String department;
    private String title;
    private LocalDateTime createdAt;
}
