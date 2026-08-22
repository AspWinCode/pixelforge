import { apiClient } from './client';

export interface PathNode {
  type: 'LECTURE' | 'ASSIGNMENT';
  targetId: number;
  title: string;
  completed: boolean;
  locked: boolean;
}

export async function fetchPath(classId: number, userId: number): Promise<PathNode[]> {
  const { data } = await apiClient.get<PathNode[]>(`/classes/${classId}/path`, { params: { userId } });
  return data;
}
