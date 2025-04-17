package com.tasktracker.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tasktracker.model.Task;
import com.tasktracker.repository.TaskRepository;
import com.tasktracker.service.TaskService;
import com.tasktracker.dto.TaskDto;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TaskRepositoryTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private TaskDto taskDto;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("This is a test task.");

        taskDto = new TaskDto();
        taskDto.setId(1L);
        taskDto.setTitle("Test Task");
        taskDto.setDescription("This is a test task.");
    }

    @Test
    public void testFindAll() {
        when(taskRepository.findAll()).thenReturn(Arrays.asList(task));

        List<TaskDto> tasks = taskService.getAllTasks();
        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getTitle());
    }

    @Test
    public void testFindById() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskDto foundTask = taskService.getTaskById(1L);
        assertNotNull(foundTask);
        assertEquals("Test Task", foundTask.getTitle());
    }

    @Test
    public void testSave() {
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskDto savedTask = taskService.createTask(taskDto);
        assertNotNull(savedTask);
        assertEquals("Test Task", savedTask.getTitle());
    }

    @Test
    public void testDelete() {
        doNothing().when(taskRepository).deleteById(1L);

        assertDoesNotThrow(() -> taskService.deleteTask(1L));
        verify(taskRepository, times(1)).deleteById(1L);
    }
}