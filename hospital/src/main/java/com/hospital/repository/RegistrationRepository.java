package com.hospital.repository;

import com.hospital.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByDepartmentId(Long departmentId);
    List<Registration> findByDoctorId(Long doctorId);
    List<Registration> findByPhone(String phone);
    List<Registration> findByIdCard(String idCard);
}