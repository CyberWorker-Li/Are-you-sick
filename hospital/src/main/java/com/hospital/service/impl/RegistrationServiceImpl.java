package com.hospital.service.impl;

import com.hospital.dto.RegistrationRequest;
import com.hospital.dto.RegistrationResponse;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.entity.Registration;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.RegistrationRepository;
import com.hospital.service.RegistrationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    
    @Override
    @Transactional
    public RegistrationResponse createRegistration(RegistrationRequest request) {
        // 验证科室是否存在
        Department department = departmentRepository.findById(request.getDepartmentId())
            .orElseThrow(() -> new EntityNotFoundException("科室不存在"));
            
        // 验证医生是否存在
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
            .orElseThrow(() -> new EntityNotFoundException("医生不存在"));
            
        // 创建挂号记录
        Registration registration = new Registration();
        registration.setDepartmentId(request.getDepartmentId());
        registration.setDoctorId(request.getDoctorId());
        registration.setPatientName(request.getPatientName());
        registration.setPhone(request.getPhone());
        registration.setIdCard(request.getIdCard());
        registration.setRegistrationTime(request.getRegistrationTime());
        registration.setStatus("待就诊");
        
        Registration savedRegistration = registrationRepository.save(registration);
        return convertToResponse(savedRegistration);
    }
    
    @Override
    public List<RegistrationResponse> getRegistrations() {
        return registrationRepository.findAll().stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public Resource exportToExcel() {
        List<Registration> registrations = registrationRepository.findAll();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("挂号记录");
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"科室", "医生", "患者姓名", "手机号", "身份证号", "预约时间", "状态", "创建时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // 填充数据
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            int rowNum = 1;
            for (Registration registration : registrations) {
                Row row = sheet.createRow(rowNum++);
                Department dept = departmentRepository.findById(registration.getDepartmentId()).orElse(null);
                Doctor doc = doctorRepository.findById(registration.getDoctorId()).orElse(null);
                
                row.createCell(0).setCellValue(dept != null ? dept.getName() : "");
                row.createCell(1).setCellValue(doc != null ? doc.getName() : "");
                row.createCell(2).setCellValue(registration.getPatientName());
                row.createCell(3).setCellValue(registration.getPhone());
                row.createCell(4).setCellValue(registration.getIdCard());
                row.createCell(5).setCellValue(registration.getRegistrationTime().format(formatter));
                row.createCell(6).setCellValue(registration.getStatus());
                row.createCell(7).setCellValue(registration.getCreatedAt().format(formatter));
            }
            
            // 调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 转换为Resource
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return new ByteArrayResource(outputStream.toByteArray());
            
        } catch (IOException e) {
            throw new RuntimeException("导出Excel失败", e);
        }
    }
    
    @Override
    public List<RegistrationResponse> getRegistrationsByDepartment(Long departmentId) {
        return registrationRepository.findByDepartmentId(departmentId).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<RegistrationResponse> getRegistrationsByDoctor(Long doctorId) {
        return registrationRepository.findByDoctorId(doctorId).stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    private RegistrationResponse convertToResponse(Registration registration) {
        Department department = departmentRepository.findById(registration.getDepartmentId()).orElse(null);
        Doctor doctor = doctorRepository.findById(registration.getDoctorId()).orElse(null);
        
        RegistrationResponse response = new RegistrationResponse();
        response.setId(registration.getId());
        response.setDepartmentName(department != null ? department.getName() : null);
        response.setDoctorName(doctor != null ? doctor.getName() : null);
        response.setPatientName(registration.getPatientName());
        response.setPhone(registration.getPhone());
        response.setIdCard(registration.getIdCard());
        response.setRegistrationTime(registration.getRegistrationTime());
        response.setStatus(registration.getStatus());
        response.setCreatedAt(registration.getCreatedAt());
        return response;
    }
}