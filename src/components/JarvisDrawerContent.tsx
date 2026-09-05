import React, { useState, useRef } from 'react';
import { 
  MessageSquare, 
  Settings, 
  Trash2, 
  Plus, 
  X,
  Share2,
  Edit2,
  Download,
  Check,
  Sparkles,
  Sliders,
  CheckSquare,
  FileCode2,
  Terminal
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
  onDeleteConversation: (id: string) => void;
  onRenameConversation?: (id: string, newTitle: string) => void;
  onShareConversation?: (conv: Conversation) => void;
  onOpenUpdateModal?: () => void;
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
  onDeleteConversation,
  onRenameConversation,
  onShareConversation,
  onOpenUpdateModal,
  settings
}) => {
  // Context menu for sections (triggered via long press on mobile or right click on desktop)
  const [contextMenuConvId, setContextMenuConvId] = useState<string | null>(null);
  const [editingConvId, setEditingConvId] = useState<string | null>(null);
  const [editingTitle, setEditingTitle] = useState("");
  const [shareToast, setShareToast] = useState<string | null>(null);

  const longPressTimerRef = useRef<NodeJS.Timeout | null>(null);

  if (!isOpen) return null;

  // Touch Long-press handlers
  const handleTouchStart = (convId: string) => {
    longPressTimerRef.current = setTimeout(() => {
      setContextMenuConvId(convId);
      // Vibrate if supported
      if (navigator.vibrate) navigator.vibrate(50);
    }, 500);
  };

  const handleTouchEnd = () => {
    if (longPressTimerRef.current) {
      clearTimeout(longPressTimerRef.current);
      longPressTimerRef.current = null;
    }
  };

  const handleContextMenu = (e: React.MouseEvent, convId: string) => {
    e.preventDefault();
    setContextMenuConvId(convId);
  };

  const handleStartRename = (conv: Conversation) => {
    setEditingConvId(conv.id);
    setEditingTitle(conv.title);
    setContextMenuConvId(null);
  };

  const handleSaveRename = (convId: string) => {
    if (editingTitle.trim() && onRenameConversation) {
      onRenameConversation(convId, editingTitle.trim());
    }
    setEditingConvId(null);
  };

  const handleShare = (conv: Conversation) => {
    setContextMenuConvId(null);
    if (onShareConversation) {
      onShareConversation(conv);
    } else {
      const shareText = `T-HACK AI • Section: ${conv.title}\n` + 
        conv.messages.map(m => `[${m.role === 'user' ? 'Moi' : 'T-HACK'}]: ${m.content}`).join('\n\n');
      navigator.clipboard.writeText(shareText);
      setShareToast("Lien et contenu de la section copiés !");
      setTimeout(() => setShareToast(null), 2500);
    }
  };

  return (
    <div className="fixed inset-0 z-40 flex">
      {/* Backdrop */}
      <div 
        className="fixed inset-0 bg-black/75 backdrop-blur-xs transition-opacity" 
        onClick={() => {
          setContextMenuConvId(null);
          onClose();
        }}
      />

      {/* Clean Minimalist Drawer (ChatGPT / DeepSeek style) */}
      <div className="relative w-72 sm:w-80 max-w-[85vw] h-full bg-[#070D12] border-r border-[#007C91]/30 flex flex-col z-10 shadow-[0_0_30px_rgba(0,124,145,0.3)]">
        
        {/* Header */}
        <div className="p-3.5 border-b border-[#007C91]/20 flex items-center justify-between bg-[#0A1219]">
          <div className="flex items-center space-x-2.5">
            <div className="w-7 h-7 rounded bg-[#00E5FF]/10 border border-[#00E5FF]/50 flex items-center justify-center text-[#00E5FF] font-bold text-xs">
              TH
            </div>
            <span className="text-sm font-bold font-['Chakra_Petch',sans-serif] tracking-wider text-[#E5FCFF]">
              T-HACKMAN AI
            </span>
          </div>
          <button 
            onClick={onClose}
            className="p-1 rounded text-[#6F9DA6] hover:text-[#00E5FF] transition-colors cursor-pointer"
            title="Fermer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Main Navigation links */}
        <div className="p-3 border-b border-[#007C91]/20 space-y-1 font-mono text-xs">
          <button
            onClick={() => {
              onSelectScreen('HOME');
              onClose();
            }}
            className={`w-full py-2 px-3 rounded flex items-center space-x-2.5 transition-all cursor-pointer ${
              currentScreen === 'HOME'
                ? 'bg-[#00E5FF]/20 border border-[#00E5FF] text-[#E5FCFF] font-bold'
                : 'text-[#8CA0A8] hover:bg-[#0A1219] hover:text-[#E5FCFF]'
            }`}
          >
            <Sparkles className="w-4 h-4 text-[#00E5FF]" />
            <span>Accueil T-HACKMAN</span>
          </button>

          <button
            onClick={() => {
              onSelectScreen('COMMAND_CENTER');
              onClose();
            }}
            className={`w-full py-2 px-3 rounded flex items-center space-x-2.5 transition-all cursor-pointer ${
              currentScreen === 'COMMAND_CENTER'
                ? 'bg-[#00E5FF]/20 border border-[#00E5FF] text-[#E5FCFF] font-bold'
                : 'text-[#8CA0A8] hover:bg-[#0A1219] hover:text-[#E5FCFF]'
            }`}
          >
            <Sliders className="w-4 h-4 text-[#00E5FF]" />
            <span>Centre de Commande</span>
          </button>

          <button
            onClick={() => {
              onSelectScreen('TASKS');
              onClose();
            }}
            className={`w-full py-2 px-3 rounded flex items-center space-x-2.5 transition-all cursor-pointer ${
              currentScreen === 'TASKS'
                ? 'bg-[#00E5FF]/20 border border-[#00E5FF] text-[#E5FCFF] font-bold'
                : 'text-[#8CA0A8] hover:bg-[#0A1219] hover:text-[#E5FCFF]'
            }`}
          >
            <CheckSquare className="w-4 h-4 text-[#31F5A3]" />
            <span>Tâches & Protocoles</span>
          </button>

          <button
            onClick={() => {
              onSelectScreen('KOTLIN_STUDIO');
              onClose();
            }}
            className={`w-full py-2 px-3 rounded flex items-center justify-between transition-all cursor-pointer ${
              currentScreen === 'KOTLIN_STUDIO'
                ? 'bg-[#A97BFF]/25 border border-[#A97BFF] text-[#E5FCFF] font-bold'
                : 'text-[#A97BFF] hover:bg-[#A97BFF]/10 hover:text-[#E5FCFF]'
            }`}
          >
            <div className="flex items-center space-x-2.5">
              <FileCode2 className="w-4 h-4 text-[#A97BFF]" />
              <span className="font-semibold">Sources Android (Kotlin)</span>
            </div>
            <span className="text-[10px] px-1 py-0.5 rounded bg-[#A97BFF]/20 text-[#A97BFF] font-mono">100% KT</span>
          </button>

          <button
            onClick={() => {
              onNewConversation();
              onSelectScreen('CHAT');
              onClose();
            }}
            className="w-full py-2 px-3 rounded bg-[#00E5FF]/15 hover:bg-[#00E5FF]/25 border border-[#00E5FF]/40 text-[#00E5FF] font-medium flex items-center justify-center space-x-2 transition-all cursor-pointer shadow-[0_0_10px_rgba(0,229,255,0.15)] mt-1.5"
          >
            <Plus className="w-4 h-4" />
            <span>Nouvelle discussion IA</span>
          </button>
        </div>

        {/* Sections où on a travaillé (Conversation list) */}
        <div className="flex-1 overflow-y-auto p-3 space-y-1">
          <div className="text-[10px] font-mono text-[#6F9DA6] uppercase px-2 mb-2 tracking-wider flex items-center justify-between">
            <span>SECTIONS DE TRAVAIL</span>
            <span className="text-[9px] text-[#426972]">(Appui long = Options)</span>
          </div>

          {conversations.length === 0 ? (
            <div className="text-center py-8 text-xs text-[#6F9DA6] font-mono">
              Aucune section enregistrée
            </div>
          ) : (
            conversations.map((conv) => {
              const isActive = conv.id === activeConversationId;
              const isEditing = editingConvId === conv.id;
              const showMenu = contextMenuConvId === conv.id;

              return (
                <div key={conv.id} className="relative group">
                  {isEditing ? (
                    <div className="flex items-center space-x-1 p-1 bg-[#0A1219] border border-[#00E5FF] rounded">
                      <input
                        type="text"
                        value={editingTitle}
                        onChange={(e) => setEditingTitle(e.target.value)}
                        autoFocus
                        onKeyDown={(e) => {
                          if (e.key === 'Enter') handleSaveRename(conv.id);
                          if (e.key === 'Escape') setEditingConvId(null);
                        }}
                        className="flex-1 bg-transparent text-xs text-[#E5FCFF] outline-none px-1"
                      />
                      <button
                        onClick={() => handleSaveRename(conv.id)}
                        className="p-1 text-[#31F5A3] hover:bg-[#31F5A3]/20 rounded cursor-pointer"
                        title="Valider"
                      >
                        <Check className="w-3.5 h-3.5" />
                      </button>
                      <button
                        onClick={() => setEditingConvId(null)}
                        className="p-1 text-[#FF4660] hover:bg-[#FF4660]/20 rounded cursor-pointer"
                        title="Annuler"
                      >
                        <X className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  ) : (
                    <div
                      onTouchStart={() => handleTouchStart(conv.id)}
                      onTouchEnd={handleTouchEnd}
                      onTouchMove={handleTouchEnd}
                      onContextMenu={(e) => handleContextMenu(e, conv.id)}
                      onClick={() => {
                        onSelectConversation(conv.id);
                        onClose();
                      }}
                      className={`w-full text-left py-2.5 px-3 rounded flex items-center justify-between text-xs transition-all cursor-pointer select-none ${
                        isActive
                          ? 'bg-[#00E5FF]/20 border border-[#00E5FF]/60 text-[#E5FCFF] font-medium'
                          : 'text-[#8CA0A8] hover:bg-[#0A1219] hover:text-[#E5FCFF]'
                      }`}
                    >
                      <div className="flex items-center space-x-2.5 truncate">
                        <MessageSquare className={`w-3.5 h-3.5 shrink-0 ${isActive ? 'text-[#00E5FF]' : 'text-[#6F9DA6]'}`} />
                        <span className="truncate">{conv.title}</span>
                      </div>
                      
                      {/* Mobile options dots button */}
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          setContextMenuConvId(showMenu ? null : conv.id);
                        }}
                        className="opacity-60 hover:opacity-100 p-1 text-[#6F9DA6] hover:text-[#00E5FF] transition-opacity cursor-pointer"
                        title="Options de la section"
                      >
                        &bull;&bull;&bull;
                      </button>
                    </div>
                  )}

                  {/* Context Menu Modal / Popover */}
                  {showMenu && (
                    <div className="absolute right-2 top-10 z-50 w-48 bg-[#0D1821] border border-[#00E5FF]/50 rounded-md shadow-xl py-1 text-xs animate-in fade-in duration-150">
                      <div className="px-3 py-1.5 border-b border-[#007C91]/30 text-[10px] font-mono text-[#6F9DA6] uppercase truncate">
                        {conv.title}
                      </div>

                      {/* Modifier une section */}
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          handleStartRename(conv);
                        }}
                        className="w-full text-left px-3 py-2 flex items-center space-x-2 text-[#E5FCFF] hover:bg-[#00E5FF]/15 transition-colors cursor-pointer"
                      >
                        <Edit2 className="w-3.5 h-3.5 text-[#00E5FF]" />
                        <span>Modifier le titre</span>
                      </button>

                      {/* Partager une section */}
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          handleShare(conv);
                        }}
                        className="w-full text-left px-3 py-2 flex items-center space-x-2 text-[#E5FCFF] hover:bg-[#00E5FF]/15 transition-colors cursor-pointer"
                      >
                        <Share2 className="w-3.5 h-3.5 text-[#31F5A3]" />
                        <span>Partager la section</span>
                      </button>

                      {/* Supprimer une section */}
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setContextMenuConvId(null);
                          onDeleteConversation(conv.id);
                        }}
                        className="w-full text-left px-3 py-2 flex items-center space-x-2 text-[#FF4660] hover:bg-[#FF4660]/15 transition-colors cursor-pointer"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                        <span>Supprimer la section</span>
                      </button>
                    </div>
                  )}
                </div>
              );
            })
          )}
        </div>

        {/* Share Feedback Toast */}
        {shareToast && (
          <div className="mx-3 mb-2 p-2 bg-[#31F5A3]/20 border border-[#31F5A3] rounded text-[11px] text-[#31F5A3] text-center font-mono">
            {shareToast}
          </div>
        )}

        {/* Footer: Only Settings & Update trigger */}
        <div className="p-3 border-t border-[#007C91]/20 space-y-1.5 bg-[#0A1219]">
          
          {/* Update button */}
          <button
            onClick={() => {
              if (onOpenUpdateModal) onOpenUpdateModal();
              onClose();
            }}
            className="w-full py-2 px-3 rounded flex items-center justify-between text-xs font-mono text-[#31F5A3] bg-[#31F5A3]/10 hover:bg-[#31F5A3]/20 border border-[#31F5A3]/30 transition-all cursor-pointer"
          >
            <div className="flex items-center space-x-2">
              <Download className="w-4 h-4 text-[#31F5A3]" />
              <span>Mise à jour v2.5</span>
            </div>
            <span className="text-[10px] px-1.5 py-0.5 rounded bg-[#31F5A3]/20 text-[#31F5A3] font-bold">
              Prête
            </span>
          </button>

          {/* Reboot / Splash trigger */}
          <button
            onClick={() => {
              onSelectScreen('SPLASH');
              onClose();
            }}
            className="w-full py-1.5 px-3 rounded flex items-center justify-between text-xs font-mono text-[#6F9DA6] hover:text-[#00E5FF] hover:bg-[#070D12] transition-all cursor-pointer"
            title="Relancer l'animation d'initialisation système"
          >
            <div className="flex items-center space-x-2">
              <Sparkles className="w-3.5 h-3.5 text-[#00E5FF]" />
              <span>Initialisation Système (Splash)</span>
            </div>
            <span className="text-[10px] text-[#00E5FF]">&gt;</span>
          </button>

          {/* Settings button */}
          <button
            onClick={() => {
              onSelectScreen('SETTINGS');
              onClose();
            }}
            className={`w-full py-2 px-3 rounded flex items-center justify-between text-xs font-mono transition-all cursor-pointer ${
              currentScreen === 'SETTINGS'
                ? 'bg-[#00E5FF]/20 border border-[#00E5FF] text-[#00E5FF] font-bold'
                : 'text-[#6F9DA6] hover:bg-[#070D12] hover:text-[#E5FCFF]'
            }`}
          >
            <div className="flex items-center space-x-2">
              <Settings className="w-4 h-4 text-[#00E5FF]" />
              <span>Paramètres</span>
            </div>
            <span className="text-[10px] text-[#6F9DA6]">&bull;</span>
          </button>
        </div>

      </div>
    </div>
  );
};
