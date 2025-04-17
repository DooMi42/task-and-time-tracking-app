package com.tasktracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tasktracker.dto.TimeEntryDto;
import com.tasktracker.service.TimeEntryService;

import java.util.List;

@RestController
@RequestMapping("/api/time-entries")
public class TimeEntryController {

    @Autowired
    private TimeEntryService timeEntryService;

    @PostMapping
    public ResponseEntity<TimeEntryDto> createTimeEntry(@RequestBody TimeEntryDto timeEntryDto) {
        TimeEntryDto createdTimeEntry = timeEntryService.createTimeEntry(timeEntryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTimeEntry);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeEntryDto> getTimeEntryById(@PathVariable Long id) {
        TimeEntryDto timeEntryDto = timeEntryService.getTimeEntryById(id);
        return ResponseEntity.ok(timeEntryDto);
    }

    @GetMapping
    public ResponseEntity<List<TimeEntryDto>> getAllTimeEntries() {
        List<TimeEntryDto> timeEntries = timeEntryService.getAllTimeEntries();
        return ResponseEntity.ok(timeEntries);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeEntryDto> updateTimeEntry(@PathVariable Long id, @RequestBody TimeEntryDto timeEntryDto) {
        TimeEntryDto updatedTimeEntry = timeEntryService.updateTimeEntry(id, timeEntryDto);
        return ResponseEntity.ok(updatedTimeEntry);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTimeEntry(@PathVariable Long id) {
        timeEntryService.deleteTimeEntry(id);
        return ResponseEntity.noContent().build();
    }
}