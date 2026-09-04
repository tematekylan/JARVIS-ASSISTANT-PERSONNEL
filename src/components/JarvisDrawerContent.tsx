import React from 'react';
import { 
  Home, 
  MessageSquare, 
  Cpu, 
  Terminal as TermIcon, 
  Sliders, 
  Brain, 
  FileText, 
  CheckSquare, 
  Activity, 
  Eye, 
  Settings, 
  Pin, 
  Trash2, 
  Plus, 
  X,
  ShieldCheck,
  Tv,
  Bell
} from 'lucide-react';
import { Conversation, JarvisScreen, UserSettings } from '../types';

interface JarvisDrawerContentProps {
  isOpen: boolean;
  onClose: () => void;
  currentScreen: JarvisScreen;
  onSelectScreen: (screen: JarvisScreen) => void;
  conversations: Conversation[];
  activeConversationId: string;
  onSelectConversation: (id: string) => void;
  onNewConversation: () => void;
  onTogglePinConversation: (id: string) => void;
  onDeleteConversation: (id: string) => void;
  settings: UserSettings;
}

export const JarvisDrawerContent: React.FC<JarvisDrawerContentProps> = ({
  isOpen,
  onClose,
  currentScreen,
  onSelectScreen,
  conversations,
  activeConversationId,
  onSelectConversation,
  onNewConversation,
  onTogglePinConversation,
  onDeleteConversation,
  settings
}) => {
  if (!isOpen) return null;

  const navItems = [
    { screen: 'HOME' as JarvisScreen, label: 'Tableau de Bord Holographique', icon: Home },
    { screen: 'CHAT' as JarvisScreen, label: 'Canal de Discussion', icon: MessageSquare },
    { screen: 'COMMAND_CENTER' as JarvisScreen, label: 'Command Center & Outils', icon: Sliders },
    { screen: 'EXTERNAL_APPS' as JarvisScreen, label: 'Passerelle YouTube / Apps', icon: Tv },
    { screen: 'NOTIFICATIONS' as JarvisScreen, label: 'Notifications & Alertes', icon: Bell },
    { screen: 'TASKS' as JarvisScreen, label: 'Gestionnaire de Tâches', icon: CheckSquare },
    { screen: 'TERMINAL' as JarvisScreen, label: 'Console Terminal TTY', icon: TermIcon },
    { screen: 'MEMORY' as JarvisScreen, label: 'Coffre-fort Mémoriel', icon: Brain },
    { screen: 'NOTES' as JarvisScreen, label: 'Bloc-notes & Directives', icon: FileText },
    { screen: 'SYSTEM' as JarvisScreen, label: 'Diagnostic Réacteur Arc', icon: Cpu },
    { screen: 'ACTIVITY' as JarvisScreen, label: 'Journal des Opérations', icon: Activity },
    { screen: 'SETTINGS' as JarvisScreen, label: 'Configuration & Paramètres', icon: Settings },
    { screen: 'HOLOGRAPHIC_AOD' as JarvisScreen, label: 'Écran de Veille Quantique', icon: Eye }
  ];

  return (
    <div className="fixed inset-0 z-40 flex">
      {/* Backdrop */}
      <div 
        className="fixed inset-0 bg-black/70 backdrop-blur-xs transition-opacity" 
        onClick={onClose}
      />

      {/* Drawer Panel */}
      <div className="relative w-72 sm:w-80 max-w-[85vw] h-full bg-[#070D12] border-r border-[#007C91]/40 flex flex-col z-10 shadow-[0_0_25px_rgba(0,124,145,0.3)]">
        {/* Drawer Header */}
        <div className="p-4 border-b border-[#007C91]/30 flex items-center justify-between">
          <div className="flex items-center space-x-2">
            <div className="w-8 h-8 rounded-xs bg-[#0A1219] border border-[#00E5FF] flex items-center justify-center text-[#00E5FF] font-bold shadow-[0_0_8px_rgba(0,229,255,0.4)]">
              TH
            </div>
            <div>
              <div className="text-sm font-bold font-['Chakra_Petch',sans-serif] text-[#E5FCFF]">
                T-HACKMAN AI
              </div>
              <div className="text-[10px] font-mono text-[#00E5FF]">
                {settings.securityClearanceLevel}
              </div>
            </div>
          </div>
          <button 
            onClick={onClose}
            className="p-1 rounded text-[#6F9DA6] hover:text-[#00E5FF] transition-colors cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Scrollable Navigation */}
        <div className="flex-1 overflow-y-auto p-3 space-y-4">
          {/* Main Navigation Modules */}
          <div>
            <div className="text-[10px] font-mono tracking-wider text-[#6F9DA6] uppercase px-2 mb-1">
              MODULES DU SYSTÈME
            </div>
            <div className="space-y-1">
              {navItems.map((item) => {
                const Icon = item.icon;
                const isSelected = currentScreen === item.screen;
                return (
                  <button
                    key={item.screen}
                    onClick={() => {
                      onSelectScreen(item.screen);
                      onClose();
                    }}
                    className={`w-full flex items-center space-x-2.5 px-3 py-2 rounded text-xs font-mono transition-all cursor-pointer ${
                      isSelected
                        ? 'bg-[#00E5FF]/20 border border-[#00E5FF]/60 text-[#00E5FF] shadow-[0_0_8px_rgba(0,229,255,0.2)]'
                        : 'text-[#6F9DA6] hover:text-[#E5FCFF] hover:bg-[#0A1219]'
                    }`}
                  >
                    <Icon className={`w-4 h-4 ${isSelected ? 'text-[#00E5FF]' : 'text-[#6F9DA6]'}`} />
                    <span className="truncate">{item.label}</span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Sessions List */}
          <div>
            <div className="flex items-center justify-between px-2 mb-1">
              <span className="text-[10px] font-mono tracking-wider text-[#6F9DA6] uppercase">
                SESSIONS ENREGISTRÉES
              </span>
              <button
                onClick={() => {
                  onNewConversation();
                  onSelectScreen('CHAT');
                  onClose();
                }}
                className="text-[10px] font-mono text-[#00E5FF] hover:underline flex items-center gap-0.5 cursor-pointer"
              >
                <Plus className="w-3 h-3" /> NOUVELLE
              </button>
            </div>

            <div className="space-y-1">
              {conversations.map((conv) => {
                const isAct = conv.id === activeConversationId && currentScreen === 'CHAT';
                return (
                  <div
                    key={conv.id}
                    className={`group flex items-center justify-between px-2.5 py-1.5 rounded text-xs font-mono transition-all ${
                      isAct
                        ? 'bg-[#0A1219] border border-[#007C91]/60 text-[#78F7FF]'
                        : 'text-[#6F9DA6] hover:bg-[#0A1219]/60 hover:text-[#E5FCFF]'
                    }`}
                  >
                    <button
                      onClick={() => {
                        onSelectConversation(conv.id);
                        onSelectScreen('CHAT');
                        onClose();
                      }}
                      className="flex-1 flex items-center space-x-2 text-left truncate cursor-pointer"
                    >
                      <MessageSquare className="w-3.5 h-3.5 shrink-0 opacity-70" />
                      <span className="truncate">{conv.title}</span>
                    </button>

                    <div className="flex items-center space-x-1 opacity-0 group-hover:opacity-100 transition-opacity">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          onTogglePinConversation(conv.id);
                        }}
                        className={`p-1 hover:text-[#00E5FF] ${conv.isPinned ? 'text-[#00E5FF]' : 'text-[#6F9DA6]'}`}
                        title={conv.isPinned ? "Désépingler" : "Épingler"}
                      >
                        <Pin className="w-3 h-3" />
                      </button>
                      {conversations.length > 1 && (
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            onDeleteConversation(conv.id);
                          }}
                          className="p-1 hover:text-[#FF4660] text-[#6F9DA6]"
                          title="Supprimer la session"
                        >
                          <Trash2 className="w-3 h-3" />
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        {/* Drawer Footer Status */}
        <div className="p-3 border-t border-[#007C91]/30 bg-[#0A1219]">
          <div className="flex items-center space-x-2 text-xs font-mono text-[#6F9DA6]">
            <ShieldCheck className="w-4 h-4 text-[#31F5A3]" />
            <span className="truncate">STARK PROTOCOL MK-85</span>
          </div>
          <div className="text-[10px] font-mono text-[#6F9DA6]/60 mt-0.5">
            Model: {settings.aiModel}
          </div>
        </div>
      </div>
    </div>
  );
};
