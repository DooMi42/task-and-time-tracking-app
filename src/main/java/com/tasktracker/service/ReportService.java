package com.tasktracker.service;

import com.tasktracker.dto.ReportDto;

import java.util.List;

public interface ReportService {
    List<ReportDto> generateUserReport(Long userId);

    List<ReportDto> generateTaskReport(Long taskId);

    List<ReportDto> generateTimeEntryReport(Long timeEntryId);

    List<ReportDto> generateProjectReport(Long projectId);

    ReportDto generateReport(String userId, String startDate, String endDate);

    ReportDto getSummary(String userId);
}