import React from 'react';
import { AssistantState } from '../types';

interface AudioWaveformProps {
  state: AssistantState;
  audioAmplitude?: number;
  height?: number;
}

export const AudioWaveform: React.FC<AudioWaveformProps> = ({
  state,
  audioAmplitude = 0,
  height = 36
}) => {
  const barCount = 28;
  const isActive = state === 'SPEAKING' || state === 'LISTENING' || state === 'THINKING';

  return (
    <div className="w-full flex items-center justify-center space-x-1 py-1" style={{ height }}>
      {Array.from({ length: barCount }).map((_, i) => {
        // Calculate dynamic height for each bar based on position and state
        const mid = barCount / 2;
        const distFromCenter = 1 - Math.abs(i - mid) / mid;
        
        let minHeight = 4;
        let scale = 0.2;

        if (state === 'SPEAKING' || state === 'LISTENING') {
          scale = (0.2 + 0.8 * distFromCenter) * (0.3 + 0.7 * audioAmplitude);
        } else if (state === 'THINKING') {
          scale = 0.4 + 0.4 * Math.sin((i / 4) + Date.now() / 150);
        }

        const barHeight = Math.max(minHeight, Math.min(height - 4, (height - 4) * scale));

        let barColor = 'bg-[#007C91]/50';
        if (state === 'SPEAKING') barColor = 'bg-gradient-to-t from-[#0088FF] to-[#00E5FF] shadow-[0_0_6px_rgba(0,229,255,0.6)]';
        else if (state === 'LISTENING') barColor = 'bg-gradient-to-t from-[#00E5FF] to-[#78F7FF] shadow-[0_0_6px_rgba(120,247,255,0.6)]';
        else if (state === 'ERROR') barColor = 'bg-[#FF4660]';
        else if (state === 'SUCCESS') barColor = 'bg-[#31F5A3]';

        return (
          <div
            key={i}
            className={`w-1 rounded-full transition-all duration-75 ${barColor}`}
            style={{
              height: `${barHeight}px`,
              opacity: isActive ? 0.9 : 0.35
            }}
          />
        );
      })}
    </div>
  );
};
