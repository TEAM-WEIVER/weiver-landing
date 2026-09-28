import { useEffect, useRef, useState } from 'react';
import TerrainCanvas from './TerrainCanvas';
import ScenePlayer from './ScenePlayer';

export default function HeroStage({ scenes }) {
  const filmRef = useRef(null);
  const stageRef = useRef(null);
  const [playing, setPlaying] = useState(true);
  const userPausedRef = useRef(false);
  const reduce = useRef(window.matchMedia('(prefers-reduced-motion: reduce)').matches).current;

  useEffect(() => {
    let ticking = false;

    function onScroll() {
      const stage = stageRef.current;
      const film = filmRef.current;
      if (!stage || !film) return;

      const rect = stage.getBoundingClientRect();
      const dist = stage.offsetHeight - window.innerHeight;
      const p = Math.min(Math.max(-rect.top / dist, 0), 1);
      film.style.setProperty('--p', p.toFixed(3));

      if (!reduce && !userPausedRef.current) {
        if (rect.bottom <= 0) setPlaying(false);
        else setPlaying(true);
      }

      ticking = false;
    }

    function onScrollThrottled() {
      if (!ticking) {
        ticking = true;
        requestAnimationFrame(onScroll);
      }
    }

    window.addEventListener('scroll', onScrollThrottled, { passive: true });
    window.addEventListener('resize', onScroll);
    onScroll();

    return () => {
      window.removeEventListener('scroll', onScrollThrottled);
      window.removeEventListener('resize', onScroll);
    };
  }, [reduce]);

  function handleTogglePause() {
    setPlaying(prev => {
      const next = !prev;
      userPausedRef.current = !next;
      return next;
    });
  }

  return (
    <div className="stage" id="stage" ref={stageRef}>
      <section className="hero" aria-label="서비스 소개 영상">
        <div className="film" id="film" ref={filmRef}>
          <TerrainCanvas playing={playing && !reduce} />
          <div className="scrim" aria-hidden="true" />
          <ScenePlayer
            scenes={scenes}
            playing={playing}
            onTogglePause={handleTogglePause}
          />
        </div>
      </section>
    </div>
  );
}
