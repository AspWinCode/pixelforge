-- Интеграция с кабинетом ученика (learning-portal, tirskix.space):
-- ученики приходят по SSO из кабинета и опознаются по external_ref
-- вида "lp-student-{id}", а не по lms_user_id из ростер-синка.
ALTER TABLE users ADD COLUMN external_ref VARCHAR(255);

-- NULL допускается для не-портальных пользователей; в Postgres несколько
-- NULL не нарушают UNIQUE, поэтому ограничение безопасно.
ALTER TABLE users ADD CONSTRAINT uq_users_org_external_ref UNIQUE (org_id, external_ref);

-- SSO-вход заводит ученика в организации с id = 1 (тот же HARDCODED_ORG_ID,
-- что в ClassSyncService/AuthController). На свежей БД организации ещё нет —
-- создаём дефолтную, если таблица пустая.
INSERT INTO organizations (name)
    SELECT 'PixelForge'
    WHERE NOT EXISTS (SELECT 1 FROM organizations);
