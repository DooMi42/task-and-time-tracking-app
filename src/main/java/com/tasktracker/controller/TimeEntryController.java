package com.tasktracker.controller;

import com.tasktracker.dto.TimeEntryDto;
import com.tasktracker.dto.TimeEntryRequest;
import com.tasktracker.model.TimeEntry;
import com.tasktracker.service.TaskService;
import com.tasktracker.service.TimeEntryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {
    private static final Logger logger = LoggerFactory.getLogger(TimeEntryController.class);

    @Autowired
    private TimeEntryService timeEntryService;

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<?> createTimeEntry(@RequestBody TimeEntryRequest request) {
        try {
            logger.info("Received time entry request: {}", request);

            // Use the new service method to convert request to entity
            TimeEntry timeEntry = timeEntryService.createTimeEntryFromRequest(request);

            // Save the time entry
            TimeEntry savedEntry = timeEntryService.saveTimeEntry(timeEntry);

            logger.info("Successfully saved time entry with ID: {}", savedEntry.getId());

            // Convert to DTO for response
            TimeEntryDto responseDto = TimeEntryDto.builder()
                    .id(savedEntry.getId())
                    .taskId(savedEntry.getTask().getId())
                    .userId(savedEntry.getUser().getId())
                    .startTime(savedEntry.getStartTime())
                    .endTime(savedEntry.getEndTime())
                    .description(savedEntry.getDescription())
                    .durationInHours(savedEntry.getDurationInHours())
                    .running(savedEntry.isRunning())
                    .build();

            return ResponseEntity.ok(responseDto);
        } catch (IllegalArgumentException e) {
            logger.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            logger.error("Security error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating time entry", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create time entry: " + e.getMessage());
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