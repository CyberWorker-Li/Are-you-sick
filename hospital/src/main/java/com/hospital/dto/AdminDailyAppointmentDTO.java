package com.hospital.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminDailyAppointmentDTO {
    private LocalDate date;
    private long total;
    private long pending;
    private long confirmed;
    private long completed;
    private long cancelled;
}
