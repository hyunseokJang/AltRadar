package com.altradar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AltRadarApplication {

    public static void main(String[] args) {
        SpringApplication.run(AltRadarApplication.class, args);
    }
} 