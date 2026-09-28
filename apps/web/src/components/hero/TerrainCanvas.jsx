import { useEffect, useRef } from 'react';

const LOOP = 28300; // sum of scene durations
const BG = '#030407';
const L = (() => {
  const v = [-0.55, 0.75, -0.35];
  const m = Math.hypot(...v);
  return v.map(x => x / m);
})();

function terrainHeight(x, z, t) {
  const ridge =
    Math.exp(-Math.pow(x + 0.35 - 0.25 * Math.sin(t), 2) * 1.6) *
    (0.4 + 0.22 * Math.sin(0.55 * z - t + 0.8));
  return (
    0.34 * Math.sin(1.05 * x + 0.42 * z + t) +
    0.22 * Math.sin(0.62 * x - 0.78 * z + 2 * t + 1.3) +
    0.12 * Math.sin(1.9 * x + 1.4 * z - t + 2.1) +
    0.06 * Math.sin(3.2 * x - 2.1 * z + 3 * t + 0.4) +
    ridge
  );
}

export default function TerrainCanvas({ playing }) {
  const canvasRef = useRef(null);
  const stateRef = useRef({ W: 0, H: 0, clock: 0, last: 0, rafId: null });
  const playingRef = useRef(playing);

  useEffect(() => {
    playingRef.current = playing;
    if (!playing) stateRef.current.last = 0;
  }, [playing]);

  useEffect(() => {
    const cv = canvasRef.current;
    const g = cv.getContext('2d');
    const s = stateRef.current;

    function size() {
      const dpr = Math.min(window.devicePixelRatio || 1, 1.5);
      s.W = cv.clientWidth;
      s.H = cv.clientHeight;
      cv.width = Math.round(s.W * dpr);
      cv.height = Math.round(s.H * dpr);
      g.setTransform(dpr, 0, 0, dpr, 0, 0);
    }

    function draw() {
      const { W, H, clock } = s;
      const t = ((clock % LOOP) / LOOP) * Math.PI * 2;
      const small = W < 640;
      const ROWS = small ? 50 : 76;
      const PTS = small ? 90 : 140;
      const tall = H > W;
      const f = Math.min(H * 0.9, W * 1.1);
      const horizon = H * (tall ? 0.3 : 0.26);
      const camH = tall ? 2.3 : 2.0;
      const zN = 1.3, zF = 10;
      const amp = tall ? 0.95 : 1.15;

      g.fillStyle = BG;
      g.fillRect(0, 0, W, H);
      g.lineJoin = 'round';

      const px = new Array(PTS), py = new Array(PTS), lit = new Array(PTS);

      for (let r = ROWS - 1; r >= 0; r--) {
        const z = zN + (zF - zN) * (r / (ROWS - 1));
        const xm = (W / 2 / f) * z * 1.2;
        const fade = 1 - (z - zN) / (zF - zN);

        for (let p = 0; p < PTS; p++) {
          const x = -xm + (2 * xm * p) / (PTS - 1);
          const y = amp * terrainHeight(x, z, t);
          const e = 0.03;
          const dx = (amp * terrainHeight(x + e, z, t) - y) / e;
          const dz = (amp * terrainHeight(x, z + e, t) - y) / e;
          const nm = Math.hypot(dx, 1, dz);
          lit[p] = Math.max(0, (-dx * L[0] + L[1] - dz * L[2]) / nm);
          px[p] = W / 2 + (x * f) / z;
          py[p] = horizon + ((camH - y) * f) / z;
        }

        g.beginPath();
        g.moveTo(px[0], py[0]);
        for (let p = 1; p < PTS; p++) g.lineTo(px[p], py[p]);
        g.lineTo(W + 10, H + 10);
        g.lineTo(-10, H + 10);
        g.closePath();
        g.fillStyle = BG;
        g.fill();

        g.lineWidth = 0.55 + 1.0 * fade;
        const grad = g.createLinearGradient(px[0], 0, px[PTS - 1], 0);
        const span = px[PTS - 1] - px[0];
        for (let p = 0; p < PTS; p += 3) {
          const a = (0.1 + 0.8 * Math.pow(lit[p], 2.4)) * (0.18 + 0.82 * fade);
          grad.addColorStop(
            Math.min(Math.max((px[p] - px[0]) / span, 0), 1),
            `rgba(226,232,240,${a.toFixed(3)})`,
          );
        }
        g.strokeStyle = grad;
        g.beginPath();
        g.moveTo(px[0], py[0]);
        for (let p = 1; p < PTS; p++) g.lineTo(px[p], py[p]);
        g.stroke();
      }
    }

    function frame(now) {
      if (playingRef.current) {
        const dt = s.last ? now - s.last : 0;
        if (dt > 30 || !s.last) {
          s.clock += Math.min(dt, 100);
          s.last = now;
          draw();
        }
      }
      s.rafId = requestAnimationFrame(frame);
    }

    size();
    draw();

    const onResize = () => { size(); draw(); };
    window.addEventListener('resize', onResize);
    s.rafId = requestAnimationFrame(frame);

    return () => {
      cancelAnimationFrame(s.rafId);
      window.removeEventListener('resize', onResize);
    };
  }, []); // eslint-disable-line react-hooks/exhaustive-deps


  return <canvas className="terrain" ref={canvasRef} aria-hidden="true" />;
}
