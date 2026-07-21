package com.grupo3.mmorpg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MmorpgApplication {
	public static void main(String[] args) {
		SpringApplication.run(MmorpgApplication.class, args);
	}
}
