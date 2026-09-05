// Application Coordinator & External Services Dispatcher
import { speakText, playHudSound } from './audio';
import { ActionCard, UserSettings } from '../types';
import { 
  launchSpotifySearch, 
  launchGoogleSearch, 
  launchWhatsAppChat, 
  launchPhoneCall, 
  launchSms,
  toggleFlashlight, 
  requestScreenWakeLock, 
  toggleFullScreen,
  vibrateDevice
} from './phoneControl';

export interface AppActionResult {
  handled: boolean;
  action?: string;
  appName?: string;
  targetUrl?: string;
  nativeUrl?: string;
  spokenResponse?: string;
  feedback?: string;
  query?: string;
  contactName?: string;
  contactPhone?: string;
  actionCard?: ActionCard;
}

/**
 * Parses and executes natural language commands aimed at phone & external apps
 * e.g., "recherche sur spotify Drake"
 * "cherche sur le net les dernières actus"
 * "recherche mes contacts dans mon whatsapp"
 * "allume la lampe torche"
 * "Hey AI allume-toi en plein écran"
 */
export async function executeExternalAppCommand(
  command: string,
  settings: UserSettings
): Promise<AppActionResult> {
  const lower = command.toLowerCase().trim();

  // Try backend intent parser first
  try {
    const res = await fetch("/api/app-intent", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ command })
    });

    const data = await res.json();

    if (data.success && data.action !== "NONE") {
      playHudSound('success');
      vibrateDevice([60, 40, 60]);

      // Handle specific hardware/local actions
      if (data.action === "TOGGLE_FLASHLIGHT") {
        await toggleFlashlight(data.turnOn);
      } else if (data.action === "WAKE_SCREEN_FULLSCREEN") {
        await requestScreenWakeLock();
        await toggleFullScreen(true);
      } else if (data.action === "OPEN_SPOTIFY" && data.query) {
        launchSpotifySearch(data.query);
      } else if (data.action === "OPEN_GOOGLE_SEARCH" && data.query) {
        launchGoogleSearch(data.query);
      } else if (data.action === "CALL_PHONE" && data.targetUrl) {
        launchPhoneCall(data.targetUrl);
      } else if (data.targetUrl) {
        // Universal web open
        window.open(data.targetUrl, '_blank', 'noopener,noreferrer');
      }

      // Voice announcement if TTS enabled
      if (settings.ttsEnabled && data.spokenResponse) {
        speakText(data.spokenResponse, {
          rate: settings.speechRate,
          pitch: settings.speechPitch,
          language: settings.voiceLanguage
        });
      }

      // Build ActionCard for rich HUD UI
      let actionCard: ActionCard | undefined;
      if (data.action === "OPEN_SPOTIFY") {
        actionCard = {
          appName: 'Spotify',
          title: `Recherche Spotify : "${data.query}"`,
          actionUrl: data.targetUrl || `https://open.spotify.com/search/${encodeURIComponent(data.query)}`,
          nativeUrl: data.nativeUrl || `spotify:search:${encodeURIComponent(data.query)}`,
          query: data.query,
          buttonLabel: "🎧 Lancer dans Spotify",
          extraDetails: "Flux audio streaming synchronisé"
        };
      } else if (data.action === "OPEN_GOOGLE_SEARCH") {
        actionCard = {
          appName: 'Google',
          title: `Recherche Google : "${data.query}"`,
          actionUrl: data.targetUrl,
          query: data.query,
          buttonLabel: "🌐 Ouvrir sur Google",
          extraDetails: "Résultats du moteur de recherche web"
        };
      } else if (data.action === "SEARCH_WHATSAPP_CONTACTS" || data.action === "OPEN_WHATSAPP") {
        actionCard = {
          appName: 'WhatsApp',
          title: `Contact & Message WhatsApp`,
          actionUrl: data.targetUrl || "https://wa.me/",
          buttonLabel: "💬 Ouvrir WhatsApp",
          extraDetails: data.query ? `Destinataire : ${data.query}` : "Messagerie instantanée"
        };
      } else if (data.action === "OPEN_YOUTUBE") {
        actionCard = {
          appName: 'YouTube',
          title: `Vidéo YouTube : "${data.query}"`,
          actionUrl: data.targetUrl,
          query: data.query,
          buttonLabel: "▶ Regarder sur YouTube"
        };
      } else if (data.action === "CALL_PHONE") {
        actionCard = {
          appName: 'Phone',
          title: `Appel téléphonique : ${data.contactName || 'Numéro'}`,
          actionUrl: data.targetUrl || "tel:",
          buttonLabel: "📞 Appeler le contact"
        };
      } else if (data.action === "TOGGLE_FLASHLIGHT") {
        actionCard = {
          appName: 'Flashlight',
          title: "Lampe torche du téléphone",
          actionUrl: "#flashlight",
          buttonLabel: "🔦 Torche matérielle activée"
        };
      } else if (data.action === "WAKE_SCREEN_FULLSCREEN") {
        actionCard = {
          appName: 'System',
          title: "Écran et Anti-veille activés",
          actionUrl: "#wake",
          buttonLabel: "⚡ Écran maintenu allumé"
        };
      }

      return {
        handled: true,
        action: data.action,
        appName: data.appName,
        targetUrl: data.targetUrl,
        nativeUrl: data.nativeUrl,
        spokenResponse: data.spokenResponse,
        feedback: data.commandFeedback,
        query: data.query,
        contactName: data.contactName,
        actionCard
      };
    }
  } catch (err) {
    console.warn("External app coordination dispatch fallback to client:", err);
  }

  // ==========================================
  // CLIENT-SIDE DIRECT HEURISTICS (Zero-Latency)
  // ==========================================

  // 1. Spotify
  if (lower.includes("spotify") || (lower.includes("musique") && !lower.includes("youtube")) || lower.includes("ecoute") || lower.includes("écoute")) {
    let q = "Teddy Hackman";
    const spotMatch = command.match(/(?:recherche|cherche|trouve|joue|lance|écoute|ecoute|met|mets)\s+(.+?)(?:\s+sur\s+spotify|\s*$)/i);
    if (spotMatch && spotMatch[1]) {
      q = spotMatch[1].replace(/sur spotify/i, '').trim() || "Teddy Hackman";
    }
    const webUrl = launchSpotifySearch(q);
    const speech = `Recherche de "${q}" sur Spotify. Lancement de l'écoute musicale.`;
    if (settings.ttsEnabled) {
      speakText(speech, { rate: settings.speechRate, pitch: settings.speechPitch, language: settings.voiceLanguage });
    }
    return {
      handled: true,
      action: "OPEN_SPOTIFY",
      appName: "Spotify",
      targetUrl: webUrl,
      spokenResponse: speech,
      feedback: `Spotify lancé pour "${q}".`,
      actionCard: {
        appName: "Spotify",
        title: `Spotify : ${q}`,
        actionUrl: webUrl,
        query: q,
        buttonLabel: "🎧 Ouvrir dans Spotify"
      }
    };
  }

  // 2. Google Search
  if (lower.includes("google") || lower.includes("recherche sur le net") || lower.includes("cherche sur le web") || lower.includes("recherche sur internet")) {
    const q = command.replace(/google|recherche sur le net|cherche sur le net|recherche|cherche|sur internet/gi, '').trim() || "actualités";
    const url = launchGoogleSearch(q);
    const speech = `Recherche Google lancée pour "${q}".`;
    if (settings.ttsEnabled) {
      speakText(speech, { rate: settings.speechRate, pitch: settings.speechPitch, language: settings.voiceLanguage });
    }
    return {
      handled: true,
      action: "OPEN_GOOGLE_SEARCH",
      appName: "Google",
      targetUrl: url,
      spokenResponse: speech,
      feedback: `Recherche Google pour "${q}".`,
      actionCard: {
        appName: "Google",
        title: `Google : ${q}`,
        actionUrl: url,
        query: q,
        buttonLabel: "🌐 Voir sur Google"
      }
    };
  }

  // 3. YouTube
  if (lower.includes("youtube") || lower.includes("video") || lower.includes("vidéo") || lower.includes("teddy hackman")) {
    const query = "Teddy Hackman";
    const url = `https://www.youtube.com/results?search_query=${encodeURIComponent(query)}`;
    const speech = `Ouverture de YouTube et recherche de ${query}. Lancement de la vidéo.`;
    if (settings.ttsEnabled) {
      speakText(speech, { rate: settings.speechRate, pitch: settings.speechPitch, language: settings.voiceLanguage });
    }
    window.open(url, '_blank', 'noopener,noreferrer');
    return {
      handled: true,
      action: "OPEN_YOUTUBE",
      appName: "YouTube",
      targetUrl: url,
      spokenResponse: speech,
      feedback: `YouTube lancé pour "${query}".`,
      actionCard: {
        appName: "YouTube",
        title: `YouTube : ${query}`,
        actionUrl: url,
        query,
        buttonLabel: "▶ Lancer sur YouTube"
      }
    };
  }

  // 4. WhatsApp
  if (lower.includes("whatsapp") || lower.includes("contact")) {
    const url = launchWhatsAppChat("", "Bonjour via T-HACK AI");
    const speech = "Lancement de WhatsApp avec votre carnet de contacts et message pré-rempli.";
    if (settings.ttsEnabled) {
      speakText(speech, { rate: settings.speechRate, pitch: settings.speechPitch, language: settings.voiceLanguage });
    }
    return {
      handled: true,
      action: "OPEN_WHATSAPP",
      appName: "WhatsApp",
      targetUrl: url,
      spokenResponse: speech,
      feedback: "WhatsApp lancé.",
      actionCard: {
        appName: "WhatsApp",
        title: "Passerelle WhatsApp",
        actionUrl: url,
        buttonLabel: "💬 Ouvrir WhatsApp"
      }
    };
  }

  // 5. Lampe Torche
  if (lower.includes("lampe") || lower.includes("torche") || lower.includes("flash")) {
    const isOff = lower.includes("éteins") || lower.includes("eteins") || lower.includes("coupe");
    toggleFlashlight(!isOff);
    const speech = isOff ? "Lampe torche éteinte." : "Lampe torche allumée.";
    if (settings.ttsEnabled) {
      speakText(speech, { rate: settings.speechRate, pitch: settings.speechPitch, language: settings.voiceLanguage });
    }
    return {
      handled: true,
      action: "TOGGLE_FLASHLIGHT",
      appName: "Flashlight",
      spokenResponse: speech,
      feedback: speech
    };
  }

  // 6. Wake Screen & Fullscreen
  if (lower.includes("allume-toi") || lower.includes("allume toi") || lower.includes("allume l'écran") || lower.includes("plein écran") || lower.includes("reveille-toi") || lower.includes("réveille-toi")) {
    requestScreenWakeLock();
    toggleFullScreen(true);
    const speech = "Systèmes T-HACK allumés en plein écran et maintien d'écran éveillé activé.";
    if (settings.ttsEnabled) {
      speakText(speech, { rate: settings.speechRate, pitch: settings.speechPitch, language: settings.voiceLanguage });
    }
    return {
      handled: true,
      action: "WAKE_SCREEN_FULLSCREEN",
      appName: "System",
      spokenResponse: speech,
      feedback: "Écran allumé en plein écran."
    };
  }

  return { handled: false };
}

/**
 * Direct launch helper functions for buttons & menus
 */
export function launchSpotify(query: string = "Teddy Hackman"): string {
  return launchSpotifySearch(query);
}

export function launchGoogle(query: string = "actualités"): string {
  return launchGoogleSearch(query);
}

export function launchYouTube(query: string = "Teddy Hackman"): void {
  const url = `https://www.youtube.com/results?search_query=${encodeURIComponent(query)}`;
  window.open(url, '_blank', 'noopener,noreferrer');
}

export function launchWhatsApp(phone?: string, text: string = "Bonjour de la part de T-HACK AI"): string {
  return launchWhatsAppChat(phone, text);
}

export function launchGmail(to: string = "", subject: string = "Message via T-HACK AI", body: string = ""): void {
  const url = `https://mail.google.com/mail/?view=cm&fs=1&to=${encodeURIComponent(to)}&su=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
  window.open(url, '_blank', 'noopener,noreferrer');
}

export function launchMessenger(): void {
  window.open("https://m.me/", '_blank', 'noopener,noreferrer');
}

export function launchCall(phone: string): void {
  launchPhoneCall(phone);
}

export function launchMessage(phone: string, text?: string): void {
  launchSms(phone, text);
}

