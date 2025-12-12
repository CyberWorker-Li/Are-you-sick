package com.hospital.repository;

import com.hospital.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    // 根据医生ID查找排班
    List<Schedule> findByDoctorId(Long doctorId);
    
    // 根据医生ID和星期几查找排班
    List<Schedule> findByDoctorIdAndDayOfWeek(Long doctorId, DayOfWeek dayOfWeek);
    
    // 删除医生的所有排班
    void deleteByDoctorId(Long doctorId);
}

