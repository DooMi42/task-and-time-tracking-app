package com.tasktracker.service.impl;

import com.tasktracker.dto.TaskDto;
import com.tasktracker.model.Task;
import com.tasktracker.model.Task.TaskStatus;
import com.tasktracker.model.User;
import com.tasktracker.repository.TaskRepository;
import com.tasktracker.repository.UserRepository;
import com.tasktracker.service.TaskService;
import com.tasktracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

    @Override
    @Transactional
    public TaskDto createTask(TaskDto taskDto) {
        User currentUser = userService.getCurrentUser();

        Task task = Task.builder()
                .title(taskDto.getTitle())
                .description(taskDto.getDescription())
                .status(taskDto.getStatus() != null ? taskDto.getStatus() : TaskStatus.TODO)
                .priority(taskDto.getPriority())
                .dueDate(taskDto.getDueDate())
                .estimatedHours(taskDto.getEstimatedHours())
                .user(currentUser)
                .build();

        Task savedTask = taskRepository.save(task);
        return mapToDto(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDto getTaskById(Long id) {
        Task task = findTaskById(id);
        return mapToDto(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getTasksByCurrentUser() {
        User currentUser = userService.getCurrentUser();
        return taskRepository.findByUser(currentUser).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getTasksByStatus(TaskStatus status) {
        User currentUser = userService.getCurrentUser();
        return taskRepository.findByUserAndStatus(currentUser, status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getTasksDueThisWeek() {
        User currentUser = userService.getCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDate endOfWeek = today.plusDays(7);

        return taskRepository.findTasksByUserAndDueDateBetween(currentUser, today, endOfWeek).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getTasksOverdue() {
        User currentUser = userService.getCurrentUser();
        LocalDate today = LocalDate.now();

        return taskRepository.findByUserAndDueDateBefore(currentUser, today).stream()
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskDto updateTask(Long id, TaskDto taskDto) {
        try {
            Task task = findTaskById(id);
            validateUserOwnsTask(task);

            task.setTitle(taskDto.getTitle());
            task.setDescription(taskDto.getDescription());
            task.setStatus(taskDto.getStatus());
            task.setPriority(taskDto.getPriority());
            task.setDueDate(taskDto.getDueDate());
            task.setEstimatedHours(taskDto.getEstimatedHours());

            // Explicitly initialize the time entries collection before updating
            if (task.getTimeEntries() != null) {
                task.getTimeEntries().size(); // This will initialize the collection
            }

            Task updatedTask = taskRepository.save(task);
            return mapToDto(updatedTask);
        } catch (Exception e) {
            logger.error("Error updating task with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));

        validateUserOwnsTask(task);

        // First, handle time entries to avoid FK constraint violations
        if (task.getTimeEntries() != null) {
            // Either clear the collection (if using CascadeType.ALL, orphanRemoval=true)
            task.getTimeEntries().clear();

            // Or delete each time entry explicitly
            // timeEntryRepository.deleteAll(task.getTimeEntries());
        }

        // Then delete the task
        taskRepository.delete(task);
        logger.info("Successfully deleted task with ID: {}", id);
    }

    @Override
    @Transactional
    public Task saveTask(Task task) {
        // Simplified task saving
        try {
            if (task.getCreatedAt() == null) {
                task.setCreatedAt(LocalDateTime.now());
            }

            // Ensure status isn't null
            if (task.getStatus() == null) {
                task.setStatus(TaskStatus.TODO);
            }

            System.out.println("Saving task: " + task.getTitle() + " for user: " +
                    (task.getUser() != null ? task.getUser().getUsername() : "unknown"));

            return taskRepository.save(task);
        } catch (Exception e) {
            System.err.println("Error saving task: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to let the controller handle it
        }
    }

    @Override
    public Task createTask(Task task) {
        // Use the saveTask method for consistency
        return saveTask(task);
    }

    private Task findTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + id));
    }

    private void validateUserOwnsTask(Task task) {
        User currentUser = userService.getCurrentUser();
        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("You don't have permission to access this task");
        }
    }

    private TaskDto mapToDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueDate())
                .estimatedHours(task.getEstimatedHours())
                .totalSpentHours(task.getTotalSpentHours())
                .userId(task.getUser().getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> getTasksByUsername(String username) {
        try {
            logger.debug("Finding tasks for username: {}", username);

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            logger.debug("Found user: id={}, username={}", user.getId(), user.getUsername());

            List<Task> tasks = taskRepository.findByUser(user);
            logger.debug("Found {} tasks for user {}", tasks.size(), username);

            // Initialize any lazy collections if needed
            for (Task task : tasks) {
                if (task.getTimeEntries() != null) {
                    try {
                        // Use size() to initialize but catch any exceptions
                        int size = task.getTimeEntries().size();
                        logger.debug("Task {} has {} time entries", task.getId(), size);
                    } catch (Exception e) {
                        logger.warn("Could not initialize time entries for task {}: {}", task.getId(), e.getMessage());
                        // Just continue, don't fail the whole operation
                    }
                }
            }

            return tasks;
        } catch (Exception e) {
            logger.error("Error fetching tasks for username: " + username, e);
            throw new RuntimeException("Error fetching tasks: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getTaskDtosByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<Task> tasks = taskRepository.findByUser(user);
        return tasks.stream().map(this::mapToDto).collect(Collectors.toList());
    }
}