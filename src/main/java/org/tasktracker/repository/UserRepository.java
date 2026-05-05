package org.tasktracker.repository;


import org.tasktracker.db.DatabaseConnection;
import org.tasktracker.model.User;

import java.sql.*;

import static org.apache.http.HttpHeaders.FROM;
import static org.postgresql.core.SqlCommandType.SELECT;

public class UserRepository {

    public User findOrCreate(long telegramId, String username) throws SQLException {
        String selectSql = "SELECT * FROM users WHERE telegram_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {

            ps.setLong(1, telegramId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getLong("id"),
                        rs.getLong("telegram_id"),
                        rs.getString("username")
                );
            }
        }
        String insertSql = "INSERT INTO users (telegram_id, username) VALUES (?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {

            ps.setLong(1, telegramId);
            ps.setString(2, username);

            ResultSet rs = ps.executeQuery();
            rs.next();
            long newId = rs.getLong("id");

            return new User(newId, telegramId, username);
        }
    }
}
