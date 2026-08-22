import { apiClient } from './client';
import type { Assignment } from './assignments';

export interface CreateAssignmentPayload {
  classId: number;
  lectureId: number | null;
  title: string;
  description: string;
  tool: 'SNAP';
  deadline: string | null;
}

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

export async function createAssignment(payload: CreateAssignmentPayload): Promise<Assignment> {
  const { data } = await apiClient.post<Assignment>('/assignments', payload);
  return data;
}

export async function publishAssignment(assignmentId: number): Promise<Assignment> {
  const { data } = await apiClient.post<Assignment>(`/assignments/${assignmentId}/publish`);
  return data;
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

export async function uploadAssignmentImage(assignmentId: number, file: File) {
  const formData = new FormData();
  formData.append('file', file);
  const { data } = await apiClient.post(`/assignments/${assignmentId}/images`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data;
}

export async function listAllAssignments(classId: number): Promise<Assignment[]> {
  const { data } = await apiClient.get<Assignment[]>(`/classes/${classId}/assignments/all`);
  return data;
}

export async function fetchMyClasses(teacherId: number): Promise<TeacherClass[]> {
  const { data } = await apiClient.get<TeacherClass[]>('/teachers/classes', {
    params: { teacherId },
  });
  return data;
}

export async function fetchStaffClasses(userId: number, role: 'METHODIST' | 'TRAINER'): Promise<TeacherClass[]> {
  const { data } = await apiClient.get<TeacherClass[]>('/staff/classes', { params: { userId, role } });
  return data;
}
