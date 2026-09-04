import React, { useState } from 'react';
import { Mic, MicOff, Send, Square, Sparkles } from 'lucide-react';
import { AssistantState } from '../types';

interface FuturisticCommandCenterProps {
  onSendCommand: (cmd: string) => void;
  state: AssistantState;
  onToggleVoice: () => void;
  isListening: boolean;
  onStopOutput: () => void;
}

export const FuturisticCommandCenter: React.FC<FuturisticCommandCenterProps> = ({
  onSendCommand,
  state,
  onToggleVoice,
  isListening,
  onStopOutput
}) => {
  const [input, setInput] = useState("");

  const handleSubmit = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!input.trim() || state === 'THINKING' || state === 'PROCESSING') return;
    onSendCommand(input.trim());
    setInput("");
  };

  const handleChipClick = (cmd: string) => {
    onSendCommand(cmd);
  };

  const chips = [
    { label: "/image Blueprint Mark-85", cmd: "/image Blueprint holographique armure Mark 85 en néon cyan" },
    { label: "/humain Discuter", cmd: "/humain Bonjour T-HACK, fais-moi un point de situation informel." },
    { label: "/code Algorithme", cmd: "/code Écris une fonction TypeScript ultra-rapide de recherche binaire." },
    { label: "/debug Audit", cmd: "/debug Analyse la latence des sous-systèmes et vérifie les processus." },
    { label: "/plan Stratégie", cmd: "/plan Établis un plan d'action pour le déploiement du réseau T-HACK." },
    { label: "Météo", cmd: "Météo Paris" },
    { label: "Calcul", cmd: "Calcule (1024 * 64) / 8" },
    { label: "Diagnostic", cmd: "Donne-moi le statut complet du système" }
  ];

  return (
    <div className="w-full space-y-2 select-none">
      {/* Quick Suggestion Chips */}
      <div className="flex items-center space-x-1.5 overflow-x-auto py-1 px-1 scrollbar-none no-scrollbar">
        <Sparkles className="w-3.5 h-3.5 text-[#00E5FF] shrink-0" />
        {chips.map((chip, idx) => (
          <button
            key={idx}
            onClick={() => handleChipClick(chip.cmd)}
            className="shrink-0 text-[11px] font-mono px-2 py-1 rounded bg-[#0A1219] hover:bg-[#007C91]/30 text-[#78F7FF] border border-[#007C91]/40 hover:border-[#00E5FF] transition-all cursor-pointer"
          >
            {chip.label}
          </button>
        ))}
      </div>

      {/* Main Directive Bar */}
      <form onSubmit={handleSubmit} className="relative flex items-center gap-2">
        <div className="relative flex-1 flex items-center bg-[#0A1219] border border-[#007C91]/50 focus-within:border-[#00E5FF] rounded-sm transition-all shadow-[inset_0_0_10px_rgba(0,124,145,0.2)]">
          <span className="pl-3 pr-1 text-xs font-mono text-[#00E5FF] font-bold select-none">
            &gt;
          </span>
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder={isListening ? "Écoute acoustique active... parlez maintenant" : "Saisissez une directive, question ou slash commande (/code, /image, /debug)..."}
            className="w-full py-2.5 px-2 bg-transparent text-[#E5FCFF] placeholder-[#6F9DA6]/60 text-sm font-['Rajdhani',sans-serif] focus:outline-none"
            disabled={state === 'THINKING' || state === 'PROCESSING'}
          />
          {state === 'SPEAKING' && (
            <button
              type="button"
              onClick={onStopOutput}
              className="mr-2 px-2 py-1 rounded bg-[#FF4660]/20 border border-[#FF4660]/50 text-[#FF4660] text-[11px] font-mono flex items-center gap-1 hover:bg-[#FF4660]/30 transition-all cursor-pointer"
              title="Arrêter la voix"
            >
              <Square className="w-3 h-3 fill-current" /> STOP
            </button>
          )}
        </div>

        {/* Voice Trigger Button */}
        <button
          type="button"
          onClick={onToggleVoice}
          className={`p-2.5 rounded-sm border transition-all cursor-pointer flex items-center justify-center ${
            isListening
              ? 'bg-[#FF4660] border-[#FF4660] text-white shadow-[0_0_15px_rgba(255,70,96,0.6)] animate-pulse'
              : 'bg-[#0A1219] border-[#007C91]/50 text-[#00E5FF] hover:border-[#00E5FF] hover:bg-[#00E5FF]/10'
          }`}
          title={isListening ? "Arrêter l'écoute vocale" : "Activer la reconnaissance vocale"}
        >
          {isListening ? <MicOff className="w-4 h-4" /> : <Mic className="w-4 h-4" />}
        </button>

        {/* Send Button */}
        <button
          type="submit"
          disabled={!input.trim() || state === 'THINKING' || state === 'PROCESSING'}
          className={`p-2.5 rounded-sm border transition-all flex items-center justify-center ${
            input.trim() && state !== 'THINKING' && state !== 'PROCESSING'
              ? 'bg-[#00E5FF] border-[#00E5FF] text-[#030609] hover:bg-[#78F7FF] shadow-[0_0_12px_rgba(0,229,255,0.4)] cursor-pointer font-bold'
              : 'bg-[#0A1219] border-[#007C91]/30 text-[#6F9DA6]/40 cursor-not-allowed'
          }`}
          title="Transmettre la directive"
        >
          <Send className="w-4 h-4" />
        </button>
      </form>
    </div>
  );
};
