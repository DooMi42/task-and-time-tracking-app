package com.tasktracker.dto;

import java.time.LocalDateTime;

public class ReportDto {
    private String username;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private long totalTasks;
    private long totalTimeSpent; // in minutes

    public ReportDto() {
    }

    public ReportDto(String username, LocalDateTime startDate, LocalDateTime endDate, long totalTasks, long totalTimeSpent) {
        this.username = username;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalTasks = totalTasks;
        this.totalTimeSpent = totalTimeSpent;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(long totalTasks) {
        this.totalTasks = totalTasks;
    }

    public long getTotalTimeSpent() {
        return totalTimeSpent;
    }

    public void setTotalTimeSpent(long totalTimeSpent) {
        this.totalTimeSpent = totalTimeSpent;
    }
}