import { useRef, useState } from 'react';
import { NpcPopup } from './NpcPopup';
import { CoachmarkTour } from './CoachmarkTour';
import { launchConfetti } from '../utils/confetti';
import { completeOnboarding } from '../api/user';
import { namePet } from '../api/pet';
import type { NpcMessage } from '../api/npc';

type Step = 'ceo' | 'mentor' | 'pet' | 'tour' | 'reward-intro' | 'reward-card';

function scriptedMessage(id: number, character: NpcMessage['character'], message: string): NpcMessage {
  return { id, character, message, isRead: false, createdAt: new Date().toISOString() };
}

const BTN_SMALL = { fontSize: '12px', padding: '5px 14px' };

// Первый визит на сайт: оффер от CEO → знакомство с наставником → имя
// питомца → тур по интерфейсу → награда. Флаг onboardingCompleted на
// User — источник правды, компонент размонтируется через onFinish,
// дальше ребёнок просто пользуется уже отрисованным под ним сайтом.
export function OnboardingFlow({ userId, onFinish }: { userId: number; onFinish: () => void }) {
  const [step, setStep] = useState<Step>('ceo');
  const [petNameInput, setPetNameInput] = useState('');
  const [petSubmitting, setPetSubmitting] = useState(false);
  const [reward, setReward] = useState<{ balance: number; rankDisplayName: string } | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);

  async function handleSavePetName() {
    const name = petNameInput.trim() || 'Пиксель';
    setPetSubmitting(true);
    try {
      await namePet(userId, name);
    } catch {
      // Не критично для прохождения онбординга — карточка питомца просто
      // покажет дефолтное "Питомец", пока имя не сохранится при следующей попытке.
    } finally {
      setPetSubmitting(false);
      setStep('tour');
    }
  }

  async function handleClaimReward() {
    setSubmitting(true);
    setError(null);
    try {
      const result = await completeOnboarding(userId);
      setReward(result);
      setStep('reward-card');
      if (canvasRef.current) launchConfetti(canvasRef.current);
    } catch {
      setError('Не удалось сохранить прогресс — попробуй ещё раз.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div style={{ position: 'fixed', inset: 0, zIndex: 150 }}>
      {step !== 'tour' && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(20,21,31,.55)' }} />
      )}

      {step === 'ceo' && (
        <NpcPopup
          message={scriptedMessage(
            1,
            'CEO',
            'Так, слушай сюда. У нас в PixelForge не хватает толкового разработчика. Работа непростая — зато с личным питомцем, тоннами токенов и поводом похвастаться перед друзьями. По рукам?'
          )}
          onDismiss={() => setStep('mentor')}
          footer={<button className="primary" style={BTN_SMALL} onClick={() => setStep('mentor')}>Подписать контракт ✍️</button>}
        />
      )}

      {step === 'mentor' && (
        <NpcPopup
          message={scriptedMessage(
            2,
            'MENTOR',
            'Приветствую! Я твой наставник — буду рядом на каждом шаге. Всё просто: сперва короткая теория, потом сразу практика в мастерской. Готов?'
          )}
          onDismiss={() => setStep('pet')}
          footer={<button className="primary" style={BTN_SMALL} onClick={() => setStep('pet')}>Погнали →</button>}
        />
      )}

      {step === 'pet' && (
        <NpcPopup
          message={scriptedMessage(
            3,
            'ART_DIRECTOR',
            'А это — твой личный маскот. Будет расти вместе с тобой, уровень за уровнем. Как его зовут?'
          )}
          onDismiss={handleSavePetName}
          footer={
            <>
              <input
                autoFocus
                type="text"
                maxLength={16}
                placeholder="Введи имя..."
                value={petNameInput}
                onChange={(e) => setPetNameInput(e.target.value)}
                onKeyDown={(e) => { if (e.key === 'Enter') handleSavePetName(); }}
                style={{ flex: 1, minWidth: 0, marginRight: 0 }}
              />
              <button className="primary" style={BTN_SMALL} disabled={petSubmitting} onClick={handleSavePetName}>
                {petSubmitting ? '...' : 'Готово ✓'}
              </button>
            </>
          }
        />
      )}

      {step === 'tour' && <CoachmarkTour onDone={() => setStep('reward-intro')} />}

      {step === 'reward-intro' && (
        <NpcPopup
          message={scriptedMessage(4, 'CEO', 'Ты прошёл вводный день — уже неплохо! Вот твоя первая зарплата.')}
          onDismiss={handleClaimReward}
          footer={
            <button className="primary" style={BTN_SMALL} disabled={submitting} onClick={handleClaimReward}>
              {submitting ? 'Секунду...' : 'Забрать награду 🎁'}
            </button>
          }
        />
      )}

      {step === 'reward-card' && reward && (
        <div
          style={{
            position: 'fixed',
            left: '50%',
            top: '50%',
            transform: 'translate(-50%, -50%)',
            width: 'min(340px, 84vw)',
            background: 'var(--surface)',
            border: '2px solid var(--success)',
            borderRadius: '6px',
            padding: '22px 20px',
            textAlign: 'center',
            boxShadow: '0 20px 50px rgba(0,0,0,.55)',
            zIndex: 160,
          }}
        >
          <div
            style={{
              fontFamily: 'var(--font-mono)',
              fontSize: '34px',
              fontWeight: 700,
              color: 'var(--ember)',
              textShadow: '0 0 16px rgba(255,107,53,.4)',
              fontVariantNumeric: 'tabular-nums',
            }}
          >
            +15 🪙
          </div>
          <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '2px' }}>
            баланс: {reward.balance} 🪙
          </div>
          <div
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: '6px',
              fontFamily: 'var(--font-mono)',
              fontSize: '12px',
              fontWeight: 600,
              letterSpacing: '.04em',
              textTransform: 'uppercase',
              padding: '5px 12px',
              borderRadius: '2px',
              border: '1px solid var(--success)',
              color: 'var(--success)',
              margin: '14px 0 16px',
            }}
          >
            🥉 Ранг: {reward.rankDisplayName}
          </div>
          <button className="primary" style={{ width: '100%' }} onClick={onFinish}>
            Начать путь →
          </button>
        </div>
      )}

      {error && (
        <div
          className="message-banner error"
          style={{ position: 'fixed', left: '50%', bottom: '24px', transform: 'translateX(-50%)', zIndex: 170 }}
        >
          {error}
        </div>
      )}

      <canvas ref={canvasRef} style={{ position: 'fixed', inset: 0, zIndex: 165, pointerEvents: 'none' }} />
    </div>
  );
}
