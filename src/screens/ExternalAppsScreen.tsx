import React, { useState } from 'react';
import { 
  Tv, 
  MessageSquare, 
  Mail, 
  Send, 
  ExternalLink, 
  Play, 
  Sparkles, 
  Search, 
  Bell, 
  Volume2, 
  Maximize2,
  CheckCircle2
} from 'lucide-react';
import { HudPanel } from '../components/HudPanel';
import { UserSettings, NotificationItem } from '../types';
import { launchYouTube, launchWhatsApp, launchGmail, launchMessenger } from '../utils/appCoordinator';
import { simulateIncomingMessage, requestBrowserNotificationPermission } from '../utils/notificationEngine';
import { playHudBeep, playJarvisChime, speakText } from '../utils/audio';

interface ExternalAppsScreenProps {
  settings: UserSettings;
  onEnterHolographicMode: () => void;
  onNotificationReceived?: (n: NotificationItem) => void;
}

export const ExternalAppsScreen: React.FC<ExternalAppsScreenProps> = ({
  settings,
  onEnterHolographicMode,
  onNotificationReceived
}) => {
  // YouTube state
  const [ytQuery, setYtQuery] = useState("Teddy Hackman");
  
  // WhatsApp state
  const [waPhone, setWaPhone] = useState(settings.defaultWhatsappNumber || "");
  const [waMessage, setWaMessage] = useState("Bonjour, ceci est un message transmis via T-HACK AI.");

  // Gmail state
  const [mailTo, setMailTo] = useState("temateteddy@gmail.com");
  const [mailSubject, setMailSubject] = useState("Coordination Tactique T-HACK AI");
  const [mailBody, setMailBody] = useState("Bonjour,\n\nMessage généré depuis la passerelle de commande T-HACK AI.\n\nCordialement.");

  // Notifications permission state
  const [permGranted, setPermGranted] = useState(
    typeof window !== 'undefined' && 'Notification' in window && Notification.permission === 'granted'
  );

  const handleLaunchYouTube = (queryToUse?: string) => {
    const q = queryToUse || ytQuery;
    playJarvisChime();
    speakText(`Ouverture de YouTube et recherche pour ${q}. Lancement de la vidéo.`, {
      rate: settings.speechRate,
      pitch: settings.speechPitch,
      language: settings.voiceLanguage
    });
    launchYouTube(q);
  };

  const handleLaunchWhatsApp = () => {
    playJarvisChime();
    speakText(`Liaison WhatsApp initiée avec le message pré-rempli.`, {
      rate: settings.speechRate,
      pitch: settings.speechPitch,
      language: settings.voiceLanguage
    });
    launchWhatsApp(waPhone, waMessage);
  };

  const handleLaunchGmail = () => {
    playJarvisChime();
    speakText(`Ouverture de la messagerie Gmail pour composition.`, {
      rate: settings.speechRate,
      pitch: settings.speechPitch,
      language: settings.voiceLanguage
    });
    launchGmail(mailTo, mailSubject, mailBody);
  };

  const handleEnablePermissions = async () => {
    const granted = await requestBrowserNotificationPermission();
    setPermGranted(granted);
    if (granted) {
      playJarvisChime();
      speakText("Autorisation des notifications système accordée.", {
        rate: settings.speechRate,
        pitch: settings.speechPitch,
        language: settings.voiceLanguage
      });
    }
  };

  const handleTestNotification = (source: 'whatsapp' | 'messenger' | 'gmail') => {
    simulateIncomingMessage(source, settings, onNotificationReceived);
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300 pb-12">
      {/* Header Banner */}
      <div className="p-4 bg-[#070D12] border border-[#00E5FF]/40 rounded-sm hud-panel-corner">
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center space-x-2 text-[#00E5FF]">
              <Sparkles className="w-5 h-5 animate-spin-slow" />
              <span className="text-xs font-mono tracking-widest uppercase">COORDINATEUR D'APPLICATIONS & INTENTS</span>
            </div>
            <h2 className="text-xl font-bold font-['Chakra_Petch',sans-serif] tracking-wider text-[#E5FCFF] mt-1">
              PASSERELLE EXTERNE & NOTIFICATIONS VOCALES
            </h2>
            <p className="text-xs font-mono text-[#6F9DA6] mt-1 max-w-2xl">
              Coordonnez vos applications (YouTube, WhatsApp, Messenger, Gmail). T-HACK AI annonce vocalement chaque notification entrante et exécute vos directives directes.
            </p>
          </div>

          <button
            onClick={onEnterHolographicMode}
            className="px-4 py-2.5 bg-[#00E5FF]/20 border border-[#00E5FF] hover:bg-[#00E5FF]/30 text-[#00E5FF] font-mono text-xs font-bold rounded flex items-center space-x-2 transition-all cursor-pointer shadow-[0_0_15px_rgba(0,229,255,0.3)] shrink-0"
          >
            <Maximize2 className="w-4 h-4" />
            <span>ACTIVATION HOLOGRAPHIQUE (PLEIN ÉCRAN)</span>
          </button>
        </div>
      </div>

      {/* Voice Wake Word Tip */}
      <div className="p-3 bg-[#00E5FF]/10 border border-[#00E5FF]/40 rounded flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="w-2.5 h-2.5 rounded-full bg-[#31F5A3] animate-ping" />
          <div className="text-xs font-mono text-[#E5FCFF]">
            <span className="text-[#00E5FF] font-bold">MOT-CLÉ D'ACTIVATION VOCALE :</span> Dites simplement <span className="text-[#31F5A3] font-bold uppercase underline">"HACK AI démarre"</span> au micro pour réveiller le cœur en plein écran holographique !
          </div>
        </div>
        <button
          onClick={onEnterHolographicMode}
          className="text-xs font-mono text-[#00E5FF] hover:underline cursor-pointer"
        >
          Tester immédiatement &rarr;
        </button>
      </div>

      {/* App Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

        {/* 1. YOUTUBE COORDINATOR */}
        <HudPanel title="PASSERELLE YOUTUBE (STREAMING & RECHERCHE)" accentColor="#FF0000">
          <div className="space-y-4">
            <p className="text-xs text-[#6F9DA6] font-mono">
              Recherchez et lancez directement des vidéos d'artistes ou créateurs (Ex: <em>Teddy Hackman</em>).
            </p>

            <div>
              <label className="block text-xs font-mono text-[#6F9DA6] mb-1">
                Requête de recherche / Chaîne / Titre de vidéo :
              </label>
              <div className="flex space-x-2">
                <input
                  type="text"
                  value={ytQuery}
                  onChange={(e) => setYtQuery(e.target.value)}
                  placeholder="Ex: Teddy Hackman"
                  className="flex-1 bg-[#0A1219] border border-[#007C91]/50 rounded px-3 py-2 text-sm text-[#E5FCFF] focus:outline-none focus:border-[#FF4660]"
                />
                <button
                  onClick={() => handleLaunchYouTube()}
                  className="px-4 py-2 bg-[#FF4660] hover:bg-[#FF5D73] text-white font-mono font-bold text-xs rounded flex items-center space-x-1.5 transition-all cursor-pointer shadow-[0_0_10px_rgba(255,70,96,0.4)]"
                >
                  <Search className="w-4 h-4" />
                  <span>OUVRIR</span>
                </button>
              </div>
            </div>

            {/* Quick Presets */}
            <div className="flex flex-wrap gap-2 pt-1">
              <span className="text-[11px] font-mono text-[#6F9DA6] self-center">Raccourcis :</span>
              <button
                onClick={() => { setYtQuery("Teddy Hackman"); handleLaunchYouTube("Teddy Hackman"); }}
                className="px-2.5 py-1 text-xs font-mono bg-[#FF4660]/20 border border-[#FF4660]/40 text-[#FF8596] rounded hover:bg-[#FF4660]/30 transition-all cursor-pointer"
              >
                ▶ Teddy Hackman (Dernière vidéo)
              </button>
              <button
                onClick={() => { setYtQuery("Iron Man HUD Sound"); handleLaunchYouTube("Iron Man HUD Sound"); }}
                className="px-2.5 py-1 text-xs font-mono bg-[#0A1219] border border-[#007C91]/40 text-[#6F9DA6] rounded hover:text-[#E5FCFF] transition-all cursor-pointer"
              >
                Stark Soundscape
              </button>
            </div>
          </div>
        </HudPanel>

        {/* 2. WHATSAPP COORDINATOR */}
        <HudPanel title="PASSERELLE WHATSAPP (MESSAGES & RÉPONSES)" accentColor="#25D366">
          <div className="space-y-4">
            <p className="text-xs text-[#6F9DA6] font-mono">
              Répondez instantanément ou initiez une conversation WhatsApp avec texte pré-rempli.
            </p>

            <div>
              <label className="block text-xs font-mono text-[#6F9DA6] mb-1">
                Numéro destinataire (optionnel avec indicatif) :
              </label>
              <input
                type="text"
                value={waPhone}
                onChange={(e) => setWaPhone(e.target.value)}
                placeholder="Ex: +33612345678 (ou laisser vide pour choisir le contact)"
                className="w-full bg-[#0A1219] border border-[#007C91]/50 rounded px-3 py-2 text-sm text-[#E5FCFF] focus:outline-none focus:border-[#31F5A3]"
              />
            </div>

            <div>
              <label className="block text-xs font-mono text-[#6F9DA6] mb-1">
                Texte du message à transmettre :
              </label>
              <textarea
                value={waMessage}
                onChange={(e) => setWaMessage(e.target.value)}
                rows={2}
                className="w-full bg-[#0A1219] border border-[#007C91]/50 rounded px-3 py-2 text-sm text-[#E5FCFF] focus:outline-none focus:border-[#31F5A3]"
              />
            </div>

            <button
              onClick={handleLaunchWhatsApp}
              className="w-full py-2.5 bg-[#25D366] hover:bg-[#2CE570] text-[#030609] font-mono font-bold text-xs tracking-wider rounded flex items-center justify-center space-x-2 transition-all cursor-pointer shadow-[0_0_12px_rgba(37,211,102,0.4)]"
            >
              <Send className="w-4 h-4" />
              <span>OUVRIR WHATSAPP & TRANSMETTRE</span>
            </button>
          </div>
        </HudPanel>

        {/* 3. GMAIL COORDINATOR */}
        <HudPanel title="PASSERELLE GMAIL (RÉDACTION & TRANSMISSION)" accentColor="#EA4335">
          <div className="space-y-4">
            <p className="text-xs text-[#6F9DA6] font-mono">
              Préparez et envoyez des courriels pré-formatés avec l'IA.
            </p>

            <div className="grid grid-cols-2 gap-2">
              <div>
                <label className="block text-xs font-mono text-[#6F9DA6] mb-1">Destinataire :</label>
                <input
                  type="email"
                  value={mailTo}
                  onChange={(e) => setMailTo(e.target.value)}
                  className="w-full bg-[#0A1219] border border-[#007C91]/50 rounded px-3 py-1.5 text-sm text-[#E5FCFF] focus:outline-none focus:border-[#EA4335]"
                />
              </div>
              <div>
                <label className="block text-xs font-mono text-[#6F9DA6] mb-1">Objet :</label>
                <input
                  type="text"
                  value={mailSubject}
                  onChange={(e) => setMailSubject(e.target.value)}
                  className="w-full bg-[#0A1219] border border-[#007C91]/50 rounded px-3 py-1.5 text-sm text-[#E5FCFF] focus:outline-none focus:border-[#EA4335]"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-mono text-[#6F9DA6] mb-1">Corps du message :</label>
              <textarea
                value={mailBody}
                onChange={(e) => setMailBody(e.target.value)}
                rows={2}
                className="w-full bg-[#0A1219] border border-[#007C91]/50 rounded px-3 py-2 text-sm text-[#E5FCFF] focus:outline-none focus:border-[#EA4335]"
              />
            </div>

            <button
              onClick={handleLaunchGmail}
              className="w-full py-2.5 bg-[#EA4335] hover:bg-[#F05547] text-white font-mono font-bold text-xs tracking-wider rounded flex items-center justify-center space-x-2 transition-all cursor-pointer shadow-[0_0_12px_rgba(234,67,53,0.4)]"
            >
              <Mail className="w-4 h-4" />
              <span>COMPOSER DANS GMAIL</span>
            </button>
          </div>
        </HudPanel>

        {/* 4. MESSENGER COORDINATOR */}
        <HudPanel title="PASSERELLE MESSENGER" accentColor="#0084FF">
          <div className="space-y-4">
            <p className="text-xs text-[#6F9DA6] font-mono">
              Accédez directement à vos conversations Facebook Messenger.
            </p>

            <div className="p-4 bg-[#0A1219] border border-[#007C91]/30 rounded flex items-center justify-between">
              <div>
                <div className="text-sm font-bold text-[#E5FCFF]">Liaison Messenger Web & Mobile</div>
                <div className="text-xs text-[#6F9DA6] mt-0.5">Ouvre l'application ou l'interface web officielle</div>
              </div>
              <button
                onClick={() => {
                  playJarvisChime();
                  launchMessenger();
                }}
                className="px-4 py-2 bg-[#0084FF] hover:bg-[#1A91FF] text-white font-mono font-bold text-xs rounded flex items-center space-x-1.5 transition-all cursor-pointer shadow-[0_0_10px_rgba(0,132,255,0.4)]"
              >
                <ExternalLink className="w-4 h-4" />
                <span>OUVRIR</span>
              </button>
            </div>

            {/* Notification Announcement Test Panel */}
            <div className="pt-2 border-t border-[#007C91]/30">
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center space-x-2 text-[#00E5FF]">
                  <Volume2 className="w-4 h-4" />
                  <span className="text-xs font-mono font-bold">TEST D'ANNONCE VOCALE DE NOTIFICATION</span>
                </div>
                {!permGranted && (
                  <button
                    onClick={handleEnablePermissions}
                    className="text-[10px] font-mono text-[#31F5A3] underline cursor-pointer"
                  >
                    Activer alertes navigateur
                  </button>
                )}
              </div>

              <p className="text-[11px] font-mono text-[#6F9DA6] mb-3">
                Cliquez pour simuler une notification entrante : T-HACK AI vous parlera à voix haute pour vous annoncer le message !
              </p>

              <div className="grid grid-cols-3 gap-2">
                <button
                  onClick={() => handleTestNotification('whatsapp')}
                  className="py-2 px-2 text-[11px] font-mono bg-[#25D366]/20 border border-[#25D366]/50 text-[#25D366] rounded hover:bg-[#25D366]/30 transition-all cursor-pointer text-center font-bold"
                >
                  📲 Alerte WhatsApp
                </button>
                <button
                  onClick={() => handleTestNotification('gmail')}
                  className="py-2 px-2 text-[11px] font-mono bg-[#EA4335]/20 border border-[#EA4335]/50 text-[#EA4335] rounded hover:bg-[#EA4335]/30 transition-all cursor-pointer text-center font-bold"
                >
                  ✉️ Alerte Gmail
                </button>
                <button
                  onClick={() => handleTestNotification('messenger')}
                  className="py-2 px-2 text-[11px] font-mono bg-[#0084FF]/20 border border-[#0084FF]/50 text-[#0084FF] rounded hover:bg-[#0084FF]/30 transition-all cursor-pointer text-center font-bold"
                >
                  💬 Alerte Messenger
                </button>
              </div>
            </div>
          </div>
        </HudPanel>

      </div>
    </div>
  );
};
