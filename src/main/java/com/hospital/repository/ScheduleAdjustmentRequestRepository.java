package com.hospital.repository;

import com.hospital.entity.ScheduleAdjustmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleAdjustmentRequestRepository extends JpaRepository<ScheduleAdjustmentRequest, Long> {
    
    // 根据医生ID查找申请
    List<ScheduleAdjustmentRequest> findByDoctorId(Long doctorId);
    
    // 查找待处理的申请
    List<ScheduleAdjustmentRequest> findByStatus(ScheduleAdjustmentRequest.RequestStatus status);
    
    // 根据医生ID和状态查找申请
    List<ScheduleAdjustmentRequest> findByDoctorIdAndStatus(Long doctorId, ScheduleAdjustmentRequest.RequestStatus status);
}

