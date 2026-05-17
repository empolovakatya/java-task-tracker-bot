package org.tasktracker;

import org.tasktracker.model.Task;
import org.tasktracker.model.User;
import org.tasktracker.repository.TaskRepository;
import org.tasktracker.repository.UserRepository;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.sql.SQLException;

public class Bot extends TelegramLongPollingBot {
    private enum State {
        IDLE, AWAIT_TITLE, AWAIT_CATEGORY, AWAIT_DEADLINE
    }

    private final TaskRepository taskRepository = new TaskRepository();
    private final Map<Long, State> states = new ConcurrentHashMap<>();
    private final Map<Long, Task> drafts = new ConcurrentHashMap<>();

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
            String username = update.getMessage().getFrom().getUserName();

            handleMessage(chatId, text, username);
        }
    }

    private void handleMessage(long chatId, String text, String username) {
        try {
            if (username == null) username = "unknown";
            User user = userRepository.findOrCreate(chatId, username);
            State state = states.getOrDefault(chatId, State.IDLE);

            // команды всегда обрабатываются первыми — даже во время диалога
            if (text.startsWith("/") && !text.equals("/skip")) {
                handleCommand(chatId, text, user);
            } else if (state != State.IDLE) {
                handleState(chatId, text, user, state);
            } else {
                sendReply(chatId, "Не понимаю. Напишите /help");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            sendReply(chatId, "Произошла ошибка, попробуйте еще раз");
        }
    }
    private void handleCommand(long chatId, String text, User user) throws SQLException {
        if (text.startsWith("/list")) {
            listTasks(chatId, text, user);
        } else if (text.startsWith("/done")) {
            handleDone(chatId, text, user);
        } else if (text.startsWith("/delete")) {
            handleDelete(chatId, text, user);
        } else {
            switch (text) {
                case "/start"   -> sendReply(chatId, "Я твой трекер задач \nНапиши /help чтобы узнать что я умею.");
                case "/help"    -> sendReply(chatId, """
                    Вот что я умею:
                    /newtask — создать задачу
                    /list [категория] — мои задачи
                    /done [id] — отметить выполненной
                    /delete [id] — удалить задачу
                    /cancel - отменить текущее действие
                    """);
                case "/newtask" -> startNewTask(chatId, user);
                case "/cancel"  -> {
                    states.remove(chatId);
                    drafts.remove(chatId);
                    sendReply(chatId, "Отменено");
                }
                default -> sendReply(chatId, "Не понимаю. Напишите /help");
            }
        }
    }

    private void handleState(long chatId, String text, User user, State state) throws SQLException {
        switch (state) {
            case AWAIT_TITLE    -> awaitTitle(chatId, text, user);
            case AWAIT_CATEGORY -> awaitCategory(chatId, text);
            case AWAIT_DEADLINE -> awaitDeadline(chatId, text, user);
            default             -> sendReply(chatId, "Напиши /help для списка команд.");
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
    private void startNewTask(long chatId, User user) {
        drafts.put(chatId, new Task(user.getId(), "", "", null));
        states.put(chatId, State.AWAIT_TITLE);
        sendReply(chatId, "📝 Введи название задачи:");
    }

    private void awaitTitle(long chatId, String text, User user) {
        drafts.get(chatId).setTitle(text);
        states.put(chatId, State.AWAIT_CATEGORY);
        sendReply(chatId, "📂 Введи категорию:");
    }

    private void awaitCategory(long chatId, String text) {
        drafts.get(chatId).setCategory(text);
        states.put(chatId, State.AWAIT_DEADLINE);
        sendReply(chatId, "📅 Введи дедлайн в формате дд.мм.гггг чч:мм\nили /skip чтобы пропустить:");
    }

    private void awaitDeadline(long chatId, String text, User user) throws SQLException {
        Task draft = drafts.get(chatId);

        if (!text.equals("/skip")) {
            try {
                LocalDateTime deadline = LocalDateTime.parse(text,
                        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
                draft.setDeadline(deadline);
            } catch (Exception e) {
                sendReply(chatId, "⚠️ Неверный формат. Попробуй ещё раз:\nПример: 25.12.2025 18:00");
                return;
            }
        }

        taskRepository.save(draft);
        states.remove(chatId);
        drafts.remove(chatId);
        sendReply(chatId, "✅ Задача создана: " + draft.getTitle());
    }
    private final UserRepository userRepository = new UserRepository();
    private void listTasks(long chatId, String text, User user) throws SQLException {
        String[] parts = text.split(" ", 2);
        List<Task> tasks;

        if (parts.length > 1) {
            String category = parts[1].trim();
            tasks = taskRepository.findByUserIdAndCategory(user.getId(), category);
            if (tasks.isEmpty()) {
                sendReply(chatId, "Нет задач в категории «" + category + "».");
                return;
            }
        } else {
            tasks = taskRepository.findByUserId(user.getId());
            if (tasks.isEmpty()) {
                sendReply(chatId, "У тебя пока нет задач. Создай первую — /newtask");
                return;
            }
        }

        StringBuilder sb = new StringBuilder("📋 Твои задачи:\n\n");
        for (Task t : tasks) {
            sb.append("🔹 [").append(t.getId()).append("] ").append(t.getTitle()).append("\n");
            sb.append("   Категория: ").append(t.getCategory()).append("\n");
            sb.append("   Статус: ").append(t.getStatus()).append("\n");
            if (t.getDeadline() != null) {
                sb.append("   Дедлайн: ")
                        .append(t.getDeadline().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                        .append("\n");
            }
            sb.append("\n");
        }
        sendReply(chatId, sb.toString());
    }

    private void handleDone(long chatId, String text, User user) throws SQLException {
        long id = parseId(chatId, text);
        if (id < 0) return;

        taskRepository.findById(id).ifPresentOrElse(task -> {
            if (task.getUserId() != user.getId()) {
                sendReply(chatId, "Это не твоя задача.");
                return;
            }
            try {
                taskRepository.updateStatus(id, "DONE");
                sendReply(chatId, "✅ Задача [" + id + "] выполнена!");
            } catch (SQLException e) {
                sendReply(chatId, "Ошибка при обновлении задачи.");
            }
        }, () -> sendReply(chatId, "Задача не найдена."));
    }

    private void handleDelete(long chatId, String text, User user) throws SQLException {
        long id = parseId(chatId, text);
        if (id < 0) return;

        taskRepository.findById(id).ifPresentOrElse(task -> {
            if (task.getUserId() != user.getId()) {
                sendReply(chatId, "Это не твоя задача.");
                return;
            }
            try {
                taskRepository.delete(id);
                sendReply(chatId, "🗑 Задача [" + id + "] удалена.");
            } catch (SQLException e) {
                sendReply(chatId, "Ошибка при удалении задачи.");
            }
        }, () -> sendReply(chatId, "Задача не найдена."));
    }

    private long parseId(long chatId, String text) {
        String[] parts = text.split(" ");
        if (parts.length < 2) {
            sendReply(chatId, "Укажи ID задачи. Пример: /done 5");
            return -1;
        }
        try {
            return Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            sendReply(chatId, "ID должен быть числом.");
            return -1;
        }
    }
}
