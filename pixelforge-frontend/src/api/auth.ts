import { apiClient } from './client';
import type { UserProfile } from './user';

// Одноразовый обмен launch-токена от LMS на обычную PixelForge-сессию
// (см. AuthController#ssoLogin на бэкенде). После этого вызова сессия
// живёт в HttpOnly-куке — сам токен фронтенду больше не нужен и не должен
// нигде сохраняться (localStorage и т.п.).
export async function ssoLogin(token: string): Promise<UserProfile> {
  const { data } = await apiClient.post<UserProfile>('/auth/lms-sso', { token });
  return data;
}

export async function fetchMe(): Promise<UserProfile> {
  const { data } = await apiClient.get<UserProfile>('/auth/me');
  return data;
}

export async function logout(): Promise<void> {
  await apiClient.post('/auth/logout');
}
