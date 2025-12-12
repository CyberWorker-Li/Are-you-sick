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
import java.time.Duration;
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
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        LocalDateTime slotStart = determineSlotStart(appointments, appointmentTime, doctorId);
        if (slotStart == null) {
            return java.util.Collections.emptyList();
        }
        TimeWindow timeWindow = resolveWorkingWindow(doctorId, slotStart);
        LocalDateTime slotEnd = timeWindow.end();
        
        List<Appointment> filteredAppointments = appointments.stream()
                .filter(a -> {
                    LocalDateTime aptTime = a.getAppointmentTime();
                    return !aptTime.isBefore(timeWindow.start()) &&
                           aptTime.isBefore(slotEnd) &&
                           isActiveAppointment(a);
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

    private boolean isActiveAppointment(Appointment appointment) {
        return appointment.getStatus() != Appointment.AppointmentStatus.CANCELLED &&
               appointment.getStatus() != Appointment.AppointmentStatus.EXPIRED;
    }

    private LocalDateTime findUpcomingOrClosestAppointment(List<Appointment> appointments) {
        LocalDateTime now = LocalDateTime.now();
        return findBestMatch(appointments, now);
    }

    private LocalDateTime findBestMatch(List<Appointment> appointments, LocalDateTime referenceTime) {
        LocalDateTime upcoming = appointments.stream()
                .filter(this::isActiveAppointment)
                .filter(a -> !a.getAppointmentTime().isBefore(referenceTime))
                .min(Comparator.comparing(Appointment::getAppointmentTime))
                .map(Appointment::getAppointmentTime)
                .orElse(null);
        if (upcoming != null) {
            return upcoming;
        }

        return appointments.stream()
                .filter(this::isActiveAppointment)
                .max(Comparator.comparing(Appointment::getAppointmentTime))
                .map(Appointment::getAppointmentTime)
                .orElse(null);
    }

    private LocalDateTime determineSlotStart(List<Appointment> appointments,
                                             LocalDateTime requestedTime,
                                             Long doctorId) {
        if (requestedTime != null) {
            TimeWindow requestedWindow = resolveWorkingWindow(doctorId, requestedTime);
            if (hasAppointmentsInWindow(appointments, requestedWindow)) {
                return requestedWindow.start();
            }

            LocalDateTime closest = findClosestAppointmentForWindow(
                    appointments,
                    requestedWindow,
                    requestedTime
            );
            if (closest != null) {
                TimeWindow alignedWindow = requestedWindow.withDate(closest.toLocalDate());
                return alignedWindow.start();
            }
        }

        return findUpcomingOrClosestAppointment(appointments);
    }

    private TimeWindow resolveWorkingWindow(Long doctorId, LocalDateTime slotStart) {
        LocalDateTime windowStart = slotStart;
        LocalDateTime windowEnd = slotStart.plusMinutes(30);
        DayOfWeek dayOfWeek = slotStart.getDayOfWeek();
        LocalTime scheduleStartTime = slotStart.toLocalTime();
        LocalTime scheduleEndTime = windowEnd.toLocalTime();
        boolean matchedSchedule = false;

        try {
            List<ScheduleDTO> schedules = scheduleService.getDoctorSchedulesForWeek(doctorId);
            for (ScheduleDTO schedule : schedules) {
                if (schedule.getDayOfWeek() == null || schedule.getStartTime() == null || schedule.getEndTime() == null) {
                    continue;
                }
                if (!schedule.getDayOfWeek().equals(slotStart.getDayOfWeek())) {
                    continue;
                }

                LocalTime scheduleStart = schedule.getStartTime();
                LocalTime scheduleEnd = schedule.getEndTime();
                LocalTime targetTime = slotStart.toLocalTime();

                if (!targetTime.isBefore(scheduleStart) && targetTime.isBefore(scheduleEnd)) {
                    windowStart = LocalDateTime.of(slotStart.toLocalDate(), scheduleStart);
                    windowEnd = LocalDateTime.of(slotStart.toLocalDate(), scheduleEnd);
                    scheduleStartTime = scheduleStart;
                    scheduleEndTime = scheduleEnd;
                    matchedSchedule = true;
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        return new TimeWindow(windowStart, windowEnd, dayOfWeek, scheduleStartTime, scheduleEndTime, matchedSchedule);
    }

    private boolean hasAppointmentsInWindow(List<Appointment> appointments, TimeWindow window) {
        return appointments.stream()
                .filter(this::isActiveAppointment)
                .anyMatch(a -> isWithinWindow(a.getAppointmentTime(), window));
    }

    private boolean isWithinWindow(LocalDateTime time, TimeWindow window) {
        return !time.isBefore(window.start()) && time.isBefore(window.end());
    }

    private LocalDateTime findClosestAppointmentForWindow(List<Appointment> appointments,
                                                          TimeWindow window,
                                                          LocalDateTime referenceTime) {
        if (!window.hasScheduleMeta()) {
            return null;
        }

        return appointments.stream()
                .filter(this::isActiveAppointment)
                .filter(a -> matchesSchedule(window, a))
                .min(Comparator.comparingLong(a ->
                        Math.abs(Duration.between(a.getAppointmentTime(), referenceTime).toMillis())))
                .map(Appointment::getAppointmentTime)
                .orElse(null);
    }

    private boolean matchesSchedule(TimeWindow window, Appointment appointment) {
        if (!window.hasScheduleMeta()) {
            return false;
        }
        LocalDateTime appointmentTime = appointment.getAppointmentTime();
        if (appointmentTime.getDayOfWeek() != window.dayOfWeek()) {
            return false;
        }
        LocalTime time = appointmentTime.toLocalTime();
        return !time.isBefore(window.scheduleStartTime()) && time.isBefore(window.scheduleEndTime());
    }

    private static class TimeWindow {
        private final LocalDateTime start;
        private final LocalDateTime end;
        private final DayOfWeek dayOfWeek;
        private final LocalTime scheduleStartTime;
        private final LocalTime scheduleEndTime;
        private final boolean hasScheduleMeta;

        private TimeWindow(LocalDateTime start,
                           LocalDateTime end,
                           DayOfWeek dayOfWeek,
                           LocalTime scheduleStartTime,
                           LocalTime scheduleEndTime,
                           boolean hasScheduleMeta) {
            this.start = start;
            this.end = end;
            this.dayOfWeek = dayOfWeek;
            this.scheduleStartTime = scheduleStartTime;
            this.scheduleEndTime = scheduleEndTime;
            this.hasScheduleMeta = hasScheduleMeta;
        }

        public LocalDateTime start() {
            return start;
        }

        public LocalDateTime end() {
            return end;
        }

        public DayOfWeek dayOfWeek() {
            return dayOfWeek;
        }

        public LocalTime scheduleStartTime() {
            return scheduleStartTime;
        }

        public LocalTime scheduleEndTime() {
            return scheduleEndTime;
        }

        public boolean hasScheduleMeta() {
            return hasScheduleMeta;
        }

        public TimeWindow withDate(LocalDate date) {
            if (!hasScheduleMeta) {
                return this;
            }
            LocalDateTime newStart = LocalDateTime.of(date, scheduleStartTime);
            LocalDateTime newEnd = LocalDateTime.of(date, scheduleEndTime);
            return new TimeWindow(newStart, newEnd, date.getDayOfWeek(), scheduleStartTime, scheduleEndTime, true);
        }
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

