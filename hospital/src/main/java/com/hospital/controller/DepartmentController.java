package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.entity.Department;
import com.hospital.entity.Doctor;
import com.hospital.repository.DepartmentRepository;
import com.hospital.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<Department>>> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(departments));
    }
    
    @GetMapping("/{departmentId}/doctors")
    public ResponseEntity<ApiResponse<List<Doctor>>> getDoctorsByDepartment(
            @PathVariable Long departmentId) {
        List<Doctor> doctors = doctorRepository.findByDepartmentId(departmentId);
        return ResponseEntity.ok(ApiResponse.success(doctors));
    }
}