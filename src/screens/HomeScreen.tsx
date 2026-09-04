import React from 'react';
import { AICoreCanvas } from '../components/AICoreCanvas';
import { AudioWaveform } from '../components/AudioWaveform';
import { SystemStatusPanel } from '../components/SystemStatusPanel';
import { FuturisticCommandCenter } from '../components/FuturisticCommandCenter';
import { HudPanel } from '../components/HudPanel';
import { AssistantState, UserSettings, Message, Conversation } from '../types';
import { Sparkles, MessageSquare, Terminal as TermIcon, Sliders, ChevronRight, Tv, Bell, Maximize2, Send, Mail } from 'lucide-react';

interface HomeScreenProps {
  assistantState: AssistantState;
  audioAmplitude: number;
  onSendCommand: (cmd: string) => void;
  onToggleVoice: () => void;
  isListening: boolean;
  onStopOutput: () => void;
  settings: UserSettings;
  messages: Message[];
  activeConversation: Conversation | null;
  onNavigate: (screen: any) => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  assistantState,
  audioAmplitude,
  onSendCommand,
  onToggleVoice,
  isListening,
  onStopOutput,
  settings,
  messages,
  activeConversation,
  onNavigate
}) => {
  const latestAssistantMessage = [...messages].reverse().find(m => m.role === 'assistant');

  return (
    <div className="flex-1 flex flex-col justify-between p-3 sm:p-5 max-w-5xl mx-auto w-full space-y-4 overflow-y-auto">
      {/* Upper Section: Holographic AI Core & Neural State */}
      <div className="flex flex-col items-center justify-center pt-2 pb-1 relative select-none">
        {/* State Banner */}
        <div className="text-center mb-2">
          <span className="text-[11px] font-mono tracking-widest text-[#6F9DA6] uppercase flex items-center justify-center gap-1.5">
            <span className={`w-2 h-2 rounded-full ${
              assistantState === 'ERROR' ? 'bg-[#FF4660]' :
              assistantState === 'SUCCESS' ? 'bg-[#31F5A3]' :
              assistantState === 'LISTENING' ? 'bg-[#00E5FF] animate-ping' :
              assistantState === 'THINKING' ? 'bg-[#78F7FF] animate-pulse' :
              'bg-[#00E5FF]'
            }`} />
            NOYAU QUANTIQUE : {assistantState}
          </span>
          <h1 className="text-xl sm:text-2xl font-bold font-['Chakra_Petch',sans-serif] tracking-wider text-[#E5FCFF] hud-glow">
            T-HACKMAN AI CORE
          </h1>
        </div>

        {/* Dynamic Canvas Core */}
        <div className="relative my-2">
          <AICoreCanvas 
            state={assistantState}
            audioAmplitude={audioAmplitude}
            size={220}
            onClick={onToggleVoice}
          />
        </div>

        {/* Real-time Equalizer Waveform */}
        <div className="w-full max-w-md px-4 mt-1">
          <AudioWaveform 
            state={assistantState}
            audioAmplitude={audioAmplitude}
            height={32}
          />
        </div>

        {/* Current Live Speech / Response Banner */}
        {latestAssistantMessage && (
          <div 
            onClick={() => onNavigate('CHAT')}
            className="w-full max-w-xl mt-2 p-2.5 rounded bg-[#0A1219]/90 border border-[#007C91]/40 hover:border-[#00E5FF]/60 transition-all cursor-pointer flex items-start gap-2 shadow-[0_0_12px_rgba(0,124,145,0.2)]"
          >
            <Sparkles className="w-4 h-4 text-[#00E5FF] shrink-0 mt-0.5" />
            <div className="flex-1 min-w-0">
              <div className="text-[10px] font-mono text-[#00E5FF] font-bold uppercase">
                DERNIÈRE RÉPONSE DU NOYAU
              </div>
              <p className="text-xs text-[#E5FCFF] line-clamp-2 mt-0.5">
                {latestAssistantMessage.content}
              </p>
            </div>
            <ChevronRight className="w-4 h-4 text-[#6F9DA6] shrink-0 self-center" />
          </div>
        )}
      </div>

      {/* Middle Section: Quick Nav Cards & System Telemetry */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
        {/* Navigation Quick Cards */}
        <div 
          onClick={() => onNavigate('CHAT')}
          className="bg-[#0A1219]/70 hover:bg-[#0A1219] border border-[#007C91]/40 hover:border-[#00E5FF] p-3 rounded-sm transition-all cursor-pointer hud-panel-corner flex items-center justify-between"
        >
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-[#00E5FF]/10 text-[#00E5FF] rounded">
              <MessageSquare className="w-4 h-4" />
            </div>
            <div>
              <div className="text-xs font-mono font-bold text-[#E5FCFF]">CANAL DE CHAT</div>
              <div className="text-[11px] text-[#6F9DA6]">Historique & streaming direct</div>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-[#6F9DA6]" />
        </div>

        <div 
          onClick={() => onNavigate('COMMAND_CENTER')}
          className="bg-[#0A1219]/70 hover:bg-[#0A1219] border border-[#007C91]/40 hover:border-[#00E5FF] p-3 rounded-sm transition-all cursor-pointer hud-panel-corner flex items-center justify-between"
        >
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-[#00E5FF]/10 text-[#00E5FF] rounded">
              <Sliders className="w-4 h-4" />
            </div>
            <div>
              <div className="text-xs font-mono font-bold text-[#E5FCFF]">COMMAND CENTER</div>
              <div className="text-[11px] text-[#6F9DA6]">Outils & Conseil d'IA</div>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-[#6F9DA6]" />
        </div>

        <div 
          onClick={() => onNavigate('TERMINAL')}
          className="bg-[#0A1219]/70 hover:bg-[#0A1219] border border-[#007C91]/40 hover:border-[#00E5FF] p-3 rounded-sm transition-all cursor-pointer hud-panel-corner flex items-center justify-between"
        >
          <div className="flex items-center space-x-3">
            <div className="p-2 bg-[#00E5FF]/10 text-[#00E5FF] rounded">
              <TermIcon className="w-4 h-4" />
            </div>
            <div>
              <div className="text-xs font-mono font-bold text-[#E5FCFF]">CONSOLE TTY</div>
              <div className="text-[11px] text-[#6F9DA6]">Directives shell directes</div>
            </div>
          </div>
          <ChevronRight className="w-4 h-4 text-[#6F9DA6]" />
        </div>
      </div>

      {/* System Telemetry Panel */}
      <SystemStatusPanel settings={settings} />

      {/* Quick App Coordination Shortcuts & Wake Word Banner */}
      <div className="p-2.5 bg-[#070D12] border border-[#007C91]/40 rounded flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center space-x-1.5 overflow-x-auto py-0.5">
          <span className="text-[10px] font-mono text-[#6F9DA6] uppercase shrink-0">Passerelles Rapides :</span>
          <button
            onClick={() => onSendCommand("ouvre moi Youtube et recherche Teddy Hackman et tu me lie sa derniere video")}
            className="px-2.5 py-1 text-xs font-mono bg-[#FF4660]/15 border border-[#FF4660]/40 text-[#FF8596] hover:bg-[#FF4660]/25 rounded flex items-center space-x-1 cursor-pointer transition-all shrink-0"
          >
            <Tv className="w-3 h-3" />
            <span>YouTube: Teddy Hackman</span>
          </button>

          <button
            onClick={() => onSendCommand("ouvre whatsapp et prépare un message pour Teddy")}
            className="px-2.5 py-1 text-xs font-mono bg-[#25D366]/15 border border-[#25D366]/40 text-[#25D366] hover:bg-[#25D366]/25 rounded flex items-center space-x-1 cursor-pointer transition-all shrink-0"
          >
            <Send className="w-3 h-3" />
            <span>WhatsApp</span>
          </button>

          <button
            onClick={() => onSendCommand("ouvre gmail et compose un nouveau message")}
            className="px-2.5 py-1 text-xs font-mono bg-[#EA4335]/15 border border-[#EA4335]/40 text-[#EA4335] hover:bg-[#EA4335]/25 rounded flex items-center space-x-1 cursor-pointer transition-all shrink-0"
          >
            <Mail className="w-3 h-3" />
            <span>Gmail</span>
          </button>

          <button
            onClick={() => onNavigate('NOTIFICATIONS')}
            className="px-2.5 py-1 text-xs font-mono bg-[#FFB547]/15 border border-[#FFB547]/40 text-[#FFB547] hover:bg-[#FFB547]/25 rounded flex items-center space-x-1 cursor-pointer transition-all shrink-0"
          >
            <Bell className="w-3 h-3" />
            <span>Alertes Vocales</span>
          </button>
        </div>

        <button
          onClick={() => onSendCommand("HACK AI démarre")}
          className="px-3 py-1 bg-[#00E5FF]/20 border border-[#00E5FF] hover:bg-[#00E5FF]/30 text-[#00E5FF] font-mono text-xs font-bold rounded flex items-center space-x-1.5 transition-all cursor-pointer shadow-[0_0_12px_rgba(0,229,255,0.3)] shrink-0"
          title="Dites 'HACK AI démarre' ou cliquez ici pour activer le mode hologramme plein écran"
        >
          <Maximize2 className="w-3 h-3" />
          <span>HACK AI DÉMARRE (HOLOGRAMME)</span>
        </button>
      </div>

      {/* Bottom Section: Futuristic Command Center */}
      <div className="pt-1">
        <FuturisticCommandCenter 
          onSendCommand={onSendCommand}
          state={assistantState}
          onToggleVoice={onToggleVoice}
          isListening={isListening}
          onStopOutput={onStopOutput}
        />
      </div>
    </div>
  );
};
