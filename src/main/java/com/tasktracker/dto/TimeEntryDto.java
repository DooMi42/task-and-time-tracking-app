package com.tasktracker.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntryDto {
    private Long id;

    @NotNull(message = "Task ID is required")
    private Long taskId;

    private Long userId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String description;
    private Double durationInHours;
    private boolean running;
}