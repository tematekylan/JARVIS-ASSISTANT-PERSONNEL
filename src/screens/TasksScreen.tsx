import React, { useState } from 'react';
import { CheckSquare, Square, Plus, Trash2, Tag, Clock, X } from 'lucide-react';
import { TaskItem } from '../types';
import { HudPanel } from '../components/HudPanel';

interface TasksScreenProps {
  tasks: TaskItem[];
  onToggleTask: (id: string) => void;
  onAddTask: (title: string, category: string, time: string) => void;
  onDeleteTask: (id: string) => void;
}

export const TasksScreen: React.FC<TasksScreenProps> = ({
  tasks,
  onToggleTask,
  onAddTask,
  onDeleteTask
}) => {
  const [filterCategory, setFilterCategory] = useState<string>("Tous");
  const [showModal, setShowModal] = useState(false);
  const [newTitle, setNewTitle] = useState("");
  const [newCategory, setNewCategory] = useState("Système");
  const [newTime, setNewTime] = useState("12:00");

  const categories = ["Tous", "Système", "Analyse", "Rappel"];

  const filteredTasks = tasks.filter(t => 
    filterCategory === "Tous" ? true : t.category === filterCategory
  );

  const activeCount = tasks.filter(t => !t.isCompleted).length;
  const completedCount = tasks.filter(t => t.isCompleted).length;

  const handleCreate = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTitle.trim()) return;
    onAddTask(newTitle.trim(), newCategory, newTime);
    setNewTitle("");
    setShowModal(false);
  };

  return (
    <div className="flex-1 p-3 sm:p-5 max-w-4xl mx-auto w-full space-y-4 overflow-y-auto">
      {/* Header & Stats */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[#007C91]/30 pb-3">
        <div>
          <h1 className="text-xl font-bold font-['Chakra_Petch',sans-serif] text-[#E5FCFF] tracking-wider flex items-center gap-2">
            <CheckSquare className="w-5 h-5 text-[#00E5FF]" /> TASK CENTER // GESTION DES DIRECTIVES
          </h1>
          <p className="text-xs font-mono text-[#6F9DA6]">
            Planification tactique et suivi des opérations programmées
          </p>
        </div>

        <button
          onClick={() => setShowModal(true)}
          className="px-3.5 py-1.5 rounded bg-[#00E5FF] hover:bg-[#78F7FF] text-[#030609] text-xs font-mono font-bold flex items-center gap-1.5 transition-all cursor-pointer shadow-[0_0_10px_rgba(0,229,255,0.4)]"
        >
          <Plus className="w-4 h-4" /> NOUVELLE TÂCHE
        </button>
      </div>

      {/* Counters & Filter Chips */}
      <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
        <div className="p-2.5 bg-[#070D12] border border-[#007C91]/40 rounded flex flex-col">
          <span className="text-[10px] font-mono text-[#6F9DA6]">TÂCHES TOTALES</span>
          <span className="text-lg font-bold font-mono text-[#E5FCFF]">{tasks.length}</span>
        </div>
        <div className="p-2.5 bg-[#070D12] border border-[#007C91]/40 rounded flex flex-col">
          <span className="text-[10px] font-mono text-[#6F9DA6]">EN ATTENTE</span>
          <span className="text-lg font-bold font-mono text-[#00E5FF]">{activeCount}</span>
        </div>
        <div className="p-2.5 bg-[#070D12] border border-[#007C91]/40 rounded flex flex-col col-span-2 sm:col-span-1">
          <span className="text-[10px] font-mono text-[#6F9DA6]">COMPLÉTÉES</span>
          <span className="text-lg font-bold font-mono text-[#31F5A3]">{completedCount}</span>
        </div>
      </div>

      {/* Category Pills */}
      <div className="flex items-center space-x-1.5 bg-[#070D12] p-1.5 rounded border border-[#007C91]/30">
        <Tag className="w-3.5 h-3.5 text-[#00E5FF] ml-1 mr-1" />
        {categories.map((cat) => (
          <button
            key={cat}
            onClick={() => setFilterCategory(cat)}
            className={`px-3 py-1 rounded text-xs font-mono transition-all cursor-pointer ${
              filterCategory === cat
                ? 'bg-[#00E5FF] text-[#030609] font-bold shadow-[0_0_8px_rgba(0,229,255,0.3)]'
                : 'text-[#6F9DA6] hover:text-[#E5FCFF]'
            }`}
          >
            {cat}
          </button>
        ))}
      </div>

      {/* Tasks List */}
      <HudPanel title="DIRECTIVES PLANIFIÉES" badge={`${filteredTasks.length} AFFICHÉES`}>
        <div className="space-y-2 max-h-[60vh] overflow-y-auto">
          {filteredTasks.length === 0 ? (
            <div className="text-center py-8 text-xs font-mono text-[#6F9DA6]">
              Aucune directive pour cette catégorie.
            </div>
          ) : (
            filteredTasks.map((t) => (
              <div
                key={t.id}
                className={`p-3 rounded border transition-all flex items-center justify-between gap-3 ${
                  t.isCompleted
                    ? 'bg-[#070D12]/60 border-[#007C91]/20 opacity-70'
                    : 'bg-[#070D12] border-[#007C91]/50 hover:border-[#00E5FF]'
                }`}
              >
                <div className="flex items-center space-x-3 flex-1 min-w-0">
                  <button
                    onClick={() => onToggleTask(t.id)}
                    className={`shrink-0 p-0.5 rounded cursor-pointer ${
                      t.isCompleted ? 'text-[#31F5A3]' : 'text-[#6F9DA6] hover:text-[#00E5FF]'
                    }`}
                  >
                    {t.isCompleted ? (
                      <CheckSquare className="w-5 h-5 fill-[#31F5A3]/20" />
                    ) : (
                      <Square className="w-5 h-5" />
                    )}
                  </button>
                  <div className="min-w-0">
                    <p className={`text-sm ${t.isCompleted ? 'line-through text-[#6F9DA6]' : 'text-[#E5FCFF]'}`}>
                      {t.title}
                    </p>
                    <div className="flex items-center space-x-2 text-[11px] font-mono text-[#6F9DA6] mt-0.5">
                      <span className="flex items-center gap-1">
                        <Clock className="w-3 h-3 text-[#00E5FF]" /> {t.time}
                      </span>
                      <span>•</span>
                      <span className="px-1.5 py-0.2 rounded bg-[#0A1219] border border-[#007C91]/30 text-[#78F7FF]">
                        {t.category}
                      </span>
                    </div>
                  </div>
                </div>

                <button
                  onClick={() => onDeleteTask(t.id)}
                  className="p-1.5 text-[#6F9DA6] hover:text-[#FF4660] transition-colors cursor-pointer shrink-0"
                  title="Supprimer la tâche"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            ))
          )}
        </div>
      </HudPanel>

      {/* Creation Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4">
          <div className="relative w-full max-w-md bg-[#070D12] border border-[#00E5FF]/50 rounded-sm p-5 hud-panel-corner shadow-[0_0_25px_rgba(0,229,255,0.2)]">
            <button
              onClick={() => setShowModal(false)}
              className="absolute top-3 right-3 text-[#6F9DA6] hover:text-[#00E5FF] cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>

            <h3 className="text-base font-bold font-['Chakra_Petch',sans-serif] text-[#E5FCFF] mb-3">
              AJOUTER UNE DIRECTIVE TACTIQUE
            </h3>

            <form onSubmit={handleCreate} className="space-y-3">
              <div>
                <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">INTITULÉ DE LA TÂCHE</label>
                <input
                  type="text"
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  placeholder="Ex: Calibrer les repulseurs..."
                  className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-3 py-2 text-sm text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
                  autoFocus
                />
              </div>

              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">CATÉGORIE</label>
                  <select
                    value={newCategory}
                    onChange={(e) => setNewCategory(e.target.value)}
                    className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-2.5 py-2 text-xs font-mono text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
                  >
                    <option value="Système">Système</option>
                    <option value="Analyse">Analyse</option>
                    <option value="Rappel">Rappel</option>
                  </select>
                </div>

                <div>
                  <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">HEURE ESTIMÉE</label>
                  <input
                    type="time"
                    value={newTime}
                    onChange={(e) => setNewTime(e.target.value)}
                    className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-2.5 py-2 text-xs font-mono text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
                  />
                </div>
              </div>

              <button
                type="submit"
                className="w-full py-2.5 bg-[#00E5FF] hover:bg-[#78F7FF] text-[#030609] font-mono font-bold text-xs rounded transition-all cursor-pointer mt-2"
              >
                ENREGISTRER LA TÂCHE
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
