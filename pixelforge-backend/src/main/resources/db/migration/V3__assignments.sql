CREATE TABLE assignments (
    id          BIGSERIAL PRIMARY KEY,
    class_id    BIGINT NOT NULL REFERENCES classes(id),
    title       VARCHAR(255) NOT NULL,
    tool        VARCHAR(50) NOT NULL DEFAULT 'snap',
    status      VARCHAR(20) NOT NULL DEFAULT 'draft',
    deadline    TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
