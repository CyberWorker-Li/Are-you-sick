package com.hospital.service;

import com.hospital.dto.DepartmentDTO;
import com.hospital.dto.DepartmentRequest;

import java.util.List;

public interface DepartmentService {

    List<DepartmentDTO> getAllDepartments();

    DepartmentDTO getDepartmentById(Long id);

    DepartmentDTO createDepartment(DepartmentRequest request);

    DepartmentDTO updateDepartment(Long id, DepartmentRequest request);

    void deleteDepartment(Long id);
}
