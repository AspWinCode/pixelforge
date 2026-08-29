import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchPath, type PathNode } from '../api/path';
import { useSessionStore } from '../store/session';
import { PetWidget } from '../components/PetWidget';

const HARDCODED_CLASS_ID = 1;

const NODE_SIZE = 84;
const VERTICAL_GAP = 140;
const CONTAINER_WIDTH = 500;
const CENTER_X = CONTAINER_WIDTH / 2;
// Цикл смещений по горизонтали — создаёт змейку влево-вправо-влево вместо
// прямой линии. Значения в пикселях от центра контейнера.
const ZIGZAG_OFFSETS = [0, -160, 0, 160];

function nodeIcon(node: PathNode): string {
  if (node.locked) return '🔒';
  if (node.completed) return node.type === 'LECTURE' ? '📖' : '🏆';
  return node.type === 'LECTURE' ? '📖' : '🎮';
}

function nodeColor(node: PathNode, isCurrent: boolean): string {
  if (node.locked) return 'var(--border)';
  if (node.completed) return 'var(--success)';
  if (isCurrent) return 'var(--cyan)';
  return 'var(--ember)';
}

export function PathPage() {
  const [nodes, setNodes] = useState<PathNode[]>([]);
  const [loading, setLoading] = useState(true);
  const userId = useSessionStore((s) => s.userId);
  const navigate = useNavigate();

  useEffect(() => {
    if (!userId) return;
    fetchPath(HARDCODED_CLASS_ID, userId).then(setNodes).catch(() => {}).finally(() => setLoading(false));
  }, [userId]);

  function handleClick(node: PathNode) {
    if (node.locked) return;
    if (node.type === 'LECTURE') {
      navigate(`/lectures/${node.targetId}`);
    } else {
      navigate(`/assignments/${node.targetId}`);
    }
  }

  if (loading) return <div className="page">Загрузка...</div>;

  const completedCount = nodes.filter((n) => n.completed).length;
  const progressPct = nodes.length > 0 ? Math.round((completedCount / nodes.length) * 100) : 0;
  const currentIndex = nodes.findIndex((n) => !n.completed && !n.locked);

  // Координаты центра каждого узла — вычисляем один раз, используем и для
  // позиционирования кружков, и для рисования соединительных линий.
  const positions = nodes.map((_, i) => ({
    x: CENTER_X + ZIGZAG_OFFSETS[i % ZIGZAG_OFFSETS.length],
    y: i * VERTICAL_GAP + NODE_SIZE / 2,
  }));

  const containerHeight = nodes.length > 0 ? (nodes.length - 1) * VERTICAL_GAP + NODE_SIZE : 0;

  return (
    <div className="page">
      <h1>Путь</h1>

      <div style={{ display: 'flex', gap: '28px', alignItems: 'flex-start', flexWrap: 'wrap' }}>
        <div style={{ flex: '1 1 480px', maxWidth: '700px' }}>
          {nodes.length > 0 && (
            <div className="card" style={{ marginBottom: '32px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '13px', color: 'var(--text-muted)', marginBottom: '6px' }}>
                <span>Прогресс</span>
                <span className="token-counter" style={{ fontSize: '14px' }}>{completedCount} / {nodes.length}</span>
              </div>
              <div style={{ height: '8px', background: 'var(--surface-2)', borderRadius: '4px', overflow: 'hidden' }}>
                <div
                  style={{
                    height: '100%',
                    width: `${progressPct}%`,
                    background: 'linear-gradient(90deg, var(--cyan), var(--success))',
                    transition: 'width 0.5s ease',
                  }}
                />
              </div>
            </div>
          )}

          {nodes.length === 0 && <div className="message-banner">Пока нет заданий на пути.</div>}

          {nodes.length > 0 && (
            <div style={{ position: 'relative', width: `${CONTAINER_WIDTH}px`, height: `${containerHeight}px`, margin: '0 auto' }}>
              {/* Соединительные линии — рисуются ПОД кружками, диагональю
                  между центром предыдущего и текущего узла. */}
              {positions.map((pos, i) => {
                if (i === 0) return null;
                const prev = positions[i - 1];
                const dx = pos.x - prev.x;
                const dy = pos.y - prev.y;
                const length = Math.sqrt(dx * dx + dy * dy);
                const angle = Math.atan2(dy, dx) * (180 / Math.PI);
                const lineColor = nodes[i - 1].completed ? 'var(--success)' : 'var(--border)';

                return (
                  <div
                    key={`line-${i}`}
                    style={{
                      position: 'absolute',
                      left: `${prev.x}px`,
                      top: `${prev.y}px`,
                      width: `${length}px`,
                      height: '3px',
                      background: `repeating-linear-gradient(90deg, ${lineColor} 0, ${lineColor} 8px, transparent 8px, transparent 16px)`,
                      transformOrigin: '0 0',
                      transform: `rotate(${angle}deg)`,
                      zIndex: 1,
                    }}
                  />
                );
              })}

              {nodes.map((node, i) => {
                const isCurrent = i === currentIndex;
                const color = nodeColor(node, isCurrent);
                const pos = positions[i];

                return (
                  <div
                    key={`${node.type}-${node.targetId}`}
                    className={`path-node-enter ${isCurrent ? 'path-node-current' : ''}`}
                    style={{
                      position: 'absolute',
                      left: `${pos.x - NODE_SIZE / 2}px`,
                      top: `${pos.y - NODE_SIZE / 2}px`,
                      animationDelay: `${i * 70}ms`,
                      zIndex: 2,
                      display: 'flex',
                      flexDirection: 'column',
                      alignItems: 'center',
                      width: `${NODE_SIZE}px`,
                    }}
                  >
                    <div
                      onClick={() => handleClick(node)}
                      title={node.title}
                      style={{
                        width: `${NODE_SIZE}px`,
                        height: `${NODE_SIZE}px`,
                        borderRadius: '50%',
                        background: node.completed ? 'var(--surface-2)' : 'var(--surface)',
                        border: `3px solid ${color}`,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        fontSize: '32px',
                        cursor: node.locked ? 'not-allowed' : 'pointer',
                        opacity: node.locked ? 0.5 : 1,
                        transition: 'transform 0.15s ease',
                        boxShadow: isCurrent ? `0 0 20px ${color}55` : 'none',
                      }}
                      onMouseEnter={(e) => { if (!node.locked) e.currentTarget.style.transform = 'scale(1.08)'; }}
                      onMouseLeave={(e) => { e.currentTarget.style.transform = 'scale(1)'; }}
                    >
                      {nodeIcon(node)}
                    </div>
                    <div
                      style={{
                        textAlign: 'center',
                        marginTop: '8px',
                        fontSize: '13px',
                        fontWeight: isCurrent ? 700 : 500,
                        color: node.locked ? 'var(--text-muted)' : 'var(--text)',
                        whiteSpace: 'nowrap',
                      }}
                    >
                      {node.title}
                    </div>
                    <div
                      style={{
                        fontSize: '10px',
                        color: 'var(--text-muted)',
                        fontFamily: 'var(--font-mono)',
                        textTransform: 'uppercase',
                        letterSpacing: '0.05em',
                      }}
                    >
                      {node.type === 'LECTURE' ? 'брифинг' : 'задание'}
                    </div>
                  </div>
                );
              })}
            </div>
          )}

          {completedCount === nodes.length && nodes.length > 0 && (
            <div className="card" style={{ textAlign: 'center', marginTop: '16px', borderColor: 'var(--success)' }}>
              🎉 Весь путь пройден! Загляни за новыми заданиями позже.
            </div>
          )}
        </div>

        {userId && (
          <div style={{ flex: '0 0 auto', position: 'sticky', top: '24px' }}>
            <p style={{ fontSize: '12px', color: 'var(--text-muted)', fontFamily: 'var(--font-mono)', margin: '0 0 8px', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Твой напарник
            </p>
            <PetWidget userId={userId} />
          </div>
        )}
      </div>
    </div>
  );
}
