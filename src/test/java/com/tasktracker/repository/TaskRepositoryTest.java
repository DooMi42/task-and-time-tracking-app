package com.tasktracker.repository;

import com.tasktracker.dto.TaskDto;
import com.tasktracker.model.Task;
import com.tasktracker.model.User;
import com.tasktracker.service.UserService;
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

public class TaskRepositoryTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskServiceImpl taskService; // Use implementation, not the interface

    private User testUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create a test user that will be used for all tasks
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");

        // Mock UserService to return our test user
        when(userService.getCurrentUser()).thenReturn(testUser);
    }

    @Test
    public void testFindAll() {
        // Create tasks WITH users
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setUser(testUser); // Set the user!

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setUser(testUser); // Set the user!

        when(taskRepository.findAll()).thenReturn(Arrays.asList(task1, task2));

        List<TaskDto> tasks = taskService.getAllTasks();

        assertEquals(2, tasks.size());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    public void testFindById() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setUser(testUser); // Set the user!

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskDto foundTask = taskService.getTaskById(1L);

        assertNotNull(foundTask);
        assertEquals(1L, foundTask.getId());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    public void testSave() {
        TaskDto taskDto = new TaskDto();
        taskDto.setTitle("New Task");
        taskDto.setDescription("Task Description");

        Task savedTask = new Task();
        savedTask.setId(1L);
        savedTask.setTitle("New Task");
        savedTask.setDescription("Task Description");
        savedTask.setUser(testUser); // Set the user!

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskDto createdTask = taskService.createTask(taskDto);

        assertNotNull(createdTask);
        assertEquals("New Task", createdTask.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    public void testDelete() {
        Task task = new Task();
        task.setId(1L);
        task.setUser(testUser); // Set the user!

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertDoesNotThrow(() -> {
            taskService.deleteTask(1L);
        });

        verify(taskRepository, times(1)).delete(task);
    }
}