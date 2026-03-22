package org.tasktracker;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot {

    public Bot(String token) {
        super(token);
    }

    @Override
    public String getBotUsername() {
        return "itsdoable_bot";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();

            handleMessage(chatId, text);
        }
    }

    private void handleMessage(long chatId, String text) {
        switch (text) {
            case "/start" -> sendReply(chatId, "Я твой трекер задач \nНапиши /help чтобы узнать что я умею.");
            case "/help"  -> sendReply(chatId, """
                Вот что я умею:
                /newtask — создать задачу
                /list — мои задачи
                /done [id] — отметить выполненной
                /delete [id] — удалить задачу
                """);
            default -> sendReply(chatId, "Не понимаю. Напиши /help");
        }
    }
    private void sendReply(long chatId, String text) {
        try {
            execute(SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
