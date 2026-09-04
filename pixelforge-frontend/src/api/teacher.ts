import { apiClient } from './client';
import type { Assignment } from './assignments';

// Создание/публикация заданий и загрузка картинок переехали в студию
// методиста на портале (learning-portal, /pixelforge). Здесь остались
// только read-эндпоинты и проверка сдач тренером.

export interface SubmissionListItem {
  id: number;
  assignmentId: number;
  userId: number;
  userFullName: string;
  status: 'IN_PROGRESS' | 'SUBMITTED' | 'REVIEWED';
}

export interface TeacherClass {
  id: number;
  name: string;
}

export async function listSubmissions(assignmentId: number): Promise<SubmissionListItem[]> {
  const { data } = await apiClient.get<SubmissionListItem[]>(`/assignments/${assignmentId}/submissions`);
  return data;
}

export async function reviewSubmission(assignmentId: number, userId: number) {
  const { data } = await apiClient.post(
    `/assignments/${assignmentId}/submissions/review`,
    null,
    { params: { userId } }
  );
  return data;
}

export async function listAllAssignments(classId: number): Promise<Assignment[]> {
  const { data } = await apiClient.get<Assignment[]>(`/classes/${classId}/assignments/all`);
  return data;
}

export async function fetchStaffClasses(userId: number, role: 'METHODIST' | 'TRAINER'): Promise<TeacherClass[]> {
  const { data } = await apiClient.get<TeacherClass[]>('/staff/classes', { params: { userId, role } });
  return data;
}
