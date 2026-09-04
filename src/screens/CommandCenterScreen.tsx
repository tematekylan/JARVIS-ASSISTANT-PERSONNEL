import React, { useState } from 'react';
import { 
  Play, 
  Terminal, 
  Cpu, 
  HardDrive, 
  Cloud, 
  Clock, 
  Calculator, 
  FileText, 
  Brain, 
  Globe, 
  AlertOctagon, 
  CheckCircle2, 
  XCircle, 
  Trash2, 
  RotateCw,
  ShieldCheck,
  Send
} from 'lucide-react';
import { HudPanel } from '../components/HudPanel';
import { ToolLog, UserSettings, IncidentReport } from '../types';
import { AVAILABLE_TOOLS, executeTool, conveneAiCouncil } from '../utils/tools';

interface CommandCenterScreenProps {
  toolLogs: ToolLog[];
  onRefreshLogs: () => void;
  onClearLogs: () => void;
  settings: UserSettings;
}

export const CommandCenterScreen: React.FC<CommandCenterScreenProps> = ({
  toolLogs,
  onRefreshLogs,
  onClearLogs,
  settings
}) => {
  const [selectedTool, setSelectedTool] = useState("calculator");
  const [toolInput, setToolInput] = useState("1024 * 42");
  const [isExecuting, setIsExecuting] = useState(false);
  const [executionOutput, setExecutionOutput] = useState<string | null>(null);

  // AI Council states
  const [isConveningCouncil, setIsConveningCouncil] = useState(false);
  const [incidentReport, setIncidentReport] = useState<IncidentReport | null>(null);

  const handleRunTool = async () => {
    if (!selectedTool) return;
    setIsExecuting(true);
    setExecutionOutput(null);
    try {
      const res = await executeTool(selectedTool, toolInput);
      setExecutionOutput(res.result);
      onRefreshLogs();
    } catch (e: any) {
      setExecutionOutput(`Erreur: ${e.message}`);
    } finally {
      setIsExecuting(false);
    }
  };

  const handleConveneAiCouncil = async () => {
    setIsConveningCouncil(true);
    try {
      const report = await conveneAiCouncil(
        "Exception de désynchronisation de flux neuronal dans le module de télémétrie",
        "CommandCenterScreen -> Subsystem Dispatcher"
      );
      setIncidentReport(report);
    } catch (e: any) {
      alert("Erreur lors de la convocation du Collège d'IA");
    } finally {
      setIsConveningCouncil(false);
    }
  };

  const toolIcons: Record<string, any> = {
    calculator: Calculator,
    weather: Cloud,
    world_time: Clock,
    system_status: Cpu,
    notes_manager: FileText,
    memory_vault: Brain,
    web_search: Globe,
    youtube_search: Play,
    maps_navigation: Globe,
    gmail_action: Send
  };

  return (
    <div className="flex-1 p-3 sm:p-5 max-w-5xl mx-auto w-full space-y-4 overflow-y-auto">
      {/* Header Info */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 border-b border-[#007C91]/30 pb-3">
        <div>
          <h1 className="text-xl font-bold font-['Chakra_Petch',sans-serif] text-[#E5FCFF] tracking-wider flex items-center gap-2">
            <Cpu className="w-5 h-5 text-[#00E5FF]" /> COMMAND CENTER // EXÉCUTION D'OUTILS
          </h1>
          <p className="text-xs font-mono text-[#6F9DA6]">
            Passerelle directe d'interopérabilité des modules tactiques T-HACK
          </p>
        </div>

        {/* AI Council Emergency Trigger Button */}
        <button
          onClick={handleConveneAiCouncil}
          disabled={isConveningCouncil}
          className="px-3 py-1.5 rounded bg-[#FF4660]/15 hover:bg-[#FF4660]/25 border border-[#FF4660]/60 text-[#FF4660] text-xs font-mono font-bold flex items-center gap-1.5 transition-all cursor-pointer shadow-[0_0_12px_rgba(255,70,96,0.3)]"
        >
          <AlertOctagon className="w-4 h-4 animate-pulse" />
          {isConveningCouncil ? "DÉLIBÉRATION DU COLLÈGE..." : "RÉUNIR COLLÈGE D'IA D'INCIDENT"}
        </button>
      </div>

      {/* Incident Council Report Dialog if triggered */}
      {incidentReport && (
        <HudPanel title={`RAPPORT D'INCIDENT RÉSULU (${incidentReport.incidentCode})`} badge="COLLÈGE D'IA ACTIF">
          <div className="space-y-3 text-xs font-mono">
            <div className="flex flex-wrap items-center justify-between gap-2 p-2 bg-[#070D12] rounded border border-[#FF4660]/40">
              <div className="text-[#FF8095]">
                <strong>Code :</strong> {incidentReport.errorCode}
              </div>
              <div className="text-[#31F5A3] flex items-center gap-1">
                <CheckCircle2 className="w-3.5 h-3.5" /> Statut : {incidentReport.aiCouncilStatus}
              </div>
            </div>

            <div className="p-3 bg-[#070D12] rounded border border-[#007C91]/40 text-[#E5FCFF] whitespace-pre-wrap leading-relaxed max-h-60 overflow-y-auto">
              {incidentReport.aiCouncilDeliberation}
            </div>

            {incidentReport.aiCouncilHotfixCode && (
              <div className="p-2.5 bg-[#030609] rounded border border-[#31F5A3]/40">
                <div className="text-[10px] text-[#31F5A3] font-bold mb-1">PATCH CORRECTIF GÉNÉRÉ :</div>
                <pre className="text-[11px] text-[#78F7FF] font-mono overflow-x-auto">
                  {incidentReport.aiCouncilHotfixCode}
                </pre>
              </div>
            )}

            <div className="flex justify-end">
              <button
                onClick={() => setIncidentReport(null)}
                className="px-3 py-1 bg-[#0A1219] hover:bg-[#007C91]/30 border border-[#007C91]/50 text-[#00E5FF] rounded cursor-pointer"
              >
                Fermer le rapport
              </button>
            </div>
          </div>
        </HudPanel>
      )}

      {/* Tool Dispatcher Workbench */}
      <HudPanel title="BANQUETTE D'EXÉCUTION DES OUTILS" badge="PILOTAGE DIRECT">
        <div className="space-y-3">
          {/* Tool Selector Chips */}
          <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-5 gap-2">
            {AVAILABLE_TOOLS.map((tool) => {
              const Icon = toolIcons[tool.name] || Terminal;
              const isSelected = selectedTool === tool.name;
              return (
                <button
                  key={tool.name}
                  onClick={() => {
                    setSelectedTool(tool.name);
                    if (tool.name === 'calculator') setToolInput("1024 * 42");
                    else if (tool.name === 'weather') setToolInput("Paris");
                    else if (tool.name === 'world_time') setToolInput("Tokyo");
                    else if (tool.name === 'notes_manager') setToolInput("create Nouvelle note tactique");
                    else if (tool.name === 'memory_vault') setToolInput("save Préfère l'interface sombre Stark");
                    else if (tool.name === 'youtube_search') setToolInput("AC/DC Back in Black");
                    else setToolInput("");
                  }}
                  className={`p-2 rounded text-left border transition-all cursor-pointer flex flex-col justify-between ${
                    isSelected
                      ? 'bg-[#00E5FF]/20 border-[#00E5FF] text-[#00E5FF] shadow-[0_0_10px_rgba(0,229,255,0.2)]'
                      : 'bg-[#070D12] border-[#007C91]/30 text-[#6F9DA6] hover:text-[#E5FCFF] hover:border-[#007C91]'
                  }`}
                >
                  <Icon className="w-4 h-4 mb-1" />
                  <span className="text-[11px] font-mono font-bold truncate">{tool.name}</span>
                </button>
              );
            })}
          </div>

          {/* Input & Run Action */}
          <div className="flex flex-col sm:flex-row gap-2 pt-2">
            <div className="relative flex-1">
              <input
                type="text"
                value={toolInput}
                onChange={(e) => setToolInput(e.target.value)}
                placeholder="Paramètres d'entrée de l'outil..."
                className="w-full py-2 px-3 bg-[#070D12] border border-[#007C91]/50 rounded text-xs font-mono text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
                onKeyDown={(e) => e.key === 'Enter' && handleRunTool()}
              />
            </div>
            <button
              onClick={handleRunTool}
              disabled={isExecuting}
              className="px-4 py-2 bg-[#00E5FF] hover:bg-[#78F7FF] text-[#030609] font-mono font-bold text-xs rounded transition-all flex items-center justify-center gap-1.5 cursor-pointer disabled:opacity-50"
            >
              <Play className="w-3.5 h-3.5 fill-current" />
              {isExecuting ? "EXÉCUTION..." : "LANCER L'OUTIL"}
            </button>
          </div>

          {/* Tool Result Display */}
          {executionOutput && (
            <div className="p-3 bg-[#070D12] border border-[#31F5A3]/40 rounded text-xs font-mono space-y-1">
              <div className="text-[#31F5A3] font-bold flex items-center gap-1">
                <CheckCircle2 className="w-3.5 h-3.5" /> RÉSULTAT DU DISPATCHEUR :
              </div>
              <div className="text-[#E5FCFF] whitespace-pre-wrap bg-[#030609] p-2 rounded border border-[#007C91]/30">
                {executionOutput}
              </div>
            </div>
          )}
        </div>
      </HudPanel>

      {/* Historical Logs of Tool Invocations */}
      <HudPanel title="JOURNAL D'EXÉCUTION DES OUTILS" badge={`${toolLogs.length} TRACES`}>
        <div className="flex justify-between items-center mb-2 text-xs font-mono">
          <span className="text-[#6F9DA6]">Historique récent des appels système</span>
          <div className="flex items-center space-x-2">
            <button
              onClick={onRefreshLogs}
              className="text-[#00E5FF] hover:underline flex items-center gap-1 cursor-pointer"
            >
              <RotateCw className="w-3 h-3" /> Actualiser
            </button>
            {toolLogs.length > 0 && (
              <button
                onClick={onClearLogs}
                className="text-[#FF4660] hover:underline flex items-center gap-1 cursor-pointer"
              >
                <Trash2 className="w-3 h-3" /> Effacer
              </button>
            )}
          </div>
        </div>

        <div className="space-y-2 max-h-72 overflow-y-auto">
          {toolLogs.length === 0 ? (
            <div className="text-center py-6 text-xs font-mono text-[#6F9DA6]">
              Aucun log d'outil enregistré pour le moment.
            </div>
          ) : (
            toolLogs.map((log) => (
              <div
                key={log.id}
                className="p-2.5 bg-[#070D12] rounded border border-[#007C91]/30 text-xs font-mono space-y-1"
              >
                <div className="flex items-center justify-between text-[11px]">
                  <span className="text-[#00E5FF] font-bold">{log.toolName}</span>
                  <div className="flex items-center space-x-2 text-[#6F9DA6]">
                    <span>{log.durationMs}ms</span>
                    <span className={log.status === 'SUCCESS' ? 'text-[#31F5A3]' : 'text-[#FF4660]'}>
                      {log.status}
                    </span>
                  </div>
                </div>
                {log.inputParams && (
                  <div className="text-[11px] text-[#6F9DA6] truncate">
                    &gt; {log.inputParams}
                  </div>
                )}
                <div className="text-[11px] text-[#E5FCFF] bg-[#030609] p-1.5 rounded border border-[#007C91]/20 truncate">
                  {log.outputResult}
                </div>
              </div>
            ))
          )}
        </div>
      </HudPanel>
    </div>
  );
};
