package com.hospital.config;

import com.hospital.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ✅ CORS 必须在最前面配置
                .cors(Customizer.withDefaults())
                // ✅ 关闭 CSRF
                .csrf(csrf -> csrf.disable())
                // ✅ 授权配置
                .authorizeHttpRequests(auth -> auth
                        // 重要：OPTIONS 请求必须完全放行
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 认证相关接口允许匿名访问
                        .requestMatchers("/auth/**").permitAll()
                        // 医生相关接口允许匿名访问（用于前端选择医生）
                        .requestMatchers("/doctors/**").permitAll()
                        // 预约相关接口允许匿名访问（用于前端获取预约信息）
                        .requestMatchers("/appointments/**").permitAll()
                        // 调试接口允许匿名访问
                        .requestMatchers("/ping").permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                // ✅ 添加JWT认证过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }



    /**
     * ✅ CORS 配置源（供 Spring Security 使用）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 允许携带凭证（如果前端需要发送 Cookie）
        config.setAllowCredentials(true);

        // 允许的源（前端地址）
        config.setAllowedOriginPatterns(Arrays.asList("http://localhost:5173", "http://localhost:*"));

        // 允许的请求头
        config.setAllowedHeaders(Arrays.asList("*"));

        // 允许的 HTTP 方法（必须包含 OPTIONS）
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));

        // 暴露的响应头（前端可以访问的响应头）
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // 预检请求的缓存时间（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}