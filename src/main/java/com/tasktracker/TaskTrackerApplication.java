package com.tasktracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TaskTrackerApplication {

    public static void main(String[] args) {
        // Support for Render.com PORT environment variable
        String port = System.getenv("PORT");
        if (port != null) {
            System.setProperty("server.port", port);
        }

        SpringApplication.run(TaskTrackerApplication.class, args);
    }
}