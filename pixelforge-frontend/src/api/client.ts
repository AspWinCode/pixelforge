import axios from 'axios';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
});

// API_ORIGIN — без /api в конце, нужен отдельно для прямых ссылок на файлы
// (например, картинки заданий), которые не проходят через apiClient.
export const API_ORIGIN = import.meta.env.VITE_API_ORIGIN;
