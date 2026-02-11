package com.example.genggamin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${file.upload-dir}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Menambahkan handler untuk mengakses file upload secara statis
    // Maps /uploads/kyc/** ke file system lokal
    registry.addResourceHandler("/uploads/kyc/**").addResourceLocations("file:" + uploadDir + "/");
  }
}
