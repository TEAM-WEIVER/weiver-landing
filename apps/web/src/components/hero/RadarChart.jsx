const AXES = [
  ['성장가능성', 92],
  ['일관성', 81],
  ['문제해결력', 74],
  ['논리성', 96],
  ['협업·팀워크', 64],
  ['대처능력', 54],
];
const C = 100, R = 66;

function pt(i, r) {
  const a = -Math.PI / 2 + (i * Math.PI) / 3;
  return [C + r * Math.cos(a), C + r * Math.sin(a)];
}

function toPoints(arr) {
  return arr.map(([x, y]) => `${x},${y}`).join(' ');
}

export default function RadarChart() {
  const gridPoints = [1, 0.66, 0.33].map(k =>
    toPoints(AXES.map((_, i) => pt(i, R * k)))
  );
  const shapePoints = toPoints(AXES.map(([, val], i) => pt(i, (R * val) / 100)));

  return (
    <svg className="radar" viewBox="0 0 200 200" aria-hidden="true">
      {gridPoints.map((pts, j) => (
        <polygon key={j} points={pts} fill="none" stroke="#E2E8F0" />
      ))}
      {AXES.map(([label], i) => {
        const [px, py] = pt(i, R);
        const [lx, ly] = pt(i, R + 17);
        return (
          <g key={i}>
            <line x1={C} y1={C} x2={px} y2={py} stroke="#E2E8F0" />
            <text x={lx} y={ly + 3} textAnchor="middle">{label}</text>
          </g>
        );
      })}
      <polygon
        className="shape"
        points={shapePoints}
        fill="rgba(96,165,250,.45)"
        stroke="#60A5FA"
        strokeWidth="1.5"
      />
    </svg>
  );
}
