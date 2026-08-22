import { useEffect, useState } from 'react';
import { fetchPet, feedPet, interactWithPet, restPet, type PetState } from '../api/pet';

function StatBar({ label, value, color }: { label: string; value: number; color: string }) {
  return (
    <div style={{ marginBottom: '6px' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: 'var(--text-muted)' }}>
        <span>{label}</span>
        <span>{value}%</span>
      </div>
      <div style={{ height: '6px', background: 'var(--surface-2)', borderRadius: '3px', overflow: 'hidden' }}>
        <div style={{ height: '100%', width: `${value}%`, background: color, transition: 'width 0.3s' }} />
      </div>
    </div>
  );
}

export function PetWidget({ userId }: { userId: number }) {
  const [pet, setPet] = useState<PetState | null>(null);

  useEffect(() => {
    fetchPet(userId).then(setPet).catch(() => {});
  }, [userId]);

  async function handleFeed() {
    setPet(await feedPet(userId));
  }

  async function handleInteract() {
    setPet(await interactWithPet(userId));
  }

  async function handleRest() {
    setPet(await restPet(userId));
  }

  if (!pet) return null;

  return (
    <div id="tour-pet-widget" className="card" style={{ maxWidth: '260px' }}>
      <div style={{ fontWeight: 600, marginBottom: '10px' }}>
        🐾 {pet.name ?? 'Питомец'} — уровень {pet.level}
      </div>
      <StatBar label="Сытость" value={pet.hunger} color="var(--ember)" />
      <StatBar label="Настроение" value={pet.mood} color="var(--cyan)" />
      <StatBar label="Энергия" value={pet.energy} color="var(--success)" />
      <div style={{ display: 'flex', gap: '8px', marginTop: '10px', flexWrap: 'wrap' }}>
        <button onClick={handleFeed}>Покормить</button>
        <button onClick={handleInteract}>Погладить</button>
        <button onClick={handleRest}>Уложить спать</button>
      </div>
    </div>
  );
}
