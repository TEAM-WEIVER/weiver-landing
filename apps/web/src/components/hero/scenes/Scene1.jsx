export default function Scene1() {
  return (
    <>
      <div className="win" style={{ '--x': '-120px', '--y': '-70px', '--r': '-4deg', '--d': '0.1s' }}>
        <b>기업 A 자기소개서</b><span /><span /><span />
      </div>
      <div className="win" style={{ '--x': '110px', '--y': '-40px', '--r': '3deg', '--d': '0.6s' }}>
        <b>기업 B 인적성검사</b><span /><span /><span />
      </div>
      <div className="win" style={{ '--x': '-80px', '--y': '30px', '--r': '2deg', '--d': '1.05s' }}>
        <b>기업 C 포트폴리오 제출</b><span /><span /><span />
      </div>
      <div className="win" style={{ '--x': '130px', '--y': '60px', '--r': '-3deg', '--d': '1.4s' }}>
        <b>기업 D 1차 면접</b><span /><span /><span />
      </div>
      <div className="win" style={{ '--x': '0px', '--y': '0px', '--r': '0deg', '--d': '1.7s' }}>
        <b>기업 E 자기소개서</b><span /><span /><span />
      </div>
      <p className="cap">지원할 때마다, 처음부터</p>
    </>
  );
}
