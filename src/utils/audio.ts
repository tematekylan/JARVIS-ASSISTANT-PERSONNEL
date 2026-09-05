// Web Audio API Synthesizer and Voice Engine for T-HACK AI

let audioCtx: AudioContext | null = null;
let analyserNode: AnalyserNode | null = null;
let mediaStream: MediaStream | null = null;
let recognitionInstance: any = null;

function getAudioContext(): AudioContext {
  if (!audioCtx) {
    const AudioContextClass = window.AudioContext || (window as any).webkitAudioContext;
    audioCtx = new AudioContextClass();
  }
  if (audioCtx.state === 'suspended') {
    audioCtx.resume();
  }
  return audioCtx;
}

/**
 * Plays futuristic sci-fi sound effects using pure Web Audio oscillator synthesis
 */
export function playHudSound(type: 'activate' | 'beep' | 'success' | 'alert' | 'computing') {
  try {
    const ctx = getAudioContext();
    const now = ctx.currentTime;
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();

    osc.connect(gain);
    gain.connect(ctx.destination);

    switch (type) {
      case 'activate':
        osc.type = 'sine';
        osc.frequency.setValueAtTime(440, now);
        osc.frequency.exponentialRampToValueAtTime(880, now + 0.12);
        osc.frequency.exponentialRampToValueAtTime(1760, now + 0.25);
        gain.gain.setValueAtTime(0.08, now);
        gain.gain.linearRampToValueAtTime(0.12, now + 0.1);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.3);
        osc.start(now);
        osc.stop(now + 0.3);
        break;

      case 'beep':
        osc.type = 'sine';
        osc.frequency.setValueAtTime(1200, now);
        gain.gain.setValueAtTime(0.06, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.08);
        osc.start(now);
        osc.stop(now + 0.08);
        break;

      case 'success':
        osc.type = 'triangle';
        osc.frequency.setValueAtTime(523.25, now); // C5
        osc.frequency.setValueAtTime(659.25, now + 0.08); // E5
        osc.frequency.setValueAtTime(783.99, now + 0.16); // G5
        osc.frequency.setValueAtTime(1046.50, now + 0.24); // C6
        gain.gain.setValueAtTime(0.08, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.45);
        osc.start(now);
        osc.stop(now + 0.45);
        break;

      case 'alert':
        osc.type = 'sawtooth';
        osc.frequency.setValueAtTime(880, now);
        osc.frequency.linearRampToValueAtTime(440, now + 0.15);
        gain.gain.setValueAtTime(0.1, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.25);
        osc.start(now);
        osc.stop(now + 0.25);
        break;

      case 'computing':
        osc.type = 'sine';
        osc.frequency.setValueAtTime(700, now);
        osc.frequency.setValueAtTime(900, now + 0.04);
        osc.frequency.setValueAtTime(800, now + 0.08);
        gain.gain.setValueAtTime(0.03, now);
        gain.gain.exponentialRampToValueAtTime(0.001, now + 0.14);
        osc.start(now);
        osc.stop(now + 0.14);
        break;
    }
  } catch (e) {
    // Audio may be blocked until first user gesture
  }
}

/**
 * Text-To-Speech Synthesizer
 */
export function speakText(
  text: string,
  options?: {
    rate?: number;
    pitch?: number;
    language?: string;
    onStart?: () => void;
    onEnd?: () => void;
  }
): void {
  if (!('speechSynthesis' in window)) {
    console.warn("Speech Synthesis non supporté par ce navigateur.");
    return;
  }

  window.speechSynthesis.cancel();

  // Strip markdown formatting for cleaner audio speech
  const cleanText = text
    .replace(/[#*_`~>-]/g, '')
    .replace(/\[(.*?)\]\(.*?\)/g, '$1')
    .replace(/```[\s\S]*?```/g, 'Code source omis.')
    .trim();

  if (!cleanText) return;

  const utterance = new SpeechSynthesisUtterance(cleanText);
  utterance.rate = options?.rate ?? 1.0;
  utterance.pitch = options?.pitch ?? 1.0;

  const lang = options?.language === 'en' ? 'en-US' : 'fr-FR';
  utterance.lang = lang;

  // Try to pick a sophisticated voice
  const voices = window.speechSynthesis.getVoices();
  const preferredVoice = voices.find(v => 
    v.lang.startsWith(lang.split('-')[0]) && 
    (v.name.includes('Google') || v.name.includes('Natural') || v.name.includes('Enhanced') || v.name.includes('Thomas') || v.name.includes('Daniel'))
  ) || voices.find(v => v.lang.startsWith(lang.split('-')[0]));

  if (preferredVoice) {
    utterance.voice = preferredVoice;
  }

  utterance.onstart = () => {
    if (options?.onStart) options.onStart();
  };

  utterance.onend = () => {
    if (options?.onEnd) options.onEnd();
  };

  utterance.onerror = () => {
    if (options?.onEnd) options.onEnd();
  };

  window.speechSynthesis.speak(utterance);
}

export function stopSpeaking(): void {
  if ('speechSynthesis' in window) {
    window.speechSynthesis.cancel();
  }
}

export function playHudBeep(freq?: number, duration?: number): void {
  playHudSound('beep');
}

export function playJarvisChime(): void {
  playHudSound('success');
}

export function playErrorAlarm(): void {
  playHudSound('alert');
}

/**
 * Start Acoustic Microphone Listener & Speech Recognition
 */
export function startListening(
  onTranscript: (text: string) => void,
  onError?: (err: any) => void,
  language: string = 'fr'
): boolean {
  const SpeechRecognitionClass = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
  if (!SpeechRecognitionClass) {
    console.warn("SpeechRecognition API is not available on this browser.");
    if (onError) onError(new Error("Microphone API not supported"));
    return false;
  }

  try {
    recognitionInstance = new SpeechRecognitionClass();
    recognitionInstance.continuous = false;
    recognitionInstance.interimResults = false;
    recognitionInstance.lang = language === 'en' ? 'en-US' : 'fr-FR';

    recognitionInstance.onresult = (event: any) => {
      if (event.results && event.results.length > 0) {
        const transcript = event.results[0][0].transcript;
        if (transcript) {
          onTranscript(transcript);
        }
      }
    };

    recognitionInstance.onerror = (event: any) => {
      if (onError) onError(event.error);
    };

    recognitionInstance.start();
    return true;
  } catch (e) {
    if (onError) onError(e);
    return false;
  }
}


export { 
  requestScreenWakeLock, 
  releaseScreenWakeLock, 
  isScreenWakeLockActive 
} from './phoneControl';

let wakeWordRecognitionInstance: any = null;
let isWakeWordListening = false;

/**
 * Continuous Wake Word Detection Loop
 * Listens for "Hey AI, allume-toi", "Allume-toi", "Allume l'écran", "T-HACK", "Hey Jarvis", "Plein écran", etc.
 */
export function startWakeWordDetection(
  onWakeWord: (triggerPhrase: string) => void,
  onSpeechInput?: (text: string) => void,
  language: string = 'fr'
): boolean {
  const SpeechRecognitionClass = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
  if (!SpeechRecognitionClass) {
    console.warn("SpeechRecognition indisponible pour le réveil par mot-clé.");
    return false;
  }

  isWakeWordListening = true;

  const launchListener = () => {
    if (!isWakeWordListening) return;

    try {
      if (wakeWordRecognitionInstance) {
        try { wakeWordRecognitionInstance.abort(); } catch (e) {}
      }

      const rec = new SpeechRecognitionClass();
      rec.continuous = true;
      rec.interimResults = true;
      rec.lang = language === 'en' ? 'en-US' : 'fr-FR';

      rec.onresult = (event: any) => {
        for (let i = event.resultIndex; i < event.results.length; ++i) {
          const transcript = event.results[i][0].transcript.toLowerCase().trim();
          if (onSpeechInput) onSpeechInput(transcript);

          // Check wake phrases
          if (
            transcript.includes("allume-toi") ||
            transcript.includes("allume toi") ||
            transcript.includes("allume l'ecran") ||
            transcript.includes("allume l'écran") ||
            transcript.includes("hey ai allume") ||
            transcript.includes("hey ai") ||
            transcript.includes("hack ai") ||
            transcript.includes("hey jarvis") ||
            transcript.includes("t-hack") ||
            transcript.includes("reveille-toi") ||
            transcript.includes("réveille-toi") ||
            transcript.includes("active-toi") ||
            transcript.includes("plein ecran") ||
            transcript.includes("plein écran")
          ) {
            console.log("[WAKE-WORD ACTIVATED]:", transcript);
            playJarvisChime();
            onWakeWord(transcript);
            break;
          }
        }
      };

      rec.onerror = (event: any) => {
        if (event.error === 'not-allowed') {
          isWakeWordListening = false;
        }
      };

      rec.onend = () => {
        // Automatically restart loop if wake-word detection is still desired
        if (isWakeWordListening) {
          setTimeout(() => {
            launchListener();
          }, 350);
        }
      };

      wakeWordRecognitionInstance = rec;
      rec.start();
      return true;
    } catch (err) {
      console.warn("Erreur démarrage écoute mot-clé:", err);
      return false;
    }
  };

  return launchListener() || false;
}

export function stopWakeWordDetection(): void {
  isWakeWordListening = false;
  if (wakeWordRecognitionInstance) {
    try {
      wakeWordRecognitionInstance.stop();
    } catch (e) {}
    wakeWordRecognitionInstance = null;
  }
}

export function isWakeWordDetectionActive(): boolean {
  return isWakeWordListening;
}

export function stopListening(): void {
  if (recognitionInstance) {
    try {
      recognitionInstance.stop();
    } catch (e) {}
    recognitionInstance = null;
  }

  if (mediaStream) {
    mediaStream.getTracks().forEach(track => track.stop());
    mediaStream = null;
  }
  analyserNode = null;
}
