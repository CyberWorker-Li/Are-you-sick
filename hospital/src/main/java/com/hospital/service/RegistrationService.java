package com.hospital.service;

import com.hospital.dto.RegistrationRequest;
import com.hospital.dto.RegistrationResponse;
import org.springframework.core.io.Resource;
import java.util.List;

public interface RegistrationService {
    // 创建挂号记录
    RegistrationResponse createRegistration(RegistrationRequest request);
    
    // 获取挂号记录列表
    List<RegistrationResponse> getRegistrations();
    
    // 导出Excel
    Resource exportToExcel();
    
    // 根据科室ID获取挂号记录
    List<RegistrationResponse> getRegistrationsByDepartment(Long departmentId);
    
    // 根据医生ID获取挂号记录
    List<RegistrationResponse> getRegistrationsByDoctor(Long doctorId);
}
