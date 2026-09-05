import React, { useState } from 'react';
import { AICoreCanvas } from '../components/AICoreCanvas';
import { AssistantState, UserSettings, JarvisScreen } from '../types';
import { 
  Mic, 
  MicOff, 
  Send, 
  Sparkles, 
  Sliders, 
  CheckSquare, 
  Terminal, 
  FileCode2, 
  Cpu, 
  ShieldCheck 
} from 'lucide-react';
import { ActionExecutor } from '../automation/ActionExecutor';
import { playHudBeep } from '../utils/audio';

interface HomeScreenProps {
  assistantState: AssistantState;
  audioAmplitude: number;
  onSendCommand: (cmd: string) => void;
  onToggleVoice: () => void;
  isListening: boolean;
  onStopOutput: () => void;
  settings: UserSettings;
  onNavigate: (screen: JarvisScreen) => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  assistantState,
  audioAmplitude,
  onSendCommand,
  onToggleVoice,
  isListening,
  settings,
  onNavigate
}) => {
  const [inputText, setInputText] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputText.trim()) return;
    const text = inputText.trim();
    setInputText("");

    // Check if command is an automation task
    const autoResult = ActionExecutor.execute(text);
    if (autoResult.executed) {
      playHudBeep(880, 0.1);
      return;
    }

    onSendCommand(text);
    onNavigate('CHAT');
  };

  const quickShortcuts = [
    {
      title: "Centre de Commande",
      icon: Sliders,
      screen: 'COMMAND_CENTER' as JarvisScreen,
      color: "#00E5FF",
      desc: "Diagnostics & capteurs"
    },
    {
      title: "Tâches & Rappels",
      icon: CheckSquare,
      screen: 'TASKS' as JarvisScreen,
      color: "#31F5A3",
      desc: "Gestionnaire autonome"
    },
    {
      title: "Console Terminal",
      icon: Terminal,
      screen: 'TERMINAL' as JarvisScreen,
      color: "#FFB020",
      desc: "Commandes cyber"
    },
    {
      title: "Code Android (Kotlin)",
      icon: FileCode2,
      screen: 'KOTLIN_STUDIO' as JarvisScreen,
      color: "#A97BFF",
      desc: "Projet 100% Kotlin"
    }
  ];

  return (
    <div className="flex-1 flex flex-col justify-between p-4 sm:p-6 max-w-4xl mx-auto w-full min-h-[calc(100vh-65px)] select-none">
      
      {/* Top Status Bar */}
      <div className="flex items-center justify-between py-1 px-2">
        <div className="flex items-center space-x-2">
          <span className="text-[11px] font-mono tracking-wider text-[#6F9DA6] uppercase inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#0A1219] border border-[#007C91]/30">
            <span className={`w-2 h-2 rounded-full ${
              assistantState === 'ERROR' ? 'bg-[#FF4660]' :
              assistantState === 'SUCCESS' ? 'bg-[#31F5A3]' :
              assistantState === 'LISTENING' ? 'bg-[#00E5FF] animate-ping' :
              assistantState === 'THINKING' ? 'bg-[#78F7FF] animate-pulse' :
              'bg-[#00E5FF]'
            }`} />
            {assistantState === 'LISTENING' ? 'À L’ÉCOUTE...' :
             assistantState === 'THINKING' ? 'RÉFLEXION QUANTIQUE' :
             assistantState === 'SPEAKING' ? 'VOCALISATION' :
             'T-HACKMAN ASSISTANT ONLINE'}
          </span>
        </div>

        {/* Action quick links */}
        <div className="flex items-center space-x-2 font-mono text-xs">
          <button
            onClick={() => onNavigate('KOTLIN_STUDIO')}
            className="px-2.5 py-1 rounded-lg bg-[#A97BFF]/15 hover:bg-[#A97BFF]/25 border border-[#A97BFF]/40 text-[#A97BFF] flex items-center gap-1.5 transition-all cursor-pointer shadow-[0_0_10px_rgba(169,123,255,0.15)]"
            title="Accéder aux sources Android Kotlin"
          >
            <FileCode2 className="w-3.5 h-3.5" />
            <span className="hidden sm:inline">Projet Kotlin</span>
          </button>

          <button
            onClick={() => onNavigate('COMMAND_CENTER')}
            className="px-2.5 py-1 rounded-lg bg-[#0A1219] hover:bg-[#00E5FF]/20 border border-[#007C91]/30 text-[#6F9DA6] hover:text-[#00E5FF] flex items-center gap-1.5 transition-all cursor-pointer"
            title="Centre de commande"
          >
            <Cpu className="w-3.5 h-3.5 text-[#00E5FF]" />
            <span className="hidden sm:inline">Système</span>
          </button>
        </div>
      </div>

      {/* Center: Hologram Visualizer & Clean Greeting */}
      <div className="flex-1 flex flex-col items-center justify-center my-3">
        
        {/* Hologram Canvas */}
        <div 
          onClick={onToggleVoice}
          className="relative cursor-pointer group my-2"
          title="Cliquez pour activer/désactiver le micro"
        >
          <AICoreCanvas 
            state={assistantState}
            audioAmplitude={audioAmplitude}
            size={220}
            onClick={onToggleVoice}
          />
          <div className="absolute inset-0 rounded-full border border-[#00E5FF]/20 group-hover:border-[#00E5FF]/60 transition-all pointer-events-none" />
        </div>

        {/* Greeting & Subtitle */}
        <div className="text-center mt-2 max-w-md">
          <h1 className="text-2xl sm:text-3xl font-bold text-[#E5FCFF] font-['Chakra_Petch',sans-serif] tracking-wide flex items-center justify-center gap-2">
            <span>Bonjour, {settings.userName || "Teddy"}</span>
            <ShieldCheck className="w-5 h-5 text-[#31F5A3]" />
          </h1>
          <p className="text-xs sm:text-sm text-[#8CA0A8] mt-1 font-sans">
            Assistant personnel cybernétique opérationnel. Que souhaitez-vous accomplir ?
          </p>
        </div>

        {/* Quick Assistant Modules Grid */}
        <div className="mt-5 w-full max-w-lg grid grid-cols-2 sm:grid-cols-4 gap-2">
          {quickShortcuts.map((item, idx) => {
            const Icon = item.icon;
            return (
              <button
                key={idx}
                onClick={() => onNavigate(item.screen)}
                className="p-3 rounded-xl bg-[#070D12]/70 hover:bg-[#0A1219] border border-[#007C91]/30 hover:border-[#00E5FF]/60 transition-all text-left flex flex-col justify-between group cursor-pointer"
              >
                <div className="flex items-center justify-between mb-2">
                  <div 
                    className="p-1.5 rounded-lg border"
                    style={{ 
                      backgroundColor: `${item.color}15`, 
                      borderColor: `${item.color}40`,
                      color: item.color 
                    }}
                  >
                    <Icon className="w-4 h-4" />
                  </div>
                  <span className="text-[10px] text-[#52757E] group-hover:text-[#00E5FF] transition-colors">&rarr;</span>
                </div>
                <div>
                  <div className="text-xs font-bold text-[#E5FCFF] font-['Chakra_Petch',sans-serif] group-hover:text-[#00E5FF] transition-colors">
                    {item.title}
                  </div>
                  <div className="text-[10px] text-[#6F9DA6] truncate">
                    {item.desc}
                  </div>
                </div>
              </button>
            );
          })}
        </div>
      </div>

      {/* Bottom: Clean Input Bar */}
      <div className="w-full max-w-2xl mx-auto pb-2">
        <form 
          onSubmit={handleSubmit}
          className="relative flex items-center bg-[#0D1821] border border-[#007C91]/50 focus-within:border-[#00E5FF] rounded-2xl p-2 shadow-[0_0_20px_rgba(0,124,145,0.2)] transition-all"
        >
          {/* Voice Input Button */}
          <button
            type="button"
            onClick={onToggleVoice}
            className={`p-2.5 rounded-xl transition-all cursor-pointer mr-2 shrink-0 ${
              isListening
                ? 'bg-[#FF4660]/20 text-[#FF4660] animate-pulse'
                : 'text-[#6F9DA6] hover:text-[#00E5FF] hover:bg-[#00E5FF]/10'
            }`}
            title={isListening ? "Arrêter l'écoute" : "Activer la voix"}
          >
            {isListening ? <Mic className="w-5 h-5" /> : <MicOff className="w-5 h-5" />}
          </button>

          {/* Text Input Field */}
          <input
            type="text"
            value={inputText}
            onChange={(e) => setInputText(e.target.value)}
            placeholder="Posez une question ou donnez un ordre (ex: Ajoute une tâche, Diagnostic système)..."
            className="flex-1 bg-transparent text-xs sm:text-sm text-[#E5FCFF] placeholder-[#5B7B88] outline-none font-sans px-1"
          />

          {/* Submit Button */}
          <button
            type="submit"
            disabled={!inputText.trim()}
            className="p-2.5 rounded-xl bg-[#00E5FF]/20 hover:bg-[#00E5FF]/30 text-[#00E5FF] disabled:opacity-30 disabled:hover:bg-[#00E5FF]/20 transition-all cursor-pointer shrink-0 ml-1"
            title="Envoyer"
          >
            <Send className="w-4 h-4" />
          </button>
        </form>

        <div className="text-center mt-2 flex items-center justify-center space-x-2 text-[10px] text-[#52757E] font-mono">
          <span>T-HACKMAN AI Core v2.5</span>
          <span>&bull;</span>
          <span>Architecture Mobile 100% Kotlin</span>
          <span>&bull;</span>
          <span>Assistant Personnel Autonome</span>
        </div>
      </div>

    </div>
  );
};
