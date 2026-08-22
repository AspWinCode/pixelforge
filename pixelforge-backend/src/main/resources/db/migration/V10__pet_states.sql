CREATE TABLE pet_states (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL UNIQUE REFERENCES users(id),
    hunger      INTEGER NOT NULL DEFAULT 100,
    mood        INTEGER NOT NULL DEFAULT 100,
    energy      INTEGER NOT NULL DEFAULT 100,
    level       INTEGER NOT NULL DEFAULT 1,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
