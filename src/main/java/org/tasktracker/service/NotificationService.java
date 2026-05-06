package org.tasktracker.service;

import org.tasktracker.model.Task;
import org.tasktracker.model.User;
import org.tasktracker.repository.TaskRepository;
import org.tasktracker.repository.UserRepository;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NotificationService {

    private final TaskRepository taskRepository = new TaskRepository();
    private final UserRepository userRepository = new UserRepository();
    private final TelegramLongPollingBot bot;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public NotificationService(TelegramLongPollingBot bot) {
        this.bot = bot;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::check, 1, 1, TimeUnit.MINUTES);
        System.out.println("Планировщик уведомлений запущен.");
    }

    public void stop() {
        scheduler.shutdown();
    }

    private void check() {
        try {
            System.out.println("Текущее время JVM: " + LocalDateTime.now());
            List<Task> tasks = taskRepository.findActiveWithDeadline();
            LocalDateTime now = LocalDateTime.now();

            for (Task task : tasks) {
                long minutesLeft = ChronoUnit.MINUTES.between(now, task.getDeadline());

                if (minutesLeft >= 1380 && minutesLeft <= 1440)  {
                    if (!notificationSent(task.getId(), "DEADLINE_24H")) {
                        sendNotification(task, "DEADLINE_24H",
                                "⏰ Напоминание! До дедлайна задачи «" + task.getTitle() + "» осталось 24 часа.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка планировщика: " + e.getMessage());
        }
    }

    private boolean notificationSent(long taskId, String type) throws SQLException {
        String sql = "SELECT 1 FROM sent_notifications WHERE task_id = ? AND notification_type = ?";
        try (Connection conn = org.tasktracker.db.DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, taskId);
            ps.setString(2, type);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    private void sendNotification(Task task, String type, String message) throws SQLException {
        // найти telegram_id пользователя
        String sql = "SELECT telegram_id FROM users WHERE id = ?";
        try (Connection conn = org.tasktracker.db.DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, task.getUserId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return;
            long telegramId = rs.getLong("telegram_id");

            // отправить сообщение
            try {
                bot.execute(new org.telegram.telegrambots.meta.api.methods.send.SendMessage(
                        String.valueOf(telegramId), message));
            } catch (Exception e) {
                System.out.println("Ошибка отправки уведомления: " + e.getMessage());
                return;
            }

            // записать в sent_notifications
            String insert = "INSERT INTO sent_notifications (task_id, notification_type) VALUES (?, ?)";
            try (Connection c2 = org.tasktracker.db.DatabaseConnection.getConnection();
                 PreparedStatement ps2 = c2.prepareStatement(insert)) {
                ps2.setLong(1, task.getId());
                ps2.setString(2, type);
                ps2.executeUpdate();
            }
        }
    }
}