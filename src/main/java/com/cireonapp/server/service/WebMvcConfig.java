package com.cireonapp.server.service;

import com.cireonapp.server.service.interceptor.UpdateUserLastUse;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UpdateUserLastUse()).addPathPatterns("/**")
                .excludePathPatterns(
                        "/assets/**",
                        "/favicon.ico",
                        "/error",
                        "/actuator/**"
                );
//            registry.addInterceptor(new FirstTimeSetupInterceptor())
//                    .addPathPatterns("/login","/","/signup","/admin");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/fonts/**")
                .addResourceLocations("classpath:/static/assets/fonts/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS)
                        .cachePublic()
                        .immutable());

        registry.addResourceHandler("/assets/favicon/**")
                .addResourceLocations("classpath:/static/assets/favicon/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS)
                        .cachePublic()
                        .immutable());
    }
}
