package org.tasktracker;

import org.tasktracker.config.AppConfig;
import org.tasktracker.db.DatabaseConnection;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        String token = AppConfig.getBotToken();

        Bot bot = new Bot(token);
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("База данных подключена!");
        } catch (SQLException e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
        }
    }
}