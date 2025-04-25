package com.tasktracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tasktracker.dto.TimeEntryDto;
import com.tasktracker.service.TimeEntryService;
import com.tasktracker.dto.TimeEntryRequest;
import com.tasktracker.model.Task;
import com.tasktracker.model.TimeEntry;
import com.tasktracker.service.TaskService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {

    @Autowired
    private TimeEntryService timeEntryService;

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<?> createTimeEntry(@RequestBody TimeEntryRequest request) {
        try {
            // Log the request for debugging
            System.out.println("Received time entry request: " + request);

            // Basic validation
            if (request == null || request.getTaskId() == null) {
                return ResponseEntity.badRequest().body("Invalid request: missing task ID");
            }

            // Get the task
            com.tasktracker.dto.TaskDto taskDto = taskService.getTaskById(request.getTaskId());
            if (taskDto == null) {
                return ResponseEntity.badRequest().body("Task not found with ID: " + request.getTaskId());
            }

            // Convert TaskDto to Task
            Task task = new Task();
            task.setId(taskDto.getId());
            task.setTitle(taskDto.getTitle());
            // Set other necessary fields from taskDto to task

            // Parse start and end times with better error handling
            LocalDateTime startTime;
            LocalDateTime endTime;
            try {
                startTime = parseDateTime(request.getStartTime());
                endTime = parseDateTime(request.getEndTime());
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Invalid date format: " + e.getMessage());
            }

            // Validate times
            if (startTime == null || endTime == null) {
                return ResponseEntity.badRequest().body("Start time and end time are required");
            }

            if (endTime.isBefore(startTime)) {
                return ResponseEntity.badRequest().body("End time cannot be before start time");
            }

            // Create time entry
            TimeEntry timeEntry = new TimeEntry();
            timeEntry.setTask(task);
            timeEntry.setDescription(request.getDescription());
            timeEntry.setStartTime(startTime);
            timeEntry.setEndTime(endTime);

            // Log what we're saving
            System.out.println("Saving time entry: " + timeEntry);

            TimeEntry savedEntry = timeEntryService.saveTimeEntry(timeEntry);
            System.out.println("Successfully saved time entry with ID: " + savedEntry.getId());

            return ResponseEntity.ok(savedEntry);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create time entry: " + e.getMessage());
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }

        try {
            // Try standard ISO format first (yyyy-MM-ddTHH:mm:ss)
            return LocalDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            try {
                // Check if we just have a date with time separated (yyyy-MM-dd HH:mm)
                if (dateTimeStr.contains(" ")) {
                    String[] parts = dateTimeStr.split(" ");
                    if (parts.length == 2) {
                        return LocalDateTime.parse(parts[0] + "T" + parts[1] + ":00");
                    }
                }

                // Try date + time without seconds
                if (dateTimeStr.contains("T") && dateTimeStr.length() >= 16) {
                    return LocalDateTime.parse(dateTimeStr + ":00");
                }

                // Log the format for debugging
                System.out.println("Could not parse datetime: " + dateTimeStr);
                throw new IllegalArgumentException("Invalid datetime format: " + dateTimeStr);
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new IllegalArgumentException("Failed to parse datetime: " + dateTimeStr);
            }
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeEntryDto> getTimeEntryById(@PathVariable Long id) {
        TimeEntryDto timeEntryDto = timeEntryService.getTimeEntryById(id);
        return ResponseEntity.ok(timeEntryDto);
    }

    @GetMapping
    public ResponseEntity<List<TimeEntryDto>> getAllTimeEntries() {
        List<TimeEntryDto> timeEntries = timeEntryService.getAllTimeEntries();
        return ResponseEntity.ok(timeEntries);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeEntryDto> updateTimeEntry(@PathVariable Long id, @RequestBody TimeEntryDto timeEntryDto) {
        TimeEntryDto updatedTimeEntry = timeEntryService.updateTimeEntry(id, timeEntryDto);
        return ResponseEntity.ok(updatedTimeEntry);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeEntry(@PathVariable Long id) {
        timeEntryService.deleteTimeEntry(id);
        return ResponseEntity.noContent().build();
    }
}