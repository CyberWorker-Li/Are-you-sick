package com.hospital.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "registration")
public class Registration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "department_id")
    private Long departmentId;
    
    @Column(name = "doctor_id")
    private Long doctorId;
    
    @Column(name = "patient_name", nullable = false)
    private String patientName;
    
    @Column(nullable = false)
    private String phone;
    
    @Column(name = "id_card", nullable = false)
    private String idCard;
    
    @Column(name = "registration_time", nullable = false)
    private LocalDateTime registrationTime;
    
    @Column(nullable = false)
    private String status = "待就诊";  // 状态：待就诊、已就诊、已取消
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", insertable = false, updatable = false)
    private Department department;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", insertable = false, updatable = false)
    private Doctor doctor;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}