package com.hospital.service;

import com.hospital.dto.AppointmentQueueDTO;
import com.hospital.dto.ScheduleAdjustmentRequestDTO;
import com.hospital.dto.ScheduleDTO;
import com.hospital.entity.Appointment;
import com.hospital.entity.Patient;
import com.hospital.entity.ScheduleAdjustmentRequest;
import com.hospital.entity.User;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorDashboardServiceImpl implements DoctorDashboardService {
    
    private final ScheduleService scheduleService;
    private final ScheduleAdjustmentRequestRepository adjustmentRequestRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    public List<ScheduleDTO> getMySchedulesForWeek(Long doctorId) {
        return scheduleService.getDoctorSchedulesForWeek(doctorId);
    }

    @Override
    public List<AppointmentQueueDTO> getAppointmentQueue(Long doctorId, LocalDateTime appointmentTime) {
        // 获取该时间段（30分钟）内的所有预约
        LocalDateTime slotStart = appointmentTime;
        LocalDateTime slotEnd = appointmentTime.plusMinutes(30);
        
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        
        List<Appointment> filteredAppointments = appointments.stream()
                .filter(a -> {
                    LocalDateTime aptTime = a.getAppointmentTime();
                    return !aptTime.isBefore(slotStart) && 
                           aptTime.isBefore(slotEnd) &&
                           a.getStatus() != Appointment.AppointmentStatus.CANCELLED &&
                           a.getStatus() != Appointment.AppointmentStatus.EXPIRED;
                })
                .sorted(Comparator.comparing(Appointment::getAppointmentTime))
                .collect(Collectors.toList());
        
        // 转换为队列DTO
        int position = 1;
        List<AppointmentQueueDTO> queue = new java.util.ArrayList<>();
        
        for (Appointment appointment : filteredAppointments) {
            AppointmentQueueDTO dto = new AppointmentQueueDTO();
            dto.setAppointmentId(appointment.getId());
            dto.setPatientId(appointment.getPatientId());
            dto.setAppointmentTime(appointment.getAppointmentTime());
            dto.setStatus(appointment.getStatus());
            dto.setNotes(appointment.getNotes());
            dto.setQueuePosition(position - 1); // 前面还有几人
            
            // 获取患者信息
            Patient patient = patientRepository.findById(appointment.getPatientId())
                    .orElse(null);
            if (patient != null) {
                dto.setPatientName(patient.getName());
                dto.setPatientPhone(patient.getPhone());
                
                // 获取患者邮箱
                User user = userRepository.findById(patient.getUserId())
                        .orElse(null);
                if (user != null) {
                    dto.setPatientEmail(user.getEmail());
                }
            }
            
            queue.add(dto);
            position++;
        }
        
        return queue;
    }

    @Override
    @Transactional
    public void sendAppointmentReminder(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("预约不存在"));
        
        Patient patient = patientRepository.findById(appointment.getPatientId())
                .orElseThrow(() -> new RuntimeException("患者不存在"));
        
        User user = userRepository.findById(patient.getUserId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        String email = user.getEmail();
        String patientName = patient.getName() != null ? patient.getName() : "患者";
        
        // 发送提醒邮件
        String subject = "就诊提醒";
        String content = String.format(
            "尊敬的%s，\n\n" +
            "您有一个预约即将开始：\n" +
            "预约时间：%s\n" +
            "请准时到达医院就诊。\n\n" +
            "祝您身体健康！",
            patientName,
            appointment.getAppointmentTime().toString()
        );
        
        emailService.sendEmail(email, subject, content);
    }

    @Override
    @Transactional
    public ScheduleAdjustmentRequestDTO requestScheduleAdjustment(
            Long doctorId, 
            DayOfWeek dayOfWeek, 
            LocalTime startTime, 
            LocalTime endTime, 
            String reason) {
        
        ScheduleAdjustmentRequest request = new ScheduleAdjustmentRequest();
        request.setDoctorId(doctorId);
        request.setDayOfWeek(dayOfWeek);
        request.setStartTime(startTime);
        request.setEndTime(endTime);
        request.setReason(reason);
        request.setStatus(ScheduleAdjustmentRequest.RequestStatus.PENDING);
        
        ScheduleAdjustmentRequest saved = adjustmentRequestRepository.save(request);
        
        ScheduleAdjustmentRequestDTO dto = new ScheduleAdjustmentRequestDTO();
        dto.setId(saved.getId());
        dto.setDoctorId(saved.getDoctorId());
        dto.setDayOfWeek(saved.getDayOfWeek());
        dto.setStartTime(saved.getStartTime());
        dto.setEndTime(saved.getEndTime());
        dto.setReason(saved.getReason());
        dto.setStatus(saved.getStatus());
        dto.setCreatedAt(saved.getCreatedAt());
        dto.setUpdatedAt(saved.getUpdatedAt());
        
        return dto;
    }

    @Override
    public List<ScheduleAdjustmentRequestDTO> getMyAdjustmentRequests(Long doctorId) {
        return adjustmentRequestRepository.findByDoctorId(doctorId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private ScheduleAdjustmentRequestDTO convertToDTO(ScheduleAdjustmentRequest request) {
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
        return dto;
    }
}

