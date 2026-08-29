import { useEffect, useState } from 'react';
import { createLecture, listLectures, addCard, getCards, type Lecture, type LectureCard, type CardType } from '../api/lectures';

const CARD_TYPE_LABELS: Record<CardType, string> = {
  TEXT: 'Текст',
  IMAGE: 'Картинка (URL)',
  VIDEO: 'Видео (URL)',
  SNAP_SNIPPET: 'Живой сниппет Snap! (XML)',
};

export function LecturesPage() {
  const [lectures, setLectures] = useState<Lecture[]>([]);
  const [newTitle, setNewTitle] = useState('');
  const [selectedLecture, setSelectedLecture] = useState<Lecture | null>(null);
  const [cards, setCards] = useState<LectureCard[]>([]);
  const [cardType, setCardType] = useState<CardType>('TEXT');
  const [cardContent, setCardContent] = useState('');
  const [message, setMessage] = useState('');

  useEffect(() => {
    listLectures().then(setLectures).catch(() => {});
  }, []);

  async function handleCreateLecture() {
    if (!newTitle.trim()) return;
    const lecture = await createLecture(newTitle);
    setLectures((prev) => [...prev, lecture]);
    setNewTitle('');
    setSelectedLecture(lecture);
    setCards([]);
  }

  async function handleSelectLecture(lecture: Lecture) {
    setSelectedLecture(lecture);
    const list = await getCards(lecture.id);
    setCards(list);
  }

  async function handleAddCard() {
    if (!selectedLecture || !cardContent.trim()) return;
    const card = await addCard(selectedLecture.id, cardType, cardContent);
    setCards((prev) => [...prev, card]);
    setCardContent('');
    setMessage('Карточка добавлена');
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
      <h1>Брифинги</h1>
      {message && <div className="message-banner">{message}</div>}

      <div style={{ display: 'flex', gap: '24px', alignItems: 'flex-start' }}>
        <div style={{ flex: 1 }}>
          <div className="card">
            <h2 style={{ fontSize: '16px' }}>Создать брифинг</h2>
            <div style={{ display: 'flex', gap: '8px' }}>
              <input
                value={newTitle}
                onChange={(e) => setNewTitle(e.target.value)}
                placeholder="Название брифинга"
                style={{ ...inputStyle, flex: 1 }}
              />
              <button className="primary" onClick={handleCreateLecture}>Создать</button>
            </div>
          </div>

          <div className="card">
            <h2 style={{ fontSize: '16px' }}>Все брифинги</h2>
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

              <div style={{ marginTop: '12px', paddingTop: '12px', borderTop: '1px solid var(--border)' }}>
                <select
                  value={cardType}
                  onChange={(e) => setCardType(e.target.value as CardType)}
                  style={{ ...inputStyle, marginBottom: '8px' }}
                >
                  {Object.entries(CARD_TYPE_LABELS).map(([key, label]) => (
                    <option key={key} value={key}>{label}</option>
                  ))}
                </select>
                <textarea
                  value={cardContent}
                  onChange={(e) => setCardContent(e.target.value)}
                  placeholder={cardType === 'TEXT' ? 'Текст карточки' : cardType === 'SNAP_SNIPPET' ? 'XML сниппета' : 'URL'}
                  rows={4}
                  style={{ ...inputStyle, resize: 'vertical', marginBottom: '8px' }}
                />
                <button className="primary" onClick={handleAddCard}>Добавить карточку</button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
