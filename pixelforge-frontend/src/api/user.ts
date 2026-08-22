import { apiClient } from './client';

export interface UserProfile {
  id: number;
  fullName: string;
  role: string;
  onboardingCompleted: boolean;
}

export interface OnboardingCompleteResult {
  balance: number;
  rankDisplayName: string;
}

export async function fetchUser(userId: number): Promise<UserProfile> {
  const { data } = await apiClient.get<UserProfile>(`/users/${userId}`);
  return data;
}

export async function completeOnboarding(userId: number): Promise<OnboardingCompleteResult> {
  const { data } = await apiClient.post<OnboardingCompleteResult>(`/users/${userId}/onboarding/complete`);
  return data;
}
