import { useState } from 'react';

const OPTIONS = [
  {
    id: '간편 AI 면접 체험',
    badge: '부담 없이 시작',
    badgeClass: '',
    title: '간편 AI 면접 체험',
    sub: '이력서 + 간단 설문',
    desc: '자기소개서와 포트폴리오 없이 AI 면접을 연습해 보세요.',
  },
  {
    id: '역매칭 준비 체험',
    badge: '전체 과정 경험',
    badgeClass: 'full',
    title: '역매칭 준비 체험',
    sub: '이력서 + 자기소개서 + 포트폴리오',
    desc: '역량 프로필을 완성하고 정식 출시 후 기업 컨택까지 준비해요.',
  },
];

export default function DemoSection() {
  const [selected, setSelected] = useState(OPTIONS[0].id);
  const [message, setMessage] = useState('');

  function handleSubmit(e) {
    e.preventDefault();
    if (!e.target.reportValidity()) return;
    setMessage('사전예약 저장 기능을 준비 중입니다. 정식 접수 연결 후 이 입력을 저장합니다.');
  }

  return (
    <section className="demo" id="reserve" aria-labelledby="demo-title">
      <div className="demo-head">
        <div>
          <p className="section-kicker">DEMO TESTER</p>
          <h2 id="demo-title">나에게 맞는 방식으로 weiver를 먼저 경험해 보세요.</h2>
        </div>
        <p>두 가지 방식 중 하나를 선택해 AI 면접과 역량 검증 과정을 먼저 경험할 수 있어요.</p>
      </div>
      <div className="demo-options" role="radiogroup" aria-label="데모 참여 방식">
        {OPTIONS.map(opt => (
          <button
            key={opt.id}
            className="demo-option"
            type="button"
            role="radio"
            aria-checked={selected === opt.id}
            data-option={opt.id}
            onClick={() => setSelected(opt.id)}
          >
            <span className={`badge${opt.badgeClass ? ` ${opt.badgeClass}` : ''}`}>{opt.badge}</span>
            <strong>{opt.title}</strong>
            <small>{opt.sub}</small>
            <em>{opt.desc}</em>
          </button>
        ))}
      </div>
      <div className="reservation">
        <div className="reservation-copy">
          <p>선택한 체험</p>
          <output>{selected}</output>
        </div>
        <form onSubmit={handleSubmit}>
          <input name="name" type="text" autoComplete="name" placeholder="이름" required aria-label="이름" />
          <input name="email" type="email" autoComplete="email" placeholder="이메일" required aria-label="이메일" />
          <button type="submit">사전예약하기</button>
        </form>
        <p className="reservation-note">
          현재 입력 정보는 저장되지 않습니다. 개인정보 수집·이용 동의와 저장 API를 연결한 뒤 정식으로 접수합니다.
        </p>
        {message && <p className="reservation-message show" role="status">{message}</p>}
      </div>
    </section>
  );
}
