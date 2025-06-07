package com.jobsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobAlertsApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobAlertsApiApplication.class, args);
	}
}