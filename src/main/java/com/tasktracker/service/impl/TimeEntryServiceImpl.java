package com.tasktracker.service.impl;

import com.tasktracker.dto.TimeEntryDto;
import com.tasktracker.dto.TimeEntryRequest;
import com.tasktracker.model.Task;
import com.tasktracker.model.TimeEntry;
import com.tasktracker.model.User;
import com.tasktracker.repository.TaskRepository;
import com.tasktracker.repository.TimeEntryRepository;
import com.tasktracker.repository.UserRepository;
import com.tasktracker.service.TimeEntryService;
import com.tasktracker.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeEntryServiceImpl implements TimeEntryService {
    private static final Logger logger = LoggerFactory.getLogger(TimeEntryServiceImpl.class);

    private final TimeEntryRepository timeEntryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    @Transactional
    public TimeEntry saveTimeEntry(TimeEntry timeEntry) {
        // Ensure user is set if it's null
        if (timeEntry.getUser() == null) {
            User currentUser = userService.getCurrentUser();
            timeEntry.setUser(currentUser);
        }

        logger.debug("Saving time entry: {}", timeEntry);
        return timeEntryRepository.save(timeEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public TimeEntry getTimeEntryById(Long id) {
        return timeEntryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Time entry not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public TimeEntryDto getTimeEntryDtoById(Long id) {
        TimeEntry timeEntry = getTimeEntryById(id);
        validateUserOwnsTimeEntry(timeEntry);
        return mapToDto(timeEntry);
    }

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
    public List<TimeEntryDto> getAllTimeEntriesDto() {
        return timeEntryRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeEntry> getAllTimeEntries() {
        return timeEntryRepository.findAll();
    }

    @Override
    public List<TimeEntry> getTimeEntriesByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return timeEntryRepository.findByUser(user);
    }

    @Override
    public List<TimeEntry> getTimeEntriesByTaskId(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));
        return timeEntryRepository.findByTask(task);
    }

    @Override
    @Transactional
    public TimeEntry createTimeEntryFromRequest(TimeEntryRequest request) {
        if (request == null || request.getTaskId() == null) {
            throw new IllegalArgumentException("Invalid request: missing task ID");
        }

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + request.getTaskId()));

        User currentUser = userService.getCurrentUser();

        TimeEntry timeEntry = new TimeEntry();
        timeEntry.setUser(currentUser);
        timeEntry.setDescription(request.getDescription());

        // Associate the time entry with the task using the helper method
        task.addTimeEntry(timeEntry);

        try {
            LocalDateTime startTime = parseDateTime(request.getStartTime());
            LocalDateTime endTime = parseDateTime(request.getEndTime());

            // Validate times
            if (startTime == null && endTime == null) {
                throw new IllegalArgumentException("At least one of start time or end time must be provided");
            }

            if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
                throw new IllegalArgumentException("End time cannot be before start time");
            }

            timeEntry.setStartTime(startTime);
            timeEntry.setEndTime(endTime);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing date/time: " + e.getMessage(), e);
        }

        TimeEntry saved = timeEntryRepository.save(timeEntry);
        return saved;
    }

    private TimeEntryDto mapToDto(TimeEntry timeEntry) {
        return TimeEntryDto.builder()
                .id(timeEntry.getId())
                .taskId(timeEntry.getTask().getId())
                .userId(timeEntry.getUser() != null ? timeEntry.getUser().getId() : null)
                .startTime(timeEntry.getStartTime())
                .endTime(timeEntry.getEndTime())
                .description(timeEntry.getDescription())
                .durationInHours(timeEntry.getDurationInHours())
                .running(timeEntry.isRunning())
                .build();
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }

        try {
            // Try ISO format first (yyyy-MM-ddTHH:mm:ss)
            return LocalDateTime.parse(dateTimeStr);
        } catch (DateTimeParseException e) {
            try {
                // Try ISO format without seconds (yyyy-MM-ddTHH:mm)
                if (dateTimeStr.contains("T") && dateTimeStr.split("T")[1].length() == 5) {
                    return LocalDateTime.parse(dateTimeStr + ":00");
                }

                // Try format with space separator (yyyy-MM-dd HH:mm)
                if (dateTimeStr.contains(" ")) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    return LocalDateTime.parse(dateTimeStr, formatter);
                }

                // Try date-only format (yyyy-MM-dd)
                if (!dateTimeStr.contains("T") && !dateTimeStr.contains(" ")) {
                    return LocalDateTime.parse(dateTimeStr + "T00:00:00");
                }

                logger.error("Unable to parse datetime string: {}", dateTimeStr);
                throw new IllegalArgumentException("Unsupported datetime format: " + dateTimeStr);
            } catch (Exception ex) {
                logger.error("Error parsing datetime: {}", ex.getMessage());
                throw new IllegalArgumentException("Failed to parse datetime: " + dateTimeStr);
            }
        }
    }
}