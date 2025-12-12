package com.hospital.service;

import com.hospital.dto.DoctorDTO;
import com.hospital.entity.Doctor;
import com.hospital.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    
    private final DoctorRepository doctorRepository;

    @Override
    public List<DoctorDTO> getAllDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        return doctors.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DoctorDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("医生不存在"));
        return convertToDTO(doctor);
    }

    @Override
    public Optional<Doctor> getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId);
    }

    @Override
    public List<DoctorDTO> getDoctorsByDepartment(Long departmentId) {
        List<Doctor> doctors = doctorRepository.findByDepartment_Id(departmentId);
        return doctors.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Doctor createDoctor(Doctor doctor) {
        return doctorRepository.save(doctor);
    }

    @Override
    public Doctor updateDoctor(Long id, Doctor doctorDetails) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("医生不存在"));
        
        if (doctorDetails.getName() != null) {
            doctor.setName(doctorDetails.getName());
        }
        if (doctorDetails.getPhone() != null) {
            doctor.setPhone(doctorDetails.getPhone());
        }
        if (doctorDetails.getDepartment() != null) {
            doctor.setDepartment(doctorDetails.getDepartment());
        }
        if (doctorDetails.getTitle() != null) {
            doctor.setTitle(doctorDetails.getTitle());
        }
        
        return doctorRepository.save(doctor);
    }

    @Override
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("医生不存在"));
        doctorRepository.delete(doctor);
    }

    @Override
    public List<DoctorDTO> searchDoctors(String keyword) {
        String lowered = keyword.toLowerCase();
        return doctorRepository.findAll().stream()
                .filter(doctor ->
                        (doctor.getName() != null && doctor.getName().toLowerCase().contains(lowered)) ||
                        (doctor.getDepartment() != null && doctor.getDepartment().getName() != null &&
                                doctor.getDepartment().getName().toLowerCase().contains(lowered)) ||
                        (doctor.getTitle() != null && doctor.getTitle().toLowerCase().contains(lowered))
                )
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
}
