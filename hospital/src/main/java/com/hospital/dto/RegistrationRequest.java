package com.hospital.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class RegistrationRequest {
    @NotNull(message = "科室ID不能为空")
    private Long departmentId;
    
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;
    
    @NotBlank(message = "患者姓名不能为空")
    private String patientName;
    
    @NotBlank(message = "手机号不能为空")
    private String phone;
    
    @NotBlank(message = "身份证号不能为空")
    private String idCard;
    
    @NotNull(message = "预约时间不能为空")
    private LocalDateTime registrationTime;
}