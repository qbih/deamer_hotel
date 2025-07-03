package com.example.dulich.config;

import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MimeConfig implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
        
        // Add or correct any problematic MIME types
        mappings.add("css", "text/css");
        mappings.add("js", "application/javascript");
        mappings.add("html", "text/html");
        mappings.add("json", "application/json");
        mappings.add("png", "image/png");
        mappings.add("jpg", "image/jpeg");
        mappings.add("jpeg", "image/jpeg");
        mappings.add("gif", "image/gif");
        mappings.add("svg", "image/svg+xml");
        mappings.add("ico", "image/x-icon");
        mappings.add("eot", "application/vnd.ms-fontobject");
        mappings.add("ttf", "application/font-ttf");
        mappings.add("woff", "application/font-woff");
        mappings.add("woff2", "application/font-woff2");
        
        factory.setMimeMappings(mappings);
    }
}
