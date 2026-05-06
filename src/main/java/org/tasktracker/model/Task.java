package org.tasktracker.model;

import java.time.LocalDateTime;

public class Task {
    private final long id;
    private long userId;
    private String title;
    private String category;
    private String status;
    private LocalDateTime deadline;
    private final LocalDateTime createdAt;

    // конструктор для чтения из БД
    public Task(long id, LocalDateTime createdAt, long userId,
                String title, String category, String status, LocalDateTime deadline) {
        this.id = id;
        this.createdAt = createdAt;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.status = status;
        this.deadline = deadline;
    }

    // конструктор для создания новой задачи
    public Task(long userId, String title, String category, LocalDateTime deadline) {
        this.id = 0;
        this.createdAt = null;
        this.userId = userId;
        this.title = title;
        this.category = category;
        this.deadline = deadline;
        this.status = "TODO";
    }

    public long getId() { return id; }
    public long getUserId() { return userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setId(long id) { /* нужен для TaskRepository */ }
    public void setCreatedAt(LocalDateTime createdAt) { /* нужен для TaskRepository */ }
}