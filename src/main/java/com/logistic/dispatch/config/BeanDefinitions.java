package com.logistic.dispatch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanDefinitions {

    @Bean
    public ObjectMapper objectMapBean(){
        return new ObjectMapper();
    }
}
