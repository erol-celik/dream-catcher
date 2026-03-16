package com.dreamdiary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main entry point for the Dream Diary API application.
 * Bootstraps the Spring Boot context with auto-configuration.
 */
@SpringBootApplication
@EnableAsync
public class DreamDiaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DreamDiaryApplication.class, args);
    }

}
