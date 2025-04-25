package com.tasktracker.controller;

import com.tasktracker.dto.TimeEntryDto;
import com.tasktracker.dto.TimeEntryRequest;
import com.tasktracker.model.Task;
import com.tasktracker.model.TimeEntry;
import com.tasktracker.model.User;
import com.tasktracker.service.TaskService;
import com.tasktracker.service.TimeEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TimeEntryControllerTest {

    @InjectMocks
    private TimeEntryController timeEntryController;

    @Mock
    private TimeEntryService timeEntryService;

    @Mock
    private TaskService taskService;

    private TimeEntryRequest request;
    private TimeEntry timeEntry;
    private TimeEntryDto timeEntryDto;
    private Task task;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create common test objects
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setUser(user);

        // Request object
        request = new TimeEntryRequest();
        request.setTaskId(1L);
        request.setDescription("Test time entry");
        request.setStartTime("2023-01-01T09:00:00");
        request.setEndTime("2023-01-01T17:00:00");

        // Entity
        timeEntry = new TimeEntry();
        timeEntry.setId(1L);
        timeEntry.setTask(task);
        timeEntry.setUser(user);
        timeEntry.setDescription("Test time entry");
        timeEntry.setStartTime(LocalDateTime.parse("2023-01-01T09:00:00"));
        timeEntry.setEndTime(LocalDateTime.parse("2023-01-01T17:00:00"));

        // DTO
        timeEntryDto = TimeEntryDto.builder()
                .id(1L)
                .taskId(1L)
                .userId(1L)
                .description("Test time entry")
                .startTime(LocalDateTime.parse("2023-01-01T09:00:00"))
                .endTime(LocalDateTime.parse("2023-01-01T17:00:00"))
                .durationInHours(8.0)
                .running(false)
                .build();
    }

    @Test
    void testCreateTimeEntry() {
        // Mock the service methods
        when(timeEntryService.createTimeEntryFromRequest(any(TimeEntryRequest.class))).thenReturn(timeEntry);
        when(timeEntryService.saveTimeEntry(any(TimeEntry.class))).thenReturn(timeEntry);

        // Call the controller method
        ResponseEntity<?> response = timeEntryController.createTimeEntry(request);

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof TimeEntryDto);

        // Verify the service methods were called
        verify(timeEntryService).createTimeEntryFromRequest(any(TimeEntryRequest.class));
        verify(timeEntryService).saveTimeEntry(any(TimeEntry.class));
    }

    @Test
    void testGetTimeEntryById() {
        // Mock the service
        when(timeEntryService.getTimeEntryDtoById(1L)).thenReturn(timeEntryDto);

        // Call the method
        ResponseEntity<?> response = timeEntryController.getTimeEntryById(1L);

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(timeEntryDto, response.getBody());
        verify(timeEntryService).getTimeEntryDtoById(1L);
    }

    @Test
    void testGetAllTimeEntries() {
        // Create list of time entries
        List<TimeEntryDto> dtos = Arrays.asList(timeEntryDto, timeEntryDto);

        // Mock the service
        when(timeEntryService.getAllTimeEntriesDto()).thenReturn(dtos);

        // Call the method
        ResponseEntity<?> response = timeEntryController.getAllTimeEntries();

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dtos, response.getBody());
        assertEquals(2, ((List<TimeEntryDto>) response.getBody()).size());
        verify(timeEntryService).getAllTimeEntriesDto();
    }

    @Test
    void testGetTimeEntriesByTask() {
        // Create list of time entries for a task
        List<TimeEntryDto> dtos = Arrays.asList(timeEntryDto);

        // Mock the service
        when(timeEntryService.getTimeEntriesByTask(1L)).thenReturn(dtos);

        // Call the method
        ResponseEntity<?> response = timeEntryController.getTimeEntriesByTask(1L);

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dtos, response.getBody());
        verify(timeEntryService).getTimeEntriesByTask(1L);
    }

    @Test
    void testUpdateTimeEntry() {
        // Mock the service
        when(timeEntryService.updateTimeEntry(eq(1L), any(TimeEntryDto.class))).thenReturn(timeEntryDto);

        // Call the method
        ResponseEntity<?> response = timeEntryController.updateTimeEntry(1L, timeEntryDto);

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(timeEntryDto, response.getBody());
        verify(timeEntryService).updateTimeEntry(eq(1L), any(TimeEntryDto.class));
    }

    @Test
    void testDeleteTimeEntry() {
        // Call the method
        ResponseEntity<?> response = timeEntryController.deleteTimeEntry(1L);

        // Verify response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(timeEntryService).deleteTimeEntry(1L);
    }
}