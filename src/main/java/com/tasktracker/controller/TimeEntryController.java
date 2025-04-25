package com.tasktracker.controller;

import com.tasktracker.dto.TimeEntryDto;
import com.tasktracker.dto.TimeEntryRequest;
import com.tasktracker.model.Task;
import com.tasktracker.model.TimeEntry;
import com.tasktracker.model.User;
import com.tasktracker.service.TaskService;
import com.tasktracker.service.TimeEntryService;
import com.tasktracker.repository.TimeEntryRepository;
import com.tasktracker.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {
    private static final Logger logger = LoggerFactory.getLogger(TimeEntryController.class);

    @Autowired
    private TimeEntryService timeEntryService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TimeEntryRepository timeEntryRepository;

    @PostMapping
    public ResponseEntity<?> createTimeEntry(@RequestBody TimeEntryRequest request) {
        try {
            logger.info("Received time entry request: {}", request);

            if (request == null || request.getTaskId() == null) {
                return ResponseEntity.badRequest().body("Missing required task ID");
            }

            // Create a simplified process - get task, create entry, save it
            Task task = taskService.getTaskById(request.getTaskId());
            if (task == null) {
                return ResponseEntity.badRequest().body("Task not found");
            }

            // Parse dates to make sure they're valid
            LocalDateTime startTime;
            LocalDateTime endTime;
            try {
                startTime = parseDateTime(request.getStartTime());
                endTime = parseDateTime(request.getEndTime());
            } catch (Exception e) {
                logger.error("Date parsing error", e);
                return ResponseEntity.badRequest().body("Invalid date format: " + e.getMessage());
            }

            // Create the time entry directly
            TimeEntry timeEntry = new TimeEntry();
            timeEntry.setTask(task);
            timeEntry.setDescription(request.getDescription());
            timeEntry.setStartTime(startTime);
            timeEntry.setEndTime(endTime);

            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            timeEntry.setUser(user);

            // Save the entry
            TimeEntry savedEntry = timeEntryRepository.save(timeEntry);

            // Return a simple success response
            return ResponseEntity.ok(Map.of(
                    "id", savedEntry.getId(),
                    "message", "Time entry created successfully"));
        } catch (Exception e) {
            logger.error("Error creating time entry", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create time entry: " + e.getMessage());
        }
    }

    // Add this helper method to the controller
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            throw new IllegalArgumentException("Date/time cannot be empty");
        }

        try {
            // Try standard ISO format
            return LocalDateTime.parse(dateTimeStr);
        } catch (Exception e) {
            // Handle common issues with format
            if (dateTimeStr.length() == 16 && dateTimeStr.contains("T")) {
                // Format like "2023-04-25T14:30" missing seconds
                return LocalDateTime.parse(dateTimeStr + ":00");
            } else {
                throw new IllegalArgumentException("Unsupported date format: " + dateTimeStr);
            }
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTimeEntryById(@PathVariable Long id) {
        try {
            TimeEntryDto timeEntry = timeEntryService.getTimeEntryDtoById(id);
            return ResponseEntity.ok(timeEntry);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error getting time entry", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get time entry: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllTimeEntries() {
        try {
            List<TimeEntryDto> timeEntries = timeEntryService.getAllTimeEntriesDto();
            return ResponseEntity.ok(timeEntries);
        } catch (Exception e) {
            logger.error("Error getting time entries", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get time entries: " + e.getMessage());
        }
    }

    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getTimeEntriesByTask(@PathVariable Long taskId) {
        try {
            List<TimeEntryDto> timeEntries = timeEntryService.getTimeEntriesByTask(taskId);
            return ResponseEntity.ok(timeEntries);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error getting time entries for task", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get time entries: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTimeEntry(@PathVariable Long id, @RequestBody TimeEntryDto timeEntryDto) {
        try {
            TimeEntryDto updatedEntry = timeEntryService.updateTimeEntry(id, timeEntryDto);
            return ResponseEntity.ok(updatedEntry);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating time entry", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update time entry: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTimeEntry(@PathVariable Long id) {
        try {
            timeEntryService.deleteTimeEntry(id);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting time entry", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete time entry: " + e.getMessage());
        }
    }
}