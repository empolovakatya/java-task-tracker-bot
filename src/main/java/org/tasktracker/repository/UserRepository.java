package org.tasktracker.repository;


import org.tasktracker.db.DatabaseConnection;
import org.tasktracker.model.User;

import java.sql.*;

public class UserRepository {

    public User findOrCreate(long telegramId, String username) throws SQLException {
        String selectSql = "SELECT * FROM users WHERE telegram_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(selectSql)) {

            ps.setLong(1, telegramId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // обновляем username если изменился
                String updateSql = "UPDATE users SET username = ? WHERE telegram_id = ?";
                try (Connection c2 = DatabaseConnection.getConnection();
                     PreparedStatement ps2 = c2.prepareStatement(updateSql)) {
                    ps2.setString(1, username);
                    ps2.setLong(2, telegramId);
                    ps2.executeUpdate();
                }
                return new User(
                        rs.getLong("id"),
                        rs.getLong("telegram_id"),
                        username // возвращаем актуальный username
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