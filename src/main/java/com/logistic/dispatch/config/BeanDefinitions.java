package com.logistic.dispatch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistic.dispatch.utility.SecurityUtils;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class BeanDefinitions {

    @Bean
    public ObjectMapper objectMapBean(){
        return new ObjectMapper();
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(SecurityUtils.getCurrentUsername());
    }

    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilter() {
        FilterRegistrationBean<LoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LoggingFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
}
