let audioContext: AudioContext | null = null;

function getAudioContext(): AudioContext {
  if (!audioContext) {
    audioContext = new AudioContext();
  }
  return audioContext;
}

export function playNotificationSound() {
  try {
    const ctx = getAudioContext();
    const now = ctx.currentTime;

    playTone(ctx, 880, now, 0.1, 0.06);
    playTone(ctx, 1174, now + 0.09, 0.12, 0.06);
  } catch {
    // Web Audio может быть заблокирован до первого взаимодействия
    // пользователя со страницей — тихо игнорируем.
  }
}

function playTone(ctx: AudioContext, frequency: number, startTime: number, duration: number, volume: number) {
  const oscillator = ctx.createOscillator();
  const gainNode = ctx.createGain();

  oscillator.type = 'sine';
  oscillator.frequency.value = frequency;

  gainNode.gain.setValueAtTime(volume, startTime);
  gainNode.gain.exponentialRampToValueAtTime(0.001, startTime + duration);

  oscillator.connect(gainNode);
  gainNode.connect(ctx.destination);

  oscillator.start(startTime);
  oscillator.stop(startTime + duration);
}
