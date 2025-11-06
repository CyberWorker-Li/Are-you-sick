package com.hospital.controller;

import com.hospital.dto.ApiResponse;
import com.hospital.dto.RegistrationRequest;
import com.hospital.dto.RegistrationResponse;
import com.hospital.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/registrations")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<RegistrationResponse>> createRegistration(
            @Valid @RequestBody RegistrationRequest request) {
        RegistrationResponse response = registrationService.createRegistration(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> getRegistrations() {
        List<RegistrationResponse> registrations = registrationService.getRegistrations();
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }
    
    @GetMapping("/export")
    public ResponseEntity<Resource> exportToExcel() {
        Resource resource = registrationService.exportToExcel();
        String filename = "挂号记录_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
        
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .body(resource);
    }
    
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> getRegistrationsByDepartment(
            @PathVariable Long departmentId) {
        List<RegistrationResponse> registrations = registrationService.getRegistrationsByDepartment(departmentId);
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }
    
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<List<RegistrationResponse>>> getRegistrationsByDoctor(
            @PathVariable Long doctorId) {
        List<RegistrationResponse> registrations = registrationService.getRegistrationsByDoctor(doctorId);
        return ResponseEntity.ok(ApiResponse.success(registrations));
    }
}