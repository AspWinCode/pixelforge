-- Тестовые данные для проверки User↔Class↔Assignment вручную,
-- пока нет реальной синхронизации из LMS

INSERT INTO organizations (name)
VALUES ('Тестовая школа')
RETURNING id \gset org_

INSERT INTO users (org_id, lms_user_id, role, full_name)
VALUES (:org_id, 'test_student_1', 'STUDENT', 'Тестовый Ученик')
RETURNING id \gset user_

INSERT INTO classes (org_id, name)
VALUES (:org_id, 'Тестовый класс')
RETURNING id \gset class_

INSERT INTO class_members (class_id, user_id)
VALUES (:class_id, :user_id);

\echo '--- Готово ---'
\echo 'org_id =' :org_id
\echo 'user_id =' :user_id
\echo 'class_id =' :class_id
