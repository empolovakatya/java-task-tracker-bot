# Java Task Tracker Bot

Telegram-бот для управления личными задачами с поддержкой категоризации,
дедлайнов и автоматических уведомлений.

## Функциональность

- Создание задач в пошаговом диалоге
- Категоризация задач в свободной форме
- Установка дедлайнов
- Просмотр задач с фильтрацией по категории
- Автоматические уведомления за 24 часа до дедлайна
- Изменение статуса задач
- Удаление задач

## Стек

| Компонент | Технология |
|-----------|-----------|
| Язык | Java 17 |
| База данных | PostgreSQL 16 |
| Telegram API | TelegramBots 6.7.0 |
| Контейнеризация | Docker + Docker Compose |
| Сборка | Maven |

## Быстрый старт

### 1. Получить токен бота
Создание бота через [@BotFather](https://t.me/BotFather) и копирование токена.

### 2. Создать файл `.env` в корне проекта
```text
BOT_TOKEN=your_telegram_bot_token
DB_HOST=localhost
DB_PORT=5432
DB_NAME=your_db_name
DB_USER=your_db_user
DB_PASSWORD=your_db_password
```

### 3. Запуск базы данных
```bash
make start
```

### 4. Запуск бота
Запуск приложения через точку входа `org.tasktracker.Main`.

### 5. Остановка базы данных
```bash
make stop
```

## Команды Makefile

| Команда | Описание |
|---------|----------|
| `make start` | Запуск PostgreSQL в Docker |
| `make stop` | Остановка PostgreSQL |
| `make clean` | Остановка и удаление всех данных |
| `make logs` | Просмотр логов базы данных |

## Команды бота

| Команда | Описание |
|---------|----------|
| `/start` | Приветствие и справка |
| `/newtask` | Создание задачи |
| `/list` | Список всех задач |
| `/list [категория]` | Список задач по категории |
| `/done [id]` | Отметка задачи как выполненной |
| `/delete [id]` | Удаление задачи |
| `/cancel` | Отмена текущего действия |

## Структура проекта

```text
src/main/java/org/tasktracker/
├── Main.java
├── Bot.java
├── config/
│   └── AppConfig.java
├── db/
│   ├── DatabaseConnection.java
│   └── SchemaInitializer.java
├── model/
│   ├── User.java
│   └── Task.java
├── repository/
│   ├── UserRepository.java
│   └── TaskRepository.java
└── service/
    └── NotificationService.java
```

## Требования

- Java 17+
- Docker Desktop
- Maven 3.9+