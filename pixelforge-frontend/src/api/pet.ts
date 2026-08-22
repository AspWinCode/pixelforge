import { apiClient } from './client';

export interface PetState {
  userId: number;
  hunger: number;
  mood: number;
  energy: number;
  level: number;
  name: string | null;
}

export async function fetchPet(userId: number): Promise<PetState> {
  const { data } = await apiClient.get<PetState>(`/users/${userId}/pet`);
  return data;
}

export async function feedPet(userId: number): Promise<PetState> {
  const { data } = await apiClient.post<PetState>(`/users/${userId}/pet/feed`);
  return data;
}

export async function interactWithPet(userId: number): Promise<PetState> {
  const { data } = await apiClient.post<PetState>(`/users/${userId}/pet/interact`);
  return data;
}

export async function restPet(userId: number): Promise<PetState> {
  const { data } = await apiClient.post<PetState>(`/users/${userId}/pet/rest`);
  return data;
}

export async function namePet(userId: number, name: string): Promise<PetState> {
  const { data } = await apiClient.post<PetState>(`/users/${userId}/pet/name`, { name });
  return data;
}
