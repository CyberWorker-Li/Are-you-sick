// DoctorRepository.java
package com.hospital.repository;

import com.hospital.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);

    List<Doctor> findByNameContainingIgnoreCase(String keyword);

    List<Doctor> findByDepartment_NameContainingIgnoreCase(String keyword);

    List<Doctor> findByDepartment_NameIgnoreCase(String departmentName);

    List<Doctor> findByDepartment_Id(Long departmentId);
}
