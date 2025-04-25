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
        try {
            logger.info("Received time entry request: {}", request);

            // Debug log all request fields individually
            logger.debug("Request details - taskId: {}, description: {}, startTime: {}, endTime: {}",
                    request.getTaskId(), request.getDescription(),
                    request.getStartTime(), request.getEndTime());

            if (request == null || request.getTaskId() == null) {
                return ResponseEntity.badRequest().body("Missing required task ID");
            }

            // Replace the task lookup code with this implementation
            Task task;
            try {
                // First verify the task exists and is accessible to the current user
                TaskDto taskDto = taskService.getTaskById(request.getTaskId());
                logger.info("Found task DTO: ID={}, title={}", taskDto.getId(), taskDto.getTitle());

                // Now get the actual Task entity from the repository
                // For this to work, you need to inject the TaskRepository
                task = taskRepository.findById(request.getTaskId())
                        .orElseThrow(
                                () -> new EntityNotFoundException("Task not found with ID: " + request.getTaskId()));

                logger.info("Found task entity: ID={}, title={}, user={}",
                        task.getId(), task.getTitle(),
                        task.getUser() != null ? task.getUser().getUsername() : "null");
            } catch (EntityNotFoundException e) {
                logger.error("Task not found with ID: {}", request.getTaskId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Task not found with ID: " + request.getTaskId());
            } catch (Exception e) {
                logger.error("Error retrieving task with ID: {}", request.getTaskId(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error retrieving task: " + e.getMessage());
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

            // Replace the user lookup and save code
            // Get current user
            try {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                String username = auth.getName();
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

                // Add this debug statement
                logger.info("Found user: ID={}, username={}", user.getId(), user.getUsername());

                timeEntry.setUser(user);

                // Save the entry
                TimeEntry savedEntry = timeEntryRepository.save(timeEntry);

                // Return a simple success response
                Map<String, Object> response = new HashMap<>();
                response.put("id", savedEntry.getId());
                response.put("message", "Time entry created successfully");
                logger.info("Successfully created time entry with ID: {}", savedEntry.getId());
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                logger.error("Error creating time entry", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to create time entry: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error creating time entry", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create time entry: " + e.getMessage());
        }
    }

    // Replace the current parseDateTime method with this more comprehensive version
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            throw new IllegalArgumentException("Date/time cannot be empty");
        }

        logger.debug("Parsing date/time string: {}", dateTimeStr);

        try {
            // Try standard ISO format (yyyy-MM-ddTHH:mm:ss)
            if (dateTimeStr.contains("T") && dateTimeStr.length() >= 19) {
                return LocalDateTime.parse(dateTimeStr);
            }

            // Format like "2023-04-25T14:30" missing seconds
            if (dateTimeStr.contains("T") && dateTimeStr.length() == 16) {
                return LocalDateTime.parse(dateTimeStr + ":00");
            }

            // Format with date and time separated by space "2023-04-25 14:30"
            if (dateTimeStr.contains(" ") && dateTimeStr.length() >= 16) {
                String[] parts = dateTimeStr.split(" ");
                if (parts.length == 2) {
                    String timeComponent = parts[1].length() == 5 ? parts[1] + ":00" : parts[1];
                    return LocalDateTime.parse(parts[0] + "T" + timeComponent);
                }
            }

            // If it's just a date with no time
            if (!dateTimeStr.contains("T") && !dateTimeStr.contains(" ")) {
                return LocalDateTime.parse(dateTimeStr + "T00:00:00");
            }

            // Log and throw if we couldn't parse
            logger.error("Unsupported date format: {}", dateTimeStr);
            throw new IllegalArgumentException("Unsupported date format: " + dateTimeStr);
        } catch (Exception e) {
            logger.error("Error parsing date string: {} - Error: {}", dateTimeStr, e.getMessage());
            throw new IllegalArgumentException("Failed to parse date: " + dateTimeStr + " - " + e.getMessage());
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