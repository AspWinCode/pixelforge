import { apiClient } from './client';

export interface AssignmentColumn {
  id: number;
  title: string;
}

export interface StudentRow {
  userId: number;
  fullName: string;
  statusByAssignmentId: Record<string, string>;
}

export interface ClassStats {
  assignments: AssignmentColumn[];
  students: StudentRow[];
}

export async function fetchClassStats(classId: number): Promise<ClassStats> {
  const { data } = await apiClient.get<ClassStats>(`/classes/${classId}/stats`);
  return data;
}
