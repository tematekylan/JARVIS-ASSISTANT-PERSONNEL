import React, { useState, useEffect } from 'react';
import { Cpu, BatteryCharging, HardDrive, Radio, Brain } from 'lucide-react';
import { HudPanel } from './HudPanel';
import { UserSettings } from '../types';

interface SystemStatusPanelProps {
  settings: UserSettings;
}

export const SystemStatusPanel: React.FC<SystemStatusPanelProps> = ({ settings }) => {
  const [cpuUsage, setCpuUsage] = useState(24);
  const [ramUsage, setRamUsage] = useState(38);
  const [batteryLevel, setBatteryLevel] = useState(94);
  const [latency, setLatency] = useState(38);

  useEffect(() => {
    const interval = setInterval(() => {
      setCpuUsage(prev => Math.min(85, Math.max(12, prev + Math.floor(Math.random() * 9) - 4)));
      setLatency(prev => Math.min(65, Math.max(28, prev + Math.floor(Math.random() * 7) - 3)));
    }, 2500);

    // Try reading battery if available
    if ('getBattery' in navigator) {
      (navigator as any).getBattery().then((battery: any) => {
        setBatteryLevel(Math.round(battery.level * 100));
        battery.addEventListener('levelchange', () => {
          setBatteryLevel(Math.round(battery.level * 100));
        });
      }).catch(() => {});
    }

    return () => clearInterval(interval);
  }, []);

  return (
    <HudPanel title="TÉLÉMÉTRIE & SOUS-SYSTÈMES" badge="EN LIGNE">
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 pt-1">
        {/* Arc Reactor / Battery */}
        <div className="bg-[#070D12] p-2 rounded border border-[#007C91]/30 flex flex-col">
          <div className="flex items-center justify-between text-[11px] font-mono text-[#6F9DA6]">
            <span className="flex items-center gap-1">
              <BatteryCharging className="w-3.5 h-3.5 text-[#31F5A3]" /> RÉACTEUR ARC
            </span>
            <span className="text-[#31F5A3] font-bold">{batteryLevel}%</span>
          </div>
          <div className="w-full bg-[#0A1219] h-1.5 rounded-full mt-2 overflow-hidden">
            <div 
              className="bg-[#31F5A3] h-full rounded-full transition-all duration-500 shadow-[0_0_6px_rgba(49,245,163,0.5)]" 
              style={{ width: `${batteryLevel}%` }}
            />
          </div>
        </div>

        {/* CPU Flux */}
        <div className="bg-[#070D12] p-2 rounded border border-[#007C91]/30 flex flex-col">
          <div className="flex items-center justify-between text-[11px] font-mono text-[#6F9DA6]">
            <span className="flex items-center gap-1">
              <Cpu className="w-3.5 h-3.5 text-[#00E5FF]" /> QUANTUM CPU
            </span>
            <span className="text-[#00E5FF] font-bold">{cpuUsage}%</span>
          </div>
          <div className="w-full bg-[#0A1219] h-1.5 rounded-full mt-2 overflow-hidden">
            <div 
              className="bg-[#00E5FF] h-full rounded-full transition-all duration-500 shadow-[0_0_6px_rgba(0,229,255,0.5)]" 
              style={{ width: `${cpuUsage}%` }}
            />
          </div>
        </div>

        {/* RAM Tampon */}
        <div className="bg-[#070D12] p-2 rounded border border-[#007C91]/30 flex flex-col">
          <div className="flex items-center justify-between text-[11px] font-mono text-[#6F9DA6]">
            <span className="flex items-center gap-1">
              <HardDrive className="w-3.5 h-3.5 text-[#78F7FF]" /> MÉMOIRE VIVE
            </span>
            <span className="text-[#78F7FF] font-bold">{ramUsage}%</span>
          </div>
          <div className="w-full bg-[#0A1219] h-1.5 rounded-full mt-2 overflow-hidden">
            <div 
              className="bg-[#78F7FF] h-full rounded-full transition-all duration-500" 
              style={{ width: `${ramUsage}%` }}
            />
          </div>
        </div>

        {/* AI Provider & Latency */}
        <div className="bg-[#070D12] p-2 rounded border border-[#007C91]/30 flex flex-col">
          <div className="flex items-center justify-between text-[11px] font-mono text-[#6F9DA6]">
            <span className="flex items-center gap-1">
              <Brain className="w-3.5 h-3.5 text-[#FFB547]" /> IA NOYAU
            </span>
            <span className="text-[#FFB547] font-bold">{latency}ms</span>
          </div>
          <div className="text-[10px] font-mono text-[#E5FCFF] truncate mt-1">
            {settings.aiModel}
          </div>
        </div>
      </div>
    </HudPanel>
  );
};
