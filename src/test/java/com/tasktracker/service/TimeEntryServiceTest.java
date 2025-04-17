package com.tasktracker.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tasktracker.dto.TimeEntryDto;
import com.tasktracker.model.Task;
import com.tasktracker.model.TimeEntry;
import com.tasktracker.model.User;
import com.tasktracker.repository.TimeEntryRepository;
import com.tasktracker.repository.TaskRepository;
import com.tasktracker.service.impl.TimeEntryServiceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TimeEntryServiceTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TimeEntryServiceImpl timeEntryService;

    private TimeEntryDto timeEntryDto;
    private TimeEntry timeEntry;
    private Task task;
    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setUser(user);

        timeEntry = new TimeEntry();
        timeEntry.setId(1L);
        timeEntry.setTask(task);
        timeEntry.setUser(user);
        timeEntry.setStartTime(LocalDateTime.now());
        timeEntry.setEndTime(LocalDateTime.now().plusHours(1));

        timeEntryDto = new TimeEntryDto();
        timeEntryDto.setId(1L);
        timeEntryDto.setTaskId(1L);
        timeEntryDto.setUserId(1L);
        timeEntryDto.setStartTime(LocalDateTime.now());
        timeEntryDto.setEndTime(LocalDateTime.now().plusHours(1));
    }

    @Test
    public void testCreateTimeEntry() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.of(task));
        when(userService.getCurrentUser()).thenReturn(user);
        when(timeEntryRepository.save(any(TimeEntry.class))).thenReturn(timeEntry);

        TimeEntryDto result = timeEntryService.createTimeEntry(timeEntryDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(timeEntryRepository).save(any(TimeEntry.class));
    }

    @Test
    public void testGetTimeEntryById() {
        when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(timeEntry));
        when(userService.getCurrentUser()).thenReturn(user);

        TimeEntryDto result = timeEntryService.getTimeEntryById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void testGetAllTimeEntries() {
        when(timeEntryRepository.findAll()).thenReturn(Arrays.asList(timeEntry));

        List<TimeEntryDto> results = timeEntryService.getAllTimeEntries();

        assertEquals(1, results.size());
    }
}