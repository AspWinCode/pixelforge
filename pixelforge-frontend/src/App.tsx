import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AssignmentPage } from './pages/AssignmentPage';
import { TeacherPage } from './pages/TeacherPage';
import { ReviewPage } from './pages/ReviewPage';
import { LecturesPage } from './pages/LecturesPage';
import { LecturePage } from './pages/LecturePage';
import { PathPage } from './pages/PathPage';
import { TrainerPage } from './pages/TrainerPage';
import { SsoLandingPage } from './pages/SsoLandingPage';
import { Header } from './components/Header';
import { OnboardingFlow } from './components/OnboardingFlow';
import { useSessionStore } from './store/session';
import { useEffect, useState } from 'react';
import { fetchUser } from './api/user';

export default function App() {
  const setSession = useSessionStore((s) => s.setSession);
  const userId = useSessionStore((s) => s.userId);
  const [showOnboarding, setShowOnboarding] = useState(false);

  useEffect(() => {
    setSession({ userId: 1, role: 'STUDENT', fullName: 'Тестовый Ученик' });
  }, [setSession]);

  // Онбординг только для учеников и только пока флаг не выставлен на
  // бэкенде. Если профиль не удалось загрузить — не блокируем сайт,
  // просто не показываем онбординг в этот раз.
  useEffect(() => {
    if (!userId) return;
    fetchUser(userId)
      .then((profile) => setShowOnboarding(profile.role === 'STUDENT' && !profile.onboardingCompleted))
      .catch(() => {});
  }, [userId]);

  return (
    <BrowserRouter>
      <Header />
      <Routes>
        <Route path="/" element={<Navigate to="/path" replace />} />
        <Route path="/sso" element={<SsoLandingPage />} />
        <Route path="/assignments/:id" element={<AssignmentPage />} />
        <Route path="/teacher" element={<TeacherPage />} />
        <Route path="/teacher/review/:assignmentId/:userId" element={<ReviewPage />} />
        <Route path="/lectures" element={<LecturesPage />} />
        <Route path="/lectures/:id" element={<LecturePage />} />
        <Route path="/path" element={<PathPage />} />
        <Route path="/trainer" element={<TrainerPage />} />
      </Routes>
      {userId && showOnboarding && (
        <OnboardingFlow userId={userId} onFinish={() => setShowOnboarding(false)} />
      )}
    </BrowserRouter>
  );
}
