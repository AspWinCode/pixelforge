import { useEffect, useRef, useState } from 'react';
import { fetchNpcMessages, markNpcMessageRead, type NpcMessage } from '../api/npc';
import { NpcPopup } from './NpcPopup';
import { playNotificationSound } from '../utils/sound';

const CHARACTER_EMOJI: Record<string, string> = {
  MENTOR: '🧑‍🏫',
  CEO: '💼',
  ART_DIRECTOR: '🎨',
};

export function NpcInbox({ userId }: { userId: number }) {
  const [messages, setMessages] = useState<NpcMessage[]>([]);
  const [open, setOpen] = useState(false);
  const [popupMessage, setPopupMessage] = useState<NpcMessage | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  // Запоминаем ID уже виденных сообщений, чтобы не показывать попап
  // повторно для того же события при каждом поллинге.
  const seenIdsRef = useRef<Set<number>>(new Set());
  const isFirstLoadRef = useRef(true);

  async function refresh() {
    try {
      const list = await fetchNpcMessages(userId);

      if (isFirstLoadRef.current) {
        // При самой первой загрузке просто запоминаем всё, что уже есть,
        // не показываем попап для старых сообщений с прошлых сессий.
        list.forEach((m) => seenIdsRef.current.add(m.id));
        isFirstLoadRef.current = false;
      } else {
        const newUnread = list.find((m) => !m.isRead && !seenIdsRef.current.has(m.id));
        if (newUnread) {
          seenIdsRef.current.add(newUnread.id);
          setPopupMessage(newUnread);
          playNotificationSound();
        }
      }

      setMessages(list);
    } catch {
      // не критично
    }
  }

  useEffect(() => {
    refresh();
    const interval = setInterval(refresh, 30_000);
    return () => clearInterval(interval);
  }, [userId]);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const unreadCount = messages.filter((m) => !m.isRead).length;

  async function handleMessageClick(message: NpcMessage) {
    if (!message.isRead) {
      await markNpcMessageRead(message.id);
      setMessages((prev) => prev.map((m) => (m.id === message.id ? { ...m, isRead: true } : m)));
    }
  }

  async function handlePopupDismiss() {
    if (popupMessage) {
      await markNpcMessageRead(popupMessage.id);
      setMessages((prev) => prev.map((m) => (m.id === popupMessage.id ? { ...m, isRead: true } : m)));
    }
    setPopupMessage(null);
  }

  return (
    <>
      <div ref={containerRef} style={{ position: 'relative' }}>
        <button id="tour-npc-bell" onClick={() => setOpen((prev) => !prev)} style={{ position: 'relative' }}>
          🔔
          {unreadCount > 0 && (
            <span
              style={{
                position: 'absolute', top: '-4px', right: '-4px',
                background: 'var(--danger)', color: '#14151F',
                borderRadius: '50%', width: '16px', height: '16px',
                fontSize: '10px', fontFamily: 'var(--font-mono)',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
              }}
            >
              {unreadCount}
            </span>
          )}
        </button>

        {open && (
          <div
            className="card"
            style={{ position: 'absolute', right: 0, top: '40px', width: '320px', maxHeight: '400px', overflowY: 'auto', zIndex: 10 }}
          >
            {messages.length === 0 && <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>Пока нет сообщений</p>}
            {messages.map((m) => (
              <div
                key={m.id}
                onClick={() => handleMessageClick(m)}
                style={{ padding: '8px 0', borderBottom: '1px solid var(--border)', opacity: m.isRead ? 0.6 : 1, cursor: 'pointer' }}
              >
                <div style={{ fontSize: '13px' }}>
                  {CHARACTER_EMOJI[m.character] ?? '🤖'} {m.message}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {popupMessage && <NpcPopup message={popupMessage} onDismiss={handlePopupDismiss} />}
    </>
  );
}
