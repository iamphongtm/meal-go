package com.mealgo.identify_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.mealgo.identify_service.problem.ProblemProperties;

@SpringBootApplication
@EnableConfigurationProperties(ProblemProperties.class)
public class IdentifyServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(IdentifyServiceApplication.class, args);
	}

}
