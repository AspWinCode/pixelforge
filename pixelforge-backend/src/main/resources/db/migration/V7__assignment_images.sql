CREATE TABLE assignment_images (
    id             BIGSERIAL PRIMARY KEY,
    assignment_id  BIGINT NOT NULL REFERENCES assignments(id),
    s3_key         VARCHAR(500) NOT NULL,
    original_name  VARCHAR(255) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);
