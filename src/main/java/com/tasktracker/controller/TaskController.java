package com.tasktracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.tasktracker.dto.TaskDto;
import com.tasktracker.service.TaskService;
import com.tasktracker.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDto> createTask(@RequestBody TaskDto taskDto) {
        TaskDto createdTask = taskService.createTask(taskDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long id) {
        TaskDto task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    @GetMapping
    public ResponseEntity<?> getAllTasks() {
        try {
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // Add more debug info
            logger.debug("Authentication: {}, Principal: {}, Details: {}",
                    auth,
                    auth != null ? auth.getPrincipal() : "null",
                    auth != null ? auth.getDetails() : "null");

            if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                logger.error("Not properly authenticated: {}", auth);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "Not authenticated"));
            }

            String username = auth.getName();
            logger.info("Getting tasks for user: {}", username);

            // Try-catch specifically for the service call to get better error info
            try {
                // Get tasks for the current user
                List<Task> tasks = taskService.getTasksByUsername(username);

                if (tasks == null) {
                    logger.warn("TaskService returned null task list for user: {}", username);
                    return ResponseEntity.ok(Collections.emptyList());
                }

                // Log each task for debugging
                tasks.forEach(task -> logger.debug("Task: id={}, title={}", task.getId(), task.getTitle()));

                // Convert Tasks to TaskDtos
                List<TaskDto> taskDtos = tasks.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList());

                logger.info("Returning {} tasks for user {}", taskDtos.size(), username);
                return ResponseEntity.ok(taskDtos);
            } catch (Exception e) {
                logger.error("Error in TaskService.getTasksByUsername", e);
                throw e; // Re-throw to be caught by outer catch
            }
        } catch (Exception e) {
            logger.error("Error fetching tasks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Error fetching tasks: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long id, @RequestBody TaskDto taskDto) {
        TaskDto updatedTask = taskService.updateTask(id, taskDto);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Convert Task entity to TaskDto without using a mapper
     */
    private TaskDto convertToDto(Task task) {
        if (task == null) {
            return null;
        }

        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());

        if (task.getDueDate() != null) {
            dto.setDueDate(task.getDueDate().toString());
        }

        if (task.getUser() != null) {
            dto.setUserId(task.getUser().getId());
            dto.setUsername(task.getUser().getUsername());
        }

        return dto;
    }
}