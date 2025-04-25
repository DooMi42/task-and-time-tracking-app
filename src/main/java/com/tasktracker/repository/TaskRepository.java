package com.tasktracker.repository;

import com.tasktracker.model.Task;
import com.tasktracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser(User user);

    List<Task> findByUserAndStatus(User user, Task.TaskStatus status);

    List<Task> findTasksByUserAndDueDateBetween(User user, LocalDate startDate, LocalDate endDate);

    List<Task> findByUserAndDueDateBefore(User user, LocalDate date);

    List<Task> findByUserId(Long userId);
}