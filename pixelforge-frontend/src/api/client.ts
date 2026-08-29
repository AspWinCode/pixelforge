import axios from 'axios';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  // Нужно для сессионной куки LMS SSO (см. api/auth.ts) — без этого браузер
  // не отправит и не примет Set-Cookie для кросс-origin запроса (фронтенд
  // и бэкенд на разных портах даже в деве).
  withCredentials: true,
});

// API_ORIGIN — без /api в конце, нужен отдельно для прямых ссылок на файлы
// (например, картинки заданий), которые не проходят через apiClient.
export const API_ORIGIN = import.meta.env.VITE_API_ORIGIN;
