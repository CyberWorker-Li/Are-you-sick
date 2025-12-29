package com.hospital.service;

import com.hospital.dto.ScheduleDTO;
import com.hospital.dto.CreateScheduleRequest;
import com.hospital.dto.TimeSlotDTO;
import com.hospital.entity.Schedule;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.ScheduleRepository;
import com.hospital.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {
    
    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;
    private final DoctorRepository doctorRepository;

    @Override
    @Transactional
    public Schedule createSchedule(CreateScheduleRequest request) {
        // 验证时间段（必须是2小时）
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();
        
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new RuntimeException("结束时间必须晚于开始时间");
        }
        
        long hours = java.time.Duration.between(startTime, endTime).toHours();
        if (hours != 2) {
            throw new RuntimeException("每个工作时段必须为2小时");
        }
        
        // 检查是否已存在相同的排班
        List<Schedule> existing = scheduleRepository.findByDoctorIdAndDayOfWeek(
            request.getDoctorId(), request.getDayOfWeek());
        
        for (Schedule s : existing) {
            if (s.getStartTime().equals(request.getStartTime()) && 
                s.getEndTime().equals(request.getEndTime())) {
                throw new RuntimeException("该时间段已存在排班");
            }
        }
        
        Schedule schedule = new Schedule();
        schedule.setDoctorId(request.getDoctorId());
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setMaxPatients(request.getMaxPatients() != null ? request.getMaxPatients() : 10);
        
        return scheduleRepository.save(schedule);
    }

    @Override
    public List<ScheduleDTO> getSchedulesByDoctorId(Long doctorId) {
        List<Schedule> schedules = scheduleRepository.findByDoctorId(doctorId);
        return schedules.stream()
                .map(s -> convertToDTO(s))
                .collect(Collectors.toList());
    }

    @Override
    public List<ScheduleDTO> getDoctorSchedulesForWeek(Long doctorId) {
        List<Schedule> allSchedules = scheduleRepository.findByDoctorId(doctorId);
        return allSchedules.stream()
                .map(s -> convertToDTO(s))
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlotDTO> getAvailableTimeSlots(Long doctorId, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<Schedule> schedules = scheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek);
        
        List<TimeSlotDTO> timeSlots = new ArrayList<>();
        
        for (Schedule schedule : schedules) {
            LocalDateTime startDateTime = LocalDateTime.of(date, schedule.getStartTime());
            LocalDateTime endDateTime = LocalDateTime.of(date, schedule.getEndTime());
            int currentCount = countAppointmentsInRange(doctorId, startDateTime, endDateTime);
            boolean available = currentCount < schedule.getMaxPatients();
            
            // 将2小时时段分成多个30分钟的时间段
            LocalDateTime current = startDateTime;
            while (current.isBefore(endDateTime)) {
                LocalDateTime slotEnd = current.plusMinutes(30);
                if (slotEnd.isAfter(endDateTime)) {
                    break;
                }
                
                TimeSlotDTO slot = new TimeSlotDTO();
                slot.setStartTime(current);
                slot.setEndTime(slotEnd);
                slot.setMaxPatients(schedule.getMaxPatients());
                slot.setCurrentPatients(currentCount);
                slot.setIsWorkingTime(true);
                slot.setAvailable(available);
                
                timeSlots.add(slot);
                current = current.plusMinutes(30);
            }
        }
        
        return timeSlots;
    }

    @Override
    public List<LocalDate> getAvailableDates(Long departmentId, Long doctorId, LocalDate startDate, LocalDate endDate) {
        if (departmentId == null) {
            throw new RuntimeException("科室ID不能为空");
        }
        if (startDate == null || endDate == null) {
            throw new RuntimeException("日期范围不能为空");
        }
        if (endDate.isBefore(startDate)) {
            throw new RuntimeException("结束日期不能早于开始日期");
        }

        final List<Long> doctorIds;
        if (doctorId != null) {
            doctorIds = List.of(doctorId);
        } else {
            doctorIds = doctorRepository.findByDepartment_Id(departmentId)
                    .stream()
                    .map(d -> d.getId())
                    .collect(Collectors.toList());
        }

        if (doctorIds.isEmpty()) {
            return List.of();
        }

        List<LocalDate> availableDates = new ArrayList<>();
        LocalDate cursor = startDate;
        while (!cursor.isAfter(endDate)) {
            DayOfWeek dayOfWeek = cursor.getDayOfWeek();
            boolean hasAvailability = false;

            for (Long did : doctorIds) {
                List<Schedule> schedules = scheduleRepository.findByDoctorIdAndDayOfWeek(did, dayOfWeek);
                if (schedules.isEmpty()) continue;

                for (Schedule schedule : schedules) {
                    LocalDateTime startDateTime = LocalDateTime.of(cursor, schedule.getStartTime());
                    LocalDateTime endDateTime = LocalDateTime.of(cursor, schedule.getEndTime());
                    int currentCount = countAppointmentsInRange(did, startDateTime, endDateTime);
                    if (currentCount < schedule.getMaxPatients()) {
                        hasAvailability = true;
                        break;
                    }
                }

                if (hasAvailability) break;
            }

            if (hasAvailability) {
                availableDates.add(cursor);
            }
            cursor = cursor.plusDays(1);
        }

        return availableDates;
    }

    @Override
    public boolean isWithinWorkingHours(Long doctorId, LocalDateTime appointmentTime) {
        DayOfWeek dayOfWeek = appointmentTime.getDayOfWeek();
        LocalTime time = appointmentTime.toLocalTime();
        
        List<Schedule> schedules = scheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek);
        
        for (Schedule schedule : schedules) {
            if (!time.isBefore(schedule.getStartTime()) && time.isBefore(schedule.getEndTime())) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean isTimeSlotFull(Long doctorId, LocalDateTime appointmentTime) {
        int current = getCurrentAppointmentCount(doctorId, appointmentTime);
        int max = getMaxPatientsForTimeSlot(doctorId, appointmentTime);
        return max > 0 && current >= max;
    }

    @Override
    public int getCurrentAppointmentCount(Long doctorId, LocalDateTime appointmentTime) {
        Optional<Schedule> scheduleOpt = findScheduleForDateTime(doctorId, appointmentTime);
        if (scheduleOpt.isEmpty()) {
            return 0;
        }
        Schedule schedule = scheduleOpt.get();
        LocalDate date = appointmentTime.toLocalDate();
        LocalDateTime blockStart = LocalDateTime.of(date, schedule.getStartTime());
        LocalDateTime blockEnd = LocalDateTime.of(date, schedule.getEndTime());
        return countAppointmentsInRange(doctorId, blockStart, blockEnd);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }

    @Override
    @Transactional
    public Schedule updateSchedule(Long scheduleId, CreateScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("排班不存在"));
        
        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        if (request.getMaxPatients() != null) {
            schedule.setMaxPatients(request.getMaxPatients());
        }
        
        return scheduleRepository.save(schedule);
    }

    @Override
    public boolean validateDoctorWeeklySchedule(Long doctorId) {
        List<Schedule> schedules = scheduleRepository.findByDoctorId(doctorId);
        return schedules.size() >= 2;
    }
    
    private ScheduleDTO convertToDTO(Schedule schedule) {
        ScheduleDTO dto = new ScheduleDTO();
        dto.setId(schedule.getId());
        dto.setDoctorId(schedule.getDoctorId());
        
        try {
            var doctorDTO = doctorService.getDoctorById(schedule.getDoctorId());
            dto.setDoctorName(doctorDTO.getName());
        } catch (Exception e) {
            dto.setDoctorName("未知医生");
        }
        
        dto.setDayOfWeek(schedule.getDayOfWeek());
        dto.setStartTime(schedule.getStartTime());
        dto.setEndTime(schedule.getEndTime());
        dto.setMaxPatients(schedule.getMaxPatients());
        dto.setCreatedAt(schedule.getCreatedAt());
        
        // 计算当前预约人数（需要根据具体日期计算，这里先设为0）
        dto.setCurrentPatients(0);
        
        return dto;
    }
    
    private int getMaxPatientsForTimeSlot(Long doctorId, LocalDateTime appointmentTime) {
        return findScheduleForDateTime(doctorId, appointmentTime)
                .map(Schedule::getMaxPatients)
                .orElse(0);
    }
    
    private Optional<Schedule> findScheduleForDateTime(Long doctorId, LocalDateTime appointmentTime) {
        DayOfWeek dayOfWeek = appointmentTime.getDayOfWeek();
        LocalTime time = appointmentTime.toLocalTime();
        return scheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek).stream()
                .filter(schedule -> !time.isBefore(schedule.getStartTime()) && time.isBefore(schedule.getEndTime()))
                .findFirst();
    }
    
    private int countAppointmentsInRange(Long doctorId, LocalDateTime start, LocalDateTime end) {
        return (int) appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end).stream()
                .filter(a -> a.getStatus() != com.hospital.entity.Appointment.AppointmentStatus.CANCELLED &&
                             a.getStatus() != com.hospital.entity.Appointment.AppointmentStatus.EXPIRED)
                .count();
    }
}

