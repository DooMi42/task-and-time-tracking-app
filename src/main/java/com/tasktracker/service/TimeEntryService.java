package com.tasktracker.service;

import com.tasktracker.dto.TimeEntryDto;
import com.tasktracker.model.TimeEntry;

import java.time.LocalDate;
import java.util.List;

public interface TimeEntryService {
    TimeEntryDto startTimer(Long taskId);

    TimeEntryDto stopTimer(Long timeEntryId);

    TimeEntryDto getTimeEntryById(Long id);

    List<TimeEntryDto> getTimeEntriesByTask(Long taskId);

    List<TimeEntryDto> getTimeEntriesByCurrentUser();

    List<TimeEntryDto> getTimeEntriesForToday();

    List<TimeEntryDto> getTimeEntriesForWeek(LocalDate weekStartDate);

    TimeEntryDto updateTimeEntry(Long id, TimeEntryDto timeEntryDto);

    void deleteTimeEntry(Long id);

    TimeEntryDto createTimeEntry(TimeEntryDto timeEntryDto);

    List<TimeEntryDto> getAllTimeEntries();

    List<TimeEntry> getTimeEntriesByUsername(String username);

    List<TimeEntry> getTimeEntriesByTaskId(Long taskId);
}