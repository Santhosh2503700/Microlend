package com.microlend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * MicroLendApplication
 *
 * BUG FIX #5: Added @EnableScheduling to activate Spring's scheduling framework.
 * Without this annotation, the @Scheduled cron in DelinquencyScheduler
 * would never fire, meaning the nightly delinquency engine would not run.
 */
@SpringBootApplication
@EnableScheduling
public class MicroLendApplication {
    public static void main(String[] args) {
        SpringApplication.run(MicroLendApplication.class, args);
    }
}
