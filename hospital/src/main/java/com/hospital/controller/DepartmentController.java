package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.DepartmentDTO;
import com.hospital.dto.DepartmentRequest;
import com.hospital.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 公共接口：获取全部科室
     */
    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<DepartmentDTO>>> getDepartments() {
        List<DepartmentDTO> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success("获取科室列表成功", departments));
    }

    /**
     * 公共接口：根据ID获取科室
     */
    @GetMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<DepartmentDTO>> getDepartment(@PathVariable Long id) {
        DepartmentDTO department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success("获取科室信息成功", department));
    }

    /**
     * 管理员：创建科室
     */
    @PostMapping("/admin/departments")
    public ResponseEntity<ApiResponse<DepartmentDTO>> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        DepartmentDTO department = departmentService.createDepartment(request);
        return ResponseEntity.ok(ApiResponse.success("创建科室成功", department));
    }

    /**
     * 管理员：更新科室
     */
    @PutMapping("/admin/departments/{id}")
    public ResponseEntity<ApiResponse<DepartmentDTO>> updateDepartment(@PathVariable Long id,
                                                                       @Valid @RequestBody DepartmentRequest request) {
        DepartmentDTO department = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.success("更新科室成功", department));
    }

    /**
     * 管理员：删除科室
     */
    @DeleteMapping("/admin/departments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success("删除科室成功", null));
    }
}
