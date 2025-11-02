package com.keetlo.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${file.root.directory}")
  private String uploadsRoot; // e.g. /Volumes/MAXDISK/MyProjects/keetlo/uploads

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registerDir(registry, "files");
    registerDir(registry, "images");
    registerDir(registry, "videos");
  }

  private void registerDir(ResourceHandlerRegistry registry, String dir) {
    String location = java.nio.file.Paths.get(uploadsRoot, dir).toUri().toString(); // file:/.../uploads/<dir>/
    registry.addResourceHandler("/" + dir + "/**")
        .addResourceLocations(location)
        .setCacheControl(org.springframework.http.CacheControl.maxAge(30, java.util.concurrent.TimeUnit.DAYS).cachePublic())
        .resourceChain(true)
        .addResolver(new org.springframework.web.servlet.resource.PathResourceResolver());
  }
}
