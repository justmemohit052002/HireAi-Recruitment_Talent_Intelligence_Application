package com.hireai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class HireaiBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HireaiBackendApplication.class, args);
	}

}
