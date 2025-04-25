package com.tasktracker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_entries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    public boolean isRunning() {
        return endTime == null;
    }

    @Transient
    public Double getDurationInHours() {
        if (startTime == null) {
            return 0.0;
        }

        LocalDateTime end = endTime != null ? endTime : LocalDateTime.now();
        long seconds = Duration.between(startTime, end).getSeconds();
        return seconds / 3600.0;
    }

    public int getDurationInMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }

        // Calculate duration in minutes
        long startMinutes = startTime.getHour() * 60 + startTime.getMinute();
        long endMinutes = endTime.getHour() * 60 + endTime.getMinute();
        return (int) (endMinutes - startMinutes);
    }

    @Override
    public String toString() {
        return "TimeEntry{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", taskId=" + (task != null ? task.getId() : null) +
                '}';
    }
}