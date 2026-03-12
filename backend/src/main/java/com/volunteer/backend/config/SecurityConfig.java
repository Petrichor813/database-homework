package com.volunteer.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

import com.volunteer.backend.security.TokenAuthenticationFilter;
import com.volunteer.backend.service.AuthService;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter(AuthService authService) {
        return new TokenAuthenticationFilter(authService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, TokenAuthenticationFilter tokenAuthenticationFilter)
            throws Exception {
        // @formatter:off
        // 使用 JWT token 代替 CSRF token 防御
        // CSRF 攻击是利用用户在已登录状态下的浏览器发送恶意请求，
        // 而 JWT 是无状态的，不依赖于服务器端的会话状态，因此不需要 CSRF 防御。
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/api/product/get-products", "/api/activity/get-activities", "/api/activity/hot-activities", "/api/statistics/**") // 公开接口
                .permitAll() // 允许所有请求（包括未认证用户），匹配.requestMatchers
                .anyRequest() // 所有其他请求，需要认证
                .authenticated()
            )
            // 添加 token 过滤器
            .addFilterBefore(
                tokenAuthenticationFilter, // 在用户名密码认证过滤器之前添加 token 认证自定义过滤器
                UsernamePasswordAuthenticationFilter.class
        );
        // @formatter:on
        return http.build();
    }
}