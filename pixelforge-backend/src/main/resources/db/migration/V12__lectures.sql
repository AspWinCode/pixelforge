CREATE TABLE lectures (
    id          BIGSERIAL PRIMARY KEY,
    org_id      BIGINT NOT NULL REFERENCES organizations(id),
    title       VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Карточки лекции, показываются по порядку (position). Один тип контента
-- на карточку — так методисту проще собирать лекцию как последовательность
-- простых блоков, а не редактировать один большой смешанный документ.
CREATE TABLE lecture_cards (
    id           BIGSERIAL PRIMARY KEY,
    lecture_id   BIGINT NOT NULL REFERENCES lectures(id),
    position     INTEGER NOT NULL,
    card_type    VARCHAR(20) NOT NULL, -- TEXT, IMAGE, VIDEO, SNAP_SNIPPET
    content      TEXT,                  -- текст ИЛИ url картинки/видео ИЛИ XML сниппета
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_lecture_cards_lecture_id ON lecture_cards(lecture_id);

-- Assignment теперь может (не обязан) ссылаться на лекцию.
ALTER TABLE assignments ADD COLUMN lecture_id BIGINT REFERENCES lectures(id);

-- Прогресс по паре (ученик, лекция) — НЕ по заданию, как договорились:
-- если ученик прошёл лекцию для одного задания, для другого задания
-- с той же лекцией она уже будет отмечена пройденной.
CREATE TABLE lecture_progress (
    id           BIGSERIAL PRIMARY KEY,
    lecture_id   BIGINT NOT NULL REFERENCES lectures(id),
    user_id      BIGINT NOT NULL REFERENCES users(id),
    completed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (lecture_id, user_id)
);
