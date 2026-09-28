import { useEffect, useRef, useState } from 'react';

const SCREENS = [
  {
    resource: '01-profile-registration.webp',
    label: '01 · PROFILE',
    kicker: 'PROFILE REGISTRATION',
    caption: '흩어진 경험을 한 번에 정리하는 화면',
    title: '흩어진 경험을 한 번에 정리해요.',
    description: '이력서와 프로젝트, 포트폴리오를 하나의 프로필에 담습니다.',
  },
  {
    resource: '02-ai-interview.webp',
    label: '02 · INTERVIEW',
    kicker: 'INTERVIEW PRACTICE',
    caption: '내 방식으로 답하고 역량을 보여주는 화면',
    title: '내 방식으로 답하고, 역량을 보여줘요.',
    description: '한 번의 인터뷰로 당신의 일하는 방식을 더 깊게 전달합니다.',
  },
  {
    resource: '03-capability-profile.webp',
    label: '03 · PROFILE',
    kicker: 'CAPABILITY PROFILE',
    caption: '경험의 밀도가 하나의 프로필이 되는 화면',
    title: '경험의 밀도가 하나의 프로필이 돼요.',
    description: '기업은 이력서 너머의 직무 역량과 협업 방식을 함께 만납니다.',
  },
  {
    resource: '04-company-contact.webp',
    label: '04 · CONTACT',
    kicker: 'COMPANY CONTACT',
    caption: '지원 전에 기업의 제안을 받는 순간',
    title: '지원 전에, 기업의 제안을 받아요.',
    description: '준비한 프로필이 맞는 기회와 연결되는 순간입니다.',
  },
];

export default function ProductStory() {
  const storyRef = useRef(null);
  const screenRefs = useRef([]);
  const reduce = useRef(window.matchMedia('(prefers-reduced-motion: reduce)').matches).current;
  const [active, setActive] = useState(0);

  useEffect(() => {
    if (reduce) return;

    function update() {
      const el = storyRef.current;
      if (!el) return;
      const rect = el.getBoundingClientRect();
      const travel = Math.max(el.offsetHeight - window.innerHeight, 1);
      const progress = Math.min(Math.max(-rect.top / travel, 0), 1);
      const focus = progress * (SCREENS.length - 1);
      const nextActive = Math.round(focus);

      screenRefs.current.forEach((screen, i) => {
        if (!screen) return;
        const distance = Math.abs(i - focus);
        const visibility = Math.max(0, 1 - distance * 1.15);
        const scale = 0.84 + visibility * 0.16;
        screen.style.opacity = visibility.toFixed(3);
        screen.style.transform = `translateY(${((i - focus) * 26).toFixed(1)}px) scale(${scale.toFixed(3)})`;
      });

      setActive(nextActive);
    }

    let ticking = false;
    function onScroll() {
      if (!ticking) {
        ticking = true;
        requestAnimationFrame(() => { update(); ticking = false; });
      }
    }

    window.addEventListener('scroll', onScroll, { passive: true });
    window.addEventListener('resize', update);
    update();
    return () => {
      window.removeEventListener('scroll', onScroll);
      window.removeEventListener('resize', update);
    };
  }, [reduce]);

  const current = SCREENS[active];

  return (
    <div className="product-story" id="product-story" ref={storyRef} aria-label="weiver 제품 화면 스크롤 미리보기">
      <div className="product-story-sticky">
        <div className="story-caption">
          <p className="story-number">{String(active + 1).padStart(2, '0')} / {String(SCREENS.length).padStart(2, '0')}</p>
          <h3>{current.title}</h3>
          <p>{current.description}</p>
        </div>
        <div className="story-stage">
          {SCREENS.map((screen, i) => (
            <article
              key={i}
              className="story-screen"
              ref={el => (screenRefs.current[i] = el)}
              data-title={screen.title}
              data-description={screen.description}
            >
              <div className="product-shot">
                <div className="product-shot-head">
                  <span>{screen.label}</span>
                  <span>화면 리소스 대기</span>
                </div>
                <div className="product-skeleton">
                  <p className="skeleton-kicker">{screen.kicker}</p>
                  <p className="skeleton-caption">{screen.caption}</p>
                  <div className="skeleton-ui">
                    <i /><i /><i /><i className="skeleton-chip" />
                  </div>
                </div>
              </div>
            </article>
          ))}
        </div>
      </div>
    </div>
  );
}
