import { useCallback, useEffect, useRef, useState } from 'react';
import { useParams } from 'react-router-dom';
import { getProjectXML, loadProjectXML, waitForSnapReady, hasUnsavedChanges } from '../api/snapBridge';
import {
  startSubmission,
  getSavedProject,
  saveProject,
  submitAssignment,
  fetchAssignment,
  fetchAssignmentImages,
  type Assignment,
  type AssignmentImage,
} from '../api/assignments';
import { useSessionStore } from '../store/session';
import { API_ORIGIN } from '../api/client';

const SNAP_URL = import.meta.env.VITE_SNAP_URL;
const AUTOSAVE_INTERVAL_MS = 3 * 60 * 1000;

// Возвращает текст и "срочность" дедлайна — используется и для текста,
// и для выбора цвета баннера (обычный/предупреждение/просрочено).
function describeDeadline(deadline: string | null): { text: string; urgency: 'none' | 'soon' | 'overdue' } | null {
  if (!deadline) return null;

  const deadlineDate = new Date(deadline);
  const now = new Date();
  const diffMs = deadlineDate.getTime() - now.getTime();
  const diffHours = diffMs / (1000 * 60 * 60);

  const formatted = deadlineDate.toLocaleString('ru-RU', {
    day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit',
  });

  if (diffMs < 0) {
    return { text: `Дедлайн истёк: ${formatted}`, urgency: 'overdue' };
  }
  if (diffHours < 24) {
    return { text: `Дедлайн скоро: ${formatted}`, urgency: 'soon' };
  }
  return { text: `Дедлайн: ${formatted}`, urgency: 'none' };
}

export function AssignmentPage() {
  const { id } = useParams<{ id: string }>();
  const assignmentId = Number(id);
  const userId = useSessionStore((s) => s.userId);

  const iframeRef = useRef<HTMLIFrameElement>(null);
  const [assignment, setAssignment] = useState<Assignment | null>(null);
  const [images, setImages] = useState<AssignmentImage[]>([]);
  const [status, setStatus] = useState<string>('loading');
  const [message, setMessage] = useState<string>('');
  const [isError, setIsError] = useState(false);
  const [saving, setSaving] = useState(false);
  const [lastSavedAt, setLastSavedAt] = useState<Date | null>(null);

  const statusRef = useRef(status);
  useEffect(() => {
    statusRef.current = status;
  }, [status]);

  useEffect(() => {
    fetchAssignment(assignmentId).then(setAssignment).catch(() => {});
    fetchAssignmentImages(assignmentId).then(setImages).catch(() => {});
  }, [assignmentId]);

  useEffect(() => {
    if (!userId) return;
    startSubmission(assignmentId, userId)
      .then((submission) => setStatus(submission.status))
      .catch(() => {
        setIsError(true);
        setMessage('Не удалось начать задание');
      });
  }, [assignmentId, userId]);

  const handleSave = useCallback(async (silent = false): Promise<boolean> => {
    if (!userId || !iframeRef.current?.contentWindow) return false;
    const iframeWindow = iframeRef.current.contentWindow;

    try {
      const changed = await hasUnsavedChanges(iframeWindow);
      if (!changed) {
        if (!silent) { setIsError(false); setMessage('Нечего сохранять — изменений нет'); }
        return false;
      }

      if (!silent) { setIsError(false); setMessage('Сохраняю...'); }
      setSaving(true);

      const xml = await getProjectXML(iframeWindow);
      await saveProject(assignmentId, userId, xml);

      setLastSavedAt(new Date());
      if (!silent) setMessage('Сохранено');
      return true;
    } catch (err) {
      if (!silent) { setIsError(true); setMessage('Ошибка сохранения: ' + (err as Error).message); }
      return false;
    } finally {
      setSaving(false);
    }
  }, [assignmentId, userId]);

  useEffect(() => {
    const interval = setInterval(() => {
      if (statusRef.current === 'IN_PROGRESS') {
        handleSave(true);
      }
    }, AUTOSAVE_INTERVAL_MS);
    return () => clearInterval(interval);
  }, [handleSave]);

  async function handleIframeLoad() {
    if (!userId || !iframeRef.current?.contentWindow) return;
    try {
      setMessage('Жду готовности Snap!...');
      setIsError(false);
      await waitForSnapReady(iframeRef.current.contentWindow);

      const savedXml = await getSavedProject(assignmentId, userId);
      if (savedXml) {
        setMessage('Загружаю сохранённый прогресс...');
        await loadProjectXML(iframeRef.current.contentWindow, savedXml);
        setMessage('Прогресс загружен');
      } else {
        setMessage('');
      }
    } catch (err) {
      setIsError(true);
      setMessage('Не удалось загрузить сохранённый проект: ' + (err as Error).message);
    }
  }

  async function handleSubmit() {
    if (!userId || !iframeRef.current?.contentWindow) return;
    try {
      setIsError(false);
      setMessage('Сохраняю финальную версию...');
      const xml = await getProjectXML(iframeRef.current.contentWindow);
      await saveProject(assignmentId, userId, xml);
      setLastSavedAt(new Date());

      setMessage('Отправляю на проверку...');
      const submission = await submitAssignment(assignmentId, userId);
      setStatus(submission.status);
      setMessage('Задание сдано! Ожидает проверки учителем.');
    } catch (err) {
      setIsError(true);
      setMessage('Ошибка при сдаче: ' + (err as Error).message);
    }
  }

  const deadlineInfo = assignment ? describeDeadline(assignment.deadline) : null;
  const deadlineColor = deadlineInfo?.urgency === 'overdue'
    ? 'var(--danger)'
    : deadlineInfo?.urgency === 'soon'
      ? 'var(--ember)'
      : 'var(--text-muted)';

  return (
    <div className="page">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
        <h1 style={{ marginBottom: 0 }}>{assignment?.title ?? `Задание #${assignmentId}`}</h1>
        <span className={`status-chip status-${status.toLowerCase()}`}>{status}</span>
      </div>

      {deadlineInfo && (
        <p style={{ fontSize: '13px', color: deadlineColor, fontFamily: 'var(--font-mono)', marginTop: 0, marginBottom: '12px' }}>
          ⏰ {deadlineInfo.text}
        </p>
      )}

      {assignment?.description && (
        <div className="card" style={{ marginBottom: '12px', whiteSpace: 'pre-wrap' }}>
          {assignment.description}
        </div>
      )}

      {images.length > 0 && (
        <div style={{ display: 'flex', gap: '8px', marginBottom: '16px', flexWrap: 'wrap' }}>
          {images.map((img) => (
            <img
              key={img.id}
              src={`${API_ORIGIN}${img.url}`}
              alt={img.originalName}
              style={{ maxHeight: '160px', borderRadius: 'var(--radius)', border: '1px solid var(--border)' }}
            />
          ))}
        </div>
      )}

      {message && <div className={`message-banner ${isError ? 'error' : ''}`}>{message}</div>}
      {lastSavedAt && (
        <p style={{ fontSize: '12px', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
          Последнее сохранение: {lastSavedAt.toLocaleTimeString()}
        </p>
      )}

      <iframe
        ref={iframeRef}
        src={SNAP_URL}
        width="100%"
        height="600"
        style={{ border: '1px solid var(--border)', borderRadius: 'var(--radius)', marginBottom: '12px' }}
        title="Snap! редактор"
        onLoad={handleIframeLoad}
      />

      <div style={{ display: 'flex', gap: '8px' }}>
        <button onClick={() => handleSave(false)} disabled={saving || status === 'SUBMITTED'}>
          {saving ? 'Сохраняю...' : 'Сохранить'}
        </button>
        <button className="primary" onClick={handleSubmit} disabled={status === 'SUBMITTED'}>
          {status === 'SUBMITTED' ? 'Уже сдано' : 'Сдать задание'}
        </button>
      </div>
    </div>
  );
}
