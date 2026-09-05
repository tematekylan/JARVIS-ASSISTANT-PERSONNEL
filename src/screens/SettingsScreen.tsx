import React, { useState } from 'react';
import { 
  Settings, 
  Volume2, 
  Brain, 
  Mail, 
  User, 
  Save, 
  Play, 
  ShieldCheck, 
  Check, 
  Home,
  Smartphone,
  Sliders,
  Bell,
  CheckSquare,
  Terminal as TermIcon,
  FileText,
  Cpu,
  Activity,
  Eye,
  Flashlight,
  Download,
  ArrowLeft,
  MessageSquare,
  ChevronRight,
  RefreshCw,
  Sparkles,
  Lock,
  Layers,
  Zap,
  Mic
} from 'lucide-react';
import { UserSettings, JarvisScreen } from '../types';
import { speakText } from '../utils/audio';
import { usePWAInstall } from '../utils/usePWAInstall';
import { toggleFlashlight } from '../utils/phoneControl';

interface SettingsScreenProps {
  settings: UserSettings;
  onSaveSettings: (settings: UserSettings) => void;
  onNavigate?: (screen: JarvisScreen) => void;
  onBackToChat?: () => void;
}

type SettingsTab = 'modules' | 'ai' | 'voice' | 'hardware' | 'profile' | 'updates';

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  settings,
  onSaveSettings,
  onNavigate,
  onBackToChat
}) => {
  const [activeTab, setActiveTab] = useState<SettingsTab>('modules');
  const [formData, setFormData] = useState<UserSettings>({ ...settings });
  const [showSavedNotification, setShowSavedNotification] = useState(false);
  const [torchStatus, setTorchStatus] = useState<string | null>(null);
  const [isUpdating, setIsUpdating] = useState(false);
  const [updateSuccess, setUpdateSuccess] = useState(false);
  const { isInstallable, install, isInstalled } = usePWAInstall();

  const handleSave = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
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

  const handleToggleTorch = async () => {
    const res = await toggleFlashlight();
    setTorchStatus(res.message);
    setTimeout(() => setTorchStatus(null), 3000);
  };

  const handleApplyUpdate = () => {
    setIsUpdating(true);
    setTimeout(() => {
      setIsUpdating(false);
      setUpdateSuccess(true);
      speakText("Mise à jour système v2.5 installée avec succès. Rechargement du noyau.", {
        rate: formData.speechRate,
        pitch: formData.speechPitch,
        language: formData.voiceLanguage
      });
      setTimeout(() => {
        window.location.reload();
      }, 1500);
    }, 2000);
  };

  const systemModules = [
    {
      screen: 'HOME' as JarvisScreen,
      title: 'Accueil & Cœur Holographique',
      desc: 'Visualisation 3D/2D de l\'hologramme quantique, télémetrie et passerelles rapides.',
      icon: Home,
      badge: 'ACCUEIL'
    },
    {
      screen: 'CHAT' as JarvisScreen,
      title: 'Discussion avec T-HACK',
      desc: 'Interface de chat en direct, streaming de pensée et synthèse vocale.',
      icon: MessageSquare,
      badge: 'CHAT'
    },
    {
      screen: 'COMMAND_CENTER' as JarvisScreen,
      title: 'Command Center & Outils',
      desc: 'Calculatrice tactique, analyse d\'incidents et journaux d\'outils.',
      icon: Sliders,
      badge: 'CENTRE'
    },
    {
      screen: 'NOTIFICATIONS' as JarvisScreen,
      title: 'Centre de Notifications',
      desc: 'Journal des alertes de sécurité, délibérations et rappels.',
      icon: Bell,
      badge: 'ALERTES'
    },
    {
      screen: 'TASKS' as JarvisScreen,
      title: 'Gestionnaire de Tâches',
      desc: 'Gestion des priorités, deadlines et directives opérationnelles.',
      icon: CheckSquare,
      badge: 'TÂCHES'
    },
    {
      screen: 'TERMINAL' as JarvisScreen,
      title: 'Console Terminal TTY',
      desc: 'Ligne de commande interactive avec shell et commandes d\'analyse.',
      icon: TermIcon,
      badge: 'SHELL'
    },
    {
      screen: 'MEMORY' as JarvisScreen,
      title: 'Coffre-fort Mémoriel',
      desc: 'Mémoire persistante, faits enregistrés et préférences utilisateur.',
      icon: Brain,
      badge: 'MÉMOIRE'
    },
    {
      screen: 'NOTES' as JarvisScreen,
      title: 'Bloc-notes Sécurisé',
      desc: 'Directives cryptées, brouillons techniques et mémos.',
      icon: FileText,
      badge: 'NOTES'
    },
    {
      screen: 'SYSTEM' as JarvisScreen,
      title: 'Diagnostic Réacteur & Santé',
      desc: 'Statut du processeur, batterie, RAM, température et charge système.',
      icon: Cpu,
      badge: 'DIAGNOSTIC'
    },
    {
      screen: 'ACTIVITY' as JarvisScreen,
      title: 'Journal d\'Activité & Audit',
      desc: 'Historique des requêtes, outils exécutés et délibérations.',
      icon: Activity,
      badge: 'AUDIT'
    },
    {
      screen: 'HOLOGRAPHIC_AOD' as JarvisScreen,
      title: 'Écran de Veille Quantique (AOD)',
      desc: 'Mode ambiant plein écran réacteur arc basse consommation.',
      icon: Eye,
      badge: 'STANDALONE'
    }
  ];

  return (
    <div className="flex-1 flex flex-col h-full bg-[#030609] overflow-hidden">
      {/* Header Bar */}
      <div className="bg-[#070D12] border-b border-[#007C91]/30 px-4 py-3 flex items-center justify-between shrink-0">
        <div className="flex items-center space-x-3">
          <button
            onClick={() => onBackToChat ? onBackToChat() : onNavigate?.('HOME')}
            className="p-1.5 rounded bg-[#0A1219] hover:bg-[#007C91]/30 border border-[#007C91]/40 text-[#00E5FF] flex items-center gap-1.5 text-xs font-mono transition-all cursor-pointer"
            title="Quitter les paramètres"
          >
            <ArrowLeft className="w-4 h-4" />
            <span className="hidden sm:inline">SORTIR DES PARAMÈTRES</span>
          </button>
          <div className="flex items-center space-x-2">
            <Settings className="w-5 h-5 text-[#00E5FF]" />
            <h1 className="text-base font-bold font-['Chakra_Petch',sans-serif] text-[#E5FCFF] tracking-wider">
              PARAMÈTRES & CONFIGURATION
            </h1>
          </div>
        </div>

        <div className="flex items-center space-x-2">
          {showSavedNotification && (
            <span className="text-xs font-mono text-[#31F5A3] flex items-center gap-1 animate-pulse">
              <Check className="w-3.5 h-3.5" /> Enregistré !
            </span>
          )}
          <button
            onClick={() => handleSave()}
            className="px-3.5 py-1.5 rounded bg-[#00E5FF] hover:bg-[#78F7FF] text-[#030609] text-xs font-mono font-bold flex items-center gap-1.5 transition-all cursor-pointer shadow-[0_0_10px_rgba(0,229,255,0.4)]"
          >
            <Save className="w-3.5 h-3.5" /> ENREGISTRER
          </button>
        </div>
      </div>

      {/* Structured Category Tabs */}
      <div className="bg-[#0A1219] border-b border-[#007C91]/30 px-3 py-2 flex items-center gap-1.5 overflow-x-auto shrink-0 select-none">
        <button
          onClick={() => setActiveTab('modules')}
          className={`px-3 py-1.5 rounded text-xs font-mono font-bold flex items-center gap-1.5 shrink-0 transition-all cursor-pointer ${
            activeTab === 'modules'
              ? 'bg-[#00E5FF] text-[#030609] shadow-[0_0_8px_rgba(0,229,255,0.4)]'
              : 'text-[#6F9DA6] hover:text-[#E5FCFF] hover:bg-[#070D12]'
          }`}
        >
          <Layers className="w-3.5 h-3.5" />
          <span>SECTIONS & MODULES</span>
        </button>

        <button
          onClick={() => setActiveTab('ai')}
          className={`px-3 py-1.5 rounded text-xs font-mono font-bold flex items-center gap-1.5 shrink-0 transition-all cursor-pointer ${
            activeTab === 'ai'
              ? 'bg-[#00E5FF] text-[#030609] shadow-[0_0_8px_rgba(0,229,255,0.4)]'
              : 'text-[#6F9DA6] hover:text-[#E5FCFF] hover:bg-[#070D12]'
          }`}
        >
          <Brain className="w-3.5 h-3.5" />
          <span>INTELLIGENCE & PENSÉE</span>
        </button>

        <button
          onClick={() => setActiveTab('voice')}
          className={`px-3 py-1.5 rounded text-xs font-mono font-bold flex items-center gap-1.5 shrink-0 transition-all cursor-pointer ${
            activeTab === 'voice'
              ? 'bg-[#00E5FF] text-[#030609] shadow-[0_0_8px_rgba(0,229,255,0.4)]'
              : 'text-[#6F9DA6] hover:text-[#E5FCFF] hover:bg-[#070D12]'
          }`}
        >
          <Volume2 className="w-3.5 h-3.5" />
          <span>VOIX & AUDIO</span>
        </button>

        <button
          onClick={() => setActiveTab('hardware')}
          className={`px-3 py-1.5 rounded text-xs font-mono font-bold flex items-center gap-1.5 shrink-0 transition-all cursor-pointer ${
            activeTab === 'hardware'
              ? 'bg-[#00E5FF] text-[#030609] shadow-[0_0_8px_rgba(0,229,255,0.4)]'
              : 'text-[#6F9DA6] hover:text-[#E5FCFF] hover:bg-[#070D12]'
          }`}
        >
          <Smartphone className="w-3.5 h-3.5" />
          <span>TÉLÉPHONE & MATÉRIEL</span>
        </button>

        <button
          onClick={() => setActiveTab('profile')}
          className={`px-3 py-1.5 rounded text-xs font-mono font-bold flex items-center gap-1.5 shrink-0 transition-all cursor-pointer ${
            activeTab === 'profile'
              ? 'bg-[#00E5FF] text-[#030609] shadow-[0_0_8px_rgba(0,229,255,0.4)]'
              : 'text-[#6F9DA6] hover:text-[#E5FCFF] hover:bg-[#070D12]'
          }`}
        >
          <User className="w-3.5 h-3.5" />
          <span>COMPTE & ACCRÉDITATION</span>
        </button>

        <button
          onClick={() => setActiveTab('updates')}
          className={`px-3 py-1.5 rounded text-xs font-mono font-bold flex items-center gap-1.5 shrink-0 transition-all cursor-pointer ${
            activeTab === 'updates'
              ? 'bg-[#31F5A3] text-[#030609] shadow-[0_0_8px_rgba(49,245,163,0.4)]'
              : 'text-[#31F5A3] hover:bg-[#31F5A3]/15'
          }`}
        >
          <Zap className="w-3.5 h-3.5" />
          <span>MISES À JOUR & APK</span>
        </button>
      </div>

      {/* Tab Content Area */}
      <div className="flex-1 overflow-y-auto p-4 sm:p-6 max-w-4xl mx-auto w-full">

        {/* TAB 1: SECTIONS & MODULES */}
        {activeTab === 'modules' && (
          <div className="space-y-4">
            <div className="border-b border-[#007C91]/30 pb-2">
              <h2 className="text-sm font-bold font-mono text-[#E5FCFF] uppercase">
                Sections & Modules du Système
              </h2>
              <p className="text-xs text-[#6F9DA6] mt-0.5">
                Accédez et naviguez directement dans chaque section de l'application en un clic :
              </p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {systemModules.map((mod) => {
                const Icon = mod.icon;
                return (
                  <div
                    key={mod.screen}
                    className="p-3.5 rounded bg-[#070D12] border border-[#007C91]/40 hover:border-[#00E5FF] transition-all flex flex-col justify-between space-y-3 group"
                  >
                    <div className="flex items-start space-x-3">
                      <div className="w-9 h-9 rounded bg-[#0A1219] border border-[#00E5FF]/40 flex items-center justify-center text-[#00E5FF] shrink-0 group-hover:border-[#00E5FF] group-hover:shadow-[0_0_10px_rgba(0,229,255,0.3)] transition-all">
                        <Icon className="w-4 h-4" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <h4 className="text-xs font-bold font-mono text-[#E5FCFF] truncate">
                            {mod.title}
                          </h4>
                          <span className="text-[9px] font-mono px-1.5 py-0.5 rounded bg-[#007C91]/20 text-[#78F7FF] border border-[#007C91]/30">
                            {mod.badge}
                          </span>
                        </div>
                        <p className="text-[11px] text-[#6F9DA6] mt-1 leading-relaxed line-clamp-2">
                          {mod.desc}
                        </p>
                      </div>
                    </div>

                    <button
                      onClick={() => onNavigate?.(mod.screen)}
                      className="w-full py-1.5 px-3 rounded bg-[#0A1219] hover:bg-[#00E5FF] hover:text-[#030609] border border-[#007C91]/50 hover:border-[#00E5FF] text-[#00E5FF] text-xs font-mono font-bold flex items-center justify-between transition-all cursor-pointer"
                    >
                      <span>ENTRER DANS LA SECTION</span>
                      <ChevronRight className="w-3.5 h-3.5" />
                    </button>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* TAB 2: AI ENGINE & THOUGHT PROCESS */}
        {activeTab === 'ai' && (
          <div className="space-y-4">
            <div className="border-b border-[#007C91]/30 pb-2">
              <h2 className="text-sm font-bold font-mono text-[#E5FCFF] uppercase">
                Moteur d'Intelligence & Façon de Penser
              </h2>
              <p className="text-xs text-[#6F9DA6] mt-0.5">
                Personnalisez la formulation des réponses, le modèle sous-jacent et le comportement.
              </p>
            </div>

            <div className="bg-[#070D12] border border-[#007C91]/40 rounded-md p-4 space-y-4">
              <div>
                <label className="block text-xs font-mono text-[#6F9DA6] mb-1.5">
                  Fournisseur d'Intelligence Artificielle :
                </label>
                <select
                  value={formData.activeAiProvider}
                  onChange={(e) => setFormData({ ...formData, activeAiProvider: e.target.value as any })}
                  className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-3 py-2 text-xs font-mono text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
                >
                  <option value="gemini">Google Gemini 3.8 Flash (Recommandé - Ultra Réactif)</option>
                  <option value="openai">OpenAI GPT-4o Intelligence Core</option>
                  <option value="claude">Anthropic Claude 3.5 Sonnet</option>
                  <option value="groq">Groq LPU (Vitesse Extrême)</option>
                  <option value="deepseek">DeepSeek AI Core</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-mono text-[#6F9DA6] mb-1.5">
                  Nom de l'Intelligence Artificielle :
                </label>
                <input
                  type="text"
                  value={formData.assistantName}
                  onChange={(e) => setFormData({ ...formData, assistantName: e.target.value })}
                  className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-3 py-2 text-xs font-mono text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
                />
              </div>

              <div>
                <label className="block text-xs font-mono text-[#6F9DA6] mb-1.5">
                  Ton & Style de Pensée :
                </label>
                <select
                  value={formData.personalityTone}
                  onChange={(e) => setFormData({ ...formData, personalityTone: e.target.value })}
                  className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-3 py-2 text-xs font-mono text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
                >
                  <option value="Calme & Professionnel">Calme & Professionnel (Réponses directes et précises)</option>
                  <option value="Tactique & Direct">Tactique & Direct (Style J.A.R.V.I.S. concis)</option>
                  <option value="Analytique & Détaillé">Analytique & Détaillé (Explications complètes)</option>
                  <option value="Convivial & Naturel">Convivial & Naturel (Conversations fluides)</option>
                </select>
              </div>

              <div className="pt-2 border-t border-[#007C91]/30 flex items-center justify-between">
                <div>
                  <div className="text-xs font-mono text-[#E5FCFF] font-bold">Mémoire Permanente Active</div>
                  <div className="text-[11px] text-[#6F9DA6]">Retient les préférences, projets et directives d'une session à l'autre</div>
                </div>
                <input
                  type="checkbox"
                  checked={formData.memoryEnabled}
                  onChange={(e) => setFormData({ ...formData, memoryEnabled: e.target.checked })}
                  className="w-4 h-4 accent-[#00E5FF] cursor-pointer"
                />
              </div>
            </div>
          </div>
        )}

        {/* TAB 3: VOICE & AUDIO */}
        {activeTab === 'voice' && (
          <div className="space-y-4">
            <div className="border-b border-[#007C91]/30 pb-2">
              <h2 className="text-sm font-bold font-mono text-[#E5FCFF] uppercase">
                Moteur Vocal & Audio Synthétique
              </h2>
              <p className="text-xs text-[#6F9DA6] mt-0.5">
                Contrôlez la synthèse vocale, la vitesse d'élocution et le ton de voix.
              </p>
            </div>

            <div className="bg-[#070D12] border border-[#007C91]/40 rounded-md p-4 space-y-4">
              <div className="flex items-center justify-between">
                <div>
                  <div className="text-xs font-mono text-[#E5FCFF] font-bold">Lecture Vocale Automatique</div>
                  <div className="text-[11px] text-[#6F9DA6]">L'IA prononce ses réponses à voix haute dès qu'elles sont prêtes</div>
                </div>
                <input
                  type="checkbox"
                  checked={formData.autoSpeakResponses}
                  onChange={(e) => setFormData({ ...formData, autoSpeakResponses: e.target.checked })}
                  className="w-4 h-4 accent-[#00E5FF] cursor-pointer"
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
                <div>
                  <div className="flex justify-between text-xs font-mono text-[#6F9DA6] mb-1">
                    <span>Vitesse d'élocution :</span>
                    <span className="text-[#00E5FF] font-bold">{formData.speechRate}x</span>
                  </div>
                  <input
                    type="range"
                    min="0.7"
                    max="1.5"
                    step="0.05"
                    value={formData.speechRate}
                    onChange={(e) => setFormData({ ...formData, speechRate: parseFloat(e.target.value) })}
                    className="w-full accent-[#00E5FF] cursor-pointer"
                  />
                </div>

                <div>
                  <div className="flex justify-between text-xs font-mono text-[#6F9DA6] mb-1">
                    <span>Tonalité vocale :</span>
                    <span className="text-[#00E5FF] font-bold">{formData.speechPitch}x</span>
                  </div>
                  <input
                    type="range"
                    min="0.6"
                    max="1.4"
                    step="0.05"
                    value={formData.speechPitch}
                    onChange={(e) => setFormData({ ...formData, speechPitch: parseFloat(e.target.value) })}
                    className="w-full accent-[#00E5FF] cursor-pointer"
                  />
                </div>
              </div>

              <div className="pt-2">
                <button
                  onClick={handleTestVoice}
                  className="py-2 px-4 rounded bg-[#0A1219] hover:bg-[#007C91]/30 border border-[#00E5FF]/60 text-[#00E5FF] text-xs font-mono font-bold flex items-center gap-2 transition-all cursor-pointer shadow-sm"
                >
                  <Play className="w-3.5 h-3.5" />
                  <span>TESTER LA VOIX DU SYSTÈME</span>
                </button>
              </div>
            </div>
          </div>
        )}

        {/* TAB 4: PHONE & HARDWARE */}
        {activeTab === 'hardware' && (
          <div className="space-y-4">
            <div className="border-b border-[#007C91]/30 pb-2">
              <h2 className="text-sm font-bold font-mono text-[#E5FCFF] uppercase">
                Contrôle Téléphone & Matériel
              </h2>
              <p className="text-xs text-[#6F9DA6] mt-0.5">
                Interaction avec les capteurs, la lampe LED, WhatsApp et le maintien de l'écran.
              </p>
            </div>

            <div className="bg-[#070D12] border border-[#007C91]/40 rounded-md p-4 space-y-4">
              {/* Flashlight */}
              <div className="flex items-center justify-between">
                <div>
                  <div className="text-xs font-mono text-[#E5FCFF] font-bold flex items-center gap-1.5">
                    <Flashlight className="w-4 h-4 text-[#00E5FF]" />
                    <span>Lampe Torche LED Téléphone</span>
                  </div>
                  <div className="text-[11px] text-[#6F9DA6]">Allumer ou éteindre la torche intégrée du smartphone</div>
                </div>
                <button
                  onClick={handleToggleTorch}
                  className="px-3 py-1.5 rounded bg-[#0A1219] hover:bg-[#00E5FF]/20 border border-[#007C91]/50 text-[#00E5FF] text-xs font-mono font-bold transition-all cursor-pointer"
                >
                  BASCULER TORCHE
                </button>
              </div>
              {torchStatus && (
                <div className="text-xs font-mono text-[#31F5A3] bg-[#0A1A1E] p-2 rounded border border-[#31F5A3]/40">
                  {torchStatus}
                </div>
              )}

              {/* Screen Wake Lock */}
              <div className="flex items-center justify-between pt-2 border-t border-[#007C91]/30">
                <div>
                  <div className="text-xs font-mono text-[#E5FCFF] font-bold">Maintien de l'écran éveillé (Wake Lock)</div>
                  <div className="text-[11px] text-[#6F9DA6]">Empêche le téléphone de se mettre en veille pendant l'utilisation</div>
                </div>
                <input
                  type="checkbox"
                  checked={formData.screenWakeLockEnabled}
                  onChange={(e) => setFormData({ ...formData, screenWakeLockEnabled: e.target.checked })}
                  className="w-4 h-4 accent-[#00E5FF] cursor-pointer"
                />
              </div>

              {/* Wake Word Detection */}
              <div className="flex items-center justify-between pt-2 border-t border-[#007C91]/30">
                <div>
                  <div className="text-xs font-mono text-[#E5FCFF] font-bold">Réveil Vocal "HACK AI démarre"</div>
                  <div className="text-[11px] text-[#6F9DA6]">Active l'écoute automatique dès la prononciation du mot clé</div>
                </div>
                <input
                  type="checkbox"
                  checked={formData.wakeWordEnabled}
                  onChange={(e) => setFormData({ ...formData, wakeWordEnabled: e.target.checked })}
                  className="w-4 h-4 accent-[#00E5FF] cursor-pointer"
                />
              </div>

              {/* WhatsApp Default Contact */}
              <div className="pt-2 border-t border-[#007C91]/30">
                <label className="block text-xs font-mono text-[#6F9DA6] mb-1.5">
                  Numéro WhatsApp par défaut (avec indicatif international) :
                </label>
                <input
                  type="text"
                  value={formData.defaultWhatsappNumber || ""}
                  onChange={(e) => setFormData({ ...formData, defaultWhatsappNumber: e.target.value })}
                  placeholder="+33612345678"
                  className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-3 py-2 text-xs font-mono text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
                />
              </div>
            </div>
          </div>
        )}

        {/* TAB 5: PROFILE & ACCOUNT */}
        {activeTab === 'profile' && (
          <div className="space-y-4">
            <div className="border-b border-[#007C91]/30 pb-2">
              <h2 className="text-sm font-bold font-mono text-[#E5FCFF] uppercase">
                Profil Utilisateur & Photo de Profil
              </h2>
              <p className="text-xs text-[#6F9DA6] mt-0.5">
                Modifiez votre photo et vos informations afin que l'IA vous reconnaisse précisément.
              </p>
            </div>

            <div className="bg-[#070D12] border border-[#007C91]/40 rounded-md p-4 space-y-4">
              {/* Photo de profil section */}
              <div>
                <label className="block text-xs font-mono text-[#6F9DA6] mb-2">
                  Photo de profil de l'utilisateur :
                </label>
                <div className="flex items-center space-x-4">
                  <img 
                    src={formData.userAvatarUrl || "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80"} 
                    alt="Photo profil" 
                    className="w-16 h-16 rounded-full object-cover border-2 border-[#00E5FF] shadow-[0_0_12px_rgba(0,229,255,0.4)]"
                  />
                  <div className="space-y-2">
                    <label className="inline-flex items-center px-3 py-1.5 bg-[#0A1219] hover:bg-[#00E5FF]/20 border border-[#007C91]/60 text-[#00E5FF] text-xs font-mono rounded cursor-pointer transition-colors">
                      <span>Téléverser une nouvelle photo</span>
                      <input 
                        type="file" 
                        accept="image/*" 
                        onChange={(e) => {
                          const file = e.target.files?.[0];
                          if (file) {
                            const reader = new FileReader();
                            reader.onload = (ev) => {
                              if (ev.target?.result) {
                                setFormData({ ...formData, userAvatarUrl: ev.target.result as string });
                              }
                            };
                            reader.readAsDataURL(file);
                          }
                        }} 
                        className="hidden" 
                      />
                    </label>
                    <div className="flex items-center space-x-2">
                      <span className="text-[10px] text-[#6F9DA6] font-mono">Avatars rapides :</span>
                      {[
                        'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80',
                        'https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150&auto=format&fit=crop&q=80',
                        'https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150&auto=format&fit=crop&q=80'
                      ].map((url, i) => (
                        <img 
                          key={i} 
                          src={url} 
                          alt="Preset" 
                          onClick={() => setFormData({ ...formData, userAvatarUrl: url })}
                          className={`w-7 h-7 rounded-full cursor-pointer hover:scale-110 transition-transform ${formData.userAvatarUrl === url ? 'ring-2 ring-[#00E5FF]' : 'opacity-60'}`}
                        />
                      ))}
                    </div>
                  </div>
                </div>
              </div>

              <div>
                <label className="block text-xs font-mono text-[#6F9DA6] mb-1.5">
                  Nom d'utilisateur / Pseudo :
                </label>
                <input
                  type="text"
                  value={formData.userName}
                  onChange={(e) => setFormData({ ...formData, userName: e.target.value })}
                  className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-3 py-2 text-xs font-mono text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
                />
              </div>

              <div>
                <label className="block text-xs font-mono text-[#6F9DA6] mb-1.5">
                  Adresse e-mail Gmail enregistrée :
                </label>
                <input
                  type="email"
                  value={formData.userEmail}
                  onChange={(e) => setFormData({ ...formData, userEmail: e.target.value })}
                  className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-3 py-2 text-xs font-mono text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
                />
              </div>

              <div className="p-3 bg-[#0A1219] border border-[#31F5A3]/40 rounded flex items-center justify-between">
                <div>
                  <div className="text-xs font-mono text-[#31F5A3] font-bold">NIVEAU D'ACCRÉDITATION ACTUEL</div>
                  <div className="text-xs text-[#E5FCFF] font-semibold mt-0.5">{formData.securityClearanceLevel}</div>
                </div>
                <ShieldCheck className="w-6 h-6 text-[#31F5A3]" />
              </div>
            </div>
          </div>
        )}

        {/* TAB 6: UPDATES & APK INSTALLATION */}
        {activeTab === 'updates' && (
          <div className="space-y-4">
            <div className="border-b border-[#007C91]/30 pb-2">
              <h2 className="text-sm font-bold font-mono text-[#E5FCFF] uppercase">
                Mises à Jour du Système & Fichier APK Android
              </h2>
              <p className="text-xs text-[#6F9DA6] mt-0.5">
                Installez les dernières mises à jour du noyau et déployez l'application sur votre téléphone.
              </p>
            </div>

            {/* Mise à Jour Instantanée */}
            <div className="bg-[#070D12] border border-[#31F5A3]/50 rounded-md p-4 space-y-3 shadow-[0_0_15px_rgba(49,245,163,0.15)]">
              <div className="flex items-start justify-between gap-3">
                <div className="flex items-center space-x-3">
                  <div className="p-2.5 rounded bg-[#31F5A3]/20 text-[#31F5A3] border border-[#31F5A3]">
                    <Zap className="w-5 h-5" />
                  </div>
                  <div>
                    <h3 className="text-sm font-bold font-mono text-[#E5FCFF]">
                      NOUVELLE MISE À JOUR T-HACK v2.5 DISPONIBLE
                    </h3>
                    <p className="text-xs text-[#6F9DA6] mt-0.5">
                      Nouvelle intelligence autonome, rendu des sections et correctif du moteur vocal.
                    </p>
                  </div>
                </div>
                <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-[#31F5A3]/20 text-[#31F5A3] border border-[#31F5A3]">
                  STABLE
                </span>
              </div>

              {updateSuccess && (
                <div className="p-2.5 bg-[#31F5A3]/20 border border-[#31F5A3] rounded text-xs font-mono text-[#31F5A3] flex items-center gap-2">
                  <Check className="w-4 h-4" />
                  <span>Mise à jour v2.5 appliquée ! Rechargement en cours...</span>
                </div>
              )}

              <button
                onClick={handleApplyUpdate}
                disabled={isUpdating}
                className="w-full py-2.5 px-4 rounded bg-[#31F5A3] hover:bg-[#4EFAAF] text-[#030609] font-mono font-bold text-xs flex items-center justify-center gap-2 transition-all cursor-pointer shadow-[0_0_15px_rgba(49,245,163,0.4)] disabled:opacity-50"
              >
                {isUpdating ? (
                  <>
                    <RefreshCw className="w-4 h-4 animate-spin" />
                    <span>APPLICATION DES PATCHS DU NOYAU...</span>
                  </>
                ) : (
                  <>
                    <RefreshCw className="w-4 h-4" />
                    <span>METTRE À JOUR MAINTENANT EN 1 CLIC</span>
                  </>
                )}
              </button>
            </div>

            {/* APK & PWA Téléphone Mobile */}
            <div className="bg-[#070D12] border border-[#007C91]/40 rounded-md p-4 space-y-3">
              <div className="flex items-center space-x-3">
                <div className="p-2.5 rounded bg-[#00E5FF]/15 text-[#00E5FF] border border-[#00E5FF]/40">
                  <Download className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-sm font-bold font-mono text-[#E5FCFF]">
                    INSTALLATION DE L'APPLICATION SUR LE TÉLÉPHONE (APK / PWA)
                  </h3>
                  <p className="text-xs text-[#6F9DA6] mt-0.5">
                    Installez T-HACK AI comme application native Android sur votre smartphone.
                  </p>
                </div>
              </div>

              <div className="p-3 bg-[#0A1219] rounded border border-[#007C91]/30 text-xs font-mono text-[#6F9DA6] space-y-1.5">
                <div className="text-[#E5FCFF] font-bold">Guide d'installation direct Android :</div>
                <div>1. Ouvrez cette page dans Google Chrome sur votre téléphone.</div>
                <div>2. Appuyez sur les trois points verticaux ⋮ en haut à droite.</div>
                <div>3. Sélectionnez <span className="text-[#00E5FF]">"Installer l'application"</span> ou <span className="text-[#00E5FF]">"Ajouter à l'écran d'accueil"</span>.</div>
                <div>4. T-HACK AI apparaîtra avec son icône autonome sur votre écran de téléphone !</div>
              </div>

              {isInstallable && !isInstalled && (
                <button
                  onClick={install}
                  className="w-full py-2.5 px-4 rounded bg-[#00E5FF] hover:bg-[#78F7FF] text-[#030609] font-mono font-bold text-xs flex items-center justify-center gap-2 transition-all cursor-pointer shadow-[0_0_15px_rgba(0,229,255,0.4)]"
                >
                  <Download className="w-4 h-4" />
                  <span>INSTALLER L'APPLICATION DIRECTEMENT SUR CET APPAREIL</span>
                </button>
              )}
            </div>
          </div>
        )}

      </div>
    </div>
  );
};
