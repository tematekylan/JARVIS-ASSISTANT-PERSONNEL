import React, { useState, useEffect } from 'react';
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
  CheckCircle2,
  Music,
  Globe,
  Users,
  Flashlight,
  Smartphone,
  PhoneCall,
  Zap,
  Radio,
  Eye
} from 'lucide-react';
import { HudPanel } from '../components/HudPanel';
import { UserSettings, NotificationItem, ContactItem } from '../types';
import { 
  launchYouTube, 
  launchWhatsApp, 
  launchGmail, 
  launchMessenger, 
  launchSpotify, 
  launchGoogle, 
  launchCall, 
  launchMessage 
} from '../utils/appCoordinator';
import { 
  toggleFlashlight, 
  isFlashlightOn, 
  requestScreenWakeLock, 
  releaseScreenWakeLock, 
  isScreenWakeLockActive, 
  toggleFullScreen, 
  vibrateDevice, 
  pickNativeDeviceContact, 
  getDeviceBattery 
} from '../utils/phoneControl';
import { loadContacts, saveContacts } from '../utils/storage';
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
  // Spotify State
  const [spotifyQuery, setSpotifyQuery] = useState("Teddy Hackman");

  // Google Search State
  const [googleQuery, setGoogleQuery] = useState("actualités technologiques");

  // YouTube state
  const [ytQuery, setYtQuery] = useState("Teddy Hackman");
  
  // WhatsApp state
  const [waPhone, setWaPhone] = useState(settings.defaultWhatsappNumber || "");
  const [waMessage, setWaMessage] = useState("Bonjour, ceci est un message transmis via T-HACK AI.");
  const [contactSearchQuery, setContactSearchQuery] = useState("");
  const [contacts, setContacts] = useState<ContactItem[]>(loadContacts);

  // Hardware states
  const [torchActive, setTorchActive] = useState(isFlashlightOn());
  const [wakeLockActive, setWakeLockActive] = useState(isScreenWakeLockActive());
  const [batteryInfo, setBatteryInfo] = useState<{ level: number; charging: boolean } | null>(null);

  // Gmail state
  const [mailTo, setMailTo] = useState("temateteddy@gmail.com");
  const [mailSubject, setMailSubject] = useState("Coordination Tactique T-HACK AI");
  const [mailBody, setMailBody] = useState("Bonjour,\n\nMessage généré depuis la passerelle de commande T-HACK AI.\n\nCordialement.");

  // Notifications permission state
  const [permGranted, setPermGranted] = useState(
    typeof window !== 'undefined' && 'Notification' in window && Notification.permission === 'granted'
  );

  useEffect(() => {
    getDeviceBattery().then(b => {
      if (b) setBatteryInfo(b);
    });
  }, []);

  const handleLaunchSpotify = (q?: string) => {
    const target = q || spotifyQuery;
    playJarvisChime();
    speakText(`Lancement de Spotify et recherche pour ${target}.`, {
      rate: settings.speechRate,
      pitch: settings.speechPitch,
      language: settings.voiceLanguage
    });
    launchSpotify(target);
  };

  const handleLaunchGoogle = (q?: string) => {
    const target = q || googleQuery;
    playJarvisChime();
    speakText(`Recherche sur le net via Google pour ${target}.`, {
      rate: settings.speechRate,
      pitch: settings.speechPitch,
      language: settings.voiceLanguage
    });
    launchGoogle(target);
  };

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

  const handleLaunchWhatsApp = (phone?: string, text?: string) => {
    playJarvisChime();
    const p = phone !== undefined ? phone : waPhone;
    const t = text !== undefined ? text : waMessage;
    speakText(`Liaison WhatsApp initiée avec le contact sélectionné.`, {
      rate: settings.speechRate,
      pitch: settings.speechPitch,
      language: settings.voiceLanguage
    });
    launchWhatsApp(p, t);
  };

  const handleToggleTorch = async () => {
    playHudBeep(700, 0.08);
    const res = await toggleFlashlight();
    setTorchActive(res.isOn);
    vibrateDevice(res.isOn ? [80, 50, 80] : 100);
    speakText(res.message, {
      rate: settings.speechRate,
      pitch: settings.speechPitch,
      language: settings.voiceLanguage
    });
  };

  const handleToggleWakeLock = async () => {
    playHudBeep(800, 0.08);
    if (wakeLockActive) {
      await releaseScreenWakeLock();
      setWakeLockActive(false);
      speakText("Mode veille classique réactivé.", { rate: settings.speechRate, pitch: settings.speechPitch, language: settings.voiceLanguage });
    } else {
      const ok = await requestScreenWakeLock();
      setWakeLockActive(ok);
      speakText("Écran maintenu allumé. Mode anti-veille activé.", { rate: settings.speechRate, pitch: settings.speechPitch, language: settings.voiceLanguage });
    }
  };

  const handleToggleFullscreenMode = async () => {
    playJarvisChime();
    const isFull = await toggleFullScreen();
    speakText(isFull ? "Affichage plein écran enclenché." : "Sortie du mode plein écran.", {
      rate: settings.speechRate,
      pitch: settings.speechPitch,
      language: settings.voiceLanguage
    });
  };

  const handlePickNativeContact = async () => {
    playHudBeep(600, 0.05);
    const contact = await pickNativeDeviceContact();
    if (contact) {
      setWaPhone(contact.tel);
      const newContact: ContactItem = {
        id: `cnt_${Date.now()}`,
        name: contact.name,
        phone: contact.tel,
        email: contact.email,
        category: 'Général',
        isFavorite: true
      };
      const updated = [newContact, ...contacts];
      setContacts(updated);
      saveContacts(updated);
      playJarvisChime();
      speakText(`Contact ${contact.name} importé avec succès.`, {
        rate: settings.speechRate,
        pitch: settings.speechPitch,
        language: settings.voiceLanguage
      });
    }
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

  // Filter contacts
  const filteredContacts = contacts.filter(c => 
    c.name.toLowerCase().includes(contactSearchQuery.toLowerCase()) ||
    c.phone.includes(contactSearchQuery)
  );

  return (
    <div className="space-y-6 animate-in fade-in duration-300 pb-12">
      {/* Hardware Control HUD Ribbon */}
      <div className="p-4 bg-[#070D12] border border-[#00E5FF]/40 rounded-sm hud-panel-corner">
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
          <div>
            <div className="flex items-center space-x-2 text-[#00E5FF]">
              <Smartphone className="w-5 h-5 text-[#31F5A3]" />
              <span className="text-xs font-mono tracking-widest uppercase">CONTRÔLE INTÉGRAL DU TÉLÉPHONE & MATÉRIEL</span>
            </div>
            <h2 className="text-xl font-bold font-['Chakra_Petch',sans-serif] tracking-wider text-[#E5FCFF] mt-1">
              PASSERELLES APPS, SPOTIFY, GOOGLE & HARDWARE
            </h2>
            <p className="text-xs font-mono text-[#6F9DA6] mt-1 max-w-2xl">
              Contrôlez votre téléphone à la voix ou en tactile : Spotify, recherches Google, carnet de contacts WhatsApp, torche, et maintien d'écran éveillé.
            </p>
          </div>

          <div className="flex flex-wrap gap-2 shrink-0">
            {/* Screen Wake Lock Button */}
            <button
              onClick={handleToggleWakeLock}
              className={`px-3 py-2 border rounded font-mono text-xs font-bold flex items-center space-x-1.5 transition-all cursor-pointer ${
                wakeLockActive 
                  ? 'bg-[#31F5A3]/20 border-[#31F5A3] text-[#31F5A3] shadow-[0_0_12px_rgba(49,245,163,0.3)]' 
                  : 'bg-[#0A1219] border-[#007C91]/50 text-[#6F9DA6] hover:text-[#E5FCFF]'
              }`}
              title="Garde l'écran de votre téléphone allumé en continu"
            >
              <Eye className="w-4 h-4" />
              <span>{wakeLockActive ? "ÉCRAN ÉVEILLÉ (ON)" : "ANTI-VEILLE (OFF)"}</span>
            </button>

            {/* Flashlight Button */}
            <button
              onClick={handleToggleTorch}
              className={`px-3 py-2 border rounded font-mono text-xs font-bold flex items-center space-x-1.5 transition-all cursor-pointer ${
                torchActive 
                  ? 'bg-[#FFDE00]/20 border-[#FFDE00] text-[#FFDE00] shadow-[0_0_12px_rgba(255,222,0,0.4)]' 
                  : 'bg-[#0A1219] border-[#007C91]/50 text-[#6F9DA6] hover:text-[#E5FCFF]'
              }`}
            >
              <Flashlight className="w-4 h-4" />
              <span>{torchActive ? "TORCHE ALLUMÉE" : "LAMPE TORCHE"}</span>
            </button>

            {/* Fullscreen Button */}
            <button
              onClick={handleToggleFullscreenMode}
              className="px-3 py-2 bg-[#00E5FF]/20 border border-[#00E5FF] hover:bg-[#00E5FF]/30 text-[#00E5FF] font-mono text-xs font-bold rounded flex items-center space-x-1.5 transition-all cursor-pointer shadow-[0_0_12px_rgba(0,229,255,0.3)]"
            >
              <Maximize2 className="w-4 h-4" />
              <span>PLEIN ÉCRAN</span>
            </button>
          </div>
        </div>

        {/* Battery & Status Strip */}
        <div className="mt-3 pt-3 border-t border-[#007C91]/30 flex flex-wrap items-center justify-between text-xs font-mono text-[#6F9DA6] gap-2">
          <div className="flex items-center space-x-4">
            <span>BATTERIE : <strong className="text-[#31F5A3]">{batteryInfo ? `${batteryInfo.level}% ${batteryInfo.charging ? '⚡ (En charge)' : ''}` : '98% NOMINALE'}</strong></span>
            <span>CAPTEURS : <strong className="text-[#00E5FF]">MICRO + AUDIO + CAM</strong></span>
            <span>WAKE LOCK : <strong className={wakeLockActive ? 'text-[#31F5A3]' : 'text-[#6F9DA6]'}>{wakeLockActive ? 'ACTIF' : 'INACTIF'}</strong></span>
          </div>
          <div className="flex items-center space-x-2">
            <span className="text-[#E5FCFF]">ORDRE VOCAL :</span>
            <span className="text-[#31F5A3] font-bold">"Hey AI, allume-toi"</span>
          </div>
        </div>
      </div>

      {/* Voice Wake Word Tip */}
      <div className="p-3 bg-[#00E5FF]/10 border border-[#00E5FF]/40 rounded flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="w-2.5 h-2.5 rounded-full bg-[#31F5A3] animate-ping" />
          <div className="text-xs font-mono text-[#E5FCFF]">
            <span className="text-[#00E5FF] font-bold">WAKE WORD VOCAL PERMANENT :</span> Dites <span className="text-[#31F5A3] font-bold uppercase underline">"Hey AI, allume-toi"</span> ou <span className="text-[#31F5A3] font-bold uppercase underline">"Allume l'écran"</span> pour réveiller instantanément votre téléphone et lancer vos recherches !
          </div>
        </div>
        <button
          onClick={onEnterHolographicMode}
          className="text-xs font-mono text-[#00E5FF] hover:underline cursor-pointer"
        >
          Mode Hologramme &rarr;
        </button>
      </div>

      {/* App Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

        {/* 1. SPOTIFY COORDINATOR */}
        <HudPanel title="PASSERELLE SPOTIFY (STREAMING & MUSIQUE)" accentColor="#1DB954">
          <div className="space-y-4">
            <p className="text-xs text-[#6F9DA6] font-mono">
              Recherchez des artistes, albums, morceaux ou playlists et lancez-les immédiatement sur Spotify.
            </p>

            <div>
              <label className="block text-xs font-mono text-[#6F9DA6] mb-1">
                Artiste, titre ou playlist Spotify :
              </label>
              <div className="flex space-x-2">
                <input
                  type="text"
                  value={spotifyQuery}
                  onChange={(e) => setSpotifyQuery(e.target.value)}
                  placeholder="Ex: Teddy Hackman, Drake, Cyberpunk..."
                  className="flex-1 bg-[#0A1219] border border-[#007C91]/50 rounded px-3 py-2 text-sm text-[#E5FCFF] focus:outline-none focus:border-[#1DB954]"
                />
                <button
                  onClick={() => handleLaunchSpotify()}
                  className="px-4 py-2 bg-[#1DB954] hover:bg-[#1ed760] text-black font-mono font-bold text-xs rounded flex items-center space-x-1.5 transition-all cursor-pointer shadow-[0_0_12px_rgba(29,185,84,0.4)]"
                >
                  <Music className="w-4 h-4" />
                  <span>JOUER</span>
                </button>
              </div>
            </div>

            {/* Quick Presets */}
            <div className="flex flex-wrap gap-2 pt-1">
              <span className="text-[11px] font-mono text-[#6F9DA6] self-center">Raccourcis :</span>
              <button
                onClick={() => { setSpotifyQuery("Teddy Hackman"); handleLaunchSpotify("Teddy Hackman"); }}
                className="px-2.5 py-1 text-xs font-mono bg-[#1DB954]/20 border border-[#1DB954]/40 text-[#1DB954] rounded hover:bg-[#1DB954]/30 transition-all cursor-pointer"
              >
                🎧 Teddy Hackman
              </button>
              <button
                onClick={() => { setSpotifyQuery("Hip Hop 2026"); handleLaunchSpotify("Hip Hop 2026"); }}
                className="px-2.5 py-1 text-xs font-mono bg-[#0A1219] border border-[#007C91]/40 text-[#6F9DA6] rounded hover:text-[#E5FCFF] transition-all cursor-pointer"
              >
                Top Hip-Hop
              </button>
              <button
                onClick={() => { setSpotifyQuery("Cyberpunk Synthesizer"); handleLaunchSpotify("Cyberpunk Synthesizer"); }}
                className="px-2.5 py-1 text-xs font-mono bg-[#0A1219] border border-[#007C91]/40 text-[#6F9DA6] rounded hover:text-[#E5FCFF] transition-all cursor-pointer"
              >
                Ambiance Tactique
              </button>
            </div>
          </div>
        </HudPanel>

        {/* 2. GOOGLE & WEB SEARCH */}
        <HudPanel title="PASSERELLE GOOGLE (RECHERCHE SUR LE NET)" accentColor="#4285F4">
          <div className="space-y-4">
            <p className="text-xs text-[#6F9DA6] font-mono">
              Effectuez instantanément des recherches web sur le moteur Google.
            </p>

            <div>
              <label className="block text-xs font-mono text-[#6F9DA6] mb-1">
                Requête de recherche internet :
              </label>
              <div className="flex space-x-2">
                <input
                  type="text"
                  value={googleQuery}
                  onChange={(e) => setGoogleQuery(e.target.value)}
                  placeholder="Ex: actualités du jour, cours du Bitcoin, météo..."
                  className="flex-1 bg-[#0A1219] border border-[#007C91]/50 rounded px-3 py-2 text-sm text-[#E5FCFF] focus:outline-none focus:border-[#4285F4]"
                />
                <button
                  onClick={() => handleLaunchGoogle()}
                  className="px-4 py-2 bg-[#4285F4] hover:bg-[#5a95f5] text-white font-mono font-bold text-xs rounded flex items-center space-x-1.5 transition-all cursor-pointer shadow-[0_0_12px_rgba(66,133,244,0.4)]"
                >
                  <Globe className="w-4 h-4" />
                  <span>CHERCHER</span>
                </button>
              </div>
            </div>

            {/* Quick Presets */}
            <div className="flex flex-wrap gap-2 pt-1">
              <span className="text-[11px] font-mono text-[#6F9DA6] self-center">Recherches rapides :</span>
              <button
                onClick={() => { setGoogleQuery("Dernières actualités technologiques"); handleLaunchGoogle("Dernières actualités technologiques"); }}
                className="px-2.5 py-1 text-xs font-mono bg-[#4285F4]/20 border border-[#4285F4]/40 text-[#4285F4] rounded hover:bg-[#4285F4]/30 transition-all cursor-pointer"
              >
                🌐 Actualités Tech
              </button>
              <button
                onClick={() => { setGoogleQuery("Teddy Hackman projets"); handleLaunchGoogle("Teddy Hackman projets"); }}
                className="px-2.5 py-1 text-xs font-mono bg-[#0A1219] border border-[#007C91]/40 text-[#6F9DA6] rounded hover:text-[#E5FCFF] transition-all cursor-pointer"
              >
                Teddy Hackman
              </button>
            </div>
          </div>
        </HudPanel>

        {/* 3. WHATSAPP & CONTACTS CARNET */}
        <HudPanel title="PASSERELLE WHATSAPP & CONTACTS" accentColor="#25D366">
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <p className="text-xs text-[#6F9DA6] font-mono">
                Recherchez vos contacts et échangez directement sur WhatsApp.
              </p>
              {typeof navigator !== 'undefined' && 'contacts' in navigator && (
                <button
                  onClick={handlePickNativeContact}
                  className="text-[11px] font-mono text-[#31F5A3] hover:underline flex items-center space-x-1 cursor-pointer"
                >
                  <Users className="w-3.5 h-3.5" />
                  <span>Importer répertoire téléphone</span>
                </button>
              )}
            </div>

            {/* Contact Search & Selection */}
            <div>
              <label className="block text-xs font-mono text-[#6F9DA6] mb-1">
                Rechercher un contact dans WhatsApp :
              </label>
              <div className="relative mb-2">
                <input
                  type="text"
                  value={contactSearchQuery}
                  onChange={(e) => setContactSearchQuery(e.target.value)}
                  placeholder="Filtrer par nom ou numéro..."
                  className="w-full bg-[#0A1219] border border-[#007C91]/50 rounded pl-8 pr-3 py-1.5 text-xs text-[#E5FCFF] focus:outline-none focus:border-[#25D366]"
                />
                <Search className="w-3.5 h-3.5 text-[#6F9DA6] absolute left-2.5 top-2.5" />
              </div>

              {/* Contact Chips */}
              <div className="space-y-1 max-h-32 overflow-y-auto pr-1">
                {filteredContacts.map((c) => (
                  <div
                    key={c.id}
                    onClick={() => { setWaPhone(c.phone); playHudBeep(600, 0.05); }}
                    className={`p-1.5 rounded text-xs font-mono flex items-center justify-between cursor-pointer transition-all ${
                      waPhone === c.phone
                        ? 'bg-[#25D366]/20 border border-[#25D366] text-[#25D366]'
                        : 'bg-[#0A1219] border border-[#007C91]/30 text-[#E5FCFF] hover:border-[#25D366]/50'
                    }`}
                  >
                    <div className="flex items-center space-x-2">
                      <div className="w-2 h-2 rounded-full bg-[#25D366]" />
                      <span className="font-bold">{c.name}</span>
                      <span className="text-[#6F9DA6] text-[11px]">({c.phone})</span>
                    </div>
                    <div className="flex items-center space-x-1">
                      <button
                        onClick={(e) => { e.stopPropagation(); launchCall(c.phone); }}
                        className="p-1 hover:bg-[#00E5FF]/20 rounded text-[#00E5FF]"
                        title="Appeler"
                      >
                        <PhoneCall className="w-3 h-3" />
                      </button>
                      <button
                        onClick={(e) => { e.stopPropagation(); handleLaunchWhatsApp(c.phone); }}
                        className="p-1 hover:bg-[#25D366]/20 rounded text-[#25D366]"
                        title="Ouvrir WhatsApp"
                      >
                        <MessageSquare className="w-3 h-3" />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div>
              <label className="block text-xs font-mono text-[#6F9DA6] mb-1">
                Texte du message à transmettre :
              </label>
              <textarea
                value={waMessage}
                onChange={(e) => setWaMessage(e.target.value)}
                rows={2}
                className="w-full bg-[#0A1219] border border-[#007C91]/50 rounded px-3 py-2 text-sm text-[#E5FCFF] focus:outline-none focus:border-[#25D366]"
              />
            </div>

            <button
              onClick={() => handleLaunchWhatsApp()}
              className="w-full py-2.5 bg-[#25D366] hover:bg-[#2CE570] text-[#030609] font-mono font-bold text-xs tracking-wider rounded flex items-center justify-center space-x-2 transition-all cursor-pointer shadow-[0_0_12px_rgba(37,211,102,0.4)]"
            >
              <Send className="w-4 h-4" />
              <span>OUVRIR WHATSAPP AVEC LE CONTACT</span>
            </button>
          </div>
        </HudPanel>

        {/* 4. YOUTUBE COORDINATOR */}
        <HudPanel title="PASSERELLE YOUTUBE (VIDÉOS & STREAMING)" accentColor="#FF0000">
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

        {/* 5. GMAIL COORDINATOR */}
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

        {/* 6. MESSENGER & NOTIFICATIONS VOCALES */}
        <HudPanel title="PASSERELLE MESSENGER & ALERTES" accentColor="#0084FF">
          <div className="space-y-4">
            <p className="text-xs text-[#6F9DA6] font-mono">
              Accédez directement à vos conversations Facebook Messenger et testez les annonces vocales.
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
                  <span className="text-xs font-mono font-bold">ANNONCE VOCALE DES NOTIFICATIONS</span>
                </div>
                {!permGranted && (
                  <button
                    onClick={handleEnablePermissions}
                    className="text-[10px] font-mono text-[#31F5A3] underline cursor-pointer"
                  >
                    Activer alertes
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
                  📲 WhatsApp
                </button>
                <button
                  onClick={() => handleTestNotification('gmail')}
                  className="py-2 px-2 text-[11px] font-mono bg-[#EA4335]/20 border border-[#EA4335]/50 text-[#EA4335] rounded hover:bg-[#EA4335]/30 transition-all cursor-pointer text-center font-bold"
                >
                  ✉️ Gmail
                </button>
                <button
                  onClick={() => handleTestNotification('messenger')}
                  className="py-2 px-2 text-[11px] font-mono bg-[#0084FF]/20 border border-[#0084FF]/50 text-[#0084FF] rounded hover:bg-[#0084FF]/30 transition-all cursor-pointer text-center font-bold"
                >
                  💬 Messenger
                </button>
              </div>
            </div>
          </div>
        </HudPanel>

      </div>
    </div>
  );
};
