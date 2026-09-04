import React, { useState, useEffect } from 'react';
import { Wifi, ShieldCheck, Zap, Activity } from 'lucide-react';

import { AssistantState, UserSettings } from '../types';

interface FuturisticHeaderProps {
  systemStatusText?: string;
  clearanceLevel?: string;
  settings?: UserSettings;
  assistantState?: AssistantState;
  onOpenSettings?: () => void;
  onOpenAmbient?: () => void;
}

export const FuturisticHeader: React.FC<FuturisticHeaderProps> = ({
  systemStatusText = "T-HACK AI CORE",
  clearanceLevel = "LEVEL 5",
  settings,
  assistantState,
  onOpenSettings,
  onOpenAmbient
}) => {
  const [timeStr, setTimeStr] = useState("");
  const [blink, setBlink] = useState(true);

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      setTimeStr(now.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit', second: '2-digit' }));
    };
    updateTime();
    const interval = setInterval(updateTime, 1000);
    const blinkInterval = setInterval(() => setBlink(b => !b), 600);
    return () => {
      clearInterval(interval);
      clearInterval(blinkInterval);
    };
  }, []);

  return (
    <header className="w-full bg-[#070D12]/95 border-b border-[#007C91]/40 px-3 py-2 flex flex-wrap items-center justify-between gap-2 select-none backdrop-blur-md z-20">
      {/* Left: Brand and active system mode */}
      <div className="flex items-center space-x-2.5">
        <div className="relative flex items-center justify-center w-7 h-7 bg-[#0A1219] border border-[#00E5FF]/60 rounded-xs shadow-[0_0_8px_rgba(0,229,255,0.4)]">
          <Zap className="w-4 h-4 text-[#00E5FF] animate-pulse" />
          <span className="absolute -top-0.5 -right-0.5 w-1.5 h-1.5 bg-[#31F5A3] rounded-full" />
        </div>
        <div>
          <div className="flex items-center space-x-1.5">
            <span className="text-sm font-bold tracking-widest text-[#E5FCFF] font-['Chakra_Petch',sans-serif]">
              T-HACK<span className="text-[#00E5FF]">.AI</span>
            </span>
            <span className="text-[10px] font-mono px-1.5 py-0.2 rounded bg-[#00E5FF]/15 text-[#00E5FF] border border-[#00E5FF]/30">
              {clearanceLevel}
            </span>
          </div>
          <div className="text-[10px] font-mono text-[#6F9DA6] tracking-wider uppercase flex items-center gap-1">
            <span className={`w-1.5 h-1.5 rounded-full ${blink ? 'bg-[#31F5A3]' : 'bg-[#31F5A3]/40'}`} />
            {systemStatusText}
          </div>
        </div>
      </div>

      {/* Center/Right: Telemetry Metrics */}
      <div className="flex items-center space-x-3 text-xs font-mono">
        <div className="hidden sm:flex items-center space-x-1 text-[#6F9DA6]">
          <Wifi className="w-3.5 h-3.5 text-[#31F5A3]" />
          <span className="text-[11px]">5G SECURE</span>
        </div>

        <div className="hidden md:flex items-center space-x-1 text-[#6F9DA6]">
          <ShieldCheck className="w-3.5 h-3.5 text-[#00E5FF]" />
          <span className="text-[11px]">AES-256</span>
        </div>

        <div className="flex items-center space-x-1 bg-[#0A1219] px-2 py-1 rounded border border-[#007C91]/30">
          <Activity className="w-3.5 h-3.5 text-[#00E5FF] animate-spin" style={{ animationDuration: '6s' }} />
          <span className="text-[#78F7FF] font-bold tracking-widest">{timeStr}</span>
        </div>
      </div>
    </header>
  );
};
