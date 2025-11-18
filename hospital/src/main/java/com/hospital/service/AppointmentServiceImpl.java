package com.hospital.service;

import com.hospital.dto.AppointmentDTO;
import com.hospital.dto.AppointmentRequest;
import com.hospital.entity.Appointment;
import com.hospital.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;

    @Override
    @Transactional
    public Appointment createAppointment(AppointmentRequest request) {
        // 检查时间冲突
        if (checkTimeConflict(request.getDoctorId(), request.getAppointmentTime())) {
            throw new RuntimeException("该时间段医生已有预约，请选择其他时间");
        }
        
        Appointment appointment = new Appointment();
        appointment.setPatientId(request.getPatientId());
        appointment.setDoctorId(request.getDoctorId());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setNotes(request.getNotes());
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);
        
        return appointmentRepository.save(appointment);
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
        return appointmentRepository.existsByDoctorIdAndAppointmentTime(doctorId, appointmentTime);
    }

    @Override
    public List<LocalDateTime> getAvailableTimeSlots(Long doctorId, LocalDate date) {
        // 生成当天的工作时间段 (9:00-17:00, 每30分钟一个时间段)
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(17, 0);
        
        // 生成时间段列表
        List<LocalTime> timeSlots = new java.util.ArrayList<>();
        LocalTime currentTime = startTime;
        while (!currentTime.isAfter(endTime)) {
            timeSlots.add(currentTime);
            currentTime = currentTime.plusMinutes(30);
        }
        
        return timeSlots.stream()
                .map(time -> LocalDateTime.of(date, time))
                .filter(time -> !checkTimeConflict(doctorId, time))
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
                dto.setDoctorDepartment(doctorDTO.getDepartment());
                dto.setDoctorTitle(doctorDTO.getTitle());
            } catch (Exception e) {
                // 如果获取医生信息失败，设置默认值
                dto.setDoctorName("未知医生");
                dto.setDoctorDepartment("未知科室");
                dto.setDoctorTitle("未知职称");
            }
        }
        
        return dto;
    }
}
