import React, { useState } from 'react';
import { FileText, Plus, Search, Trash2, Edit3, X, Tag } from 'lucide-react';
import { Note } from '../types';
import { HudPanel } from '../components/HudPanel';

interface NotesScreenProps {
  notes: Note[];
  onAddNote: (title: string, content: string, tags: string) => void;
  onUpdateNote: (note: Note) => void;
  onDeleteNote: (id: string) => void;
}

export const NotesScreen: React.FC<NotesScreenProps> = ({
  notes,
  onAddNote,
  onUpdateNote,
  onDeleteNote
}) => {
  const [searchQuery, setSearchQuery] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [editingNote, setEditingNote] = useState<Note | null>(null);
  const [titleInput, setTitleInput] = useState("");
  const [contentInput, setContentInput] = useState("");
  const [tagsInput, setTagsInput] = useState("");

  const filteredNotes = notes.filter(n =>
    searchQuery.trim() === ""
      ? true
      : n.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        n.content.toLowerCase().includes(searchQuery.toLowerCase()) ||
        n.tags.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const openAdd = () => {
    setEditingNote(null);
    setTitleInput("");
    setContentInput("");
    setTagsInput("stark,tactique");
    setShowModal(true);
  };

  const openEdit = (note: Note) => {
    setEditingNote(note);
    setTitleInput(note.title);
    setContentInput(note.content);
    setTagsInput(note.tags);
    setShowModal(true);
  };

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    if (!titleInput.trim()) return;

    if (editingNote) {
      onUpdateNote({
        ...editingNote,
        title: titleInput.trim(),
        content: contentInput.trim(),
        tags: tagsInput.trim(),
        updatedAt: Date.now()
      });
    } else {
      onAddNote(titleInput.trim(), contentInput.trim(), tagsInput.trim() || "general");
    }
    setShowModal(false);
  };

  return (
    <div className="flex-1 p-3 sm:p-5 max-w-4xl mx-auto w-full space-y-4 overflow-y-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[#007C91]/30 pb-3">
        <div>
          <h1 className="text-xl font-bold font-['Chakra_Petch',sans-serif] text-[#E5FCFF] tracking-wider flex items-center gap-2">
            <FileText className="w-5 h-5 text-[#00E5FF]" /> BLOC-NOTES // DIRECTIVES ENREGISTRÉES
          </h1>
          <p className="text-xs font-mono text-[#6F9DA6]">
            Base de notes et de fiches techniques Stark Industries
          </p>
        </div>

        <button
          onClick={openAdd}
          className="px-3.5 py-1.5 rounded bg-[#00E5FF] hover:bg-[#78F7FF] text-[#030609] text-xs font-mono font-bold flex items-center gap-1.5 transition-all cursor-pointer shadow-[0_0_10px_rgba(0,229,255,0.4)]"
        >
          <Plus className="w-4 h-4" /> NOUVELLE NOTE
        </button>
      </div>

      {/* Search Input */}
      <div className="relative flex items-center bg-[#070D12] border border-[#007C91]/40 rounded px-3 py-2">
        <Search className="w-4 h-4 text-[#00E5FF] mr-2" />
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Rechercher une note par titre, composant ou tag..."
          className="w-full bg-transparent text-sm text-[#E5FCFF] placeholder-[#6F9DA6]/60 focus:outline-none font-mono"
        />
        {searchQuery && (
          <button onClick={() => setSearchQuery("")} className="text-[#6F9DA6] hover:text-[#E5FCFF]">
            <X className="w-4 h-4" />
          </button>
        )}
      </div>

      {/* Notes Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
        {filteredNotes.length === 0 ? (
          <div className="col-span-2 text-center py-10 text-xs font-mono text-[#6F9DA6]">
            Aucune note ne correspond aux critères.
          </div>
        ) : (
          filteredNotes.map((note) => (
            <div
              key={note.id}
              className="bg-[#070D12] border border-[#007C91]/40 hover:border-[#00E5FF]/80 p-3.5 rounded-sm transition-all hud-panel-corner flex flex-col justify-between space-y-3"
            >
              <div>
                <div className="flex items-start justify-between gap-2">
                  <h3 className="text-sm font-bold text-[#E5FCFF] font-['Chakra_Petch',sans-serif] tracking-wide">
                    {note.title}
                  </h3>
                  <div className="flex items-center space-x-1 shrink-0">
                    <button
                      onClick={() => openEdit(note)}
                      className="p-1 text-[#6F9DA6] hover:text-[#00E5FF] transition-colors cursor-pointer"
                      title="Modifier la note"
                    >
                      <Edit3 className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => onDeleteNote(note.id)}
                      className="p-1 text-[#6F9DA6] hover:text-[#FF4660] transition-colors cursor-pointer"
                      title="Supprimer la note"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>

                <p className="text-xs text-[#6F9DA6] mt-1.5 whitespace-pre-wrap leading-relaxed line-clamp-4">
                  {note.content}
                </p>
              </div>

              <div className="flex items-center justify-between pt-2 border-t border-[#007C91]/20 text-[10px] font-mono text-[#6F9DA6]">
                <div className="flex items-center gap-1 flex-wrap">
                  {note.tags.split(',').map((tag, idx) => (
                    <span key={idx} className="px-1.5 py-0.2 rounded bg-[#0A1219] border border-[#007C91]/30 text-[#78F7FF]">
                      #{tag.trim()}
                    </span>
                  ))}
                </div>
                <span>{new Date(note.updatedAt).toLocaleDateString('fr-FR')}</span>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Note Creation / Edit Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4">
          <div className="relative w-full max-w-lg bg-[#070D12] border border-[#00E5FF]/50 rounded-sm p-5 hud-panel-corner shadow-[0_0_25px_rgba(0,229,255,0.2)]">
            <button
              onClick={() => setShowModal(false)}
              className="absolute top-3 right-3 text-[#6F9DA6] hover:text-[#00E5FF] cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>

            <h3 className="text-base font-bold font-['Chakra_Petch',sans-serif] text-[#E5FCFF] mb-3">
              {editingNote ? "MODIFIER LA FICHE TACTIQUE" : "NOUVELLE NOTE TACTIQUE"}
            </h3>

            <form onSubmit={handleSave} className="space-y-3">
              <div>
                <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">TITRE DE LA NOTE</label>
                <input
                  type="text"
                  value={titleInput}
                  onChange={(e) => setTitleInput(e.target.value)}
                  placeholder="Ex: Plan d'action nanotechnologique..."
                  className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-3 py-2 text-sm text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
                  autoFocus
                />
              </div>

              <div>
                <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">CONTENU DÉTAILLÉ</label>
                <textarea
                  rows={5}
                  value={contentInput}
                  onChange={(e) => setContentInput(e.target.value)}
                  placeholder="Saisissez vos observations techniques..."
                  className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-3 py-2 text-xs text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF] font-mono leading-relaxed"
                />
              </div>

              <div>
                <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">TAGS (SÉPARÉS PAR DES VIRGULES)</label>
                <input
                  type="text"
                  value={tagsInput}
                  onChange={(e) => setTagsInput(e.target.value)}
                  placeholder="ex: stark, securite, ia"
                  className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-3 py-1.5 text-xs text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF] font-mono"
                />
              </div>

              <button
                type="submit"
                className="w-full py-2.5 bg-[#00E5FF] hover:bg-[#78F7FF] text-[#030609] font-mono font-bold text-xs rounded transition-all cursor-pointer mt-2"
              >
                {editingNote ? "METTRE À JOUR LA FICHE" : "ENREGISTRER LA FICHE"}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
