package com.hospital.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateDoctorRequest {
    private String name;

    private String phone;

    private Long departmentId;

    private String title;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String password;
}
