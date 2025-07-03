package com.example.dulich.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Add handler for Chrome DevTools requests
        registry.addResourceHandler("/.well-known/appspecific/**")
                .addResourceLocations("classpath:/static/.well-known/appspecific/")
                .setCachePeriod(0)
                .resourceChain(true);
                
        // Add specific handlers for static assets
        registry.addResourceHandler("/page/**")
                .addResourceLocations("classpath:/static/page/")
                .setCachePeriod(3600)
                .resourceChain(true);
        
        registry.addResourceHandler("/admin/**")
                .addResourceLocations("classpath:/static/admin/")
                .setCachePeriod(3600)
                .resourceChain(true);
                  // Add a more specific handler for static resources
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600)
                .resourceChain(true);
                
        // Add handler for uploads directory
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/")
                .setCachePeriod(3600)
                .resourceChain(true);
    }
}
