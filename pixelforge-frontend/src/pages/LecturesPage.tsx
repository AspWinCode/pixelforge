import { useEffect, useState } from 'react';
import { listLectures, getCards, type Lecture, type LectureCard, type CardType } from '../api/lectures';

// Создание брифингов и карточек переехало в студию методиста на портале
// (learning-portal, раздел /pixelforge). Здесь — только просмотр.

const CARD_TYPE_LABELS: Record<CardType, string> = {
  TEXT: 'Текст',
  IMAGE: 'Картинка (URL)',
  VIDEO: 'Видео (URL)',
  SNAP_SNIPPET: 'Живой сниппет Snap! (XML)',
};

export function LecturesPage() {
  const [lectures, setLectures] = useState<Lecture[]>([]);
  const [selectedLecture, setSelectedLecture] = useState<Lecture | null>(null);
  const [cards, setCards] = useState<LectureCard[]>([]);

  useEffect(() => {
    listLectures().then(setLectures).catch(() => {});
  }, []);

  async function handleSelectLecture(lecture: Lecture) {
    setSelectedLecture(lecture);
    const list = await getCards(lecture.id);
    setCards(list);
  }

  return (
    <div className="page">
      <h1>Брифинги</h1>

      <div style={{ display: 'flex', gap: '24px', alignItems: 'flex-start' }}>
        <div style={{ flex: 1 }}>
          <div className="card">
            <h2 style={{ fontSize: '16px' }}>Все брифинги</h2>
            {lectures.length === 0 && <p style={{ color: 'var(--text-muted)' }}>Пока нет брифингов</p>}
            {lectures.map((l) => (
              <div
                key={l.id}
                onClick={() => handleSelectLecture(l)}
                style={{
                  padding: '8px 0',
                  borderTop: '1px solid var(--border)',
                  cursor: 'pointer',
                  fontWeight: selectedLecture?.id === l.id ? 600 : 400,
                  color: selectedLecture?.id === l.id ? 'var(--cyan)' : 'var(--text)',
                }}
              >
                {l.title} <span style={{ color: 'var(--text-muted)', fontSize: '12px' }}>#{l.id}</span>
              </div>
            ))}
          </div>
        </div>

        {selectedLecture && (
          <div style={{ flex: 1 }}>
            <div className="card">
              <h2 style={{ fontSize: '16px' }}>Карточки: «{selectedLecture.title}»</h2>
              {cards.length === 0 && <p style={{ color: 'var(--text-muted)' }}>В брифинге пока нет карточек</p>}
              {cards.map((c) => (
                <div key={c.id} style={{ padding: '8px 0', borderTop: '1px solid var(--border)' }}>
                  <span className="status-chip" style={{ marginBottom: '4px', display: 'inline-block' }}>
                    {CARD_TYPE_LABELS[c.cardType]}
                  </span>
                  <div style={{ fontSize: '13px', color: 'var(--text-muted)', whiteSpace: 'pre-wrap' }}>
                    {c.content.length > 100 ? c.content.slice(0, 100) + '...' : c.content}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
