package com.tasktracker.controller;

import com.tasktracker.dto.TimeEntryDto;
import com.tasktracker.dto.TimeEntryRequest;
import com.tasktracker.dto.TaskDto;
import com.tasktracker.model.Task;
import com.tasktracker.model.TimeEntry;
import com.tasktracker.model.User;
import com.tasktracker.service.TaskService;
import com.tasktracker.service.TimeEntryService;
import com.tasktracker.repository.TaskRepository;
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
import java.util.Collections;
import java.util.HashMap;
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

    @Autowired
    private TaskRepository taskRepository;

    @PostMapping
    public ResponseEntity<?> createTimeEntry(@RequestBody TimeEntryRequest request) {
        logger.info("Received time entry request: {}", request);

        try {
            // Basic validation
            if (request == null || request.getTaskId() == null) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Missing required task ID"));
            }

            // Better logging of the incoming request
            logger.info("Creating time entry - taskId: {}, description: {}, startTime: {}, endTime: {}",
                    request.getTaskId(), request.getDescription(), request.getStartTime(), request.getEndTime());

            // Get the task using the repository directly
            Task task;
            try {
                task = taskRepository.findById(request.getTaskId())
                        .orElseThrow(
                                () -> new EntityNotFoundException("Task not found with ID: " + request.getTaskId()));
                logger.info("Found task: {}", task.getId());
            } catch (EntityNotFoundException e) {
                logger.error("Task not found: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("error", e.getMessage()));
            }

            // Parse dates with explicit error handling
            LocalDateTime startTime;
            LocalDateTime endTime;
            try {
                startTime = parseDateTime(request.getStartTime());
                endTime = parseDateTime(request.getEndTime());

                // Validate time range
                if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
                    logger.error("End time is before start time: {} - {}", startTime, endTime);
                    return ResponseEntity.badRequest().body(Collections.singletonMap("error",
                            "End time cannot be before start time"));
                }
            } catch (Exception e) {
                logger.error("Date parsing error: {}", e.getMessage());
                return ResponseEntity.badRequest().body(Collections.singletonMap("error",
                        "Invalid date format: " + e.getMessage()));
            }

            // Get current user
            User user;
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = auth.getName();
                user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found: " + username));
                logger.info("Found user: {}", user.getUsername());
            } catch (Exception e) {
                logger.error("User error: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonMap("error", "User authentication error: " + e.getMessage()));
            }

            // Create and save time entry
            try {
                TimeEntry timeEntry = new TimeEntry();
                timeEntry.setTask(task);
                timeEntry.setUser(user);
                timeEntry.setDescription(request.getDescription());
                timeEntry.setStartTime(startTime);
                timeEntry.setEndTime(endTime);

                TimeEntry savedEntry = timeEntryRepository.save(timeEntry);
                logger.info("Successfully created time entry with ID: {}", savedEntry.getId());

                // Return success response
                Map<String, Object> response = new HashMap<>();
                response.put("id", savedEntry.getId());
                response.put("message", "Time entry created successfully");
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                logger.error("Error saving time entry: {}", e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonMap("error", "Failed to save time entry: " + e.getMessage()));
            }
        } catch (Exception e) {
            // Catch-all for any other exceptions
            logger.error("Unexpected error creating time entry: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Unexpected error: " + e.getMessage()));
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }

        dateTimeStr = dateTimeStr.trim();
        logger.debug("Parsing date/time string: '{}'", dateTimeStr);

        try {
            // First try direct ISO format
            if (dateTimeStr.contains("T")) {
                if (dateTimeStr.length() >= 19) {
                    // Full format: 2023-04-25T14:30:00
                    return LocalDateTime.parse(dateTimeStr);
                } else if (dateTimeStr.length() == 16) {
                    // Missing seconds: 2023-04-25T14:30
                    return LocalDateTime.parse(dateTimeStr + ":00");
                }
            }

            // Try with space separator
            if (dateTimeStr.contains(" ")) {
                String[] parts = dateTimeStr.split(" ");
                if (parts.length == 2) {
                    String datePart = parts[0];
                    String timePart = parts[1];

                    // Add seconds if needed
                    if (timePart.length() == 5) { // HH:mm
                        timePart = timePart + ":00";
                    }

                    return LocalDateTime.parse(datePart + "T" + timePart);
                }
            }

            // Try with just date
            if (dateTimeStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDateTime.parse(dateTimeStr + "T00:00:00");
            }

            throw new IllegalArgumentException("Unknown date format: " + dateTimeStr);
        } catch (Exception e) {
            logger.error("Failed to parse date: {} - Error: {}", dateTimeStr, e.getMessage());
            throw new IllegalArgumentException("Cannot parse date '" + dateTimeStr + "': " + e.getMessage());
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