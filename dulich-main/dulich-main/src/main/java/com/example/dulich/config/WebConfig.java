package com.example.dulich.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Add a resource handler for Chrome DevTools requests
        registry.addResourceHandler("/.well-known/appspecific/**")
                .addResourceLocations("classpath:/static/.well-known/appspecific/")
                .setCachePeriod(0);
                
        // Add or customize other resource handlers if needed
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }
}
