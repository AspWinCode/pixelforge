CREATE TABLE token_events (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id),
    org_id      BIGINT NOT NULL REFERENCES organizations(id),
    amount      INTEGER NOT NULL,
    reason      VARCHAR(100) NOT NULL,
    meta        JSONB,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_token_events_user_id ON token_events(user_id);
