import React, { useState, useEffect, useRef } from 'react';
import { Terminal as TermIcon, Play, Trash2 } from 'lucide-react';
import { FuturisticHeader } from '../components/FuturisticHeader';

interface TerminalScreenProps {
  onExecuteCommand: (cmd: string) => void;
}

export const TerminalScreen: React.FC<TerminalScreenProps> = ({ onExecuteCommand }) => {
  const [lines, setLines] = useState<string[]>([
    "T-HACKMAN SYSTEM INITIALIZED...",
    "AI CORE ONLINE [QUANTUM STREAM ACTIVE]",
    "AUDIO SYNTHESIZER & LISTENER CONNECTED.",
    "MEMORY VAULT: LOCAL ENCRYPTION VERIFIED.",
    "NEURAL LINK ESTABLISHED WITH GEMINI-3.8-FLASH.",
    "AWAITING INSTRUCTION... TYPE 'help' FOR LIST OF SYSTEM DIRECTIVES."
  ]);
  const [commandInput, setCommandInput] = useState("");
  const [cursorVisible, setCursorVisible] = useState(true);
  const bottomRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    const interval = setInterval(() => setCursorVisible(v => !v), 500);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [lines]);

  const handleCommand = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!commandInput.trim()) return;

    const cmd = commandInput.trim();
    const time = new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    const newLines = [...lines, `[${time}] > ${cmd}`];

    switch (cmd.toLowerCase()) {
      case "help":
        newLines.push(
          "DIRECTIVES DU SYSTÈME DISPONIBLES :",
          "- status  : Vérifier la télémétrie et les sous-systèmes",
          "- clear   : Vider le tampon d'affichage de la console",
          "- demo    : Déclencher une boucle de démonstration visuelle",
          "- ping    : Mesurer la latence de la liaison neurale",
          "- date    : Afficher la date et l'heure atomique Stark",
          "- /image  : Générer une projection graphique",
          "- /code   : Mode programmation haute fidélité",
          "Toute autre phrase sera transmise directement au Core IA."
        );
        break;
      case "clear":
        setLines([]);
        setCommandInput("");
        return;
      case "status":
        newLines.push(
          "[STATUS] TOUS LES SOUS-SYSTÈMES SONT NOMINAUX.",
          "BATTERIE RÉACTEUR : 98%. QUANTUM CPU : 24%.",
          "MÉMOIRE CHIFVRÉE : INTÈGRE. UPLINK 5G : VERROUILLÉ."
        );
        break;
      case "ping":
        newLines.push("[PONG] Liaison neurale vers le Core : 38ms de réponse synchrone.");
        break;
      case "demo":
        newLines.push("[DÉMO] Cycle des états de l'IA Core en cours d'exécution...");
        break;
      case "date":
        newLines.push(`[DATE ATOMIQUE] ${new Date().toUTCString()}`);
        break;
      default:
        newLines.push("[EXEC] Routage en cours vers le Noyau IA T-HACK...");
        onExecuteCommand(cmd);
        break;
    }

    setLines(newLines);
    setCommandInput("");
  };

  return (
    <div className="flex-1 flex flex-col h-full bg-[#030609] p-2 sm:p-4 overflow-hidden font-mono">
      <div className="flex-1 flex flex-col bg-[#070D12] border border-[#007C91]/50 rounded p-3 shadow-[0_0_20px_rgba(0,124,145,0.2)] overflow-hidden">
        {/* Terminal Title Bar */}
        <div className="flex items-center justify-between border-b border-[#007C91]/30 pb-2 mb-2 text-xs">
          <div className="flex items-center space-x-2 text-[#00E5FF]">
            <TermIcon className="w-4 h-4" />
            <span className="font-bold">TTY_1 CONSOLE [STARK T-HACK OS]</span>
          </div>
          <button
            onClick={() => setLines([])}
            className="text-[10px] text-[#6F9DA6] hover:text-[#FF4660] flex items-center gap-1 cursor-pointer"
            title="Effacer l'écran"
          >
            <Trash2 className="w-3 h-3" /> CLEAR
          </button>
        </div>

        {/* Console Buffer Scroll Area */}
        <div className="flex-1 overflow-y-auto space-y-1 pr-1 text-xs select-text">
          {lines.map((line, idx) => (
            <div
              key={idx}
              className={`leading-relaxed ${
                line.startsWith("[") && line.includes(">")
                  ? "text-[#78F7FF] font-bold"
                  : line.includes("DIRECTIVES") || line.includes("[PONG]") || line.includes("[STATUS]")
                  ? "text-[#31F5A3]"
                  : "text-[#31F5A3]/90"
              }`}
            >
              {line}
            </div>
          ))}
          <div ref={bottomRef} />
        </div>

        {/* Command Line Prompt */}
        <form onSubmit={handleCommand} className="pt-2 mt-2 border-t border-[#007C91]/30 flex items-center gap-2">
          <span className="text-[#00E5FF] text-xs font-bold shrink-0">
            root@t-hack:~#
          </span>
          <input
            type="text"
            value={commandInput}
            onChange={(e) => setCommandInput(e.target.value)}
            placeholder="Tapez 'help' ou une directive..."
            className="flex-1 bg-transparent text-[#E5FCFF] text-xs focus:outline-none font-mono"
            autoFocus
          />
          {/* Blinking block cursor */}
          <div className={`w-2 h-4 bg-[#00E5FF] ${cursorVisible ? 'opacity-100' : 'opacity-0'} shrink-0`} />
          <button
            type="submit"
            className="px-2.5 py-1 bg-[#00E5FF] hover:bg-[#78F7FF] text-[#030609] text-[10px] font-bold rounded cursor-pointer shrink-0"
          >
            EXEC
          </button>
        </form>
      </div>
    </div>
  );
};
