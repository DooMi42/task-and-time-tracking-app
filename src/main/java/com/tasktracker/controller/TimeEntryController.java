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

            // Get the task
            Task task = taskService.getTaskById(request.getTaskId());
            if (task == null) {
                return ResponseEntity.badRequest().body("Task not found");
            }

            // Create time entry
            TimeEntry timeEntry = new TimeEntry();
            timeEntry.setTask(task);
            timeEntry.setDescription(request.getDescription());

            // Parse start and end times
            timeEntry.setStartTime(parseDateTime(request.getStartTime()));
            timeEntry.setEndTime(parseDateTime(request.getEndTime()));

            TimeEntry savedEntry = timeEntryService.saveTimeEntry(timeEntry);

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
            // Try ISO format first (from form data)
            return LocalDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            // If that fails, try date + time format
            if (dateTimeStr.length() >= 16) {
                String date = dateTimeStr.substring(0, 10);
                String time = dateTimeStr.substring(11, 16);
                return LocalDateTime.parse(date + "T" + time + ":00");
            }
            throw new IllegalArgumentException("Invalid datetime format: " + dateTimeStr);
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