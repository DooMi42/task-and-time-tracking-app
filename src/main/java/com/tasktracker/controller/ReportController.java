package com.tasktracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tasktracker.service.ReportService;
import com.tasktracker.dto.ReportDto;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/generate")
    public ResponseEntity<ReportDto> generateReport(@RequestParam String userId, @RequestParam String startDate,
            @RequestParam String endDate) {
        ReportDto report = reportService.generateReport(userId, startDate, endDate);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/summary")
    public ResponseEntity<ReportDto> getSummary(@RequestParam String userId) {
        ReportDto summary = reportService.getSummary(userId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/user")
    public ResponseEntity<List<ReportDto>> generateUserReport(@RequestParam Long userId) {
        List<ReportDto> report = reportService.generateUserReport(userId);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/task")
    public ResponseEntity<List<ReportDto>> generateTaskReport(@RequestParam Long taskId) {
        List<ReportDto> report = reportService.generateTaskReport(taskId);
        return ResponseEntity.ok(report);
    }
}