package com.example.event_ticketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EventTicketingApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventTicketingApplication.class, args);
	}

}
