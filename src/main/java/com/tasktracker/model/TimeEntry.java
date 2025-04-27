package com.tasktracker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JsonBackReference
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // Making this optional as we'll set it in service layer
    private User user;

    @Column(name = "start_time") // Removed nullable=false to allow for more flexibility
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
        return startTime != null && endTime == null;
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

    @Transient
    public int getDurationInMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }

        return (int) Duration.between(startTime, endTime).toMinutes();
    }

    // Fix toString to avoid circular references
    @Override
    public String toString() {
        return "TimeEntry{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", taskId=" + (task != null ? task.getId() : null) +
                ", userId=" + (user != null ? user.getId() : null) +
                '}';
    }
}