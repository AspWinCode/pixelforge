import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getCards, getCompletion, markComplete, type LectureCard } from '../api/lectures';
import { useSessionStore } from '../store/session';

export function LecturePage() {
  const { id } = useParams<{ id: string }>();
  const lectureId = Number(id);
  const userId = useSessionStore((s) => s.userId);
  const navigate = useNavigate();

  const [cards, setCards] = useState<LectureCard[]>([]);
  const [index, setIndex] = useState(0);
  const [alreadyCompleted, setAlreadyCompleted] = useState(false);
  const [justCompleted, setJustCompleted] = useState(false);

  useEffect(() => {
    getCards(lectureId).then(setCards).catch(() => {});
    if (userId) {
      getCompletion(lectureId, userId).then(setAlreadyCompleted).catch(() => {});
    }
  }, [lectureId, userId]);

  async function handleFinish() {
    if (!userId) return;
    await markComplete(lectureId, userId);
    setJustCompleted(true);
  }

  if (cards.length === 0) {
    return <div className="page">Загрузка...</div>;
  }

  const card = cards[index];
  const isLast = index === cards.length - 1;

  return (
    <div className="page">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
        <h1 style={{ marginBottom: 0 }}>Брифинг</h1>
        {(alreadyCompleted || justCompleted) && (
          <span className="status-chip status-reviewed">Пройдено</span>
        )}
      </div>

      <p style={{ fontSize: '12px', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
        Карточка {index + 1} из {cards.length}
      </p>

      <div className="card" style={{ minHeight: '200px' }}>
        {card.cardType === 'TEXT' && <p style={{ whiteSpace: 'pre-wrap' }}>{card.content}</p>}
        {card.cardType === 'IMAGE' && <img src={card.content} alt="" style={{ maxWidth: '100%', borderRadius: 'var(--radius)' }} />}
        {card.cardType === 'VIDEO' && (
          <video src={card.content} controls style={{ maxWidth: '100%', borderRadius: 'var(--radius)' }} />
        )}
        {card.cardType === 'SNAP_SNIPPET' && (
          <p style={{ color: 'var(--text-muted)' }}>
            🧩 Живой сниппет Snap! (полноценный интерактивный просмотр — следующий шаг развития этой фичи)
          </p>
        )}
      </div>

      <div style={{ display: 'flex', gap: '8px', marginTop: '12px' }}>
        <button onClick={() => setIndex((i) => Math.max(0, i - 1))} disabled={index === 0}>
          ← Назад
        </button>
        {!isLast && (
          <button className="primary" onClick={() => setIndex((i) => i + 1)}>
            Дальше →
          </button>
        )}
        {isLast && !alreadyCompleted && !justCompleted && (
          <button className="primary" onClick={handleFinish}>
            Прошёл брифинг ✓
          </button>
        )}
        {isLast && (alreadyCompleted || justCompleted) && (
          <button onClick={() => navigate(-1)}>Вернуться к заданию</button>
        )}
      </div>
    </div>
  );
}
