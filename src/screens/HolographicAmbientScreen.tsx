import React, { useEffect, useRef, useState } from 'react';
import { X, Mic, BatteryCharging, Radio, Eye } from 'lucide-react';
import { AssistantState } from '../types';

interface HolographicAmbientScreenProps {
  onExit: () => void;
  assistantState: AssistantState;
  onToggleVoice: () => void;
  isListening: boolean;
}

interface MoleculeNode {
  id: number;
  baseAngle: number;
  orbitRadius: number;
  speed: number;
  nodeRadius: number;
  color: string;
  name: string;
}

export const HolographicAmbientScreen: React.FC<HolographicAmbientScreenProps> = ({
  onExit,
  assistantState,
  onToggleVoice,
  isListening
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const [timeStr, setTimeStr] = useState("");
  const [dateStr, setDateStr] = useState("");

  useEffect(() => {
    // Attempt fullscreen if available
    if (document.documentElement.requestFullscreen && !document.fullscreenElement) {
      document.documentElement.requestFullscreen().catch(() => {
        // Fullscreen may require explicit user gesture in some browsers
      });
    }

    return () => {
      if (document.exitFullscreen && document.fullscreenElement) {
        document.exitFullscreen().catch(() => {});
      }
    };
  }, []);

  const handleSafeExit = () => {
    if (document.exitFullscreen && document.fullscreenElement) {
      document.exitFullscreen().catch(() => {});
    }
    onExit();
  };


  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animId: number;
    let startTime = performance.now();

    // Resize canvas to full parent
    const handleResize = () => {
      canvas.width = canvas.parentElement?.clientWidth || window.innerWidth;
      canvas.height = canvas.parentElement?.clientHeight || window.innerHeight;
    };
    handleResize();
    window.addEventListener('resize', handleResize);

    const nodes: MoleculeNode[] = [
      { id: 1, baseAngle: 0, orbitRadius: 130, speed: 0.4, nodeRadius: 5, color: '#00E5FF', name: 'NEURAL' },
      { id: 2, baseAngle: 72, orbitRadius: 180, speed: -0.3, nodeRadius: 4, color: '#78F7FF', name: 'QUANTUM' },
      { id: 3, baseAngle: 144, orbitRadius: 220, speed: 0.25, nodeRadius: 6, color: '#31F5A3', name: 'STARK' },
      { id: 4, baseAngle: 216, orbitRadius: 150, speed: -0.45, nodeRadius: 4.5, color: '#0088FF', name: 'CORE' },
      { id: 5, baseAngle: 288, orbitRadius: 260, speed: 0.2, nodeRadius: 5.5, color: '#FFB547', name: 'REACTOR' }
    ];

    const render = (time: number) => {
      const elapsed = (time - startTime) / 1000;
      const w = canvas.width;
      const h = canvas.height;
      const cx = w / 2;
      const cy = h / 2;

      ctx.clearRect(0, 0, w, h);

      // Background radial energy glow
      const bgGrad = ctx.createRadialGradient(cx, cy, 0, cx, cy, Math.min(w, h) * 0.7);
      bgGrad.addColorStop(0, 'rgba(0, 229, 255, 0.08)');
      bgGrad.addColorStop(0.5, 'rgba(0, 124, 145, 0.03)');
      bgGrad.addColorStop(1, 'transparent');
      ctx.fillStyle = bgGrad;
      ctx.fillRect(0, 0, w, h);

      // Subtle background orbital grid tracks
      [90, 140, 190, 240, 290].forEach((r, idx) => {
        ctx.strokeStyle = `rgba(0, 124, 145, ${0.12 - idx * 0.02})`;
        ctx.lineWidth = 1;
        ctx.setLineDash([4, 8]);
        ctx.beginPath();
        ctx.arc(cx, cy, r, 0, Math.PI * 2);
        ctx.stroke();
      });

      // Rotating central radar sweep
      ctx.save();
      ctx.translate(cx, cy);
      ctx.rotate(elapsed * 0.4);
      const sweepGrad = ctx.createRadialGradient(0, 0, 0, 0, 0, 250);
      sweepGrad.addColorStop(0, 'rgba(0, 229, 255, 0.15)');
      sweepGrad.addColorStop(1, 'transparent');
      ctx.fillStyle = sweepGrad;
      ctx.beginPath();
      ctx.moveTo(0, 0);
      ctx.arc(0, 0, 250, 0, Math.PI / 4);
      ctx.closePath();
      ctx.fill();
      ctx.restore();

      // Draw Molecule Nodes and links
      nodes.forEach((n) => {
        const curAngle = n.baseAngle * (Math.PI / 180) + elapsed * n.speed;
        const px = cx + Math.cos(curAngle) * n.orbitRadius;
        const py = cy + Math.sin(curAngle) * n.orbitRadius;

        // Connector line to center
        ctx.strokeStyle = `${n.color}25`;
        ctx.lineWidth = 1;
        ctx.setLineDash([2, 4]);
        ctx.beginPath();
        ctx.moveTo(cx, cy);
        ctx.lineTo(px, py);
        ctx.stroke();

        // Outer glow
        const nodeGlow = ctx.createRadialGradient(px, py, 0, px, py, n.nodeRadius * 3);
        nodeGlow.addColorStop(0, `${n.color}aa`);
        nodeGlow.addColorStop(1, 'transparent');
        ctx.fillStyle = nodeGlow;
        ctx.beginPath();
        ctx.arc(px, py, n.nodeRadius * 3, 0, Math.PI * 2);
        ctx.fill();

        // Node dot
        ctx.fillStyle = n.color;
        ctx.beginPath();
        ctx.arc(px, py, n.nodeRadius, 0, Math.PI * 2);
        ctx.fill();

        // Label
        ctx.fillStyle = 'rgba(229, 252, 255, 0.7)';
        ctx.font = '9px monospace';
        ctx.fillText(n.name, px + 8, py + 3);
      });

      // Central Quantum Core Node
      const corePulse = Math.sin(elapsed * 2) * 0.5 + 0.5;
      const coreR = 30 + corePulse * 6;

      const coreGlow = ctx.createRadialGradient(cx, cy, 0, cx, cy, coreR * 2);
      coreGlow.addColorStop(0, 'rgba(0, 229, 255, 0.5)');
      coreGlow.addColorStop(0.6, 'rgba(0, 136, 255, 0.2)');
      coreGlow.addColorStop(1, 'transparent');
      ctx.fillStyle = coreGlow;
      ctx.beginPath();
      ctx.arc(cx, cy, coreR * 2, 0, Math.PI * 2);
      ctx.fill();

      ctx.fillStyle = '#00E5FF';
      ctx.beginPath();
      ctx.arc(cx, cy, coreR * 0.5, 0, Math.PI * 2);
      ctx.fill();

      animId = requestAnimationFrame(render);
    };

    animId = requestAnimationFrame(render);
    return () => {
      cancelAnimationFrame(animId);
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  return (
    <div className="fixed inset-0 z-50 bg-[#030609] flex flex-col justify-between p-6 select-none overflow-hidden font-mono">
      {/* Dynamic Background Canvas */}
      <div className="absolute inset-0 pointer-events-none">
        <canvas ref={canvasRef} className="w-full h-full" />
      </div>

      {/* Top Telemetry */}
      <div className="relative z-10 flex items-center justify-between">
        <div className="flex items-center space-x-2 text-xs text-[#00E5FF]">
          <Eye className="w-4 h-4 animate-pulse" />
          <span className="font-bold tracking-widest">VEILLE QUANTIQUE // PROTOCOLE AOD</span>
        </div>

        <button
          onClick={handleSafeExit}
          className="p-2 rounded bg-[#0A1219]/80 border border-[#007C91]/50 text-[#6F9DA6] hover:text-[#00E5FF] transition-colors cursor-pointer"
          title="Quitter la veille"
        >
          <X className="w-5 h-5" />
        </button>
      </div>

      {/* Center Clock & Holographic Telemetry */}
      <div className="relative z-10 flex flex-col items-center justify-center text-center space-y-2 pointer-events-none">
        <div className="text-4xl sm:text-6xl md:text-7xl font-bold font-['Chakra_Petch',sans-serif] tracking-widest text-[#E5FCFF] hud-glow">
          {timeStr}
        </div>
        <div className="text-xs sm:text-sm tracking-widest text-[#00E5FF] font-mono">
          {dateStr}
        </div>
        <div className="text-[11px] text-[#6F9DA6] font-mono mt-1">
          Noyau T-HACK en état de confinement optimal
        </div>
      </div>

      {/* Bottom Controls */}
      <div className="relative z-10 flex items-center justify-between text-xs text-[#6F9DA6]">
        <div className="flex items-center space-x-1.5">
          <BatteryCharging className="w-4 h-4 text-[#31F5A3]" />
          <span>RÉACTEUR : 98%</span>
        </div>

        <button
          onClick={onToggleVoice}
          className={`px-4 py-2 rounded-full border flex items-center gap-2 cursor-pointer transition-all ${
            isListening
              ? 'bg-[#FF4660] border-[#FF4660] text-white shadow-[0_0_15px_rgba(255,70,96,0.6)] animate-pulse'
              : 'bg-[#0A1219]/90 border-[#00E5FF]/60 text-[#00E5FF] hover:bg-[#00E5FF]/20 shadow-[0_0_12px_rgba(0,229,255,0.3)]'
          }`}
        >
          <Mic className="w-4 h-4" />
          <span>{isListening ? "ÉCOUTE EN COURS..." : "COMMANDE VOCALE"}</span>
        </button>

        <div className="hidden sm:flex items-center space-x-1.5">
          <Radio className="w-4 h-4 text-[#00E5FF]" />
          <span>FLUX SYNCHRONE</span>
        </div>
      </div>
    </div>
  );
};
