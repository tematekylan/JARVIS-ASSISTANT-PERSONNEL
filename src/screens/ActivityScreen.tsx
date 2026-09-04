import React from 'react';
import { Activity, ShieldCheck, Wrench, MessageSquare, AlertTriangle, RotateCw } from 'lucide-react';
import { HudPanel } from '../components/HudPanel';
import { ToolLog, Message, IncidentReport } from '../types';

interface ActivityScreenProps {
  toolLogs: ToolLog[];
  messages: Message[];
  incidents: IncidentReport[];
  onRefresh: () => void;
}

export const ActivityScreen: React.FC<ActivityScreenProps> = ({
  toolLogs,
  messages,
  incidents,
  onRefresh
}) => {
  // Combine all activities chronologically
  const items: Array<{
    id: string;
    type: 'tool' | 'chat' | 'incident' | 'system';
    title: string;
    detail: string;
    timestamp: number;
    badge: string;
    color: string;
  }> = [];

  toolLogs.forEach(l => {
    items.push({
      id: l.id,
      type: 'tool',
      title: `Appel d'outil : ${l.toolName}`,
      detail: l.outputResult,
      timestamp: l.timestamp,
      badge: l.status,
      color: l.status === 'SUCCESS' ? 'text-[#31F5A3]' : 'text-[#FF4660]'
    });
  });

  messages.slice(-15).forEach(m => {
    items.push({
      id: m.id,
      type: 'chat',
      title: m.role === 'user' ? "Directive utilisateur transmise" : "Réponse générée par le Core",
      detail: m.content,
      timestamp: m.timestamp,
      badge: m.role.toUpperCase(),
      color: m.role === 'user' ? 'text-[#00E5FF]' : 'text-[#78F7FF]'
    });
  });

  incidents.forEach(inc => {
    items.push({
      id: inc.id,
      type: 'incident',
      title: `Incident résolu : ${inc.incidentCode}`,
      detail: inc.errorMessage,
      timestamp: inc.timestamp,
      badge: inc.aiCouncilStatus,
      color: 'text-[#FF8095]'
    });
  });

  // Sort descending
  items.sort((a, b) => b.timestamp - a.timestamp);

  return (
    <div className="flex-1 p-3 sm:p-5 max-w-4xl mx-auto w-full space-y-4 overflow-y-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[#007C91]/30 pb-3">
        <div>
          <h1 className="text-xl font-bold font-['Chakra_Petch',sans-serif] text-[#E5FCFF] tracking-wider flex items-center gap-2">
            <Activity className="w-5 h-5 text-[#00E5FF]" /> JOURNAL D'ACTIVITÉ // AUDIT TRAIL
          </h1>
          <p className="text-xs font-mono text-[#6F9DA6]">
            Historique complet des transactions, outils exécutés et événements système
          </p>
        </div>

        <button
          onClick={onRefresh}
          className="px-3.5 py-1.5 rounded bg-[#0A1219] hover:bg-[#007C91]/30 border border-[#007C91]/50 text-[#00E5FF] text-xs font-mono flex items-center gap-1.5 transition-all cursor-pointer"
        >
          <RotateCw className="w-3.5 h-3.5" /> ACTUALISER
        </button>
      </div>

      {/* Activity Timeline */}
      <HudPanel title="FLUX DES ÉVÉNEMENTS RÉCENTS" badge={`${items.length} ENTRÉES`}>
        <div className="space-y-2.5 max-h-[65vh] overflow-y-auto">
          {items.length === 0 ? (
            <div className="text-center py-8 text-xs font-mono text-[#6F9DA6]">
              Aucun événement récent enregistré.
            </div>
          ) : (
            items.map((item) => (
              <div
                key={item.id}
                className="p-3 bg-[#070D12] rounded border border-[#007C91]/30 flex items-start justify-between gap-3 text-xs font-mono"
              >
                <div className="space-y-1 flex-1 min-w-0">
                  <div className="flex items-center space-x-2">
                    <span className={`font-bold ${item.color}`}>{item.title}</span>
                    <span className="text-[10px] px-1.5 py-0.2 rounded bg-[#0A1219] border border-[#007C91]/30 text-[#6F9DA6]">
                      {item.badge}
                    </span>
                  </div>
                  <p className="text-[11px] text-[#6F9DA6] line-clamp-2">
                    {item.detail}
                  </p>
                </div>

                <div className="text-[10px] text-[#6F9DA6] shrink-0 text-right">
                  {new Date(item.timestamp).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
                </div>
              </div>
            ))
          )}
        </div>
      </HudPanel>
    </div>
  );
};
