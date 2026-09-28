import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import './styles.css';

function App() {
  return (
    <main>
      <p className="eyebrow">WEIVER LANDING</p>
      <h1>React 랜딩페이지 전환 준비가 완료되었습니다.</h1>
      <p>
        기존 랜딩의 검증된 화면을 이 위치로 옮긴 뒤, 방문 이벤트와 사전예약 API를 연결합니다.
      </p>
    </main>
  );
}

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <App />
  </StrictMode>,
);
