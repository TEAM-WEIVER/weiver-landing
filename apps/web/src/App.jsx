import SiteHeader from './components/SiteHeader';
import HeroStage from './components/hero/HeroStage';

// Phase 3에서 실제 씬 컴포넌트로 교체
const PLACEHOLDER_SCENES = Array.from({ length: 8 }, (_, i) => (
  <p className="cap" style={{ color: '#fff' }}>씬 {i + 1}</p>
));

export default function App() {
  return (
    <>
      <SiteHeader />
      <main id="top">
        <HeroStage scenes={PLACEHOLDER_SCENES} />
        {/* Phase 4: content sections */}
      </main>
    </>
  );
}
