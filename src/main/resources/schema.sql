CREATE TABLE IF NOT EXISTS users (
    id          BIGSERIAL PRIMARY KEY,
    telegram_id BIGINT UNIQUE NOT NULL,
    username    VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS tasks (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(500) NOT NULL,
    category    VARCHAR(100),
    status      VARCHAR(50) NOT NULL DEFAULT 'TODO',
    priority    VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    deadline    TIMESTAMP,
    created_at  TIMESTAMP DEFAULT NOW()
);