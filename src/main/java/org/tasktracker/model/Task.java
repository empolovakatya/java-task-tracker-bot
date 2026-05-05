package org.tasktracker.model;

import java.time.LocalDateTime;

public class Task {
    private final long id;
    private long userId;
    private String title;
    private String category;
    private String status;
    private  String priority;
    private LocalDateTime deadline;
    private final LocalDateTime createdAt;

    public Task(
            long id,
            LocalDateTime createdAt,
            long userId,
            String title,
            String category,
            String status,
            String priority,
            LocalDateTime deadline
            ) {
        this.id = id;
        this.createdAt = createdAt;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.status = status;
        this.priority = priority;
        this.deadline = deadline;
    }

    public Task(long userId, String title, String category, String priority, LocalDateTime deadline) {
        this.id = 0;
        this.createdAt = null;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.status = "TODO";
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
