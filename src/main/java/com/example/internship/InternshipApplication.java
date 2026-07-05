package com.example.internship;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application Entry Point
 *
 * @EnableAsync     — Member 9: enables @Async on EmailService so emails
 *                   don't block the HTTP request thread
 * @EnableCaching   — Member 10: enables @Cacheable / @CacheEvict on analytics
 * @EnableScheduling — Member 5: enables @Scheduled cron for auto-closing jobs
 */
@SpringBootApplication
@EnableAsync
@EnableCaching
@EnableScheduling
public class InternshipApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternshipApplication.class, args);
    }
}
