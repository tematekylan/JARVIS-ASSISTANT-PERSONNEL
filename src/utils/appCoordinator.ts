// Application Coordinator & External Services Dispatcher
import { speakText, playHudSound } from './audio';
import { UserSettings } from '../types';

export interface AppActionResult {
  handled: boolean;
  action?: string;
  appName?: string;
  targetUrl?: string;
  spokenResponse?: string;
  feedback?: string;
  query?: string;
}

/**
 * Parses and executes natural language commands aimed at external apps
 * e.g., "ouvre moi Youtube et recherche Teddy Hackman et tu me lie sa derniere video"
 * "réponds à mon message whatsapp"
 * "ouvre gmail et écris un mail"
 */
export async function executeExternalAppCommand(
  command: string,
  settings: UserSettings
): Promise<AppActionResult> {
  try {
    const res = await fetch("/api/app-intent", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ command })
    });

    const data = await res.json();

    if (data.success && data.action !== "NONE") {
      playHudSound('success');

      // Voice announcement if TTS enabled
      if (settings.ttsEnabled && data.spokenResponse) {
        speakText(data.spokenResponse, {
          rate: settings.speechRate,
          pitch: settings.speechPitch,
          language: settings.voiceLanguage
        });
      }

      // Open target application in new window/tab or universal link
      if (data.targetUrl) {
        window.open(data.targetUrl, '_blank', 'noopener,noreferrer');
      }

      return {
        handled: true,
        action: data.action,
        appName: data.appName,
        targetUrl: data.targetUrl,
        spokenResponse: data.spokenResponse,
        feedback: data.commandFeedback,
        query: data.query
      };
    }
  } catch (err) {
    console.warn("External app coordination dispatch failed:", err);
  }

  // Direct client-side heuristic fallback if server is unreachable
  const lower = command.toLowerCase();
  if (lower.includes("youtube") || lower.includes("teddy hackman") || lower.includes("video") || lower.includes("vidéo")) {
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
      feedback: `YouTube lancé pour "${query}".`
    };
  }

  if (lower.includes("whatsapp")) {
    const url = `https://wa.me/?text=${encodeURIComponent("Bonjour via T-HACK AI")}`;
    const speech = "Lancement de WhatsApp avec votre message pré-rempli.";
    if (settings.ttsEnabled) {
      speakText(speech, { rate: settings.speechRate, pitch: settings.speechPitch, language: settings.voiceLanguage });
    }
    window.open(url, '_blank', 'noopener,noreferrer');
    return {
      handled: true,
      action: "OPEN_WHATSAPP",
      appName: "WhatsApp",
      targetUrl: url,
      spokenResponse: speech,
      feedback: "WhatsApp lancé."
    };
  }

  return { handled: false };
}

/**
 * Direct launch helpers
 */
export function launchYouTube(query: string = "Teddy Hackman"): void {
  const url = `https://www.youtube.com/results?search_query=${encodeURIComponent(query)}`;
  window.open(url, '_blank', 'noopener,noreferrer');
}

export function launchWhatsApp(phone?: string, text: string = "Bonjour de la part de T-HACK AI"): void {
  const cleanPhone = (phone || "").replace(/\D/g, '');
  const url = cleanPhone 
    ? `https://wa.me/${cleanPhone}?text=${encodeURIComponent(text)}`
    : `https://wa.me/?text=${encodeURIComponent(text)}`;
  window.open(url, '_blank', 'noopener,noreferrer');
}

export function launchGmail(to: string = "", subject: string = "Message via T-HACK AI", body: string = ""): void {
  const url = `https://mail.google.com/mail/?view=cm&fs=1&to=${encodeURIComponent(to)}&su=${encodeURIComponent(subject)}&body=${encodeURIComponent(body)}`;
  window.open(url, '_blank', 'noopener,noreferrer');
}

export function launchMessenger(): void {
  window.open("https://m.me/", '_blank', 'noopener,noreferrer');
}
