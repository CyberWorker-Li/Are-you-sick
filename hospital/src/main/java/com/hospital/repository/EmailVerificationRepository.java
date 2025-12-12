// EmailVerificationRepository.java
package com.hospital.repository;

import com.hospital.entity.EmailVerification;
import com.hospital.entity.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findByEmailAndCodeAndTypeAndUsedAndExpireTimeAfter(
            String email,
            String code,
            VerificationType type,
            Integer used,
            LocalDateTime now
    );

    void deleteByExpireTimeBefore(LocalDateTime now);
}
