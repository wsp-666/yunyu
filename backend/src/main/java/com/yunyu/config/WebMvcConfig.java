package com.yunyu.config;

import com.yunyu.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    private static final String UPLOAD_BASE = System.getProperty("user.dir") + "/uploads/";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/user/login", "/api/user/register", "/api/admin/login")
                .excludePathPatterns("/api/venue/list", "/api/venue/detail/**")
                .excludePathPatterns("/api/session/list/**")
                .excludePathPatterns("/api/weather/**")
                .excludePathPatterns("/api/spot/list", "/api/spot/detail/**")
                .excludePathPatterns("/api/ai/**");

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String imagesPath = UPLOAD_BASE + "images/";
        String videosPath = UPLOAD_BASE + "videos/";
        new File(imagesPath).mkdirs();
        new File(videosPath).mkdirs();

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + imagesPath);
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:" + videosPath);
    }
}
