package com.tasktracker.dto;

public class TimeEntryRequest {
    private Long taskId;
    private String description;
    private String startTime;
    private String endTime;

    // Default constructor required for JSON deserialization
    public TimeEntryRequest() {
    }

    // Constructor with all fields
    public TimeEntryRequest(Long taskId, String description, String startTime, String endTime) {
        this.taskId = taskId;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and setters
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "TimeEntryRequest{" +
                "taskId=" + taskId +
                ", description='" + description + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }
}
