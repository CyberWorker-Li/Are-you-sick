// EmailService.java
package com.hospital.service;

import com.hospital.entity.EmailVerification;
import com.hospital.entity.VerificationType;
import com.hospital.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailVerificationRepository verificationRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${verification.code.expiration}")
    private Integer codeExpiration;

    @Value("${verification.code.length}")
    private Integer codeLength;

    /**
     * 发送验证码
     */
    @Async
    @Transactional
    public void sendVerificationCode(String email, VerificationType type) {
        // 生成验证码
        String code = generateCode();

        // 保存验证码到数据库
        EmailVerification verification = new EmailVerification();
        verification.setEmail(email);
        verification.setCode(code);
        verification.setType(type);
        verification.setExpireTime(LocalDateTime.now().plusSeconds(codeExpiration));
        verification.setUsed(0);
        verificationRepository.save(verification);

        // 发送邮件
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(email);

        if (type == VerificationType.REGISTER) {
            message.setSubject("【医院管理系统】注册验证码");
            message.setText(String.format(
                    "您好！\n\n" +
                            "您正在注册医院管理系统账号，验证码为：%s\n\n" +
                            "验证码有效期为 %d 秒，请尽快使用。\n\n" +
                            "如非本人操作，请忽略此邮件。",
                    code, codeExpiration
            ));
        } else if (type == VerificationType.RESET_PASSWORD) {
            message.setSubject("【医院管理系统】重置密码验证码");
            message.setText(String.format(
                    "您好！\n\n" +
                            "您正在重置医院管理系统账号密码，验证码为：%s\n\n" +
                            "验证码有效期为 %d 秒，请尽快使用。\n\n" +
                            "如非本人操作，请立即修改密码并联系管理员。",
                    code, codeExpiration
            ));
        }

        try {
            mailSender.send(message);
            log.info("验证码邮件发送成功: email={}, type={}", email, type);
        } catch (Exception e) {
            log.error("验证码邮件发送失败: email={}, error={}", email, e.getMessage());
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }

    /**
     * 验证验证码
     */
    @Transactional
    public boolean verifyCode(String email, String code, VerificationType type) {
        var verification = verificationRepository
                .findByEmailAndCodeAndTypeAndUsedAndExpireTimeAfter(
                        email, code, type, 0, LocalDateTime.now()
                );

        if (verification.isPresent()) {
            // 标记验证码为已使用
            EmailVerification v = verification.get();
            v.setUsed(1);
            verificationRepository.save(v);
            return true;
        }
        return false;
    }

    /**
     * 生成随机验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 发送普通邮件
     */
    @Async
    public void sendEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        
        try {
            mailSender.send(message);
            log.info("邮件发送成功: to={}, subject={}", to, subject);
        } catch (Exception e) {
            log.error("邮件发送失败: to={}, error={}", to, e.getMessage());
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }

    /**
     * 清理过期验证码(定时任务)
     */
    @Transactional
    public void cleanExpiredCodes() {
        verificationRepository.deleteByExpireTimeBefore(LocalDateTime.now());
        log.info("已清理过期验证码");
    }
}