-- Группа (c) студии методиста: автотесты и подсказки задачи.
-- Тесты — данные («ожидаемый результат»), автопроверки в PixelForge нет,
-- checker принимается, но эффективно всегда ручной (см. спеку студии §7.2).

CREATE TABLE task_test (
    id              BIGSERIAL PRIMARY KEY,
    assignment_id   BIGINT NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
    test_type       VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',   -- PUBLIC | HIDDEN
    input_data      TEXT,
    expected_output TEXT,
    checker         VARCHAR(20) NOT NULL DEFAULT 'EXACT',    -- EXACT | TRIMMED | REGEX | MANUAL
    weight          NUMERIC NOT NULL DEFAULT 1,
    order_index     INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_task_test_assignment_id ON task_test(assignment_id);

CREATE TABLE task_hint (
    id              BIGSERIAL PRIMARY KEY,
    assignment_id   BIGINT NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
    level           INTEGER NOT NULL DEFAULT 1,
    unlock_attempts INTEGER NOT NULL DEFAULT 3,
    coin_cost       INTEGER NOT NULL DEFAULT 0,
    content         TEXT NOT NULL,
    order_index     INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_task_hint_assignment_id ON task_hint(assignment_id);
