import { useEffect, useState } from 'react';
import type { ReactNode } from 'react';
import type { NpcMessage } from '../api/npc';
import ceoAvatar from '../assets/npc/ceo.png';
import mentorAvatar from '../assets/npc/mentor.png';
import artDirectorAvatar from '../assets/npc/art_director.png';

const CHARACTER_INFO: Record<string, { avatar: string; emoji: string; name: string; color: string }> = {
  MENTOR: { avatar: mentorAvatar, emoji: '🧑‍🏫', name: 'МЕНТОР', color: 'var(--cyan)' },
  CEO: { avatar: ceoAvatar, emoji: '💼', name: 'CEO', color: 'var(--ember)' },
  ART_DIRECTOR: { avatar: artDirectorAvatar, emoji: '🎨', name: 'АРТ-ДИРЕКТОР', color: 'var(--success)' },
};

const TYPE_SPEED_MS = 25;

export function NpcPopup({
  message,
  onDismiss,
  footer,
}: {
  message: NpcMessage;
  onDismiss: () => void;
  // Необязательная замена дефолтной кнопки "Понятно" — нужно, например,
  // онбордингу, где вместо неё показывается поле ввода имени питомца.
  footer?: ReactNode;
}) {
  const [displayedText, setDisplayedText] = useState('');
  const [isTyping, setIsTyping] = useState(true);

  const info = CHARACTER_INFO[message.character] ?? { avatar: undefined, emoji: '🤖', name: 'NPC', color: 'var(--text-muted)' };

  useEffect(() => {
    setDisplayedText('');
    setIsTyping(true);
    let i = 0;
    const interval = setInterval(() => {
      i++;
      setDisplayedText(message.message.slice(0, i));
      if (i >= message.message.length) {
        clearInterval(interval);
        setIsTyping(false);
      }
    }, TYPE_SPEED_MS);
    return () => clearInterval(interval);
  }, [message.id, message.message]);

  function handleSkip() {
    if (isTyping) {
      setDisplayedText(message.message);
      setIsTyping(false);
    }
  }

  return (
    <div
      style={{
        position: 'fixed',
        left: '24px',
        right: '24px',
        bottom: '24px',
        maxWidth: '560px',
        margin: '0 auto',
        zIndex: 100,
        animation: 'node-appear 0.35s ease-out',
      }}
    >
      <div
        onClick={handleSkip}
        style={{
          display: 'flex',
          background: 'var(--surface)',
          border: `3px solid ${info.color}`,
          borderRadius: 'var(--radius-lg)',
          boxShadow: `0 12px 32px rgba(0, 0, 0, 0.18), 0 0 0 1px rgba(255,255,255,0.4)`,
          overflow: 'hidden',
          cursor: isTyping ? 'pointer' : 'default',
        }}
      >
        {/* Портрет персонажа — квадратный, в стиле RPG-диалогового окна,
            а не круглый аватар мессенджера. Пульсирует, пока идёт печать. */}
        <div
          style={{
            width: '84px',
            minWidth: '84px',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            background: 'var(--surface-2)',
            borderRight: `2px solid ${info.color}`,
            padding: '10px 4px',
          }}
        >
          {info.avatar ? (
            <img
              src={info.avatar}
              alt={info.name}
              style={{
                width: '58px',
                height: 'auto',
                imageRendering: 'pixelated',
                animation: isTyping ? 'pulse-glow 1.1s ease-in-out infinite' : 'none',
                filter: isTyping ? `drop-shadow(0 0 6px ${info.color})` : 'none',
              }}
            />
          ) : (
            <div
              style={{
                fontSize: '34px',
                animation: isTyping ? 'pulse-glow 1.1s ease-in-out infinite' : 'none',
                filter: isTyping ? `drop-shadow(0 0 6px ${info.color})` : 'none',
              }}
            >
              {info.emoji}
            </div>
          )}
        </div>

        <div style={{ flex: 1, padding: '12px 16px', display: 'flex', flexDirection: 'column' }}>
          {/* Именная табличка — моноширинная, как игровой UI-лейбл. */}
          <div
            style={{
              display: 'inline-block',
              alignSelf: 'flex-start',
              fontFamily: 'var(--font-mono)',
              fontSize: '11px',
              fontWeight: 700,
              letterSpacing: '0.08em',
              color: 'var(--on-accent)',
              background: info.color,
              padding: '3px 9px',
              borderRadius: '999px',
              marginBottom: '8px',
            }}
          >
            {info.name}
          </div>

          <div style={{ fontSize: '14px', lineHeight: 1.5, minHeight: '42px' }}>
            {displayedText}
            {isTyping && <span style={{ opacity: 0.6 }}>▌</span>}
          </div>

          <div style={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center', gap: '8px', marginTop: '8px' }}>
            {!isTyping ? (
              footer ?? (
                <button
                  onClick={(e) => { e.stopPropagation(); onDismiss(); }}
                  className="primary"
                  style={{ fontSize: '12px', padding: '5px 14px' }}
                >
                  Понятно
                </button>
              )
            ) : (
              <span style={{ fontSize: '11px', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)' }}>
                нажми, чтобы пропустить...
              </span>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
