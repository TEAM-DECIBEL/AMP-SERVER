package com.amp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AmpApplication {

	public static void main(String[] args) {
		SpringApplication.run(AmpApplication.class, args);
	}

}
