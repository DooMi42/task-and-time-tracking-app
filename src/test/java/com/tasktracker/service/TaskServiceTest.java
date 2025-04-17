package com.tasktracker.service;

import com.tasktracker.dto.TaskDto;
import com.tasktracker.model.Task;
import com.tasktracker.model.Task.TaskStatus;
import com.tasktracker.model.Task.TaskPriority;
import com.tasktracker.model.User;
import com.tasktracker.repository.TaskRepository;
import com.tasktracker.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskServiceImpl taskService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateTask() {
        // Create a TaskDto
        TaskDto taskDto = new TaskDto();
        taskDto.setTitle("Test Task");
        taskDto.setDescription("Test Description");

        // Create a Task for the mock response
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");

        User user = new User();
        user.setId(1L);
        task.setUser(user);

        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskDto createdTaskDto = taskService.createTask(taskDto);

        assertNotNull(createdTaskDto);
        assertEquals("Test Task", createdTaskDto.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    public void testGetAllTasks() {
        // Create a user first
        User user = new User();
        user.setId(1L);

        // Set the user for each task
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setUser(user); // Set the user

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setUser(user); // Set the user

        when(taskRepository.findAll()).thenReturn(Arrays.asList(task1, task2));

        List<TaskDto> taskDtos = taskService.getAllTasks();

        assertEquals(2, taskDtos.size());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    public void testGetTaskById() {
        // Create and set up a user
        User user = new User();
        user.setId(1L);

        // Create and set up a task with the user
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setUser(user); // Set the user on the task

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskDto result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(taskRepository).findById(1L);
    }

    @Test
    public void testUpdateTask() {
        // Create and set up a user
        User user = new User();
        user.setId(1L);

        // Create task with user
        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setUser(user);

        // Create DTO for update
        TaskDto updateDto = new TaskDto();
        updateDto.setId(1L);
        updateDto.setTitle("Updated Title");
        updateDto.setDescription("Updated Description");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(existingTask);
        when(userService.getCurrentUser()).thenReturn(user);

        TaskDto result = taskService.updateTask(1L, updateDto);

        assertNotNull(result);
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    public void testDeleteTask() {
        // Create and set up a user
        User user = new User();
        user.setId(1L);

        // Create task with user
        Task task = new Task();
        task.setId(1L);
        task.setUser(user);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userService.getCurrentUser()).thenReturn(user);

        taskService.deleteTask(1L);

        verify(taskRepository).findById(1L);
        verify(taskRepository).delete(task);
    }
}