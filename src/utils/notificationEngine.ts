// Notification Engine & Spoken Alert Announcer for T-HACK AI
import { NotificationItem, UserSettings } from '../types';
import { speakText, playHudSound } from './audio';
import { addNotification } from './storage';

export async function requestBrowserNotificationPermission(): Promise<boolean> {
  if (!('Notification' in window)) {
    console.warn("Browser does not support notifications.");
    return false;
  }

  if (Notification.permission === 'granted') {
    return true;
  }

  if (Notification.permission !== 'denied') {
    const permission = await Notification.requestPermission();
    return permission === 'granted';
  }

  return false;
}

/**
 * Dispatches an incoming notification to the app:
 * - Plays HUD alert chime
 * - Displays in-app and browser notification
 * - ACTUALLY SPEAKS the notification aloud: "Commandant, vous avez reçu une notification de..."
 */
export function dispatchNotification(
  item: Omit<NotificationItem, 'id' | 'timestamp' | 'isRead'>,
  settings: UserSettings,
  onNotificationReceived?: (notification: NotificationItem) => void
): NotificationItem {
  const fullItem: NotificationItem = {
    ...item,
    id: `notif-${Date.now()}-${Math.floor(Math.random() * 1000)}`,
    timestamp: Date.now(),
    isRead: false
  };

  // 1. Save to local persistence
  addNotification(fullItem);

  // 2. Play alert sound
  playHudSound('alert');

  // 3. Spoken voice announcement
  if (settings.voiceAnnounceNotifications && settings.ttsEnabled) {
    const sourceLabel = 
      item.source === 'whatsapp' ? 'WhatsApp' :
      item.source === 'gmail' ? 'Gmail' :
      item.source === 'messenger' ? 'Messenger' :
      item.source === 'youtube' ? 'YouTube' : 'votre appareil';

    const spokenAnnouncement = `Commandant, nouvelle notification reçue de ${sourceLabel}. De la part de ${item.sender}. Message : "${item.message}".`;
    
    // Announce with a slight delay so the HUD alert beep finishes first
    setTimeout(() => {
      speakText(spokenAnnouncement, {
        rate: settings.speechRate,
        pitch: settings.speechPitch,
        language: settings.voiceLanguage
      });
    }, 400);
  }

  // 4. Native Browser Notification (if granted)
  if ('Notification' in window && Notification.permission === 'granted') {
    try {
      new Notification(`T-HACK AI // [${item.source.toUpperCase()}] ${item.sender}`, {
        body: item.message,
        icon: '/favicon.svg'
      });
    } catch (e) {
      console.warn("Could not display native notification:", e);
    }
  }

  if (onNotificationReceived) {
    onNotificationReceived(fullItem);
  }

  return fullItem;
}

/**
 * Simulates real incoming message samples from WhatsApp, Messenger, or Gmail
 */
export function simulateIncomingMessage(
  source: 'whatsapp' | 'messenger' | 'gmail',
  settings: UserSettings,
  onReceived?: (n: NotificationItem) => void
): NotificationItem {
  if (source === 'whatsapp') {
    return dispatchNotification({
      source: 'whatsapp',
      sender: 'Teddy Hackman',
      message: 'Salut ! Peux-tu vérifier les coordonnées de notre projet et me répondre au plus vite ?',
      actionUrl: `https://wa.me/?text=${encodeURIComponent("Bien reçu Teddy, je regarde ça immédiatement !")}`,
      replySuggestion: "Bien reçu Teddy, je regarde ça immédiatement !"
    }, settings, onReceived);
  }

  if (source === 'messenger') {
    return dispatchNotification({
      source: 'messenger',
      sender: 'Alex Dupont',
      message: 'La présentation de T-HACK AI commence dans 15 minutes. Est-ce que les hologrammes sont prêts ?',
      actionUrl: 'https://m.me/',
      replySuggestion: "Affirmatif, les systèmes et le cœur holographique sont opérationnels à 100%."
    }, settings, onReceived);
  }

  return dispatchNotification({
    source: 'gmail',
    sender: 'Direction Technique',
    message: 'Validation d’accréditation niveau 5 confirmée pour votre terminal.',
    actionUrl: 'https://mail.google.com/mail/u/0/#inbox',
    replySuggestion: "Merci pour la confirmation. Prise de commandement effectuée."
  }, settings, onReceived);
}
