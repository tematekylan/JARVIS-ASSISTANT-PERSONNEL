import React, { useEffect, useState } from 'react';
import { Sparkles, ShieldCheck, Zap, Cpu } from 'lucide-react';
import { playHudBeep, playJarvisChime } from '../utils/audio';

interface SplashScreenProps {
  onComplete: () => void;
}

export const SplashScreen: React.FC<SplashScreenProps> = ({ onComplete }) => {
  const [progress, setProgress] = useState(0);
  const [stageText, setStageText] = useState('INITIALIZING T-HACKMAN AI...');
  const [isSystemOnline, setIsSystemOnline] = useState(false);

  useEffect(() => {
    playHudBeep(440, 0.15);

    const interval = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 100) {
          clearInterval(interval);
          setStageText('SYSTEM ONLINE');
          setIsSystemOnline(true);
          playJarvisChime();
          setTimeout(() => {
            onComplete();
          }, 800);
          return 100;
        }

        const next = prev + Math.floor(Math.random() * 8) + 6;
        if (next > 30 && next <= 60) {
          setStageText('CHARGEMENT DU NOYAU NEURONAL & FIREBASE...');
        } else if (next > 60 && next < 100) {
          setStageText('ÉTABLISSEMENT DU PROTOCOLE SÉCURISÉ...');
        }
        return next > 100 ? 100 : next;
      });
    }, 120);

    return () => clearInterval(interval);
  }, [onComplete]);

  return (
    <div className="fixed inset-0 z-50 flex flex-col items-center justify-center bg-[#030609] text-[#E5FCFF] font-mono select-none overflow-hidden p-6">
      
      {/* Background cyber grid & glow */}
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_center,rgba(0,229,255,0.12)_0%,rgba(3,6,9,0.95)_70%)] pointer-events-none" />
      <div className="absolute inset-0 bg-[linear-gradient(to_right,rgba(0,124,145,0.05)_1px,transparent_1px),linear-gradient(to_bottom,rgba(0,124,145,0.05)_1px,transparent_1px)] bg-[size:32px_32px] pointer-events-none" />

      {/* Central Core with rotating quantum rings */}
      <div className="relative flex items-center justify-center my-8">
        {/* Outer glowing ring */}
        <div className="w-56 h-56 rounded-full border border-[#00E5FF]/20 animate-spin [animation-duration:18s] flex items-center justify-center">
          <div className="w-full h-full rounded-full border-t-2 border-r-2 border-[#00E5FF]/60" />
        </div>

        {/* Counter-rotating dashed ring */}
        <div className="absolute w-44 h-44 rounded-full border border-dashed border-[#31F5A3]/40 animate-spin [animation-duration:10s] [animation-direction:reverse]" />

        {/* Core glowing sphere */}
        <div className="absolute w-32 h-32 rounded-full bg-linear-to-tr from-[#007C91]/40 via-[#00E5FF]/20 to-[#31F5A3]/30 backdrop-blur-md border-2 border-[#00E5FF] shadow-[0_0_40px_rgba(0,229,255,0.6)] flex flex-col items-center justify-center">
          <Cpu className={`w-12 h-12 text-[#00E5FF] transition-all duration-300 ${isSystemOnline ? 'scale-110 text-[#31F5A3]' : 'animate-pulse'}`} />
        </div>
      </div>

      {/* Brand Title */}
      <div className="text-center relative z-10 space-y-2 mt-4">
        <h1 className="text-3xl sm:text-4xl font-bold tracking-[0.25em] font-['Chakra_Petch',sans-serif] text-transparent bg-clip-text bg-linear-to-r from-[#E5FCFF] via-[#00E5FF] to-[#31F5A3]">
          T-HACKMAN AI
        </h1>
        <div className="text-xs sm:text-sm tracking-widest text-[#6F9DA6] flex items-center justify-center gap-2">
          <Sparkles className="w-3.5 h-3.5 text-[#00E5FF]" />
          <span>CYBER COMMAND & INTELLIGENT MESSAGING</span>
        </div>
      </div>

      {/* Status & Progress Bar */}
      <div className="w-full max-w-sm mt-8 space-y-3 relative z-10">
        <div className="flex justify-between text-xs text-[#8CA0A8]">
          <span className="font-semibold text-[#00E5FF] flex items-center gap-1.5">
            <Zap className="w-3.5 h-3.5 animate-pulse" />
            {stageText}
          </span>
          <span className="font-bold text-[#31F5A3]">{progress}%</span>
        </div>

        <div className="w-full h-2 bg-[#070D12] border border-[#007C91]/50 rounded-full overflow-hidden p-0.5 shadow-[0_0_10px_rgba(0,124,145,0.3)]">
          <div 
            className="h-full bg-linear-to-r from-[#007C91] via-[#00E5FF] to-[#31F5A3] rounded-full transition-all duration-150"
            style={{ width: `${progress}%` }}
          />
        </div>

        <div className="text-[11px] text-center text-[#5B7B88] pt-2">
          Architecture v2.5 &bull; Firebase Auth & Cloud Firestore &bull; WebRTC Ready
        </div>
      </div>

      {/* Skip button if user wants immediate entry */}
      <button
        onClick={onComplete}
        className="mt-8 text-xs text-[#6F9DA6] hover:text-[#00E5FF] underline decoration-dotted cursor-pointer transition-colors"
      >
        Passer l'initialisation &rarr;
      </button>

    </div>
  );
};
