import { useEffect, useState } from 'react';

interface TourStep {
  targetId: string;
  text: string;
}

// Идентификаторы должны совпадать с id, расставленными на реальных
// элементах интерфейса: Header.tsx (ссылка на Путь и счётчик токенов),
// NpcInbox.tsx (колокольчик), PetWidget.tsx (карточка питомца на Пути).
const TOUR_STEPS: TourStep[] = [
  { targetId: 'tour-path-link', text: 'Здесь твой Путь — теория и практика идут рука об руку.' },
  { targetId: 'tour-npc-bell', text: 'Сюда я буду присылать сообщения. Не пропусти!' },
  { targetId: 'tour-pet-widget', text: 'Загляни сюда — можно покормить и погладить питомца.' },
  { targetId: 'tour-token-counter', text: 'А тут растёт твой баланс. Трать с умом... или нет 😉' },
];

const PAD = 6;

export function CoachmarkTour({ onDone }: { onDone: () => void }) {
  const [index, setIndex] = useState(0);
  const [rect, setRect] = useState<DOMRect | null>(null);

  useEffect(() => {
    const el = document.getElementById(TOUR_STEPS[index].targetId);
    if (!el) {
      // Целевой элемент не найден (например, страница ещё не отрисовалась) —
      // не подвешиваем тур, а просто пропускаем шаг.
      if (index < TOUR_STEPS.length - 1) setIndex((i) => i + 1);
      else onDone();
      return;
    }
    el.scrollIntoView({ block: 'center' });
    setRect(el.getBoundingClientRect());

    const onResize = () => setRect(el.getBoundingClientRect());
    window.addEventListener('resize', onResize);
    return () => window.removeEventListener('resize', onResize);
  }, [index, onDone]);

  function handleNext() {
    if (index === TOUR_STEPS.length - 1) onDone();
    else setIndex((i) => i + 1);
  }

  if (!rect) return <div style={{ position: 'fixed', inset: 0, background: 'rgba(20,21,31,.7)', zIndex: 200 }} />;

  const captionLeft = Math.min(Math.max(rect.left, 12), window.innerWidth - 246);
  const captionTop = Math.min(rect.bottom + 14, window.innerHeight - 120);

  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 200 }}>
      <div
        style={{
          position: 'fixed',
          left: rect.left - PAD,
          top: rect.top - PAD,
          width: rect.width + PAD * 2,
          height: rect.height + PAD * 2,
          border: '2px solid var(--cyan)',
          borderRadius: '6px',
          boxShadow: '0 0 0 4000px rgba(20,21,31,.7), 0 0 18px rgba(78,205,196,.5)',
          transition: 'left .25s ease, top .25s ease, width .25s ease, height .25s ease',
          pointerEvents: 'none',
        }}
      />

      <div
        style={{
          position: 'fixed',
          left: `${captionLeft}px`,
          top: `${captionTop}px`,
          maxWidth: '230px',
          background: 'var(--surface)',
          border: '1px solid var(--cyan)',
          borderRadius: 'var(--radius)',
          padding: '10px 12px',
          fontSize: '13px',
          lineHeight: 1.4,
          boxShadow: '0 8px 20px rgba(0,0,0,.4)',
        }}
      >
        <div>{TOUR_STEPS[index].text}</div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginTop: '8px' }}>
          <div style={{ display: 'flex', gap: '4px' }}>
            {TOUR_STEPS.map((_, i) => (
              <div
                key={i}
                style={{
                  width: '5px',
                  height: '5px',
                  borderRadius: '50%',
                  background: i <= index ? 'var(--cyan)' : 'var(--border)',
                }}
              />
            ))}
          </div>
          <button onClick={handleNext} style={{ fontSize: '11px', padding: '5px 10px' }}>
            {index === TOUR_STEPS.length - 1 ? 'Понятно' : 'Дальше'}
          </button>
        </div>
      </div>
    </div>
  );
}
