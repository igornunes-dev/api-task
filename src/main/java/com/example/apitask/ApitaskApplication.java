package com.example.apitask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ApitaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApitaskApplication.class, args);
	}

}
