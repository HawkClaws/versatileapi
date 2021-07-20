package com.flex.versatileapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.flex.versatileapi.interceptor.PerClientRateLimitInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE");
  }
  
  @Bean
  public PerClientRateLimitInterceptor perClientRateLimitInterceptor() {
      return new PerClientRateLimitInterceptor();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
      registry.addInterceptor(perClientRateLimitInterceptor()).addPathPatterns("/**"); 

  }
}

