import React from 'react';
import { Zap, MessageSquare, Smartphone, Terminal, Menu, CheckSquare, Sparkles } from 'lucide-react';
import { JarvisScreen } from '../types';
import { playHudBeep } from '../utils/audio';

interface MobileBottomNavProps {
  currentScreen: JarvisScreen;
  onNavigate: (screen: JarvisScreen) => void;
  onToggleDrawer: () => void;
  unreadNotifications?: number;
}

export const MobileBottomNav: React.FC<MobileBottomNavProps> = ({
  currentScreen,
  onNavigate,
  onToggleDrawer,
  unreadNotifications = 0
}) => {
  const navItems = [
    {
      id: 'HOME' as JarvisScreen,
      label: 'Core',
      icon: Zap,
      badge: null
    },
    {
      id: 'CHAT' as JarvisScreen,
      label: 'Chat IA',
      icon: MessageSquare,
      badge: null
    },
    {
      id: 'EXTERNAL_APPS' as JarvisScreen,
      label: 'Apps & Tél',
      icon: Smartphone,
      badge: 'PRO'
    },
    {
      id: 'COMMAND_CENTER' as JarvisScreen,
      label: 'Outils',
      icon: Terminal,
      badge: null
    }
  ];

  const handleSelect = (screen: JarvisScreen) => {
    playHudBeep(680, 0.05);
    onNavigate(screen);
  };

  return (
    <nav className="w-full bg-[#050A0E]/98 border-t border-[#007C91]/40 px-2 py-1.5 flex items-center justify-around z-30 select-none backdrop-blur-xl shrink-0 safe-area-pb">
      {navItems.map((item) => {
        const Icon = item.icon;
        const isActive = currentScreen === item.id;
        return (
          <button
            key={item.id}
            id={`nav-item-${item.id.toLowerCase()}`}
            onClick={() => handleSelect(item.id)}
            className={`flex-1 flex flex-col items-center justify-center py-1 px-1 rounded transition-all relative cursor-pointer ${
              isActive 
                ? 'text-[#00E5FF] bg-[#00E5FF]/10' 
                : 'text-[#6F9DA6] hover:text-[#E5FCFF] hover:bg-[#007C91]/10'
            }`}
          >
            {item.badge && (
              <span className="absolute top-0.5 right-2 text-[8px] font-mono font-bold px-1 bg-[#00E5FF]/20 text-[#00E5FF] rounded border border-[#00E5FF]/30">
                {item.badge}
              </span>
            )}
            <Icon className={`w-5 h-5 transition-transform ${isActive ? 'scale-110 drop-shadow-[0_0_8px_rgba(0,229,255,0.7)]' : ''}`} />
            <span className={`text-[10px] font-mono tracking-wider mt-0.5 ${isActive ? 'font-bold text-[#E5FCFF]' : ''}`}>
              {item.label}
            </span>
            {isActive && (
              <span className="w-3 h-0.5 bg-[#00E5FF] rounded-full mt-0.5 shadow-[0_0_6px_#00E5FF]" />
            )}
          </button>
        );
      })}

      {/* Drawer / All Modules Trigger */}
      <button
        id="nav-item-more"
        onClick={() => {
          playHudBeep(720, 0.05);
          onToggleDrawer();
        }}
        className="flex-1 flex flex-col items-center justify-center py-1 px-1 rounded text-[#6F9DA6] hover:text-[#00E5FF] hover:bg-[#007C91]/10 transition-all relative cursor-pointer"
      >
        {unreadNotifications > 0 && (
          <span className="absolute top-0.5 right-3 w-2 h-2 bg-[#FF1744] rounded-full animate-ping" />
        )}
        <Menu className="w-5 h-5" />
        <span className="text-[10px] font-mono tracking-wider mt-0.5">
          Menu
        </span>
      </button>
    </nav>
  );
};
