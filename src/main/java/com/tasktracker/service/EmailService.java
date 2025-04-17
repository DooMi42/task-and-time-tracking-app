package com.tasktracker.service;

public interface EmailService {
    void sendTaskNotification(String to, String subject, String body);
    void sendReport(String to, String subject, String body);
}