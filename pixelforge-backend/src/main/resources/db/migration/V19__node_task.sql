-- Группа (b) студии методиста: привязка задач (assignment) к узлам дерева
-- курса + послабления в самой assignment под шаблоны студии.

-- Задачи, созданные в студии, — шаблоны без класса (class_id = null).
ALTER TABLE assignments ALTER COLUMN class_id DROP NOT NULL;

-- updated_at для authoring-правок (у существующих строк = created_at).
ALTER TABLE assignments ADD COLUMN updated_at TIMESTAMPTZ;
UPDATE assignments SET updated_at = created_at WHERE updated_at IS NULL;
ALTER TABLE assignments ALTER COLUMN updated_at SET NOT NULL;
ALTER TABLE assignments ALTER COLUMN updated_at SET DEFAULT now();

CREATE TABLE node_task (
    id            BIGSERIAL PRIMARY KEY,
    node_id       BIGINT NOT NULL REFERENCES course_node(id) ON DELETE CASCADE,
    assignment_id BIGINT NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
    sort_order    INTEGER NOT NULL DEFAULT 0,
    is_required   BOOLEAN NOT NULL DEFAULT true,
    UNIQUE (node_id, assignment_id)
);

CREATE INDEX idx_node_task_node_id ON node_task(node_id);
CREATE INDEX idx_node_task_assignment_id ON node_task(assignment_id);
