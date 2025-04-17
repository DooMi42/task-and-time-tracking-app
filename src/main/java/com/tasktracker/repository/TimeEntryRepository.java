package com.tasktracker.repository;

import com.tasktracker.model.Task;
import com.tasktracker.model.TimeEntry;
import com.tasktracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
    List<TimeEntry> findByUser(User user);

    List<TimeEntry> findByTask(Task task);

    List<TimeEntry> findByUserAndStartTimeBetween(User user, LocalDateTime start, LocalDateTime end);

    @Query("SELECT t FROM TimeEntry t WHERE t.user = ?1 AND t.endTime IS NULL")
    Optional<TimeEntry> findRunningTimeEntry(User user);
}