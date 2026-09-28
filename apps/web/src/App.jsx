import { useRef } from 'react';
import SiteHeader from './components/SiteHeader';
import HeroStage from './components/hero/HeroStage';
import Scene1 from './components/hero/scenes/Scene1';
import Scene2 from './components/hero/scenes/Scene2';
import Scene3 from './components/hero/scenes/Scene3';
import Scene4 from './components/hero/scenes/Scene4';
import Scene4b from './components/hero/scenes/Scene4b';
import Scene5a from './components/hero/scenes/Scene5a';
import Scene5b from './components/hero/scenes/Scene5b';
import Scene6 from './components/hero/scenes/Scene6';
import { PROFILE_IDX } from './components/hero/ScenePlayer';

const SCENE_CLASS_NAMES = ['s1', 's2', 's3', 's4', 's4b', 's5a', 's5b', 's6'];

export default function App() {
  const scene4EnterRef = useRef(null);

  function handleSceneEnter(n) {
    if (n === PROFILE_IDX && scene4EnterRef.current) {
      scene4EnterRef.current();
    }
  }

  const SCENES = [
    <Scene1 />,
    <Scene2 />,
    <Scene3 />,
    <Scene4 onEnter={cb => { scene4EnterRef.current = cb; }} />,
    <Scene4b />,
    <Scene5a />,
    <Scene5b />,
    <Scene6 />,
  ];

  return (
    <>
      <SiteHeader />
      <main id="top">
        <HeroStage scenes={SCENES} sceneClassNames={SCENE_CLASS_NAMES} onSceneEnter={handleSceneEnter} />
        {/* Phase 4: content sections */}
      </main>
    </>
  );
}
