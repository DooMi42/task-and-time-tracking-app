package com.tasktracker.service;

import com.tasktracker.dto.TaskDto;
import com.tasktracker.model.Task.TaskStatus;

import java.time.LocalDate;
import java.util.List;

public interface TaskService {
    TaskDto createTask(TaskDto taskDto);

    TaskDto getTaskById(Long id);

    List<TaskDto> getAllTasks();

    List<TaskDto> getTasksByCurrentUser();

    List<TaskDto> getTasksByStatus(TaskStatus status);

    List<TaskDto> getTasksDueThisWeek();

    List<TaskDto> getTasksOverdue();

    TaskDto updateTask(Long id, TaskDto taskDto);

    void deleteTask(Long id);
}