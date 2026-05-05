package org.tasktracker.model;

public class User {
    private final long id;
    private final long telegramId;
    private String username;

    public User(long telegramId, String username) {
        this.id = 0;
        this.telegramId = telegramId;
        this.username = username;
    }

    public User(long id, long telegramId, String username) {
        this.id = id;
        this.telegramId = telegramId;
        this.username = username;
    }

    public long getId() {
        return id;
    }

    public long getTelegramId() {
        return telegramId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

