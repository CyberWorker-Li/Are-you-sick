package com.hospital.repository;

import com.hospital.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    
    // 根据患者ID查找预约
    List<Appointment> findByPatientId(Long patientId);
    
    // 根据医生ID查找预约
    List<Appointment> findByDoctorId(Long doctorId);
    
    // 根据状态查找预约
    List<Appointment> findByStatus(Appointment.AppointmentStatus status);
    
    // 查找特定时间段内的预约
    @Query("SELECT a FROM Appointment a WHERE a.appointmentTime BETWEEN :start AND :end")
    List<Appointment> findByAppointmentTimeBetween(@Param("start") LocalDateTime start, 
                                                  @Param("end") LocalDateTime end);
    
    // 查找患者特定状态的预约
    List<Appointment> findByPatientIdAndStatus(Long patientId, Appointment.AppointmentStatus status);
    
    // 查找医生特定状态的预约
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, Appointment.AppointmentStatus status);
    
    // 检查特定时间段医生是否有预约冲突
    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctorId = :doctorId " +
           "AND a.appointmentTime = :appointmentTime AND a.status NOT IN ('CANCELLED', 'EXPIRED')")
    boolean existsByDoctorIdAndAppointmentTime(@Param("doctorId") Long doctorId, 
                                              @Param("appointmentTime") LocalDateTime appointmentTime);
}
