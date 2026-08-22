import { apiClient } from './client';

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

export async function createLecture(title: string): Promise<Lecture> {
  const { data } = await apiClient.post<Lecture>('/lectures', { title });
  return data;
}

export async function listLectures(): Promise<Lecture[]> {
  const { data } = await apiClient.get<Lecture[]>('/lectures');
  return data;
}

export async function addCard(lectureId: number, cardType: CardType, content: string): Promise<LectureCard> {
  const { data } = await apiClient.post<LectureCard>(`/lectures/${lectureId}/cards`, { cardType, content });
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
