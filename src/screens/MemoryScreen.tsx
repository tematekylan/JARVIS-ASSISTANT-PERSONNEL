import React, { useState } from 'react';
import { Brain, Plus, Trash2, Edit3, ShieldAlert, Check, X, ToggleLeft, ToggleRight } from 'lucide-react';
import { Memory, UserSettings } from '../types';
import { HudPanel } from '../components/HudPanel';

interface MemoryScreenProps {
  memories: Memory[];
  onAddMemory: (key: string, content: string, category: Memory['category']) => void;
  onUpdateMemory: (mem: Memory) => void;
  onDeleteMemory: (id: string) => void;
  onWipeAllMemories: () => void;
  settings: UserSettings;
  onUpdateSettings: (settings: UserSettings) => void;
}

export const MemoryScreen: React.FC<MemoryScreenProps> = ({
  memories,
  onAddMemory,
  onUpdateMemory,
  onDeleteMemory,
  onWipeAllMemories,
  settings,
  onUpdateSettings
}) => {
  const [selectedCat, setSelectedCat] = useState<string>("Toutes");
  const [showAddModal, setShowAddModal] = useState(false);
  const [showWipeConfirm, setShowWipeConfirm] = useState(false);
  const [keyInput, setKeyInput] = useState("");
  const [contentInput, setContentInput] = useState("");
  const [categoryInput, setCategoryInput] = useState<Memory['category']>("General");

  const categories = ["Toutes", "General", "Preferences", "Work", "Identity", "System"];

  const filteredMemories = memories.filter(m =>
    selectedCat === "Toutes" ? true : m.category === selectedCat
  );

  const activeCount = memories.filter(m => m.isActive).length;

  const handleToggleMemoryEngine = () => {
    onUpdateSettings({
      ...settings,
      memoryEnabled: !settings.memoryEnabled
    });
  };

  const handleToggleMemoryItem = (mem: Memory) => {
    onUpdateMemory({
      ...mem,
      isActive: !mem.isActive
    });
  };

  const handleAdd = (e: React.FormEvent) => {
    e.preventDefault();
    if (!keyInput.trim() || !contentInput.trim()) return;
    onAddMemory(keyInput.trim(), contentInput.trim(), categoryInput);
    setKeyInput("");
    setContentInput("");
    setShowAddModal(false);
  };

  return (
    <div className="flex-1 p-3 sm:p-5 max-w-4xl mx-auto w-full space-y-4 overflow-y-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[#007C91]/30 pb-3">
        <div>
          <h1 className="text-xl font-bold font-['Chakra_Petch',sans-serif] text-[#E5FCFF] tracking-wider flex items-center gap-2">
            <Brain className="w-5 h-5 text-[#00E5FF]" /> MEMORY VAULT // COFFRE MÉMORIEL
          </h1>
          <p className="text-xs font-mono text-[#6F9DA6]">
            Stockage neuronal à long terme injecté dans le contexte du Core IA
          </p>
        </div>

        <div className="flex items-center space-x-2">
          <button
            onClick={() => setShowWipeConfirm(true)}
            className="px-3 py-1.5 rounded bg-[#FF4660]/10 hover:bg-[#FF4660]/20 border border-[#FF4660]/40 text-[#FF4660] text-xs font-mono flex items-center gap-1 cursor-pointer"
          >
            <Trash2 className="w-3.5 h-3.5" /> PURGER
          </button>
          <button
            onClick={() => setShowAddModal(true)}
            className="px-3.5 py-1.5 rounded bg-[#00E5FF] hover:bg-[#78F7FF] text-[#030609] text-xs font-mono font-bold flex items-center gap-1.5 transition-all cursor-pointer shadow-[0_0_10px_rgba(0,229,255,0.4)]"
          >
            <Plus className="w-4 h-4" /> NOUVEAU SOUVENIR
          </button>
        </div>
      </div>

      {/* Global Memory Engine Toggle Switch */}
      <div className="p-3.5 bg-[#070D12] border border-[#007C91]/40 rounded flex items-center justify-between hud-panel-corner">
        <div>
          <div className="text-sm font-bold text-[#E5FCFF] flex items-center gap-2">
            <span>Moteur d'injection mémorielle</span>
            <span className={`text-[10px] font-mono px-2 py-0.5 rounded ${
              settings.memoryEnabled ? 'bg-[#31F5A3]/15 text-[#31F5A3] border border-[#31F5A3]/40' : 'bg-[#FF4660]/15 text-[#FF4660] border border-[#FF4660]/40'
            }`}>
              {settings.memoryEnabled ? "ACTIF" : "DÉSACTIVÉ"}
            </span>
          </div>
          <p className="text-xs text-[#6F9DA6] mt-0.5">
            Injecte automatiquement les faits enregistrés dans le prompt système du Core IA.
          </p>
        </div>
        <button
          onClick={handleToggleMemoryEngine}
          className="p-1 text-[#00E5FF] cursor-pointer"
        >
          {settings.memoryEnabled ? (
            <ToggleRight className="w-8 h-8 text-[#00E5FF]" />
          ) : (
            <ToggleLeft className="w-8 h-8 text-[#6F9DA6]" />
          )}
        </button>
      </div>

      {/* Category Pills */}
      <div className="flex items-center space-x-1.5 bg-[#070D12] p-1.5 rounded border border-[#007C91]/30 overflow-x-auto no-scrollbar">
        {categories.map((cat) => (
          <button
            key={cat}
            onClick={() => setSelectedCat(cat)}
            className={`px-3 py-1 rounded text-xs font-mono transition-all shrink-0 cursor-pointer ${
              selectedCat === cat
                ? 'bg-[#00E5FF] text-[#030609] font-bold shadow-[0_0_8px_rgba(0,229,255,0.3)]'
                : 'text-[#6F9DA6] hover:text-[#E5FCFF]'
            }`}
          >
            {cat}
          </button>
        ))}
      </div>

      {/* Memories List */}
      <HudPanel title="FAITS ENREGISTRÉS" badge={`${activeCount} / ${memories.length} ACTIFS`}>
        <div className="space-y-2.5 max-h-[55vh] overflow-y-auto">
          {filteredMemories.length === 0 ? (
            <div className="text-center py-8 text-xs font-mono text-[#6F9DA6]">
              Aucun souvenir enregistré dans cette catégorie.
            </div>
          ) : (
            filteredMemories.map((mem) => (
              <div
                key={mem.id}
                className={`p-3 rounded border transition-all flex items-start justify-between gap-3 ${
                  mem.isActive
                    ? 'bg-[#070D12] border-[#007C91]/50 hover:border-[#00E5FF]'
                    : 'bg-[#070D12]/40 border-[#007C91]/20 opacity-60'
                }`}
              >
                <div className="flex-1 min-w-0 space-y-1">
                  <div className="flex items-center space-x-2">
                    <span className="text-xs font-mono font-bold text-[#78F7FF]">
                      {mem.key}
                    </span>
                    <span className="text-[10px] font-mono px-1.5 py-0.2 rounded bg-[#0A1219] border border-[#007C91]/30 text-[#00E5FF]">
                      {mem.category}
                    </span>
                  </div>
                  <p className="text-xs text-[#E5FCFF] font-['Rajdhani',sans-serif] leading-relaxed">
                    {mem.content}
                  </p>
                </div>

                <div className="flex items-center space-x-2 shrink-0">
                  <button
                    onClick={() => handleToggleMemoryItem(mem)}
                    className="cursor-pointer"
                    title={mem.isActive ? "Désactiver ce souvenir" : "Activer ce souvenir"}
                  >
                    {mem.isActive ? (
                      <ToggleRight className="w-6 h-6 text-[#31F5A3]" />
                    ) : (
                      <ToggleLeft className="w-6 h-6 text-[#6F9DA6]" />
                    )}
                  </button>
                  <button
                    onClick={() => onDeleteMemory(mem.id)}
                    className="p-1 text-[#6F9DA6] hover:text-[#FF4660] transition-colors cursor-pointer"
                    title="Supprimer ce souvenir"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </HudPanel>

      {/* Add Memory Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4">
          <div className="relative w-full max-w-md bg-[#070D12] border border-[#00E5FF]/50 rounded-sm p-5 hud-panel-corner shadow-[0_0_25px_rgba(0,229,255,0.2)]">
            <button
              onClick={() => setShowAddModal(false)}
              className="absolute top-3 right-3 text-[#6F9DA6] hover:text-[#00E5FF] cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>

            <h3 className="text-base font-bold font-['Chakra_Petch',sans-serif] text-[#E5FCFF] mb-3">
              ENREGISTRER UN FAIT MÉMORIEL
            </h3>

            <form onSubmit={handleAdd} className="space-y-3">
              <div>
                <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">CLÉ DU SOUVENIR</label>
                <input
                  type="text"
                  value={keyInput}
                  onChange={(e) => setKeyInput(e.target.value)}
                  placeholder="Ex: Titre du projet, Langue préférée..."
                  className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-3 py-2 text-sm text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
                  autoFocus
                />
              </div>

              <div>
                <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">CATÉGORIE</label>
                <select
                  value={categoryInput}
                  onChange={(e) => setCategoryInput(e.target.value as any)}
                  className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-2.5 py-2 text-xs font-mono text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
                >
                  <option value="General">General</option>
                  <option value="Preferences">Preferences</option>
                  <option value="Work">Work</option>
                  <option value="Identity">Identity</option>
                  <option value="System">System</option>
                </select>
              </div>

              <div>
                <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">CONTENU À RETENIR</label>
                <textarea
                  rows={4}
                  value={contentInput}
                  onChange={(e) => setContentInput(e.target.value)}
                  placeholder="Ex: L'utilisateur travaille actuellement sur l'armure Mark-85..."
                  className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-3 py-2 text-xs text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF] font-mono"
                />
              </div>

              <button
                type="submit"
                className="w-full py-2.5 bg-[#00E5FF] hover:bg-[#78F7FF] text-[#030609] font-mono font-bold text-xs rounded transition-all cursor-pointer mt-2"
              >
                ENREGISTRER DANS LE COFFRE
              </button>
            </form>
          </div>
        </div>
      )}

      {/* Wipe Confirmation Modal */}
      {showWipeConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4">
          <div className="relative w-full max-w-sm bg-[#070D12] border border-[#FF4660]/60 rounded-sm p-5 hud-panel-corner shadow-[0_0_25px_rgba(255,70,96,0.3)] text-center space-y-3">
            <ShieldAlert className="w-10 h-10 text-[#FF4660] mx-auto" />
            <h3 className="text-sm font-bold font-['Chakra_Petch',sans-serif] text-[#FF8095]">
              CONFIRMER LA PURGE MÉMORIELLE
            </h3>
            <p className="text-xs text-[#6F9DA6]">
              Cette action supprimera définitivement l'ensemble des faits neuronaux enregistrés. Êtes-vous certain ?
            </p>
            <div className="flex space-x-2 pt-2">
              <button
                onClick={() => setShowWipeConfirm(false)}
                className="flex-1 py-2 bg-[#0A1219] hover:bg-[#007C91]/30 border border-[#007C91]/40 rounded text-xs font-mono text-[#E5FCFF] cursor-pointer"
              >
                ANNULER
              </button>
              <button
                onClick={() => {
                  onWipeAllMemories();
                  setShowWipeConfirm(false);
                }}
                className="flex-1 py-2 bg-[#FF4660] hover:bg-[#FF6680] text-white rounded text-xs font-mono font-bold cursor-pointer"
              >
                PURGER TOUT
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
