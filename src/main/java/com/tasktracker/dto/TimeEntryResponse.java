package com.tasktracker.dto;

import com.tasktracker.model.TimeEntry;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeEntryResponse {
    private Long id;
    private Long taskId;
    private String taskTitle;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String formattedStartTime;
    private String formattedEndTime;
    private long durationMinutes;

    public TimeEntryResponse() {
    }

    // Convert TimeEntry entity to DTO
    public static TimeEntryResponse fromEntity(TimeEntry timeEntry) {
        TimeEntryResponse dto = new TimeEntryResponse();
        dto.setId(timeEntry.getId());

        if (timeEntry.getTask() != null) {
            dto.setTaskId(timeEntry.getTask().getId());
            dto.setTaskTitle(timeEntry.getTask().getTitle());
        }

        dto.setDescription(timeEntry.getDescription());
        dto.setStartTime(timeEntry.getStartTime());
        dto.setEndTime(timeEntry.getEndTime());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        if (timeEntry.getStartTime() != null) {
            dto.setFormattedStartTime(timeEntry.getStartTime().format(formatter));
        }

        if (timeEntry.getEndTime() != null) {
            dto.setFormattedEndTime(timeEntry.getEndTime().format(formatter));
        }

        // Calculate duration if both times exist
        if (timeEntry.getStartTime() != null && timeEntry.getEndTime() != null) {
            long minutes = java.time.Duration.between(
                    timeEntry.getStartTime(),
                    timeEntry.getEndTime()).toMinutes();
            dto.setDurationMinutes(minutes);
        }

        return dto;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getFormattedStartTime() {
        return formattedStartTime;
    }

    public void setFormattedStartTime(String formattedStartTime) {
        this.formattedStartTime = formattedStartTime;
    }

    public String getFormattedEndTime() {
        return formattedEndTime;
    }

    public void setFormattedEndTime(String formattedEndTime) {
        this.formattedEndTime = formattedEndTime;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
