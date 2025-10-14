package com.keetlo.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${file.root.directory}")
    private String FILE_ROOT_DIRECTORY;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
       String location = "file:" + FILE_ROOT_DIRECTORY + "/images/";
        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);
    }
    
}
