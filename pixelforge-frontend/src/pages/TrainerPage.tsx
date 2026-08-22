import { useEffect, useState } from 'react';
import { fetchStaffClasses, type TeacherClass } from '../api/teacher';
import { fetchClassStats, type ClassStats } from '../api/stats';

const HARDCODED_TRAINER_ID = 6;

const STATUS_LABELS: Record<string, string> = {
  NOT_STARTED: '—',
  IN_PROGRESS: '⏳',
  SUBMITTED: '📤',
  REVIEWED: '✅',
};

const STATUS_COLORS: Record<string, string> = {
  NOT_STARTED: 'var(--text-muted)',
  IN_PROGRESS: 'var(--ember)',
  SUBMITTED: 'var(--cyan)',
  REVIEWED: 'var(--success)',
};

export function TrainerPage() {
  const [classes, setClasses] = useState<TeacherClass[]>([]);
  const [selectedClassId, setSelectedClassId] = useState<number | null>(null);
  const [stats, setStats] = useState<ClassStats | null>(null);

  useEffect(() => {
    fetchStaffClasses(HARDCODED_TRAINER_ID, 'TRAINER').then((list) => {
      setClasses(list);
      if (list.length > 0) setSelectedClassId(list[0].id);
    }).catch(() => {});
  }, []);

  useEffect(() => {
    if (selectedClassId) {
      fetchClassStats(selectedClassId).then(setStats).catch(() => {});
    }
  }, [selectedClassId]);

  if (classes.length === 0) {
    return (
      <div className="page">
        <h1>Кабинет тренера</h1>
        <div className="message-banner">У вас пока нет ни одного класса.</div>
      </div>
    );
  }

  return (
    <div className="page">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: '16px' }}>
        <h1 style={{ marginBottom: 0 }}>Кабинет тренера</h1>
        <span className="status-chip">{classes.find((c) => c.id === selectedClassId)?.name}</span>
      </div>

      {!stats && <p>Загрузка...</p>}

      {stats && stats.assignments.length === 0 && (
        <div className="message-banner">В этом классе пока нет опубликованных заданий.</div>
      )}

      {stats && stats.assignments.length > 0 && (
        <div className="card" style={{ overflowX: 'auto' }}>
          <table style={{ borderCollapse: 'collapse', width: '100%', fontSize: '13px' }}>
            <thead>
              <tr>
                <th style={{ textAlign: 'left', padding: '8px', borderBottom: '1px solid var(--border)', position: 'sticky', left: 0, background: 'var(--surface)' }}>
                  Ученик
                </th>
                {stats.assignments.map((a) => (
                  <th
                    key={a.id}
                    style={{
                      padding: '8px',
                      borderBottom: '1px solid var(--border)',
                      fontWeight: 500,
                      color: 'var(--text-muted)',
                      minWidth: '90px',
                      maxWidth: '120px',
                      whiteSpace: 'nowrap',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                    }}
                    title={a.title}
                  >
                    {a.title}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {stats.students.map((student) => (
                <tr key={student.userId}>
                  <td style={{ padding: '8px', borderBottom: '1px solid var(--border)', position: 'sticky', left: 0, background: 'var(--surface)' }}>
                    {student.fullName}
                  </td>
                  {stats.assignments.map((a) => {
                    const status = student.statusByAssignmentId[String(a.id)] ?? 'NOT_STARTED';
                    return (
                      <td
                        key={a.id}
                        style={{
                          padding: '8px',
                          borderBottom: '1px solid var(--border)',
                          textAlign: 'center',
                          color: STATUS_COLORS[status],
                          fontSize: '16px',
                        }}
                        title={status}
                      >
                        {STATUS_LABELS[status] ?? status}
                      </td>
                    );
                  })}
                </tr>
              ))}
            </tbody>
          </table>

          <div style={{ display: 'flex', gap: '16px', marginTop: '12px', fontSize: '12px', color: 'var(--text-muted)' }}>
            <span>— не начато</span>
            <span style={{ color: 'var(--ember)' }}>⏳ в процессе</span>
            <span style={{ color: 'var(--cyan)' }}>📤 сдано</span>
            <span style={{ color: 'var(--success)' }}>✅ проверено</span>
          </div>
        </div>
      )}
    </div>
  );
}
