package com.hospital.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TimeSlotDTO {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTime;
    
    private Integer currentPatients;
    private Integer maxPatients;
    private Boolean available; // 是否可用（未满员且在工作时间段内）
    private Boolean isWorkingTime; // 是否是医生的工作时间段
}

