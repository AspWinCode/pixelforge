const COLORS = ['#FF6B35', '#4ECDC4', '#6FCF97', '#F2E9DC'];

export function launchConfetti(canvas: HTMLCanvasElement) {
  const ctx = canvas.getContext('2d');
  if (!ctx) return;

  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;

  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return;

  const particles = Array.from({ length: 90 }, () => ({
    x: canvas.width / 2 + (Math.random() - 0.5) * 80,
    y: canvas.height * 0.25,
    vx: (Math.random() - 0.5) * 7,
    vy: -Math.random() * 7 - 2,
    size: 4 + Math.random() * 4,
    color: COLORS[Math.floor(Math.random() * COLORS.length)],
    rot: Math.random() * Math.PI,
    vrot: (Math.random() - 0.5) * 0.3,
  }));

  let frame = 0;
  function step() {
    frame++;
    ctx!.clearRect(0, 0, canvas.width, canvas.height);
    particles.forEach((p) => {
      p.vy += 0.18;
      p.x += p.vx;
      p.y += p.vy;
      p.rot += p.vrot;
      ctx!.save();
      ctx!.translate(p.x, p.y);
      ctx!.rotate(p.rot);
      ctx!.fillStyle = p.color;
      ctx!.globalAlpha = Math.max(0, 1 - frame / 110);
      ctx!.fillRect(-p.size / 2, -p.size / 2, p.size, p.size);
      ctx!.restore();
    });
    if (frame < 110) {
      requestAnimationFrame(step);
    } else {
      ctx!.clearRect(0, 0, canvas.width, canvas.height);
    }
  }
  requestAnimationFrame(step);
}
