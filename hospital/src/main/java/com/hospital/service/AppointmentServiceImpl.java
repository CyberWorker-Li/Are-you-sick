package com.hospital.service;

import com.hospital.dto.AppointmentDTO;
import com.hospital.dto.AppointmentRequest;
import com.hospital.entity.Appointment;
import com.hospital.repository.AppointmentRepository;
import com.hospital.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final ScheduleService scheduleService;

    @Override
    @Transactional
    public Appointment createAppointment(AppointmentRequest request) {
        // 检查是否在医生的工作时间段内
        if (!scheduleService.isWithinWorkingHours(request.getDoctorId(), request.getAppointmentTime())) {
            throw new RuntimeException("该时间段不在医生的工作时间内，请选择医生的工作时间段");
        }
        
        // 检查时间段是否已满员
        if (scheduleService.isTimeSlotFull(request.getDoctorId(), request.getAppointmentTime())) {
            throw new RuntimeException("该时间段已满员，请选择其他时间");
        }
        
        Appointment appointment = new Appointment();
        appointment.setPatientId(request.getPatientId());
        appointment.setDoctorId(request.getDoctorId());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setNotes(request.getNotes());
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);
        
        Appointment saved = appointmentRepository.save(appointment);
        updatePatientContactInfo(request);
        return saved;
    }

    @Override
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
    }

    @Override
    public List<AppointmentDTO> getAppointmentsByPatientId(Long patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        return appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentDTO> getAppointmentsByDoctorId(Long doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        return appointments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Appointment cancelAppointment(Long id, String reason) {
        Appointment appointment = getAppointmentById(id);
        if (appointment.getStatus() != Appointment.AppointmentStatus.PENDING && 
            appointment.getStatus() != Appointment.AppointmentStatus.CONFIRMED) {
            throw new RuntimeException("只有待确认或已确认的预约可以取消");
        }
        
        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointment.setNotes(reason);
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment confirmAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        if (appointment.getStatus() != Appointment.AppointmentStatus.PENDING) {
            throw new RuntimeException("只有待确认的预约可以确认");
        }
        
        appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
        return appointmentRepository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment completeAppointment(Long id) {
        Appointment appointment = getAppointmentById(id);
        if (appointment.getStatus() != Appointment.AppointmentStatus.CONFIRMED) {
            throw new RuntimeException("只有已确认的预约可以完成");
        }
        
        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        return appointmentRepository.save(appointment);
    }

    @Override
    public boolean checkTimeConflict(Long doctorId, LocalDateTime appointmentTime) {
        // 对外暴露的冲突检查与排班容量保持一致
        return scheduleService.isTimeSlotFull(doctorId, appointmentTime);
    }

    @Override
    public List<LocalDateTime> getAvailableTimeSlots(Long doctorId, LocalDate date) {
        // 获取医生在该日期的工作时间段
        List<com.hospital.dto.TimeSlotDTO> timeSlots = scheduleService.getAvailableTimeSlots(doctorId, date);
        
        // 只返回可用且未满员的时间段
        return timeSlots.stream()
                .filter(slot -> slot.getAvailable() && slot.getIsWorkingTime())
                .map(com.hospital.dto.TimeSlotDTO::getStartTime)
                .collect(Collectors.toList());
    }

    private AppointmentDTO convertToDTO(Appointment appointment) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appointment.getId());
        dto.setPatientId(appointment.getPatientId());
        dto.setDoctorId(appointment.getDoctorId());
        dto.setAppointmentTime(appointment.getAppointmentTime());
        dto.setStatus(appointment.getStatus());
        dto.setNotes(appointment.getNotes());
        dto.setCreatedAt(appointment.getCreatedAt());
        
        // 填充医生信息
        if (appointment.getDoctorId() != null) {
            try {
                var doctorDTO = doctorService.getDoctorById(appointment.getDoctorId());
                dto.setDoctorName(doctorDTO.getName());
                dto.setDoctorDepartment(doctorDTO.getDepartmentName());
                dto.setDoctorDepartmentId(doctorDTO.getDepartmentId());
                dto.setDoctorTitle(doctorDTO.getTitle());
            } catch (Exception e) {
                // 如果获取医生信息失败，设置默认值
                dto.setDoctorName("未知医生");
                dto.setDoctorDepartment("未知科室");
                dto.setDoctorTitle("未知职称");
            }
        }
        
        // 填充患者信息
        if (appointment.getPatientId() != null) {
            patientRepository.findById(appointment.getPatientId()).ifPresent(patient -> {
                dto.setPatientName(patient.getName());
                dto.setPatientPhone(patient.getPhone());
            });
        }
        
        return dto;
    }
    
    private void updatePatientContactInfo(AppointmentRequest request) {
        if (request.getPatientId() == null) {
            return;
        }
        patientRepository.findById(request.getPatientId()).ifPresent(patient -> {
            boolean updated = false;
            if (StringUtils.hasText(request.getPatientName()) && !request.getPatientName().equals(patient.getName())) {
                patient.setName(request.getPatientName());
                updated = true;
            }
            if (StringUtils.hasText(request.getPatientPhone()) && !request.getPatientPhone().equals(patient.getPhone())) {
                patient.setPhone(request.getPatientPhone());
                updated = true;
            }
            if (updated) {
                patientRepository.save(patient);
            }
        });
    }
}
