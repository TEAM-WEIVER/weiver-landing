import { useEffect, useRef } from 'react';
import ProductStory from './ProductStory';
import DemoSection from './DemoSection';
import { track } from '../api';

// 섹션별 IntersectionObserver — 50% 노출 시 세션당 1회 전송
function useSectionTracking() {
  useEffect(() => {
    const seen = new Set();
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          const id = entry.target.dataset.sectionId;
          if (entry.isIntersecting && id && !seen.has(id)) {
            seen.add(id);
            track('section_view', { sectionId: id, threshold: '0.5' });
          }
        });
      },
      { threshold: 0.5 }
    );

    document.querySelectorAll('[data-section-id]').forEach((el) => observer.observe(el));
    return () => observer.disconnect();
  }, []);
}

function handleCtaClick(ctaId, sectionId) {
  track('cta_clicked', { ctaId, sectionId, target: 'reserve' });
}

export default function ContentSections() {
  useSectionTracking();

  return (
    <div className="content">
      <div className="wrap">

        {/* lead */}
        <section className="lead" data-section-id="lead">
          <p className="tagline">이력서를 더 많이 보내는 대신, 나를 더 잘 보여주는 방법</p>
          <h1>나를 계속 복사하지 않아도 되는 구직.</h1>
          <p className="desc">
            이력서, 포트폴리오, 그리고 일하는 방식까지. 한 번의 준비를 하나의 역량 프로필로 만들면,
            맞는 기업이 먼저 당신을 발견합니다.
          </p>
          <div className="ctas">
            <a
              className="btn btn-lg btn-primary"
              href="#reserve"
              onClick={() => handleCtaClick('lead_reserve', 'lead')}
            >
              내 프로필로 시작하기
            </a>
            <a className="btn btn-lg btn-ghost" href="#profile">프로필이 되는 과정</a>
          </div>
        </section>

        {/* manifesto */}
        <section className="manifesto" id="profile" aria-labelledby="profile-title" data-section-id="profile">
          <div className="manifesto-intro">
            <h2 id="profile-title">프로필은 이력서가 아닙니다.</h2>
            <p>
              무엇을 했는지에 더해, 어떻게 일하고 어떤 가능성을 가진 사람인지까지.
              weiver는 흩어진 경험을 하나의 발견 가능한 이야기로 엮습니다.
            </p>
          </div>
          <div className="profile-ledger">
            <article className="ledger-input">
              <p className="ledger-label">YOU BRING</p>
              <h3>당신이 남긴 경험</h3>
              <ul className="ledger-list">
                <li>이력서와 프로젝트 <span>무엇을 했는지</span></li>
                <li>포트폴리오와 기록 <span>어떻게 해냈는지</span></li>
                <li>인터뷰와 대화 <span>어떻게 일하는지</span></li>
              </ul>
            </article>
            <article className="ledger-output">
              <p className="ledger-label">WEIVER PROFILE</p>
              <h3>기업이 먼저 만나는 당신</h3>
              <ul className="ledger-list">
                <li>직무 역량 <span>경험의 밀도</span></li>
                <li>일하는 방식 <span>협업의 결</span></li>
                <li>성장 가능성 <span>다음 기회의 방향</span></li>
              </ul>
              <p className="ledger-stamp">한 번의 준비, 여러 번의 발견</p>
            </article>
          </div>
        </section>

        {/* steps + product story */}
        <section className="steps" id="how" data-section-id="how">
          <h2>한 번 만든 프로필이 새로운 기회로 이어집니다.</h2>
          <ol>
            <li><h3>서류 등록</h3><p>나의 경험과 역량을 한 번에 등록해요.</p></li>
            <li><h3>AI 면접 진행</h3><p>인적성과 직무 역량을 한 번에 검증해요.</p></li>
            <li><h3>역량 프로필 완성</h3><p>서류와 면접 결과가 하나의 프로필이 돼요.</p></li>
            <li><h3>기업이 먼저 컨택</h3><p>기업이 역량을 확인하고 먼저 기회를 제안해요.</p></li>
          </ol>
          <ProductStory />
        </section>

        {/* why */}
        <section className="why" aria-labelledby="why-title" data-section-id="why">
          <p className="section-kicker">WHY WEIVER</p>
          <h2 id="why-title">기업마다 반복하던 지원 과정, 이제 한 번이면 충분해요.</h2>
          <div className="comparison">
            <article className="comparison-card old">
              <p>기존 구직</p>
              <h3>지원할 때마다 처음부터</h3>
              <ul>
                <li><span>1</span>기업별 지원 서류 작성</li>
                <li><span>2</span>기업별 인적성·면접 반복</li>
                <li><span>3</span>구직자가 매번 먼저 지원</li>
              </ul>
            </article>
            <article className="comparison-card new">
              <p>WEIVER · 한 번만</p>
              <h3>한 번 준비하고, 기회는 여러 번</h3>
              <ul>
                <li><span>1</span>서류와 포트폴리오를 한 번만 등록</li>
                <li><span>2</span>AI 면접 한 번으로 역량 검증</li>
                <li><span>3</span>기업이 먼저 컨택</li>
              </ul>
            </article>
          </div>
        </section>

        {/* demo + reservation */}
        <DemoSection />

        {/* footer */}
        <footer>
          <span className="logo" role="img" aria-label="weiver" style={{ height: '18px', color: '#fff', display: 'block', marginBottom: '10px' }} />
          한 번의 준비로, 더 많은 가능성을.
        </footer>

      </div>
    </div>
  );
}
