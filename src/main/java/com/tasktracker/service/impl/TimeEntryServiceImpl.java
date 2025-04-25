package com.tasktracker.service.impl;

import com.tasktracker.dto.TimeEntryDto;
import com.tasktracker.model.Task;
import com.tasktracker.model.TimeEntry;
import com.tasktracker.model.User;
import com.tasktracker.repository.TaskRepository;
import com.tasktracker.repository.TimeEntryRepository;
import com.tasktracker.service.TimeEntryService;
import com.tasktracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeEntryServiceImpl implements TimeEntryService {

    private final TimeEntryRepository timeEntryRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;

    @Override
    @Transactional
    public TimeEntryDto startTimer(Long taskId) {
        User currentUser = userService.getCurrentUser();

        // Check if there's already a running timer
        Optional<TimeEntry> runningEntry = timeEntryRepository.findRunningTimeEntry(currentUser);
        if (runningEntry.isPresent()) {
            throw new IllegalStateException(
                    "There is already a running timer. Please stop it before starting a new one.");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("You don't have permission to create time entries for this task");
        }

        TimeEntry timeEntry = TimeEntry.builder()
                .task(task)
                .user(currentUser)
                .startTime(LocalDateTime.now())
                .build();

        TimeEntry savedEntry = timeEntryRepository.save(timeEntry);
        return mapToDto(savedEntry);
    }

    @Override
    @Transactional
    public TimeEntryDto stopTimer(Long timeEntryId) {
        TimeEntry timeEntry = findTimeEntryById(timeEntryId);
        validateUserOwnsTimeEntry(timeEntry);

        if (timeEntry.getEndTime() != null) {
            throw new IllegalStateException("This timer is already stopped");
        }

        timeEntry.setEndTime(LocalDateTime.now());
        TimeEntry savedEntry = timeEntryRepository.save(timeEntry);
        return mapToDto(savedEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public TimeEntryDto getTimeEntryById(Long id) {
        TimeEntry timeEntry = findTimeEntryById(id);
        validateUserOwnsTimeEntry(timeEntry);
        return mapToDto(timeEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeEntryDto> getTimeEntriesByTask(Long taskId) {
        User currentUser = userService.getCurrentUser();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        if (!task.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("You don't have permission to view time entries for this task");
        }

        return timeEntryRepository.findByTask(task).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeEntryDto> getTimeEntriesByCurrentUser() {
        User currentUser = userService.getCurrentUser();
        return timeEntryRepository.findByUser(currentUser).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeEntryDto> getTimeEntriesForToday() {
        User currentUser = userService.getCurrentUser();
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        return timeEntryRepository.findByUserAndStartTimeBetween(currentUser, startOfDay, endOfDay).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeEntryDto> getTimeEntriesForWeek(LocalDate weekStartDate) {
        User currentUser = userService.getCurrentUser();
        LocalDateTime startOfWeek = LocalDateTime.of(weekStartDate, LocalTime.MIN);
        LocalDateTime endOfWeek = LocalDateTime.of(weekStartDate.plusDays(6), LocalTime.MAX);

        return timeEntryRepository.findByUserAndStartTimeBetween(currentUser, startOfWeek, endOfWeek).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TimeEntryDto updateTimeEntry(Long id, TimeEntryDto timeEntryDto) {
        TimeEntry timeEntry = findTimeEntryById(id);
        validateUserOwnsTimeEntry(timeEntry);

        timeEntry.setStartTime(timeEntryDto.getStartTime());
        timeEntry.setEndTime(timeEntryDto.getEndTime());
        timeEntry.setDescription(timeEntryDto.getDescription());

        TimeEntry updatedEntry = timeEntryRepository.save(timeEntry);
        return mapToDto(updatedEntry);
    }

    @Override
    @Transactional
    public void deleteTimeEntry(Long id) {
        TimeEntry timeEntry = findTimeEntryById(id);
        validateUserOwnsTimeEntry(timeEntry);
        timeEntryRepository.delete(timeEntry);
    }

    private TimeEntry findTimeEntryById(Long id) {
        return timeEntryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time entry not found with id: " + id));
    }

    private void validateUserOwnsTimeEntry(TimeEntry timeEntry) {
        User currentUser = userService.getCurrentUser();
        if (!timeEntry.getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("You don't have permission to access this time entry");
        }
    }

    private TimeEntryDto mapToDto(TimeEntry timeEntry) {
        return TimeEntryDto.builder()
                .id(timeEntry.getId())
                .taskId(timeEntry.getTask().getId())
                .userId(timeEntry.getUser().getId())
                .startTime(timeEntry.getStartTime())
                .endTime(timeEntry.getEndTime())
                .description(timeEntry.getDescription())
                .durationInHours(timeEntry.getDurationInHours())
                .running(timeEntry.isRunning())
                .build();
    }

    @Override
    public TimeEntryDto createTimeEntry(TimeEntryDto timeEntryDto) {
        User currentUser = userService.getCurrentUser();
        Task task = taskRepository.findById(timeEntryDto.getTaskId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + timeEntryDto.getTaskId()));

        TimeEntry timeEntry = TimeEntry.builder()
                .task(task)
                .user(currentUser)
                .startTime(timeEntryDto.getStartTime())
                .endTime(timeEntryDto.getEndTime())
                .description(timeEntryDto.getDescription())
                .build();

        TimeEntry savedEntry = timeEntryRepository.save(timeEntry);
        return mapToDto(savedEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeEntryDto> getAllTimeEntries() {
        return timeEntryRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeEntry> getTimeEntriesByUsername(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTimeEntriesByUsername'");
    }

    @Override
    public List<TimeEntry> getTimeEntriesByTaskId(Long taskId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTimeEntriesByTaskId'");
    }
}