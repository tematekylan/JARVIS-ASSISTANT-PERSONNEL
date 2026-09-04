import React, { useState } from 'react';
import { 
  Bell, 
  Volume2, 
  Trash2, 
  Send, 
  ExternalLink, 
  CheckCheck, 
  Sparkles, 
  MessageCircle, 
  Mail, 
  MessageSquare
} from 'lucide-react';
import { NotificationItem, UserSettings } from '../types';
import { HudPanel } from '../components/HudPanel';
import { speakText, playHudBeep, playJarvisChime } from '../utils/audio';
import { launchWhatsApp, launchGmail, launchMessenger } from '../utils/appCoordinator';
import { simulateIncomingMessage, requestBrowserNotificationPermission } from '../utils/notificationEngine';

interface NotificationsScreenProps {
  notifications: NotificationItem[];
  settings: UserSettings;
  onUpdateNotifications: (list: NotificationItem[]) => void;
  onUpdateSettings: (s: UserSettings) => void;
}

export const NotificationsScreen: React.FC<NotificationsScreenProps> = ({
  notifications,
  settings,
  onUpdateNotifications,
  onUpdateSettings
}) => {
  const [filter, setFilter] = useState<'all' | 'whatsapp' | 'gmail' | 'messenger'>('all');

  const filtered = notifications.filter(n => {
    if (filter === 'all') return true;
    return n.source === filter;
  });

  const handleSpeakAloud = (item: NotificationItem) => {
    playHudBeep(700, 0.05);
    const speech = `Notification de ${item.source === 'whatsapp' ? 'WhatsApp' : item.source === 'gmail' ? 'Gmail' : 'Messenger'}, expéditeur ${item.sender} : "${item.message}".`;
    speakText(speech, {
      rate: settings.speechRate,
      pitch: settings.speechPitch,
      language: settings.voiceLanguage
    });
  };

  const handleQuickReply = (item: NotificationItem) => {
    playJarvisChime();
    const replyText = item.replySuggestion || "Message reçu via T-HACK AI. Merci !";

    if (item.source === 'whatsapp') {
      launchWhatsApp(undefined, replyText);
    } else if (item.source === 'gmail') {
      launchGmail(item.sender.includes('@') ? item.sender : "", "Re: " + item.message.slice(0, 30), replyText);
    } else {
      launchMessenger();
    }
  };

  const handleClearAll = () => {
    onUpdateNotifications([]);
    playHudBeep(440, 0.1);
  };

  const handleToggleVoiceAnnounce = () => {
    const updated = !settings.voiceAnnounceNotifications;
    onUpdateSettings({
      ...settings,
      voiceAnnounceNotifications: updated
    });
    playJarvisChime();
    speakText(
      updated 
        ? "Annonce vocale des notifications activée. Je lirai chaque message à haute voix dès sa réception." 
        : "Annonce vocale des notifications désactivée.",
      { rate: settings.speechRate, pitch: settings.speechPitch, language: settings.voiceLanguage }
    );
  };

  return (
    <div className="space-y-6 animate-in fade-in duration-300 pb-12">
      {/* Top Banner with Toggle */}
      <div className="p-4 bg-[#070D12] border border-[#00E5FF]/40 rounded-sm hud-panel-corner flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2 text-[#00E5FF]">
            <Bell className="w-5 h-5 animate-pulse" />
            <span className="text-xs font-mono tracking-widest uppercase">CENTRE DES NOTIFICATIONS & LECTURE VOCALE</span>
          </div>
          <h2 className="text-xl font-bold font-['Chakra_Petch',sans-serif] tracking-wider text-[#E5FCFF] mt-1">
            FLUX DE NOTIFICATIONS & MESSAGES ENTRANT
          </h2>
          <p className="text-xs font-mono text-[#6F9DA6] mt-1">
            Dès qu'une notification WhatsApp, Messenger ou Gmail arrive, T-HACK AI vous parle pour vous l'annoncer.
          </p>
        </div>

        {/* Voice Announce Toggle */}
        <div className="flex items-center space-x-3 bg-[#0A1219] p-3 rounded border border-[#007C91]/40 shrink-0">
          <Volume2 className={`w-5 h-5 ${settings.voiceAnnounceNotifications ? 'text-[#31F5A3]' : 'text-[#6F9DA6]'}`} />
          <div>
            <div className="text-xs font-mono font-bold text-[#E5FCFF]">ANNONCE VOCALE ACTIVE</div>
            <div className="text-[10px] font-mono text-[#6F9DA6]">Parle dès réception d'un message</div>
          </div>
          <button
            onClick={handleToggleVoiceAnnounce}
            className={`w-12 h-6 rounded-full transition-colors relative cursor-pointer ${
              settings.voiceAnnounceNotifications ? 'bg-[#31F5A3]' : 'bg-[#20333D]'
            }`}
          >
            <div 
              className={`w-5 h-5 rounded-full bg-black transition-transform absolute top-0.5 ${
                settings.voiceAnnounceNotifications ? 'left-6.5' : 'left-0.5'
              }`} 
            />
          </button>
        </div>
      </div>

      {/* Action Bar & Quick Simulation */}
      <div className="flex flex-wrap items-center justify-between gap-3 bg-[#070D12] p-3 border border-[#007C91]/40 rounded">
        {/* Filter pills */}
        <div className="flex space-x-2">
          {(['all', 'whatsapp', 'gmail', 'messenger'] as const).map(f => (
            <button
              key={f}
              onClick={() => setFilter(f)}
              className={`px-3 py-1 text-xs font-mono rounded cursor-pointer transition-all ${
                filter === f
                  ? 'bg-[#00E5FF] text-[#030609] font-bold shadow-[0_0_10px_rgba(0,229,255,0.4)]'
                  : 'bg-[#0A1219] text-[#6F9DA6] hover:text-[#E5FCFF] border border-[#007C91]/30'
              }`}
            >
              {f === 'all' ? 'Toutes' : f.toUpperCase()}
            </button>
          ))}
        </div>

        {/* Simulation Buttons */}
        <div className="flex items-center space-x-2">
          <span className="text-[11px] font-mono text-[#6F9DA6]">Tester l'annonce vocale :</span>
          <button
            onClick={() => simulateIncomingMessage('whatsapp', settings, (n) => onUpdateNotifications([n, ...notifications]))}
            className="px-2.5 py-1 text-xs font-mono bg-[#25D366]/20 border border-[#25D366]/50 text-[#25D366] rounded hover:bg-[#25D366]/30 transition-all cursor-pointer"
          >
            + WhatsApp
          </button>
          <button
            onClick={() => simulateIncomingMessage('gmail', settings, (n) => onUpdateNotifications([n, ...notifications]))}
            className="px-2.5 py-1 text-xs font-mono bg-[#EA4335]/20 border border-[#EA4335]/50 text-[#EA4335] rounded hover:bg-[#EA4335]/30 transition-all cursor-pointer"
          >
            + Gmail
          </button>
          <button
            onClick={() => simulateIncomingMessage('messenger', settings, (n) => onUpdateNotifications([n, ...notifications]))}
            className="px-2.5 py-1 text-xs font-mono bg-[#0084FF]/20 border border-[#0084FF]/50 text-[#0084FF] rounded hover:bg-[#0084FF]/30 transition-all cursor-pointer"
          >
            + Messenger
          </button>
          {notifications.length > 0 && (
            <button
              onClick={handleClearAll}
              className="p-1 text-[#6F9DA6] hover:text-[#FF4660] transition-colors ml-2 cursor-pointer"
              title="Vider les notifications"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          )}
        </div>
      </div>

      {/* Notifications List */}
      <div className="space-y-3">
        {filtered.length === 0 ? (
          <div className="p-8 text-center bg-[#070D12] border border-[#007C91]/30 rounded font-mono text-sm text-[#6F9DA6]">
            Aucune notification reçue dans cette catégorie.
          </div>
        ) : (
          filtered.map(item => (
            <div 
              key={item.id}
              className="p-4 bg-[#070D12] border border-[#007C91]/40 rounded-sm hud-panel-corner flex flex-col md:flex-row items-start md:items-center justify-between gap-4 transition-all hover:border-[#00E5FF]/60"
            >
              <div className="flex items-start space-x-3">
                <div className={`p-2 rounded mt-0.5 ${
                  item.source === 'whatsapp' ? 'bg-[#25D366]/10 text-[#25D366] border border-[#25D366]/40' :
                  item.source === 'gmail' ? 'bg-[#EA4335]/10 text-[#EA4335] border border-[#EA4335]/40' :
                  'bg-[#0084FF]/10 text-[#0084FF] border border-[#0084FF]/40'
                }`}>
                  {item.source === 'whatsapp' ? <MessageCircle className="w-5 h-5" /> :
                   item.source === 'gmail' ? <Mail className="w-5 h-5" /> :
                   <MessageSquare className="w-5 h-5" />}
                </div>

                <div>
                  <div className="flex items-center space-x-2">
                    <span className="text-xs font-mono font-bold uppercase tracking-wider text-[#00E5FF]">
                      [{item.source}]
                    </span>
                    <span className="text-sm font-semibold text-[#E5FCFF]">
                      {item.sender}
                    </span>
                    <span className="text-[10px] font-mono text-[#6F9DA6]">
                      {new Date(item.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  <p className="text-sm text-[#B3C8CD] mt-1 font-mono">
                    "{item.message}"
                  </p>
                </div>
              </div>

              {/* Action buttons */}
              <div className="flex items-center space-x-2 self-end md:self-center shrink-0">
                <button
                  onClick={() => handleSpeakAloud(item)}
                  className="px-3 py-1.5 text-xs font-mono bg-[#00E5FF]/10 border border-[#00E5FF]/40 text-[#00E5FF] rounded hover:bg-[#00E5FF]/20 flex items-center space-x-1.5 transition-all cursor-pointer"
                  title="Écouter à haute voix"
                >
                  <Volume2 className="w-3.5 h-3.5" />
                  <span>LIRE</span>
                </button>

                <button
                  onClick={() => handleQuickReply(item)}
                  className={`px-3 py-1.5 text-xs font-mono font-bold rounded flex items-center space-x-1.5 transition-all cursor-pointer ${
                    item.source === 'whatsapp' ? 'bg-[#25D366] text-black hover:bg-[#2CE570]' :
                    item.source === 'gmail' ? 'bg-[#EA4335] text-white hover:bg-[#F05547]' :
                    'bg-[#0084FF] text-white hover:bg-[#1A91FF]'
                  }`}
                >
                  <Send className="w-3.5 h-3.5" />
                  <span>RÉPONDRE</span>
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
