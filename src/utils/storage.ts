import { Conversation, IncidentReport, Memory, Message, Note, TaskItem, ToolLog, UserSettings } from '../types';

export const DEFAULT_USER_SETTINGS: UserSettings = {
  id: 1,
  userName: "Sir",
  userEmail: "",
  userPhone: "",
  authProvider: "guest",
  isLoggedIn: false,
  securityClearanceLevel: "LEVEL 5 (COMMANDER)",
  assistantName: "T-HACK AI",
  voiceLanguage: "fr",
  ttsEnabled: true,
  autoSpeakResponses: false,
  speechRate: 1.0,
  speechPitch: 1.0,
  aiModel: "gemini-3.8-flash",
  temperature: 0.7,
  memoryEnabled: true,
  isDemoMode: false,
  personalityTone: "Calm & Professional",
  activeAiProvider: "gemini",
  customGeminiApiKey: "",
  customOpenAiApiKey: "",
  customClaudeApiKey: "",
  customGroqApiKey: "",
  customDeepSeekApiKey: "",
  voicePersonaName: "JARVIS Neural",
  voicePersonaDescription: "Assistant synthétique d'ingénierie tactique",
  voicePersonaPitch: 0.95,
  voicePersonaRate: 1.05,
  voicePersonaPromptStyle: "Tu es T-HACK AI, l'intelligence artificielle tactique personnelle hautement sophistiquée inspirée de J.A.R.V.I.S.",
  isVoicePersonaActive: true,
  developerAlertEmail: "temateteddy@gmail.com",
  autoSendErrorAlerts: true
};

const STORAGE_KEYS = {
  SETTINGS: 'thack_settings_v1',
  CONVERSATIONS: 'thack_conversations_v1',
  MESSAGES: 'thack_messages_v1',
  MEMORIES: 'thack_memories_v1',
  NOTES: 'thack_notes_v1',
  TASKS: 'thack_tasks_v1',
  INCIDENTS: 'thack_incidents_v1',
  TOOL_LOGS: 'thack_tool_logs_v1'
};

export function loadSettings(): UserSettings {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.SETTINGS);
    if (raw) return { ...DEFAULT_USER_SETTINGS, ...JSON.parse(raw) };
  } catch (e) {
    console.warn("Failed to load settings:", e);
  }
  return DEFAULT_USER_SETTINGS;
}

export function saveSettings(settings: UserSettings): void {
  try {
    localStorage.setItem(STORAGE_KEYS.SETTINGS, JSON.stringify(settings));
  } catch (e) {
    console.warn("Failed to save settings:", e);
  }
}

export function loadConversations(): Conversation[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.CONVERSATIONS);
    if (raw) return JSON.parse(raw);
  } catch (e) {
    console.warn("Failed to load conversations:", e);
  }
  const defaultConv: Conversation = {
    id: "conv-1",
    title: "Session Tactique Principale",
    createdAt: Date.now() - 3600000,
    updatedAt: Date.now(),
    isPinned: true,
    summary: "Session initiale avec le Core T-HACK AI"
  };
  saveConversations([defaultConv]);
  return [defaultConv];
}

export function saveConversations(convs: Conversation[]): void {
  try {
    localStorage.setItem(STORAGE_KEYS.CONVERSATIONS, JSON.stringify(convs));
  } catch (e) {
    console.warn("Failed to save conversations:", e);
  }
}

export function loadMessages(): Message[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.MESSAGES);
    if (raw) return JSON.parse(raw);
  } catch (e) {
    console.warn("Failed to load messages:", e);
  }
  return [];
}

export function saveMessages(messages: Message[]): void {
  try {
    localStorage.setItem(STORAGE_KEYS.MESSAGES, JSON.stringify(messages));
  } catch (e) {
    console.warn("Failed to save messages:", e);
  }
}

export function loadMemories(): Memory[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.MEMORIES);
    if (raw) return JSON.parse(raw);
  } catch (e) {
    console.warn("Failed to load memories:", e);
  }
  const defaults: Memory[] = [
    {
      id: "mem-1",
      key: "Nom du Commandant",
      content: "L'utilisateur est le Commandant en Chef du système T-HACK.",
      category: "Identity",
      timestamp: Date.now() - 86400000,
      isActive: true
    },
    {
      id: "mem-2",
      key: "Style de Réponse Préféré",
      content: "Réponses précises, directes, avec analyse tactique si nécessaire.",
      category: "Preferences",
      timestamp: Date.now() - 80000000,
      isActive: true
    },
    {
      id: "mem-3",
      key: "Projet d'Architecture",
      content: "Développement d'un assistant cybernétique à noyau holographique et moteur vocal.",
      category: "Work",
      timestamp: Date.now() - 40000000,
      isActive: true
    }
  ];
  saveMemories(defaults);
  return defaults;
}

export function saveMemories(memories: Memory[]): void {
  try {
    localStorage.setItem(STORAGE_KEYS.MEMORIES, JSON.stringify(memories));
  } catch (e) {
    console.warn("Failed to save memories:", e);
  }
}

export function loadNotes(): Note[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.NOTES);
    if (raw) return JSON.parse(raw);
  } catch (e) {
    console.warn("Failed to load notes:", e);
  }
  const defaults: Note[] = [
    {
      id: "note-1",
      title: "Spécifications Armure Mark 85",
      content: "Alliage or-titane renforcé par nanotechnologie. Alimentation principale : Réacteur Arc Palladium nouvelle génération.",
      tags: "ingénierie,stark",
      createdAt: Date.now() - 7200000,
      updatedAt: Date.now() - 7200000
    },
    {
      id: "note-2",
      title: "Protocole d'urgence Incidents",
      content: "En cas d'anomalie critique, convocation automatique du Collège d'IA de Résolution (Architecte, Débogueur, Ingénieur Patch).",
      tags: "sécurité,ai",
      createdAt: Date.now() - 3600000,
      updatedAt: Date.now() - 3600000
    }
  ];
  saveNotes(defaults);
  return defaults;
}

export function saveNotes(notes: Note[]): void {
  try {
    localStorage.setItem(STORAGE_KEYS.NOTES, JSON.stringify(notes));
  } catch (e) {
    console.warn("Failed to save notes:", e);
  }
}

export function loadTasks(): TaskItem[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.TASKS);
    if (raw) return JSON.parse(raw);
  } catch (e) {
    console.warn("Failed to load tasks:", e);
  }
  const defaults: TaskItem[] = [
    { id: "1", title: "Vérifier la connectivité neuronale de T-HACK AI", time: "09:00", category: "Système", isCompleted: true },
    { id: "2", title: "Synchroniser la mémoire à long terme", time: "11:30", category: "Analyse", isCompleted: false },
    { id: "3", title: "Rappel : Réunion de calibrage de l'IA", time: "14:00", category: "Rappel", isCompleted: false },
    { id: "4", title: "Optimiser le cache audio et les modèles de synthèse vocale", time: "16:45", category: "Système", isCompleted: false }
  ];
  saveTasks(defaults);
  return defaults;
}

export function saveTasks(tasks: TaskItem[]): void {
  try {
    localStorage.setItem(STORAGE_KEYS.TASKS, JSON.stringify(tasks));
  } catch (e) {
    console.warn("Failed to save tasks:", e);
  }
}

export function loadToolLogs(): ToolLog[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.TOOL_LOGS);
    if (raw) return JSON.parse(raw);
  } catch (e) {
    console.warn("Failed to load tool logs:", e);
  }
  const defaults: ToolLog[] = [
    {
      id: "log-1",
      toolName: "system_status",
      inputParams: "{}",
      outputResult: "CPU 18%, RAM 38%, BATTERY 90%, UPLINK SECURE",
      timestamp: Date.now() - 900000,
      durationMs: 42,
      status: "SUCCESS"
    },
    {
      id: "log-2",
      toolName: "calculator",
      inputParams: "sqrt(1024) * 42",
      outputResult: "1344",
      timestamp: Date.now() - 600000,
      durationMs: 12,
      status: "SUCCESS"
    }
  ];
  saveToolLogs(defaults);
  return defaults;
}

export function saveToolLogs(logs: ToolLog[]): void {
  try {
    localStorage.setItem(STORAGE_KEYS.TOOL_LOGS, JSON.stringify(logs));
  } catch (e) {
    console.warn("Failed to save tool logs:", e);
  }
}

export function loadIncidents(): IncidentReport[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEYS.INCIDENTS);
    if (raw) return JSON.parse(raw);
  } catch (e) {
    console.warn("Failed to load incidents:", e);
  }
  return [];
}

export function saveIncidents(incidents: IncidentReport[]): void {
  try {
    localStorage.setItem(STORAGE_KEYS.INCIDENTS, JSON.stringify(incidents));
  } catch (e) {
    console.warn("Failed to save incidents:", e);
  }
}
