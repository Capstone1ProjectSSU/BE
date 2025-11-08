package com.example.cap1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Cap1Application {

	public static void main(String[] args) {
		SpringApplication.run(Cap1Application.class, args);
	}

}