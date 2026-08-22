import { useEffect, useState } from 'react';
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

  return (
    <header
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '16px 24px',
        borderBottom: '1px solid var(--border)',
      }}
    >
      <Link to="/path" className="wordmark">
        Pixel<span className="accent">Forge</span>
      </Link>
      <nav style={{ display: 'flex', gap: '20px', alignItems: 'center', fontSize: '14px' }}>
        <Link id="tour-path-link" to="/path">Путь</Link>
        <Link to="/teacher">Методист</Link>
        <Link to="/trainer">Тренер</Link>
        <Link to="/lectures">Лекции</Link>
        {balance !== null && (
          <div id="tour-token-counter" className="token-counter" style={{ fontSize: '16px' }}>
            🪙 {balance}
          </div>
        )}
        {userId && <NpcInbox userId={userId} />}
      </nav>
    </header>
  );
}
