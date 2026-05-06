package org.tasktracker.repository;

import org.tasktracker.db.DatabaseConnection;
import org.tasktracker.model.Task;

import java.sql.*;
        import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskRepository {

    public Task save(Task task) throws SQLException {
        String sql = "INSERT INTO tasks (user_id, title, category, status, deadline) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING id, created_at";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, task.getUserId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getCategory());
            ps.setString(4, task.getStatus());
            ps.setObject(5, task.getDeadline());

            ResultSet rs = ps.executeQuery();
            rs.next();
            task.setId(rs.getLong("id"));
            task.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
            return task;
        }
    }

    public List<Task> findByUserId(long userId) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE user_id = ? AND status != 'DONE' " +
                "ORDER BY deadline ASC NULLS LAST";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            return mapList(rs);
        }
    }

    public Optional<Task> findById(long id) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
            return Optional.empty();
        }
    }

    public List<Task> findActiveWithDeadline() throws SQLException {
        String sql = "SELECT * FROM tasks WHERE status != 'DONE' AND deadline IS NOT NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            return mapList(rs);
        }
    }

    public void updateStatus(long taskId, String status) throws SQLException {
        String sql = "UPDATE tasks SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setLong(2, taskId);
            ps.executeUpdate();
        }
    }

    public void delete(long taskId) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, taskId);
            ps.executeUpdate();
        }
    }

    private Task map(ResultSet rs) throws SQLException {
        return new Task(
                rs.getLong("id"),
                rs.getObject("created_at", LocalDateTime.class),
                rs.getLong("user_id"),
                rs.getString("title"),
                rs.getString("category"),
                rs.getString("status"),
                rs.getObject("deadline", LocalDateTime.class)
        );
    }

    private List<Task> mapList(ResultSet rs) throws SQLException {
        List<Task> list = new ArrayList<>();
        while (rs.next()) list.add(map(rs));
        return list;
    }
    public List<Task> findByUserIdAndCategory(long userId, String category) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE user_id = ? AND status != 'DONE' " +
                "AND LOWER(category) = LOWER(?) ORDER BY deadline ASC NULLS LAST";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setString(2, category);
            ResultSet rs = ps.executeQuery();
            return mapList(rs);
        }
    }
}