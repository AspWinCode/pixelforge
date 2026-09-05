-- §8.1 студии: зачисление ученика на курс независимо от классов.
-- Портал зачисляет учеников (lp-student-{N}) при выдаче доступа к курсу.

CREATE TABLE course_enrollment (
    id          BIGSERIAL PRIMARY KEY,
    course_id   BIGINT NOT NULL REFERENCES course(id) ON DELETE CASCADE,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    enrolled_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (course_id, user_id)
);

CREATE INDEX idx_course_enrollment_course_id ON course_enrollment(course_id);
CREATE INDEX idx_course_enrollment_user_id ON course_enrollment(user_id);
