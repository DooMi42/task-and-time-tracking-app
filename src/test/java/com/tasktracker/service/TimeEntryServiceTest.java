package com.tasktracker.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.tasktracker.dto.TimeEntryRequest;
import com.tasktracker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TimeEntryServiceImpl timeEntryService;

    private TimeEntryDto timeEntryDto;
    private TimeEntry timeEntry;
    private Task task;
    private User user;
    private TimeEntryRequest timeEntryRequest;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Set up user
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        // Set up task
        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setUser(user);

        // Set up time entry
        timeEntry = new TimeEntry();
        timeEntry.setId(1L);
        timeEntry.setTask(task);
        timeEntry.setUser(user);
        timeEntry.setDescription("Test entry");
        timeEntry.setStartTime(LocalDateTime.now());
        timeEntry.setEndTime(LocalDateTime.now().plusHours(1));

        // Set up time entry DTO
        timeEntryDto = TimeEntryDto.builder()
                .id(1L)
                .taskId(1L)
                .userId(1L)
                .description("Test entry")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(1))
                .durationInHours(1.0)
                .running(false)
                .build();

        // Set up time entry request
        timeEntryRequest = new TimeEntryRequest();
        timeEntryRequest.setTaskId(1L);
        timeEntryRequest.setDescription("Test entry");
        timeEntryRequest.setStartTime(LocalDateTime.now().toString());
        timeEntryRequest.setEndTime(LocalDateTime.now().plusHours(1).toString());
    }

    @Test
    public void testSaveTimeEntry() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(timeEntryRepository.save(any(TimeEntry.class))).thenReturn(timeEntry);

        TimeEntry result = timeEntryService.saveTimeEntry(timeEntry);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(timeEntryRepository).save(timeEntry);
    }

    @Test
    public void testGetTimeEntryById() {
        when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(timeEntry));

        TimeEntry result = timeEntryService.getTimeEntryById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(timeEntryRepository).findById(1L);
    }

    @Test
    public void testGetTimeEntryDtoById() {
        when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(timeEntry));
        when(userService.getCurrentUser()).thenReturn(user);

        TimeEntryDto result = timeEntryService.getTimeEntryDtoById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getTaskId());
        verify(timeEntryRepository).findById(1L);
    }

    @Test
    public void testGetAllTimeEntries() {
        when(timeEntryRepository.findAll()).thenReturn(Arrays.asList(timeEntry));

        List<TimeEntry> results = timeEntryService.getAllTimeEntries();

        assertEquals(1, results.size());
        verify(timeEntryRepository).findAll();
    }

    @Test
    public void testGetAllTimeEntriesDto() {
        when(timeEntryRepository.findAll()).thenReturn(Arrays.asList(timeEntry));

        List<TimeEntryDto> results = timeEntryService.getAllTimeEntriesDto();

        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).getId());
        verify(timeEntryRepository).findAll();
    }

    @Test
    public void testGetTimeEntriesByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(timeEntryRepository.findByUser(user)).thenReturn(Arrays.asList(timeEntry));

        List<TimeEntry> results = timeEntryService.getTimeEntriesByUsername("testuser");

        assertEquals(1, results.size());
        verify(userRepository).findByUsername("testuser");
        verify(timeEntryRepository).findByUser(user);
    }

    @Test
    public void testGetTimeEntriesByTaskId() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(timeEntryRepository.findByTask(task)).thenReturn(Arrays.asList(timeEntry));

        List<TimeEntry> results = timeEntryService.getTimeEntriesByTaskId(1L);

        assertEquals(1, results.size());
        verify(taskRepository).findById(1L);
        verify(timeEntryRepository).findByTask(task);
    }

    @Test
    public void testCreateTimeEntryFromRequest() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userService.getCurrentUser()).thenReturn(user);

        TimeEntry result = timeEntryService.createTimeEntryFromRequest(timeEntryRequest);

        assertNotNull(result);
        assertEquals("Test entry", result.getDescription());
        assertEquals(task, result.getTask());
        assertEquals(user, result.getUser());
        verify(taskRepository).findById(1L);
        verify(userService).getCurrentUser();
    }

    @Test
    public void testCreateTimeEntryFromRequest_TaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            timeEntryService.createTimeEntryFromRequest(timeEntryRequest);
        });

        verify(taskRepository).findById(1L);
    }

    @Test
    public void testUpdateTimeEntry() {
        TimeEntryDto updateDto = TimeEntryDto.builder()
                .description("Updated entry")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .build();

        when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(timeEntry));
        when(userService.getCurrentUser()).thenReturn(user);
        when(timeEntryRepository.save(any(TimeEntry.class))).thenReturn(timeEntry);

        TimeEntryDto result = timeEntryService.updateTimeEntry(1L, updateDto);

        assertNotNull(result);
        verify(timeEntryRepository).findById(1L);
        verify(timeEntryRepository).save(any(TimeEntry.class));
    }
}