import { useEffect, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { ssoLogin } from '../api/auth';
import { useSessionStore } from '../store/session';

// Точка входа из LMS: LMS перенаправляет сюда браузер с ?token=<JWT>.
// Мы сразу обмениваем токен на сессию и уходим на /path — токен нигде не
// сохраняется и не остаётся в адресной строке дольше одного рендера
// (navigate(..., {replace: true}) убирает /sso?token=... из истории).
export function SsoLandingPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const setSession = useSessionStore((s) => s.setSession);
  const [error, setError] = useState<string | null>(null);
  const startedRef = useRef(false);

  useEffect(() => {
    if (startedRef.current) return;
    startedRef.current = true;

    const token = searchParams.get('token');
    if (!token) {
      setError('Ссылка для входа повреждена — отсутствует токен.');
      return;
    }

    ssoLogin(token)
      .then((profile) => {
        setSession({ userId: profile.id, role: profile.role, fullName: profile.fullName });
        navigate('/path', { replace: true });
      })
      .catch(() => {
        setError('Не удалось войти. Попробуй открыть PixelForge заново из курса.');
      });
  }, [searchParams, navigate, setSession]);

  return (
    <div className="page">
      {error ? (
        <div className="message-banner error">{error}</div>
      ) : (
        <p>Входим...</p>
      )}
    </div>
  );
}
