package com.hospital.service;

import com.hospital.dto.*;
import com.hospital.entity.Doctor;
import com.hospital.entity.Schedule;
import com.hospital.entity.ScheduleAdjustmentRequest;
import com.hospital.entity.User;
import com.hospital.entity.UserRole;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleAdjustmentRequestRepository adjustmentRequestRepository;
    private final ScheduleService scheduleService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public DoctorDTO createDoctor(CreateDoctorRequest request) {
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("该邮箱已被注册");
        }
        
        // 创建用户账号
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.DOCTOR);
        user.setStatus(1);
        User savedUser = userRepository.save(user);
        
        // 创建医生信息
        Doctor doctor = new Doctor();
        doctor.setUserId(savedUser.getId());
        doctor.setName(request.getName());
        doctor.setPhone(request.getPhone());
        doctor.setDepartment(request.getDepartment());
        doctor.setTitle(request.getTitle());
        
        Doctor savedDoctor = doctorRepository.save(doctor);
        
        // 转换为DTO
        DoctorDTO dto = new DoctorDTO();
        dto.setId(savedDoctor.getId());
        dto.setUserId(savedDoctor.getUserId());
        dto.setName(savedDoctor.getName());
        dto.setPhone(savedDoctor.getPhone());
        dto.setDepartment(savedDoctor.getDepartment());
        dto.setTitle(savedDoctor.getTitle());
        dto.setCreatedAt(savedDoctor.getCreatedAt());
        
        return dto;
    }

    @Override
    @Transactional
    public DoctorDTO updateDoctor(Long doctorId, CreateDoctorRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("医生不存在"));
        
        // 更新医生信息
        if (request.getName() != null) {
            doctor.setName(request.getName());
        }
        if (request.getPhone() != null) {
            doctor.setPhone(request.getPhone());
        }
        if (request.getDepartment() != null) {
            doctor.setDepartment(request.getDepartment());
        }
        if (request.getTitle() != null) {
            doctor.setTitle(request.getTitle());
        }
        
        // 如果提供了新邮箱，更新用户邮箱
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            User user = userRepository.findById(doctor.getUserId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            
            if (!user.getEmail().equals(request.getEmail())) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new RuntimeException("该邮箱已被注册");
                }
                user.setEmail(request.getEmail());
                userRepository.save(user);
            }
        }
        
        // 如果提供了新密码，更新密码
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            User user = userRepository.findById(doctor.getUserId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            userRepository.save(user);
        }
        
        Doctor updatedDoctor = doctorRepository.save(doctor);
        
        // 转换为DTO
        DoctorDTO dto = new DoctorDTO();
        dto.setId(updatedDoctor.getId());
        dto.setUserId(updatedDoctor.getUserId());
        dto.setName(updatedDoctor.getName());
        dto.setPhone(updatedDoctor.getPhone());
        dto.setDepartment(updatedDoctor.getDepartment());
        dto.setTitle(updatedDoctor.getTitle());
        dto.setCreatedAt(updatedDoctor.getCreatedAt());
        
        return dto;
    }

    @Override
    @Transactional
    public void deleteDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("医生不存在"));
        
        // 删除排班
        scheduleRepository.deleteByDoctorId(doctorId);
        
        // 删除医生
        doctorRepository.delete(doctor);
        
        // 删除用户账号
        userRepository.deleteById(doctor.getUserId());
    }

    @Override
    public List<DoctorDTO> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ScheduleDTO createSchedule(CreateScheduleRequest request) {
        Schedule schedule = scheduleService.createSchedule(request);
        return convertScheduleToDTO(schedule);
    }

    @Override
    @Transactional
    public ScheduleDTO updateSchedule(Long scheduleId, CreateScheduleRequest request) {
        Schedule schedule = scheduleService.updateSchedule(scheduleId, request);
        return convertScheduleToDTO(schedule);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
    }

    @Override
    public List<ScheduleDTO> getDoctorSchedules(Long doctorId) {
        return scheduleService.getSchedulesByDoctorId(doctorId);
    }

    @Override
    public List<ScheduleAdjustmentRequestDTO> getAllAdjustmentRequests() {
        return adjustmentRequestRepository.findAll().stream()
                .map(this::convertAdjustmentRequestToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ScheduleAdjustmentRequestDTO approveAdjustmentRequest(Long requestId, String response) {
        ScheduleAdjustmentRequest request = adjustmentRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("申请不存在"));
        
        if (request.getStatus() != ScheduleAdjustmentRequest.RequestStatus.PENDING) {
            throw new RuntimeException("该申请已处理");
        }
        
        // 创建新的排班
        CreateScheduleRequest scheduleRequest = new CreateScheduleRequest();
        scheduleRequest.setDoctorId(request.getDoctorId());
        scheduleRequest.setDayOfWeek(request.getDayOfWeek());
        scheduleRequest.setStartTime(request.getStartTime());
        scheduleRequest.setEndTime(request.getEndTime());
        
        scheduleService.createSchedule(scheduleRequest);
        
        // 更新申请状态
        request.setStatus(ScheduleAdjustmentRequest.RequestStatus.APPROVED);
        request.setAdminResponse(response);
        ScheduleAdjustmentRequest updated = adjustmentRequestRepository.save(request);
        
        return convertAdjustmentRequestToDTO(updated);
    }

    @Override
    @Transactional
    public ScheduleAdjustmentRequestDTO rejectAdjustmentRequest(Long requestId, String response) {
        ScheduleAdjustmentRequest request = adjustmentRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("申请不存在"));
        
        if (request.getStatus() != ScheduleAdjustmentRequest.RequestStatus.PENDING) {
            throw new RuntimeException("该申请已处理");
        }
        
        request.setStatus(ScheduleAdjustmentRequest.RequestStatus.REJECTED);
        request.setAdminResponse(response);
        ScheduleAdjustmentRequest updated = adjustmentRequestRepository.save(request);
        
        return convertAdjustmentRequestToDTO(updated);
    }
    
    private DoctorDTO convertToDTO(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        dto.setUserId(doctor.getUserId());
        dto.setName(doctor.getName());
        dto.setPhone(doctor.getPhone());
        dto.setDepartment(doctor.getDepartment());
        dto.setTitle(doctor.getTitle());
        dto.setCreatedAt(doctor.getCreatedAt());
        return dto;
    }
    
    private ScheduleDTO convertScheduleToDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        dto.setDoctorId(schedule.getDoctorId());
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setMaxPatients(schedule.getMaxPatients());
        dto.setCreatedAt(schedule.getCreatedAt());
        dto.setCurrentPatients(0);
        return dto;
    }
    
    private ScheduleAdjustmentRequestDTO convertAdjustmentRequestToDTO(ScheduleAdjustmentRequest request) {
        ScheduleAdjustmentRequestDTO dto = new ScheduleAdjustmentRequestDTO();
        dto.setId(request.getId());
        dto.setDoctorId(request.getDoctorId());
        dto.setDayOfWeek(request.getDayOfWeek());
        dto.setStartTime(request.getStartTime());
        dto.setEndTime(request.getEndTime());
        dto.setReason(request.getReason());
        dto.setStatus(request.getStatus());
        dto.setAdminResponse(request.getAdminResponse());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        
        // 获取医生姓名
        try {
            Doctor doctor = doctorRepository.findById(request.getDoctorId())
                    .orElse(null);
            if (doctor != null) {
                dto.setDoctorName(doctor.getName());
            }
        } catch (Exception e) {
            // 忽略错误
        }
        
        return dto;
    }
}

