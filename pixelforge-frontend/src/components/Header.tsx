import { useEffect, useState } from 'react';
import type { CSSProperties } from 'react';
import { Link } from 'react-router-dom';
import { useSessionStore } from '../store/session';
import { NpcInbox } from './NpcInbox';
import { fetchBalance } from '../api/assignments';

const BALANCE_POLL_MS = 30_000;

export function Header() {
  const userId = useSessionStore((s) => s.userId);
  const [balance, setBalance] = useState<number | null>(null);

  // Баланс — общий HUD-элемент, живёт в шапке на всех страницах, а не
  // только на одной. Тот же интервал опроса, что у NpcInbox для сообщений.
  useEffect(() => {
    if (!userId) return;
    let cancelled = false;
    function refresh() {
      fetchBalance(userId!).then((b) => { if (!cancelled) setBalance(b.balance); }).catch(() => {});
    }
    refresh();
    const interval = setInterval(refresh, BALANCE_POLL_MS);
    return () => { cancelled = true; clearInterval(interval); };
  }, [userId]);

  const navLinkStyle: CSSProperties = {
    fontFamily: 'var(--font-mono)',
    fontSize: '12px',
    fontWeight: 600,
    letterSpacing: '0.04em',
    textTransform: 'uppercase',
  };

  return (
    <header style={{ borderBottom: '2px solid var(--border)' }}>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          maxWidth: '960px',
          margin: '0 auto',
          padding: '16px 24px',
        }}
      >
        <Link to="/path" className="wordmark">
          <span aria-hidden="true">👾</span>
          Pixel<span className="accent">Forge</span>
        </Link>
        <nav style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
          <Link id="tour-path-link" to="/path" style={navLinkStyle}>Путь</Link>
          <Link to="/teacher" style={navLinkStyle}>Методист</Link>
          <Link to="/trainer" style={navLinkStyle}>Тренер</Link>
          <Link to="/lectures" style={navLinkStyle}>Брифинги</Link>
          {balance !== null && (
            <div
              id="tour-token-counter"
              className="token-counter"
              style={{
                fontSize: '14px',
                padding: '5px 12px',
                borderRadius: '999px',
                border: '2px solid var(--border)',
                background: 'var(--surface-2)',
              }}
            >
              🪙 {balance}
            </div>
          )}
          {userId && <NpcInbox userId={userId} />}
        </nav>
      </div>
    </header>
  );
}
