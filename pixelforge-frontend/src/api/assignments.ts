import { apiClient } from './client';

export interface Assignment {
  id: number;
  classId: number;
  title: string;
  description: string | null;
  tool: string;
  status: 'DRAFT' | 'PUBLISHED';
  deadline: string | null;
  createdAt: string;
}

export interface AssignmentImage {
  id: number;
  originalName: string;
  url: string;
}

export interface Submission {
  id: number;
  assignmentId: number;
  userId: number;
  s3Key: string | null;
  status: 'IN_PROGRESS' | 'SUBMITTED' | 'REVIEWED';
  createdAt: string;
  updatedAt: string;
}

export interface Balance {
  userId: number;
  balance: number;
  rank: string;
  rankDisplayName: string;
}

export async function fetchAssignment(assignmentId: number): Promise<Assignment> {
  const { data } = await apiClient.get<Assignment>(`/assignments/${assignmentId}`);
  return data;
}

export async function fetchAssignmentImages(assignmentId: number): Promise<AssignmentImage[]> {
  const { data } = await apiClient.get<AssignmentImage[]>(`/assignments/${assignmentId}/images`);
  return data;
}

export async function startSubmission(assignmentId: number, userId: number): Promise<Submission> {
  const { data } = await apiClient.post<Submission>(
    `/assignments/${assignmentId}/submissions/start`,
    null,
    { params: { userId } }
  );
  return data;
}

export async function getSavedProject(assignmentId: number, userId: number): Promise<string | null> {
  const { data } = await apiClient.get<{ xml: string | null }>(
    `/assignments/${assignmentId}/submissions/project`,
    { params: { userId } }
  );
  return data.xml;
}

export async function saveProject(assignmentId: number, userId: number, xml: string): Promise<Submission> {
  const { data } = await apiClient.post<Submission>(
    `/assignments/${assignmentId}/submissions/save`,
    { xml },
    { params: { userId } }
  );
  return data;
}

export async function submitAssignment(assignmentId: number, userId: number): Promise<Submission> {
  const { data } = await apiClient.post<Submission>(
    `/assignments/${assignmentId}/submissions/submit`,
    null,
    { params: { userId } }
  );
  return data;
}

export async function fetchBalance(userId: number): Promise<Balance> {
  const { data } = await apiClient.get<Balance>(`/users/${userId}/balance`);
  return data;
}
