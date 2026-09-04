import React, { useEffect, useRef } from 'react';
import { AssistantState } from '../types';

interface AICoreCanvasProps {
  state: AssistantState;
  audioAmplitude?: number;
  size?: number;
  onClick?: () => void;
}

export const AICoreCanvas: React.FC<AICoreCanvasProps> = ({
  state,
  audioAmplitude = 0,
  size = 220,
  onClick
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animId: number;
    let startTime = performance.now();

    const render = (time: number) => {
      const elapsed = (time - startTime) / 1000;
      const width = canvas.width;
      const height = canvas.height;
      const cx = width / 2;
      const cy = height / 2;
      const maxR = (Math.min(width, height) / 2) * 0.92;

      ctx.clearRect(0, 0, width, height);

      // Animation variables
      const pulseSpeed = state === 'THINKING' ? 4 : state === 'LISTENING' ? 3 : 1.5;
      const pulse = Math.sin(elapsed * pulseSpeed) * 0.5 + 0.5;
      const rot1 = elapsed * 35; // degrees
      const rot2 = -elapsed * 25;
      const rot3 = elapsed * 15;
      const rot4 = -elapsed * 50;

      // Color scheme based on AssistantState
      let primaryColor = '#00E5FF';
      let glowColor = '#78F7FF';
      let accentColor = '#0088FF';

      if (state === 'ERROR') {
        primaryColor = '#FF4660';
        glowColor = '#FF8095';
        accentColor = '#FFB547';
      } else if (state === 'SUCCESS') {
        primaryColor = '#31F5A3';
        glowColor = '#94FFD6';
        accentColor = '#00E5FF';
      } else if (state === 'THINKING') {
        primaryColor = '#78F7FF';
        glowColor = '#FFFFFF';
        accentColor = '#0088FF';
      } else if (state === 'PROCESSING') {
        primaryColor = '#00E5FF';
        glowColor = '#78F7FF';
        accentColor = '#31F5A3';
      } else if (state === 'LISTENING') {
        primaryColor = '#00E5FF';
        glowColor = '#FFFFFF';
        accentColor = '#0088FF';
      } else if (state === 'SPEAKING') {
        primaryColor = '#00E5FF';
        glowColor = '#78F7FF';
        accentColor = '#0088FF';
      }

      const amp = Math.min(1.0, Math.max(0, audioAmplitude));

      // LEVEL 7: Ambient Holographic Energy Halo
      const haloRadius = maxR * (0.85 + pulse * 0.12 + amp * 0.15);
      const haloGrad = ctx.createRadialGradient(cx, cy, 0, cx, cy, haloRadius);
      haloGrad.addColorStop(0, `${primaryColor}44`);
      haloGrad.addColorStop(0.6, `${primaryColor}15`);
      haloGrad.addColorStop(1, 'transparent');
      ctx.fillStyle = haloGrad;
      ctx.beginPath();
      ctx.arc(cx, cy, haloRadius, 0, Math.PI * 2);
      ctx.fill();

      // LEVEL 6: Radial Telemetry Lines
      const radialCount = 12;
      for (let i = 0; i < radialCount; i++) {
        const angle = (i * (360 / radialCount) * Math.PI) / 180;
        const startR = maxR * 0.42;
        const endR = maxR * (i % 3 === 0 ? 0.94 : 0.78);
        const alpha = i % 3 === 0 ? 0.35 : 0.15;

        ctx.strokeStyle = `${accentColor}${Math.floor(alpha * 255).toString(16).padStart(2, '0')}`;
        ctx.lineWidth = i % 3 === 0 ? 1.5 : 1.0;
        ctx.beginPath();
        ctx.moveTo(cx + Math.cos(angle) * startR, cy + Math.sin(angle) * startR);
        ctx.lineTo(cx + Math.cos(angle) * endR, cy + Math.sin(angle) * endR);
        ctx.stroke();
      }

      // LEVEL 5: Orbital Particles
      const particleCount = 14;
      for (let i = 0; i < particleCount; i++) {
        const pSpeed = state === 'THINKING' ? 3.0 : state === 'LISTENING' ? 2.0 : 1.0;
        const pAngle = ((rot1 * pSpeed + (i * 360) / particleCount) * Math.PI) / 180;
        const pRadius = maxR * (0.55 + 0.35 * Math.sin(i * 1.5 + elapsed));
        const px = cx + Math.cos(pAngle) * pRadius;
        const py = cy + Math.sin(pAngle) * pRadius;
        const pSize = 1.5 + (i % 3);

        ctx.fillStyle = `${primaryColor}${Math.floor((0.4 + 0.6 * Math.sin(elapsed * 2 + i)) * 255).toString(16).padStart(2, '0')}`;
        ctx.beginPath();
        ctx.arc(px, py, pSize, 0, Math.PI * 2);
        ctx.fill();
      }

      // LEVEL 4 & 3: Multi-Speed Counter-Rotating Rings & Segmented Arcs
      // Ring 1 (Outer dashed)
      ctx.save();
      ctx.translate(cx, cy);
      ctx.rotate((rot1 * Math.PI) / 180);
      ctx.strokeStyle = `${primaryColor}66`;
      ctx.lineWidth = 1.5;
      ctx.setLineDash([8, 8]);
      ctx.beginPath();
      ctx.arc(0, 0, maxR * 0.92, 0, Math.PI * 2);
      ctx.stroke();
      ctx.restore();

      // Ring 2 (Middle segmented arcs)
      ctx.save();
      ctx.translate(cx, cy);
      ctx.rotate((rot2 * Math.PI) / 180);
      ctx.strokeStyle = accentColor;
      ctx.lineWidth = 2;
      ctx.setLineDash([20, 15, 5, 15]);
      ctx.beginPath();
      ctx.arc(0, 0, maxR * 0.78, 0, Math.PI * 2);
      ctx.stroke();
      ctx.restore();

      // Ring 3 (Internal tick ring)
      ctx.save();
      ctx.translate(cx, cy);
      ctx.rotate((rot3 * Math.PI) / 180);
      ctx.strokeStyle = `${glowColor}99`;
      ctx.lineWidth = 1.2;
      ctx.setLineDash([3, 10]);
      ctx.beginPath();
      ctx.arc(0, 0, maxR * 0.65, 0, Math.PI * 2);
      ctx.stroke();
      ctx.restore();

      // Cardinal HUD Ticks (N, S, E, W markers)
      ctx.strokeStyle = `${primaryColor}aa`;
      ctx.lineWidth = 2;
      const tickLength = 7;
      [0, 90, 180, 270].forEach(deg => {
        const rad = (deg * Math.PI) / 180;
        const rInner = maxR * 0.88;
        ctx.beginPath();
        ctx.moveTo(cx + Math.cos(rad) * rInner, cy + Math.sin(rad) * rInner);
        ctx.lineTo(cx + Math.cos(rad) * (rInner + tickLength), cy + Math.sin(rad) * (rInner + tickLength));
        ctx.stroke();
      });

      // Processing Arc if PROCESSING
      if (state === 'PROCESSING') {
        const procAngle = (elapsed * 3) % (Math.PI * 2);
        ctx.strokeStyle = '#31F5A3';
        ctx.lineWidth = 3.5;
        ctx.setLineDash([]);
        ctx.beginPath();
        ctx.arc(cx, cy, maxR * 0.86, procAngle, procAngle + Math.PI * 1.2);
        ctx.stroke();
      }

      // Success wave pulse if SUCCESS
      if (state === 'SUCCESS') {
        const waveR = maxR * (0.4 + pulse * 0.55);
        ctx.strokeStyle = `#31F5A3${Math.floor((1 - pulse) * 200).toString(16).padStart(2, '0')}`;
        ctx.lineWidth = 2.0;
        ctx.setLineDash([]);
        ctx.beginPath();
        ctx.arc(cx, cy, waveR, 0, Math.PI * 2);
        ctx.stroke();
      }

      // LEVEL 2: Concentric Circles
      ctx.strokeStyle = `${primaryColor}38`;
      ctx.lineWidth = 1.2;
      ctx.setLineDash([]);
      ctx.beginPath();
      ctx.arc(cx, cy, maxR * 0.60, 0, Math.PI * 2);
      ctx.stroke();

      ctx.strokeStyle = `${accentColor}44`;
      ctx.lineWidth = 1.0;
      ctx.beginPath();
      ctx.arc(cx, cy, maxR * 0.40, 0, Math.PI * 2);
      ctx.stroke();

      // LEVEL 1: Central Luminous Core (Breathing and Pulsing)
      const coreR = maxR * (0.22 + pulse * 0.04 + (state === 'LISTENING' ? amp * 0.1 : 0));

      // Core radial glow
      const coreGlow = ctx.createRadialGradient(cx, cy, 0, cx, cy, coreR * 1.5);
      coreGlow.addColorStop(0, `${glowColor}ee`);
      coreGlow.addColorStop(0.5, `${primaryColor}aa`);
      coreGlow.addColorStop(0.8, `${accentColor}44`);
      coreGlow.addColorStop(1, 'transparent');

      ctx.fillStyle = coreGlow;
      ctx.beginPath();
      ctx.arc(cx, cy, coreR * 1.5, 0, Math.PI * 2);
      ctx.fill();

      // Central solid core orb
      const orbGrad = ctx.createRadialGradient(cx - coreR * 0.2, cy - coreR * 0.2, 0, cx, cy, coreR);
      orbGrad.addColorStop(0, '#FFFFFF');
      orbGrad.addColorStop(0.4, glowColor);
      orbGrad.addColorStop(1, primaryColor);

      ctx.fillStyle = orbGrad;
      ctx.beginPath();
      ctx.arc(cx, cy, coreR, 0, Math.PI * 2);
      ctx.fill();

      // Center tech crosshair dot
      ctx.fillStyle = '#030609';
      ctx.beginPath();
      ctx.arc(cx, cy, 2, 0, Math.PI * 2);
      ctx.fill();

      animId = requestAnimationFrame(render);
    };

    animId = requestAnimationFrame(render);
    return () => cancelAnimationFrame(animId);
  }, [state, audioAmplitude]);

  return (
    <div 
      className="relative flex items-center justify-center cursor-pointer select-none group"
      onClick={onClick}
      style={{ width: size, height: size }}
      title={`AI Core [${state}] - Cliquez pour activer l'écoute ou stopper l'audio`}
    >
      <canvas
        ref={canvasRef}
        width={size * 2}
        height={size * 2}
        style={{ width: size, height: size }}
        className="transition-transform duration-300 group-hover:scale-105"
      />
      {/* State label badge on bottom */}
      <div className="absolute -bottom-2 px-2.5 py-0.5 rounded-full bg-[#0A1219]/90 border border-[#00E5FF]/40 text-[10px] tracking-widest font-mono text-[#00E5FF] shadow-[0_0_10px_rgba(0,229,255,0.3)] pointer-events-none">
        {state}
      </div>
    </div>
  );
};
