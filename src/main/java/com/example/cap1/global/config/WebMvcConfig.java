package com.example.cap1.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // /files/** 요청을 서버 내부 /workspace/files/ 폴더에서 매핑
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:/data/files/");

    }
}
