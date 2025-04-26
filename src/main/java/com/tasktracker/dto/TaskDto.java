package com.tasktracker.dto;

import com.tasktracker.model.Task.TaskPriority;
import com.tasktracker.model.Task.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Status is required")
    private TaskStatus status;

    private TaskPriority priority;

    private LocalDate dueDate;

    @Positive(message = "Estimated hours must be positive")
    private Double estimatedHours;

    private Double totalSpentHours;

    private Long userId;

    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDueDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            this.dueDate = null;
            return;
        }
        this.dueDate = LocalDate.parse(dateString);
    }
}