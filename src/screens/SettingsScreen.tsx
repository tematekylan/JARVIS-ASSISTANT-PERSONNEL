import React, { useState } from 'react';
import { 
  Settings, 
  Volume2, 
  Brain, 
  Key, 
  Mail, 
  User, 
  Save, 
  Play, 
  ShieldCheck, 
  Check, 
  AlertCircle 
} from 'lucide-react';
import { UserSettings } from '../types';
import { HudPanel } from '../components/HudPanel';
import { speakText } from '../utils/audio';

interface SettingsScreenProps {
  settings: UserSettings;
  onSaveSettings: (settings: UserSettings) => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  settings,
  onSaveSettings
}) => {
  const [formData, setFormData] = useState<UserSettings>({ ...settings });
  const [showSavedNotification, setShowSavedNotification] = useState(false);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    onSaveSettings(formData);
    setShowSavedNotification(true);
    setTimeout(() => setShowSavedNotification(false), 2500);
  };

  const handleTestVoice = () => {
    speakText(
      `Bonjour ${formData.userName}. Test du synthétiseur vocal T-HACK AI. Vitesse : ${formData.speechRate}, tonalité : ${formData.speechPitch}.`,
      {
        rate: formData.speechRate,
        pitch: formData.speechPitch,
        language: formData.voiceLanguage
      }
    );
  };

  return (
    <div className="flex-1 p-3 sm:p-5 max-w-4xl mx-auto w-full space-y-4 overflow-y-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[#007C91]/30 pb-3">
        <div>
          <h1 className="text-xl font-bold font-['Chakra_Petch',sans-serif] text-[#E5FCFF] tracking-wider flex items-center gap-2">
            <Settings className="w-5 h-5 text-[#00E5FF]" /> CONFIGURATION // PROTOCOLE T-HACK
          </h1>
          <p className="text-xs font-mono text-[#6F9DA6]">
            Personnalisation du Core, des modèles de langage et du synthétiseur vocal
          </p>
        </div>

        <div className="flex items-center space-x-2">
          {showSavedNotification && (
            <span className="text-xs font-mono text-[#31F5A3] flex items-center gap-1 animate-pulse">
              <Check className="w-3.5 h-3.5" /> PARAMÈTRES ENREGISTRÉS
            </span>
          )}
          <button
            onClick={handleSave}
            className="px-4 py-1.5 rounded bg-[#00E5FF] hover:bg-[#78F7FF] text-[#030609] text-xs font-mono font-bold flex items-center gap-1.5 transition-all cursor-pointer shadow-[0_0_10px_rgba(0,229,255,0.4)]"
          >
            <Save className="w-4 h-4" /> ENREGISTRER
          </button>
        </div>
      </div>

      <form onSubmit={handleSave} className="space-y-4">
        {/* Section 1: Profil & Identité */}
        <HudPanel title="PROFIL DU COMMANDANT" badge={formData.securityClearanceLevel}>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-1">
            <div>
              <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">NOM OU TITRE</label>
              <input
                type="text"
                value={formData.userName}
                onChange={(e) => setFormData({ ...formData, userName: e.target.value })}
                className="w-full bg-[#070D12] border border-[#007C91]/40 rounded px-3 py-2 text-xs font-mono text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
              />
            </div>
            <div>
              <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">NOM DE L'ASSISTANT</label>
              <input
                type="text"
                value={formData.assistantName}
                onChange={(e) => setFormData({ ...formData, assistantName: e.target.value })}
                className="w-full bg-[#070D12] border border-[#007C91]/40 rounded px-3 py-2 text-xs font-mono text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
              />
            </div>
          </div>
        </HudPanel>

        {/* Section 2: Moteur Vocal & Synthèse */}
        <HudPanel title="MOTEUR VOCAL (TTS & RECONNAISSANCE)" badge="WEB AUDIO">
          <div className="space-y-3 pt-1">
            <div className="flex items-center justify-between">
              <div>
                <span className="text-xs font-mono text-[#E5FCFF] font-bold">Lecture Vocale Automatique</span>
                <p className="text-[11px] text-[#6F9DA6]">Énoncer automatiquement chaque réponse du Core à voix haute.</p>
              </div>
              <input
                type="checkbox"
                checked={formData.autoSpeakResponses}
                onChange={(e) => setFormData({ ...formData, autoSpeakResponses: e.target.checked })}
                className="w-4 h-4 accent-[#00E5FF] cursor-pointer"
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2 border-t border-[#007C91]/20">
              <div>
                <div className="flex justify-between text-xs font-mono mb-1">
                  <span className="text-[#6F9DA6]">Vitesse d'élocution</span>
                  <span className="text-[#00E5FF] font-bold">{formData.speechRate}x</span>
                </div>
                <input
                  type="range"
                  min="0.5"
                  max="2.0"
                  step="0.1"
                  value={formData.speechRate}
                  onChange={(e) => setFormData({ ...formData, speechRate: parseFloat(e.target.value) })}
                  className="w-full accent-[#00E5FF] cursor-pointer"
                />
              </div>

              <div>
                <div className="flex justify-between text-xs font-mono mb-1">
                  <span className="text-[#6F9DA6]">Tonalité (Pitch)</span>
                  <span className="text-[#00E5FF] font-bold">{formData.speechPitch}x</span>
                </div>
                <input
                  type="range"
                  min="0.5"
                  max="2.0"
                  step="0.1"
                  value={formData.speechPitch}
                  onChange={(e) => setFormData({ ...formData, speechPitch: parseFloat(e.target.value) })}
                  className="w-full accent-[#00E5FF] cursor-pointer"
                />
              </div>
            </div>

            <div className="flex items-center justify-between pt-2 border-t border-[#007C91]/20">
              <div className="flex items-center space-x-2">
                <span className="text-xs font-mono text-[#6F9DA6]">Langue :</span>
                <select
                  value={formData.voiceLanguage}
                  onChange={(e) => setFormData({ ...formData, voiceLanguage: e.target.value })}
                  className="bg-[#070D12] border border-[#007C91]/40 rounded px-2.5 py-1 text-xs font-mono text-[#E5FCFF]"
                >
                  <option value="fr">Français (France)</option>
                  <option value="en">English (US)</option>
                </select>
              </div>

              <button
                type="button"
                onClick={handleTestVoice}
                className="px-3 py-1 bg-[#070D12] hover:bg-[#007C91]/30 border border-[#007C91]/50 text-[#78F7FF] text-xs font-mono rounded flex items-center gap-1.5 cursor-pointer"
              >
                <Play className="w-3 h-3 fill-current" /> Tester la voix
              </button>
            </div>
          </div>
        </HudPanel>

        {/* Section 3: Modèles IA & Personnalité */}
        <HudPanel title="FOURNISSEURS D'IA & COMPORTEMENT" badge="MULTI-AI">
          <div className="space-y-3 pt-1">
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div>
                <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">FOURNISSEUR ACTIF</label>
                <select
                  value={formData.activeAiProvider}
                  onChange={(e) => setFormData({ ...formData, activeAiProvider: e.target.value as any })}
                  className="w-full bg-[#070D12] border border-[#007C91]/40 rounded px-3 py-2 text-xs font-mono text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
                >
                  <option value="gemini">Google Gemini (Officiel Recommandé)</option>
                  <option value="openai">OpenAI GPT</option>
                  <option value="claude">Anthropic Claude</option>
                  <option value="groq">Groq LPU Ultra-Fast</option>
                  <option value="deepseek">DeepSeek AI</option>
                </select>
              </div>

              <div>
                <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">MODÈLE SPÉCIFIQUE</label>
                <select
                  value={formData.aiModel}
                  onChange={(e) => setFormData({ ...formData, aiModel: e.target.value })}
                  className="w-full bg-[#070D12] border border-[#007C91]/40 rounded px-3 py-2 text-xs font-mono text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
                >
                  <option value="gemini-3.8-flash">gemini-3.8-flash (Recommandé)</option>
                  <option value="gemini-2.5-pro">gemini-2.5-pro (Haute Puissance)</option>
                  <option value="gpt-4o">gpt-4o</option>
                  <option value="claude-3-7-sonnet">claude-3-7-sonnet</option>
                  <option value="deepseek-r1">deepseek-r1</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">TON DE PERSONNALITÉ DU CORE</label>
              <select
                value={formData.personalityTone}
                onChange={(e) => setFormData({ ...formData, personalityTone: e.target.value })}
                className="w-full bg-[#070D12] border border-[#007C91]/40 rounded px-3 py-2 text-xs font-mono text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
              >
                <option value="Calm & Professional">Calme & Professionnel (Protocole J.A.R.V.I.S. classique)</option>
                <option value="Tony Stark Snarky">Sarcastique & Brillant (Humour piquant Tony Stark)</option>
                <option value="Tactical Military">Militaire & Tactique (Analyses concises et défensives)</option>
                <option value="Friendly Human">Humain & Chaleureux (Conversation fluide et naturelle)</option>
              </select>
            </div>

            {/* Mode Démo toggle */}
            <div className="flex items-center justify-between pt-2 border-t border-[#007C91]/20">
              <div>
                <span className="text-xs font-mono text-[#E5FCFF] font-bold">Mode Démonstration Hors-Ligne</span>
                <p className="text-[11px] text-[#6F9DA6]">
                  Utilise le moteur de simulation local intelligent sans requêtes externes.
                </p>
              </div>
              <input
                type="checkbox"
                checked={formData.isDemoMode}
                onChange={(e) => setFormData({ ...formData, isDemoMode: e.target.checked })}
                className="w-4 h-4 accent-[#00E5FF] cursor-pointer"
              />
            </div>
          </div>
        </HudPanel>

        {/* Section 4: Alertes & Sécurité Incidents */}
        <HudPanel title="SÉCURITÉ & ALERTES INCIDENTS" badge="CONSEIL D'IA">
          <div className="space-y-3 pt-1">
            <div>
              <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">EMAIL DE NOTIFICATION DÉVELOPPEUR</label>
              <input
                type="email"
                value={formData.developerAlertEmail}
                onChange={(e) => setFormData({ ...formData, developerAlertEmail: e.target.value })}
                placeholder="temateteddy@gmail.com"
                className="w-full bg-[#070D12] border border-[#007C91]/40 rounded px-3 py-2 text-xs font-mono text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
              />
              <span className="text-[10px] text-[#6F9DA6] mt-1 block">
                Destinataire automatique des rapports de délibération générés par le Collège d'IA lors d'anomalies.
              </span>
            </div>
          </div>
        </HudPanel>
      </form>
    </div>
  );
};
