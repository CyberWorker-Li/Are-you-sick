package com.hospital.service;

import com.hospital.dto.DoctorDTO;
import com.hospital.entity.Doctor;

import java.util.List;
import java.util.Optional;

public interface DoctorService {
    
    // 获取所有医生
    List<DoctorDTO> getAllDoctors();
    
    // 根据ID获取医生
    DoctorDTO getDoctorById(Long id);
    
    // 根据用户ID获取医生
    Optional<Doctor> getDoctorByUserId(Long userId);
    
    // 根据科室获取医生
    List<DoctorDTO> getDoctorsByDepartment(String department);
    
    // 创建医生
    Doctor createDoctor(Doctor doctor);
    
    // 更新医生信息
    Doctor updateDoctor(Long id, Doctor doctor);
    
    // 删除医生
    void deleteDoctor(Long id);
    
    // 搜索医生
    List<DoctorDTO> searchDoctors(String keyword);
}
