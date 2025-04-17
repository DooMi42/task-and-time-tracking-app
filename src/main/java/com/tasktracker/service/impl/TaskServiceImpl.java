package com.tasktracker.service.impl;

import com.tasktracker.dto.TaskDto;
import com.tasktracker.model.Task;
import com.tasktracker.model.Task.TaskStatus;
import com.tasktracker.model.User;
import com.tasktracker.repository.TaskRepository;
import com.tasktracker.service.TaskService;
import com.tasktracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserService userService;

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
        Task task = findTaskById(id);
        validateUserOwnsTask(task);

        task.setTitle(taskDto.getTitle());
        task.setDescription(taskDto.getDescription());
        task.setStatus(taskDto.getStatus());
        task.setPriority(taskDto.getPriority());
        task.setDueDate(taskDto.getDueDate());
        task.setEstimatedHours(taskDto.getEstimatedHours());

        Task updatedTask = taskRepository.save(task);
        return mapToDto(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        Task task = findTaskById(id);
        validateUserOwnsTask(task);
        taskRepository.delete(task);
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
}