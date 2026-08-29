import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  createAssignment,
  publishAssignment,
  listSubmissions,
  uploadAssignmentImage,
  listAllAssignments,
  fetchStaffClasses,
  type SubmissionListItem,
  type TeacherClass,
} from '../api/teacher';
import { listLectures, type Lecture } from '../api/lectures';
import type { Assignment } from '../api/assignments';

const HARDCODED_TEACHER_ID = 5;

function formatDeadline(deadline: string | null): string {
  if (!deadline) return 'без дедлайна';
  return new Date(deadline).toLocaleString('ru-RU', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' });
}

export function TeacherPage() {
  const [classes, setClasses] = useState<TeacherClass[]>([]);
  const [selectedClassId, setSelectedClassId] = useState<number | null>(null);

  const [lectures, setLectures] = useState<Lecture[]>([]);
  const [selectedLectureId, setSelectedLectureId] = useState<string>('');

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [tool, setTool] = useState<'SNAP' | 'GDEVELOP'>('SNAP');
  const [deadline, setDeadline] = useState('');
  const [pendingFiles, setPendingFiles] = useState<File[]>([]);
  const [message, setMessage] = useState('');
  const [isError, setIsError] = useState(false);
  const [creating, setCreating] = useState(false);

  const [assignments, setAssignments] = useState<Assignment[]>([]);
  const [expandedAssignmentId, setExpandedAssignmentId] = useState<number | null>(null);
  const [submissions, setSubmissions] = useState<SubmissionListItem[]>([]);

  useEffect(() => {
    fetchStaffClasses(HARDCODED_TEACHER_ID, 'METHODIST').then((list) => {
      setClasses(list);
      if (list.length > 0) setSelectedClassId(list[0].id);
    }).catch(() => {});
    listLectures().then(setLectures).catch(() => {});
  }, []);

  async function refreshAssignments(classId: number) {
    try {
      const list = await listAllAssignments(classId);
      setAssignments(list);
    } catch {
      // не критично
    }
  }

  useEffect(() => {
    if (selectedClassId) refreshAssignments(selectedClassId);
  }, [selectedClassId]);

  function handleFilesSelected(e: React.ChangeEvent<HTMLInputElement>) {
    const files = Array.from(e.target.files ?? []);
    setPendingFiles((prev) => [...prev, ...files]);
    e.target.value = '';
  }

  function removePendingFile(index: number) {
    setPendingFiles((prev) => prev.filter((_, i) => i !== index));
  }

  async function handleCreate() {
    if (!selectedClassId) {
      setIsError(true);
      setMessage('У вас пока нет ни одного класса');
      return;
    }
    if (!title.trim()) {
      setIsError(true);
      setMessage('Введите название задания');
      return;
    }
    setCreating(true);
    try {
      const deadlineIso = deadline ? new Date(deadline).toISOString() : null;
      const lectureId = selectedLectureId ? Number(selectedLectureId) : null;

      const assignment = await createAssignment({
        classId: selectedClassId,
        lectureId,
        title,
        description,
        tool,
        deadline: deadlineIso,
      });

      for (const file of pendingFiles) {
        await uploadAssignmentImage(assignment.id, file);
      }

      setIsError(false);
      setMessage(
        `Создано: «${assignment.title}»` +
        (lectureId ? ` (привязано к лекции)` : '') +
        (pendingFiles.length > 0 ? `, загружено картинок: ${pendingFiles.length}` : '')
      );
      setTitle('');
      setDescription('');
      setTool('SNAP');
      setDeadline('');
      setSelectedLectureId('');
      setPendingFiles([]);
      await refreshAssignments(selectedClassId);
    } catch (err) {
      setIsError(true);
      setMessage('Ошибка создания: ' + (err as Error).message);
    } finally {
      setCreating(false);
    }
  }

  async function handlePublish(assignmentId: number) {
    if (!selectedClassId) return;
    try {
      await publishAssignment(assignmentId);
      setIsError(false);
      setMessage('Опубликовано');
      await refreshAssignments(selectedClassId);
    } catch (err) {
      setIsError(true);
      setMessage('Ошибка публикации: ' + (err as Error).message);
    }
  }

  async function handleToggleSubmissions(assignmentId: number) {
    if (expandedAssignmentId === assignmentId) {
      setExpandedAssignmentId(null);
      return;
    }
    try {
      const list = await listSubmissions(assignmentId);
      setSubmissions(list);
      setExpandedAssignmentId(assignmentId);
    } catch (err) {
      setIsError(true);
      setMessage('Ошибка загрузки сдач: ' + (err as Error).message);
    }
  }

  if (classes.length === 0) {
    return (
      <div className="page">
        <h1>Мастерская методиста</h1>
        <div className="message-banner">У вас пока нет ни одного класса.</div>
      </div>
    );
  }

  const inputStyle = {
    width: '100%',
    padding: '10px 12px',
    borderRadius: 'var(--radius)',
    border: '1px solid var(--border)',
    background: 'var(--surface)',
    color: 'var(--text)',
    fontFamily: 'var(--font-body)',
    fontSize: '14px',
  };

  return (
    <div className="page">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: '16px' }}>
        <h1 style={{ marginBottom: 0 }}>Мастерская методиста</h1>
        {classes.length > 1 && (
          <select value={selectedClassId ?? ''} onChange={(e) => setSelectedClassId(Number(e.target.value))}>
            {classes.map((c) => (
              <option key={c.id} value={c.id}>{c.name}</option>
            ))}
          </select>
        )}
        {classes.length === 1 && <span className="status-chip">{classes[0].name}</span>}
      </div>

      {message && <div className={`message-banner ${isError ? 'error' : ''}`}>{message}</div>}

      <div className="card">
        <h2 style={{ fontSize: '16px' }}>Создать задание</h2>
        <div style={{ marginBottom: '10px' }}>
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="Название задания"
            style={{ ...inputStyle, marginBottom: '8px' }}
          />
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="Описание и инструкция для ученика"
            rows={4}
            style={{ ...inputStyle, resize: 'vertical', marginBottom: '8px' }}
          />

          <label style={{ fontSize: '13px', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>
            Связанная лекция (необязательно)
          </label>
          <select
            value={selectedLectureId}
            onChange={(e) => setSelectedLectureId(e.target.value)}
            style={{ ...inputStyle, marginBottom: '8px' }}
          >
            <option value="">Без брифинга</option>
            {lectures.map((l) => (
              <option key={l.id} value={l.id}>{l.title}</option>
            ))}
          </select>

          <label style={{ fontSize: '13px', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>
            Движок
          </label>
          <select
            value={tool}
            onChange={(e) => setTool(e.target.value as 'SNAP' | 'GDEVELOP')}
            style={{ ...inputStyle, marginBottom: '8px' }}
          >
            <option value="SNAP">Snap! (блочный, для младших)</option>
            <option value="GDEVELOP">GDevelop (для старших)</option>
          </select>

          <label style={{ fontSize: '13px', color: 'var(--text-muted)', display: 'block', marginBottom: '4px' }}>
            Дедлайн (необязательно)
          </label>
          <input
            type="datetime-local"
            value={deadline}
            onChange={(e) => setDeadline(e.target.value)}
            style={{ ...inputStyle, marginBottom: '8px', width: 'auto' }}
          />
          <br />

          <label style={{ fontSize: '13px', color: 'var(--cyan)', cursor: 'pointer' }}>
            📎 Прикрепить картинки
            <input type="file" accept="image/*" multiple onChange={handleFilesSelected} style={{ display: 'none' }} />
          </label>

          {pendingFiles.length > 0 && (
            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginTop: '8px' }}>
              {pendingFiles.map((file, i) => (
                <span key={i} className="status-chip">
                  {file.name}
                  <button
                    onClick={() => removePendingFile(i)}
                    style={{ padding: '0 4px', marginLeft: '4px', border: 'none', background: 'none' }}
                  >
                    ✕
                  </button>
                </span>
              ))}
            </div>
          )}
        </div>
        <button className="primary" onClick={handleCreate} disabled={creating}>
          {creating ? 'Создаю...' : 'Создать'}
        </button>
      </div>

      <div className="card">
        <h2 style={{ fontSize: '16px' }}>Мои задания</h2>
        {assignments.length === 0 && <p style={{ color: 'var(--text-muted)' }}>Пока нет заданий</p>}

        {assignments.map((a) => (
          <div key={a.id} style={{ borderTop: '1px solid var(--border)', padding: '10px 0' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <div style={{ fontWeight: 600 }}>{a.title}</div>
                <div style={{ display: 'flex', gap: '8px', alignItems: 'center', marginTop: '2px' }}>
                  <span className={`status-chip status-${a.status.toLowerCase()}`}>{a.status}</span>
                  <span style={{ fontSize: '12px', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                    ⏰ {formatDeadline(a.deadline)}
                  </span>
                </div>
              </div>
              <div style={{ display: 'flex', gap: '8px' }}>
                <button onClick={() => handlePublish(a.id)} disabled={a.status === 'PUBLISHED'}>
                  {a.status === 'PUBLISHED' ? 'Опубликовано' : 'Опубликовать'}
                </button>
                <button onClick={() => handleToggleSubmissions(a.id)}>
                  {expandedAssignmentId === a.id ? 'Скрыть сдачи' : 'Показать сдачи'}
                </button>
              </div>
            </div>

            {expandedAssignmentId === a.id && (
              <div style={{ marginTop: '10px', paddingLeft: '12px' }}>
                {submissions.length === 0 && (
                  <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>Пока никто не начал</p>
                )}
                {submissions.map((s) => (
                  <div key={s.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '6px 0' }}>
                    <div>
                      <span style={{ marginRight: '8px' }}>{s.userFullName}</span>
                      <span className={`status-chip status-${s.status.toLowerCase()}`}>{s.status}</span>
                    </div>
                    <Link to={`/teacher/review/${s.assignmentId}/${s.userId}`}>
                      <button className="primary" disabled={s.status === 'IN_PROGRESS'}>
                        {s.status === 'REVIEWED' ? 'Открыть' : 'Открыть и проверить'}
                      </button>
                    </Link>
                  </div>
                ))}
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}
