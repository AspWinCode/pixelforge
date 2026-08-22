import { create } from 'zustand';

// Пока нет реального SSO — храним минимум, необходимый для работы с API.
// userId сейчас передаётся в каждый запрос вручную (как мы делали через curl),
// когда появится настоящая сессия — источник тот же самый стор, просто
// заполняться будет из ответа /api/auth/lms-sso, а не руками.
interface SessionState {
  userId: number | null;
  role: string | null;
  fullName: string | null;
  setSession: (session: { userId: number; role: string; fullName: string }) => void;
  clearSession: () => void;
}

export const useSessionStore = create<SessionState>((set) => ({
  userId: null,
  role: null,
  fullName: null,
  setSession: ({ userId, role, fullName }) => set({ userId, role, fullName }),
  clearSession: () => set({ userId: null, role: null, fullName: null }),
}));
