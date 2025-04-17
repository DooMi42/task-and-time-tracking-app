package com.tasktracker.repository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tasktracker.model.TimeEntry;
import com.tasktracker.model.Task;
import com.tasktracker.model.User;
import com.tasktracker.repository.TimeEntryRepository;
import com.tasktracker.service.impl.TimeEntryServiceImpl;

import java.time.LocalDateTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class TimeEntryRepositoryTest {

    @Mock
    private TimeEntryRepository timeEntryRepository;

    @InjectMocks
    private TimeEntryServiceImpl timeEntryService;

    private TimeEntry timeEntry;
    private Task task;
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1L);

        task = new Task();
        task.setId(1L);

        timeEntry = new TimeEntry();
        timeEntry.setId(1L);
        timeEntry.setTask(task);
        timeEntry.setUser(user);
        timeEntry.setStartTime(LocalDateTime.now());
        timeEntry.setEndTime(LocalDateTime.now().plusHours(1));
    }

    @Test
    public void testSaveTimeEntry() {
        when(timeEntryRepository.save(any(TimeEntry.class))).thenReturn(timeEntry);

        TimeEntry savedEntry = timeEntryRepository.save(timeEntry);

        assertNotNull(savedEntry);
        assertEquals(timeEntry.getId(), savedEntry.getId());
        verify(timeEntryRepository, times(1)).save(timeEntry);
    }

    @Test
    public void testFindTimeEntryById() {
        when(timeEntryRepository.findById(1L)).thenReturn(Optional.of(timeEntry));

        Optional<TimeEntry> foundEntry = timeEntryRepository.findById(1L);

        assertTrue(foundEntry.isPresent());
        assertEquals(timeEntry.getId(), foundEntry.get().getId());
        verify(timeEntryRepository, times(1)).findById(1L);
    }
}