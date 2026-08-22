import { apiClient } from './client';

export interface NpcMessage {
  id: number;
  character: 'MENTOR' | 'CEO' | 'ART_DIRECTOR';
  message: string;
  isRead: boolean;
  createdAt: string;
}

export async function fetchNpcMessages(userId: number): Promise<NpcMessage[]> {
  const { data } = await apiClient.get<NpcMessage[]>(`/users/${userId}/npc-messages`);
  return data;
}

export async function markNpcMessageRead(messageId: number) {
  await apiClient.post(`/npc-messages/${messageId}/read`);
}
