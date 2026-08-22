import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { loadProjectXML, waitForSnapReady } from '../api/snapBridge';
import { getSavedProject } from '../api/assignments';
import { reviewSubmission, listSubmissions } from '../api/teacher';

const SNAP_URL = import.meta.env.VITE_SNAP_URL;

export function ReviewPage() {
  const { assignmentId, userId } = useParams<{ assignmentId: string; userId: string }>();
  const navigate = useNavigate();
  const iframeRef = useRef<HTMLIFrameElement>(null);
  const [message, setMessage] = useState('Загружаю работу ученика...');
  const [isError, setIsError] = useState(false);
  const [currentStatus, setCurrentStatus] = useState<string | null>(null);

  // При открытии узнаём РЕАЛЬНЫЙ статус сдачи с сервера, а не предполагаем
  // "ещё не проверено" по умолчанию — иначе кнопка кажется активной даже
  // для уже проверенной работы, и клик по ней просто падает в 409.
  useEffect(() => {
    listSubmissions(Number(assignmentId))
      .then((list) => {
        const submission = list.find((s) => s.userId === Number(userId));
        setCurrentStatus(submission?.status ?? null);
      })
      .catch(() => {});
  }, [assignmentId, userId]);

  async function handleIframeLoad() {
    if (!iframeRef.current?.contentWindow) return;
    try {
      await waitForSnapReady(iframeRef.current.contentWindow);

      const xml = await getSavedProject(Number(assignmentId), Number(userId));
      if (!xml) {
        setIsError(true);
        setMessage('Ученик ещё ничего не сохранил');
        return;
      }

      await loadProjectXML(iframeRef.current.contentWindow, xml);
      setMessage('');
    } catch (err) {
      setIsError(true);
      setMessage('Не удалось загрузить работу: ' + (err as Error).message);
    }
  }

  async function handleReview() {
    try {
      await reviewSubmission(Number(assignmentId), Number(userId));
      setCurrentStatus('REVIEWED');
      setIsError(false);
      setMessage('Проверено, токены начислены');
    } catch (err) {
      setIsError(true);
      setMessage('Ошибка: ' + (err as Error).message);
    }
  }

  const canReview = currentStatus === 'SUBMITTED';

  return (
    <div className="page">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
        <h1 style={{ marginBottom: 0 }}>Просмотр работы — ученик #{userId}</h1>
        <button onClick={() => navigate(-1)}>← Назад к списку</button>
      </div>

      {message && <div className={`message-banner ${isError ? 'error' : ''}`}>{message}</div>}
      {currentStatus === 'REVIEWED' && !message && (
        <div className="message-banner">Эта работа уже проверена</div>
      )}

      <iframe
        ref={iframeRef}
        src={SNAP_URL}
        width="100%"
        height="600"
        style={{ border: '1px solid var(--border)', borderRadius: 'var(--radius)', marginBottom: '12px' }}
        title="Просмотр работы ученика"
        onLoad={handleIframeLoad}
      />

      <button className="primary" onClick={handleReview} disabled={!canReview}>
        {currentStatus === 'REVIEWED' ? 'Уже проверено' : 'Проверить и начислить токены'}
      </button>
    </div>
  );
}
