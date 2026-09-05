-- Модель курсов для встроенной студии методиста (портал learning-portal,
-- /pixelforge). Группа (a): course + course_node + дерево. node_task,
-- task_test, task_hint — следующими группами.

CREATE TABLE course (
    id          BIGSERIAL PRIMARY KEY,
    org_id      BIGINT NOT NULL DEFAULT 1 REFERENCES organizations(id),
    title       VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) UNIQUE,
    description TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'DRAFT',   -- DRAFT | PUBLISHED | ARCHIVED
    sort_order  INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE course_node (
    id          BIGSERIAL PRIMARY KEY,
    course_id   BIGINT NOT NULL REFERENCES course(id) ON DELETE CASCADE,
    parent_id   BIGINT REFERENCES course_node(id) ON DELETE CASCADE,
    type        VARCHAR(20) NOT NULL,                   -- MODULE | TOPIC | SUBTOPIC
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    status      VARCHAR(20) NOT NULL DEFAULT 'DRAFT',    -- DRAFT | PUBLISHED
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_course_node_course_id ON course_node(course_id);
CREATE INDEX idx_course_node_parent_id ON course_node(parent_id);
