package com.tasktracker.controller;

import com.tasktracker.dto.TimeEntryDto;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class TimeEntryControllerTest {

    @InjectMocks
    private TimeEntryController timeEntryController;

    @Mock
    private TimeEntryService timeEntryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTimeEntry() {
        TimeEntryDto timeEntryDto = new TimeEntryDto();
        timeEntryDto.setTaskId(1L);
        timeEntryDto.setStartTime(LocalDateTime.now());

        when(timeEntryService.createTimeEntry(any(TimeEntryDto.class))).thenReturn(timeEntryDto);

        ResponseEntity<TimeEntryDto> response = timeEntryController.createTimeEntry(timeEntryDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(timeEntryDto, response.getBody());
        verify(timeEntryService).createTimeEntry(any(TimeEntryDto.class));
    }

    @Test
    void testGetTimeEntryById() {
        TimeEntryDto timeEntryDto = new TimeEntryDto();
        timeEntryDto.setId(1L);

        when(timeEntryService.getTimeEntryById(anyLong())).thenReturn(timeEntryDto);

        ResponseEntity<TimeEntryDto> response = timeEntryController.getTimeEntryById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(timeEntryDto, response.getBody());
    }

    @Test
    void testGetAllTimeEntries() {
        List<TimeEntryDto> entries = Arrays.asList(new TimeEntryDto(), new TimeEntryDto());
        when(timeEntryService.getAllTimeEntries()).thenReturn(entries);

        ResponseEntity<List<TimeEntryDto>> response = timeEntryController.getAllTimeEntries();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
    }
}