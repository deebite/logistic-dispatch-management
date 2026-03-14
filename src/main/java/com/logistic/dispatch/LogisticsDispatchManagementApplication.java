package com.logistic.dispatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
public class LogisticsDispatchManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(LogisticsDispatchManagementApplication.class, args);
	}

}
