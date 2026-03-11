package com.volunteer.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // @formatter:off
        registry.addMapping("/api/**") // 匹配路径（接口）
                .allowedOrigins("http://localhost:20000") // 配置允许的前端域名
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS") // 配置允许的 HTTP 方法
                .allowedHeaders("*") // 配置允许的请求头
                .allowCredentials(true); // 允许携带凭证
        // @formatter:on
    }
}
