// AuthService.java
package com.hospital.service;

import com.hospital.dto.*;
import com.hospital.entity.*;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AdminRepository adminRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        log.info("开始登录处理: email={}", request.getEmail());
        
        // 检查邮箱是否存在
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("登录失败: 邮箱账号不存在 - {}", request.getEmail());
                    return new RuntimeException("邮箱账号不存在");
                });

        // 检查账号状态
        if (user.getStatus() == 0) {
            log.warn("登录失败: 账号已被禁用 - {}", request.getEmail());
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("登录失败: 密码错误 - {}", request.getEmail());
            throw new RuntimeException("密码错误");
        }

        log.info("密码验证成功: email={}", request.getEmail());

        // 生成Token
        String token = jwtService.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        log.info("Token生成成功: email={}, tokenLength={}", request.getEmail(), token.length());

        // 获取用户详细信息
        UserInfo userInfo = getUserInfo(user);

        log.info("登录成功: email={}, userId={}, role={}", request.getEmail(), user.getId(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .userInfo(userInfo)
                .build();
    }
    /**
     * 更新患者个人信息
     */
    @Transactional
    public void updatePatientProfile(Long userId, UpdatePatientProfileRequest request) {
        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 确保是患者
        if (user.getRole() != UserRole.PATIENT) {
            throw new RuntimeException("只有患者可以更新个人信息");
        }

        // 获取患者信息
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("患者信息不存在"));

        // 更新信息
        patient.setName(request.getName());
        patient.setPhone(request.getPhone());
        // 第79行修改后：
        if (request.getGender() != null && !request.getGender().isEmpty()) {
            try {
                // 将字符串转换为 Gender 枚举
                Gender gender = Gender.valueOf(request.getGender().toUpperCase());
                patient.setGender(gender);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("性别格式不正确，应为 MALE、FEMALE 或 OTHER");
            }
        }
        patient.setBirthDate(request.getBirthDate());

        patientRepository.save(patient);
        log.info("患者信息更新成功: userId={}, name={}", userId, request.getName());
    }

    /**
     * 患者注册
     */
    @Transactional
    public void register(RegisterRequest request) {
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("该邮箱已被注册");
        }

        // 验证验证码
        if (!emailService.verifyCode(
                request.getEmail(),
                request.getVerificationCode(),
                VerificationType.REGISTER
        )) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 创建用户
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.PATIENT);
        user.setStatus(1);
        User savedUser = userRepository.save(user);

        // 创建患者信息
        Patient patient = new Patient();
        patient.setUserId(savedUser.getId());
        patientRepository.save(patient);

        log.info("患者注册成功: email={}", request.getEmail());
    }

    /**
     * 重置密码
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // 检查邮箱是否存在
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("邮箱账号不存在"));

        // 验证验证码
        if (!emailService.verifyCode(
                request.getEmail(),
                request.getVerificationCode(),
                VerificationType.RESET_PASSWORD
        )) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("密码重置成功: email={}", request.getEmail());
    }

    /**
     * 发送验证码
     */
    public void sendVerificationCode(SendCodeRequest request) {
        String email = request.getEmail();
        String type = request.getType();

        // 验证类型
        VerificationType verificationType;
        try {
            verificationType = VerificationType.valueOf(type);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("验证码类型无效");
        }

        // 注册时检查邮箱是否已存在
        if (verificationType == VerificationType.REGISTER) {
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("该邮箱已被注册");
            }
        }

        // 重置密码时检查邮箱是否存在
        if (verificationType == VerificationType.RESET_PASSWORD) {
            if (!userRepository.existsByEmail(email)) {
                throw new RuntimeException("邮箱账号不存在");
            }
        }

        // 发送验证码
        try {
            emailService.sendVerificationCode(email, verificationType);
            log.info("验证码发送成功: email={}, type={}", email, type);
        } catch (Exception e) {
            log.error("验证码发送失败: email={}, error={}", email, e.getMessage());
            throw new RuntimeException("验证码发送失败，请稍后重试");
        }
    }

    /**
     * 获取当前用户信息
     */
    public UserInfo getCurrentUserInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查账号状态
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }
        
        return getUserInfo(user);
    }

    /**
     * 获取用户详细信息
     */
    private UserInfo getUserInfo(User user) {
        UserInfo.UserInfoBuilder builder = UserInfo.builder();

        switch (user.getRole()) {
            case PATIENT:
                patientRepository.findByUserId(user.getId()).ifPresent(patient -> {
                    builder.id(patient.getId())
                            .name(patient.getName())
                            .phone(patient.getPhone());
                });
                break;

            case DOCTOR:
                doctorRepository.findByUserId(user.getId()).ifPresent(doctor -> {
                    builder.id(doctor.getId())
                            .name(doctor.getName())
                            .phone(doctor.getPhone())
                            .department(doctor.getDepartment())
                            .title(doctor.getTitle());
                });
                break;

            case ADMIN:
                adminRepository.findByUserId(user.getId()).ifPresent(admin -> {
                    builder.id(admin.getId())
                            .name(admin.getName())
                            .phone(admin.getPhone());
                });
                break;
        }

        return builder.build();
    }
}