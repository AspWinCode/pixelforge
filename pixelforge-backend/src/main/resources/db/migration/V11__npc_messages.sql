CREATE TABLE npc_messages (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT NOT NULL REFERENCES users(id),
    character    VARCHAR(50) NOT NULL,
    message      TEXT NOT NULL,
    is_read      BOOLEAN NOT NULL DEFAULT false,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_npc_messages_user_id ON npc_messages(user_id);
