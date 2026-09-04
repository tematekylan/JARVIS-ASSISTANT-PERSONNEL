import React from 'react';
import { Menu, Plus, User, ShieldAlert, Sparkles, Terminal as TermIcon, Brain, CheckSquare, Settings } from 'lucide-react';
import { JarvisScreen, UserSettings } from '../types';

interface JarvisTopBarProps {
  currentScreen: JarvisScreen;
  onNavigate: (screen: JarvisScreen) => void;
  onToggleDrawer: () => void;
  onOpenAuth: () => void;
  onNewSession: () => void;
  settings: UserSettings;
}

export const JarvisTopBar: React.FC<JarvisTopBarProps> = ({
  currentScreen,
  onNavigate,
  onToggleDrawer,
  onOpenAuth,
  onNewSession,
  settings
}) => {
  const getScreenTitle = (screen: JarvisScreen) => {
    switch (screen) {
      case 'HOME': return "CENTRE DE CONTRÔLE HOLOGRAPHIQUE";
      case 'CHAT': return "CANAL DE COMMUNICATION DIRECTE";
      case 'TASKS': return "GESTIONNAIRE DE TÂCHES TACTIQUES";
      case 'TERMINAL': return "CONSOLE CYBERNÉTIQUE TTY_1";
      case 'COMMAND_CENTER': return "COMMAND CENTER & EXÉCUTION OUTILS";
      case 'MEMORY': return "COFFRE-FORT MÉMORIEL NEURONAL";
      case 'NOTES': return "BLOC-NOTES & DIRECTIVES";
      case 'SYSTEM': return "DIAGNOSTIC MATÉRIEL & RÉACTEUR";
      case 'ACTIVITY': return "JOURNAL DES OPÉRATIONS";
      case 'SETTINGS': return "CONFIGURATION DU PROTOCOLE T-HACK";
      case 'HOLOGRAPHIC_AOD': return "VEILLE QUANTIQUE HOLOGRAPHIQUE";
      default: return "T-HACK AI";
    }
  };

  return (
    <div className="w-full bg-[#070D12]/90 border-b border-[#007C91]/30 px-3 py-2 flex items-center justify-between gap-2 backdrop-blur-md z-10 select-none">
      {/* Drawer Toggle & Screen Title */}
      <div className="flex items-center space-x-2">
        <button
          onClick={onToggleDrawer}
          className="p-1.5 rounded bg-[#0A1219] hover:bg-[#007C91]/30 text-[#00E5FF] border border-[#007C91]/40 transition-all cursor-pointer"
          title="Ouvrir le menu de navigation"
        >
          <Menu className="w-4 h-4" />
        </button>

        <div className="flex items-center space-x-2">
          <span className="text-xs font-mono font-bold text-[#78F7FF] tracking-wider uppercase truncate max-w-[200px] sm:max-w-[320px]">
            {getScreenTitle(currentScreen)}
          </span>
        </div>
      </div>

      {/* Screen quick pills */}
      <div className="hidden lg:flex items-center space-x-1">
        <button
          onClick={() => onNavigate('HOME')}
          className={`px-2 py-1 text-xs font-mono rounded border transition-all cursor-pointer ${
            currentScreen === 'HOME' ? 'bg-[#00E5FF]/20 border-[#00E5FF] text-[#00E5FF]' : 'border-transparent text-[#6F9DA6] hover:text-[#E5FCFF]'
          }`}
        >
          CORE
        </button>
        <button
          onClick={() => onNavigate('CHAT')}
          className={`px-2 py-1 text-xs font-mono rounded border transition-all cursor-pointer ${
            currentScreen === 'CHAT' ? 'bg-[#00E5FF]/20 border-[#00E5FF] text-[#00E5FF]' : 'border-transparent text-[#6F9DA6] hover:text-[#E5FCFF]'
          }`}
        >
          CHAT
        </button>
        <button
          onClick={() => onNavigate('COMMAND_CENTER')}
          className={`px-2 py-1 text-xs font-mono rounded border transition-all cursor-pointer ${
            currentScreen === 'COMMAND_CENTER' ? 'bg-[#00E5FF]/20 border-[#00E5FF] text-[#00E5FF]' : 'border-transparent text-[#6F9DA6] hover:text-[#E5FCFF]'
          }`}
        >
          OUTILS
        </button>
        <button
          onClick={() => onNavigate('TASKS')}
          className={`px-2 py-1 text-xs font-mono rounded border transition-all cursor-pointer ${
            currentScreen === 'TASKS' ? 'bg-[#00E5FF]/20 border-[#00E5FF] text-[#00E5FF]' : 'border-transparent text-[#6F9DA6] hover:text-[#E5FCFF]'
          }`}
        >
          TÂCHES
        </button>
        <button
          onClick={() => onNavigate('TERMINAL')}
          className={`px-2 py-1 text-xs font-mono rounded border transition-all cursor-pointer ${
            currentScreen === 'TERMINAL' ? 'bg-[#00E5FF]/20 border-[#00E5FF] text-[#00E5FF]' : 'border-transparent text-[#6F9DA6] hover:text-[#E5FCFF]'
          }`}
        >
          TTY
        </button>
      </div>

      {/* Right actions: New session + Auth profile */}
      <div className="flex items-center space-x-1.5">
        <button
          onClick={onNewSession}
          className="flex items-center gap-1 px-2.5 py-1 text-xs font-mono rounded bg-[#0A1219] hover:bg-[#00E5FF]/20 border border-[#007C91]/40 hover:border-[#00E5FF] text-[#00E5FF] transition-all cursor-pointer"
          title="Nouvelle session de discussion"
        >
          <Plus className="w-3.5 h-3.5" />
          <span className="hidden sm:inline">NOUVELLE SESSION</span>
        </button>

        <button
          onClick={onOpenAuth}
          className="flex items-center gap-1.5 px-2 py-1 text-xs font-mono rounded bg-[#0A1219] border border-[#007C91]/40 hover:border-[#00E5FF] text-[#E5FCFF] transition-all cursor-pointer"
          title="Profil d'authentification et accréditation"
        >
          <div className="w-5 h-5 rounded-full bg-[#00E5FF]/20 border border-[#00E5FF]/50 flex items-center justify-center text-[#00E5FF] text-[10px] font-bold">
            {settings.userName ? settings.userName.charAt(0).toUpperCase() : 'C'}
          </div>
          <span className="hidden md:inline text-[11px] truncate max-w-[90px]">
            {settings.userName}
          </span>
        </button>
      </div>
    </div>
  );
};
