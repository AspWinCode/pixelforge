CREATE TABLE submissions (
    id             BIGSERIAL PRIMARY KEY,
    assignment_id  BIGINT NOT NULL REFERENCES assignments(id),
    user_id        BIGINT NOT NULL REFERENCES users(id),
    s3_key         VARCHAR(500),
    status         VARCHAR(20) NOT NULL DEFAULT 'in_progress',
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (assignment_id, user_id)
);
