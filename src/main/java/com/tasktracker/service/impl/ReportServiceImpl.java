package com.tasktracker.service.impl;

import com.tasktracker.dto.ReportDto;
import com.tasktracker.dto.TaskDto;
import com.tasktracker.exception.ResourceNotFoundException;
import com.tasktracker.model.Task;
import com.tasktracker.repository.TaskRepository;
import com.tasktracker.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public List<ReportDto> generateUserReport(Long userId) {
        List<Task> tasks = taskRepository.findByUserId(userId);
        if (tasks.isEmpty()) {
            throw new ResourceNotFoundException("No tasks found for user with ID: " + userId);
        }

        ReportDto report = new ReportDto();
        report.setUsername(tasks.get(0).getUser().getUsername());
        report.setStartDate(LocalDateTime.now().minusDays(30));
        report.setEndDate(LocalDateTime.now());
        report.setTotalTasks(tasks.size());

        // Calculate total time spent in minutes
        long totalTimeSpent = tasks.stream()
                .flatMap(task -> task.getTimeEntries().stream())
                .mapToLong(entry -> entry.getDurationInHours().longValue() * 60)
                .sum();

        report.setTotalTimeSpent(totalTimeSpent);

        return List.of(report);
    }

    @Override
    public List<ReportDto> generateTaskReport(Long taskId) {
        // Implementation for task report
        return new ArrayList<>();
    }

    @Override
    public List<ReportDto> generateTimeEntryReport(Long timeEntryId) {
        // Implementation for time entry report
        return new ArrayList<>();
    }

    @Override
    public List<ReportDto> generateProjectReport(Long projectId) {
        // Implementation for project report
        return new ArrayList<>();
    }

    @Override
    public ReportDto generateReport(String userId, String startDate, String endDate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateReport'");
    }

    @Override
    public ReportDto getSummary(String userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSummary'");
    }
}