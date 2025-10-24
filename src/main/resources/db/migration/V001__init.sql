CREATE TABLE IF NOT EXISTS roles
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    nickname    VARCHAR(150) NOT NULL,
    avatar_path TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    role_id     UUID         NOT NULL REFERENCES roles (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS auth_codes
(
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     uuid       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    passcode    varchar(6) NOT NULL,
    expire_date bigint     NOT NULL
);

CREATE TABLE IF NOT EXISTS roles
(
    id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "name" VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS users
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    nickname   VARCHAR(150) NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    last_seen  TIMESTAMP    NOT NULL,
    role_id    UUID         NOT NULL REFERENCES roles (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS auth_codes
(
    id          uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     uuid       NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    passcode    varchar(6) NOT NULL,
    expire_date bigint     NOT NULL
);

-- Таблица с комнатами конференций
CREATE TABLE IF NOT EXISTS rooms
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "name"           VARCHAR(255) NOT NULL,
    owner_id         UUID         NOT NULL REFERENCES users (id),
    status           VARCHAR(20),
    "type"           varchar(30)  NOT NULL,
    access_code      VARCHAR(255),
    max_participants INT,
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP
);

-- Таблица участников комнат
CREATE TABLE room_participants
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id   UUID NOT NULL REFERENCES rooms (id) ON DELETE CASCADE,
    user_id   UUID NOT NULL,
    joined_at TIMESTAMP,
    left_at   TIMESTAMP,
    UNIQUE (room_id, user_id)
);

-- Вложения (опционально: обмен файлами)
CREATE TABLE room_files
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id     UUID NOT NULL REFERENCES rooms (id) ON DELETE CASCADE,
    sender_id   UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    file_url    TEXT NOT NULL,
    uploaded_at TIMESTAMP,
    mime_type   VARCHAR(100)
);

-- Записи звонков (опционально)
CREATE TABLE room_recordings
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id     UUID NOT NULL REFERENCES rooms (id) ON DELETE CASCADE,
    recorded_by UUID REFERENCES users (id) ON DELETE SET NULL,
    started_at  TIMESTAMP,
    ended_at    TIMESTAMP,
    file_url    TEXT
);

-- Индексы для быстрого доступа к участникам комнаты
CREATE INDEX idx_participants_room_id ON room_participants (room_id);
CREATE INDEX idx_participants_user_id ON room_participants (user_id);

-- Индексы для ускорения поиска
CREATE INDEX idx_rooms_owner_id ON rooms (owner_id);
CREATE INDEX idx_rooms_status ON rooms (status);
