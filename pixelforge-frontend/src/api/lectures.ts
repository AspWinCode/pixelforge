import { apiClient } from './client';

// Создание лекций и карточек переехало в студию методиста на портале
// (learning-portal, /pixelforge). Здесь остались только read-эндпоинты и
// отметка прохождения учеником.

export type CardType = 'TEXT' | 'IMAGE' | 'VIDEO' | 'SNAP_SNIPPET';

export interface Lecture {
  id: number;
  title: string;
}

export interface LectureCard {
  id: number;
  position: number;
  cardType: CardType;
  content: string;
}

export async function listLectures(): Promise<Lecture[]> {
  const { data } = await apiClient.get<Lecture[]>('/lectures');
  return data;
}

export async function getCards(lectureId: number): Promise<LectureCard[]> {
  const { data } = await apiClient.get<LectureCard[]>(`/lectures/${lectureId}/cards`);
  return data;
}

export async function getCompletion(lectureId: number, userId: number): Promise<boolean> {
  const { data } = await apiClient.get<{ completed: boolean }>(`/lectures/${lectureId}/completion`, { params: { userId } });
  return data.completed;
}

export async function markComplete(lectureId: number, userId: number): Promise<void> {
  await apiClient.post(`/lectures/${lectureId}/complete`, null, { params: { userId } });
}
