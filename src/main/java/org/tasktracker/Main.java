package org.tasktracker;

import org.tasktracker.config.AppConfig;
import org.tasktracker.db.DatabaseConnection;
import org.tasktracker.db.SchemaInitializer;
import org.tasktracker.service.NotificationService;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        String token = AppConfig.getBotToken();

        SchemaInitializer.init();

        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("База данных подключена!");
        } catch (SQLException e) {
            System.out.println("Ошибка подключения: " + e.getMessage());
        }

        Bot bot = new Bot(token);

        NotificationService notificationService = new NotificationService(bot);
        notificationService.start();

        Runtime.getRuntime().addShutdownHook(new Thread(notificationService::stop));

        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);

        System.out.println("Бот запущен!");
    }
}