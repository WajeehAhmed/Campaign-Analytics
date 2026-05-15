package com.cipherlab.caampaign_analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CaampaignAnalyticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CaampaignAnalyticsApplication.class, args);
	}

}
