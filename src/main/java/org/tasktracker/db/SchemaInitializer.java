package org.tasktracker.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SchemaInitializer {

    public static void init() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            String sql = readSqlFile();
            stmt.execute(sql);
            System.out.println("Таблицы созданы!");

        } catch (SQLException | IOException e) {
            System.out.println("Ошибка при создании таблиц: " + e.getMessage());
        }
    }

    private static String readSqlFile() throws IOException {
        InputStream is = SchemaInitializer.class
                .getClassLoader()
                .getResourceAsStream("schema.sql");
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }
}