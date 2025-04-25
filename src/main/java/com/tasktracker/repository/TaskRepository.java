package com.tasktracker.repository;

import com.tasktracker.model.Task;
import com.tasktracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser(User user);

    List<Task> findByUserUsername(String username);

    List<Task> findByUserAndStatus(User user, Task.TaskStatus status);

    List<Task> findByUserOrderByDueDateAsc(User user);

    List<Task> findByUserAndDueDateBefore(User user, LocalDate date);

    @Query("SELECT t FROM Task t WHERE t.user = ?1 AND t.dueDate BETWEEN ?2 AND ?3")
    List<Task> findTasksByUserAndDueDateBetween(User user, LocalDate startDate, LocalDate endDate);

    List<Task> findByUserId(Long userId);
}