package com.hospital.service;

import com.hospital.dto.*;
import com.hospital.entity.Appointment;
import com.hospital.entity.Department;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DepartmentRepository departmentRepository;

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
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("科室不存在"));

        Doctor doctor = new Doctor();
        doctor.setUserId(savedUser.getId());
        doctor.setName(request.getName());
        doctor.setPhone(request.getPhone());
        doctor.setDepartment(department);
        doctor.setTitle(request.getTitle());

        Doctor savedDoctor = doctorRepository.save(doctor);
        
        // 转换为DTO
        DoctorDTO dto = new DoctorDTO();
        dto.setId(savedDoctor.getId());
        dto.setUserId(savedDoctor.getUserId());
        dto.setName(savedDoctor.getName());
        dto.setPhone(savedDoctor.getPhone());
        if (savedDoctor.getDepartment() != null) {
            dto.setDepartmentId(savedDoctor.getDepartment().getId());
            dto.setDepartmentName(savedDoctor.getDepartment().getName());
        }
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
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("科室不存在"));
            doctor.setDepartment(department);
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
        if (updatedDoctor.getDepartment() != null) {
            dto.setDepartmentId(updatedDoctor.getDepartment().getId());
            dto.setDepartmentName(updatedDoctor.getDepartment().getName());
        } else {
            dto.setDepartmentId(null);
            dto.setDepartmentName(null);
        }
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
    public AdminOverviewStatsDTO getOverviewStats() {
        AdminOverviewStatsDTO dto = new AdminOverviewStatsDTO();

        dto.setTotalPatients(patientRepository.count());
        dto.setTotalDoctors(doctorRepository.count());
        dto.setTotalDepartments(departmentRepository.count());

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = endOfDay(today);

        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDateTime startOfWeek = weekStart.atStartOfDay();
        LocalDateTime endOfWeek = endOfDay(today);

        dto.setTodaysAppointments(appointmentRepository.countByAppointmentTimeBetween(startOfToday, endOfToday));
        dto.setWeekAppointments(appointmentRepository.countByAppointmentTimeBetween(startOfWeek, endOfWeek));
        dto.setPendingAppointments(appointmentRepository.countByStatus(Appointment.AppointmentStatus.PENDING));

        return dto;
    }

    @Override
    public List<AdminDepartmentStatDTO> getDepartmentStats(LocalDate startDate, LocalDate endDate) {
        LocalDate[] range = resolveDateRange(startDate, endDate);
        LocalDateTime startDateTime = range[0].atStartOfDay();
        LocalDateTime endDateTime = endOfDay(range[1]);

        return departmentRepository.findAll().stream()
                .map(department -> buildDepartmentStat(department, startDateTime, endDateTime))
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminDailyAppointmentDTO> getDailyAppointmentStats(LocalDate startDate, LocalDate endDate) {
        LocalDate[] range = resolveDateRange(startDate, endDate);
        LocalDate cursor = range[0];
        List<AdminDailyAppointmentDTO> results = new ArrayList<>();

        while (!cursor.isAfter(range[1])) {
            LocalDateTime dayStart = cursor.atStartOfDay();
            LocalDateTime dayEnd = endOfDay(cursor);

            AdminDailyAppointmentDTO dto = new AdminDailyAppointmentDTO();
            dto.setDate(cursor);
            dto.setTotal(appointmentRepository.countByAppointmentTimeBetween(dayStart, dayEnd));
            dto.setPending(appointmentRepository.countByStatusAndAppointmentTimeBetween(
                    Appointment.AppointmentStatus.PENDING, dayStart, dayEnd));
            dto.setConfirmed(appointmentRepository.countByStatusAndAppointmentTimeBetween(
                    Appointment.AppointmentStatus.CONFIRMED, dayStart, dayEnd));
            dto.setCompleted(appointmentRepository.countByStatusAndAppointmentTimeBetween(
                    Appointment.AppointmentStatus.COMPLETED, dayStart, dayEnd));
            dto.setCancelled(appointmentRepository.countByStatusAndAppointmentTimeBetween(
                    Appointment.AppointmentStatus.CANCELLED, dayStart, dayEnd));

            results.add(dto);
            cursor = cursor.plusDays(1);
        }

        return results;
    }

    @Override
    public List<AdminDoctorWorkloadDTO> getDoctorWorkload(LocalDate startDate, LocalDate endDate) {
        LocalDate[] range = resolveDateRange(startDate, endDate);
        LocalDateTime startDateTime = range[0].atStartOfDay();
        LocalDateTime endDateTime = endOfDay(range[1]);

        return doctorRepository.findAll().stream()
                .map(doctor -> buildDoctorWorkload(doctor, startDateTime, endDateTime))
                .collect(Collectors.toList());
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
        if (doctor.getDepartment() != null) {
            dto.setDepartmentId(doctor.getDepartment().getId());
            dto.setDepartmentName(doctor.getDepartment().getName());
        } else {
            dto.setDepartmentId(null);
            dto.setDepartmentName(null);
        }
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

    private AdminDepartmentStatDTO buildDepartmentStat(Department department,
                                                       LocalDateTime start,
                                                       LocalDateTime end) {
        AdminDepartmentStatDTO dto = new AdminDepartmentStatDTO();
        dto.setDepartmentId(department.getId());
        dto.setDepartmentName(department.getName());
        dto.setDescription(department.getDescription());

        List<Doctor> doctors = doctorRepository.findByDepartment_Id(department.getId());
        dto.setDoctorCount(doctors.size());

        if (doctors.isEmpty()) {
            dto.setAppointmentCount(0);
        } else {
            List<Long> doctorIds = doctors.stream()
                    .map(Doctor::getId)
                    .collect(Collectors.toList());
            long appointmentCount = appointmentRepository.countByDoctorIdInAndAppointmentTimeBetween(doctorIds, start, end);
            dto.setAppointmentCount(appointmentCount);
        }

        return dto;
    }

    private AdminDoctorWorkloadDTO buildDoctorWorkload(Doctor doctor,
                                                       LocalDateTime start,
                                                       LocalDateTime end) {
        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdAndAppointmentTimeBetween(doctor.getId(), start, end);

        long total = appointments.size();
        long completed = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                .count();
        long pending = appointments.stream()
                .filter(a -> a.getStatus() == Appointment.AppointmentStatus.PENDING
                        || a.getStatus() == Appointment.AppointmentStatus.CONFIRMED)
                .count();

        AdminDoctorWorkloadDTO dto = new AdminDoctorWorkloadDTO();
        dto.setDoctorId(doctor.getId());
        dto.setDoctorName(doctor.getName());
        if (doctor.getDepartment() != null) {
            dto.setDepartmentId(doctor.getDepartment().getId());
            dto.setDepartmentName(doctor.getDepartment().getName());
        }
        dto.setTotalAppointments(total);
        dto.setCompletedAppointments(completed);
        dto.setPendingAppointments(pending);
        return dto;
    }

    private LocalDate[] resolveDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(6);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }

        return new LocalDate[]{start, end};
    }

    private LocalDateTime endOfDay(LocalDate date) {
        return date.plusDays(1).atStartOfDay().minusNanos(1);
    }
}

