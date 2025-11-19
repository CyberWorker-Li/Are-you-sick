package com.hospital.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class CreateScheduleRequest {
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;
    
    @NotNull(message = "星期几不能为空")
    private DayOfWeek dayOfWeek;
    
    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;
    
    private Integer maxPatients = 10;
}

