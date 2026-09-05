export type AssistantState = 
  | 'IDLE' 
  | 'LISTENING' 
  | 'THINKING' 
  | 'PROCESSING' 
  | 'SPEAKING' 
  | 'SUCCESS' 
  | 'ERROR';

export type JarvisScreen = 
  | 'HOME' 
  | 'CHAT' 
  | 'SPLASH'
  | 'TASKS' 
  | 'TERMINAL' 
  | 'ACTIVITY' 
  | 'SYSTEM' 
  | 'COMMAND_CENTER' 
  | 'MEMORY' 
  | 'NOTES' 
  | 'SETTINGS' 
  | 'EXTERNAL_APPS'
  | 'NOTIFICATIONS'
  | 'HOLOGRAPHIC_AOD'
  | 'KOTLIN_STUDIO';

export interface UserContact {
  uid: string;
  displayName: string;
  email: string;
  photoUrl?: string;
  status?: string;
  isOnline?: boolean;
  lastSeen?: number;
  phone?: string;
}

export interface DirectMessage {
  id: string;
  chatId: string;
  senderId: string;
  senderName: string;
  receiverId: string;
  content: string;
  mediaUrl?: string;
  timestamp: number;
  isRead: boolean;
}

export interface DirectChat {
  id: string;
  participants: string[];
  participantNames: Record<string, string>;
  participantAvatars: Record<string, string>;
  lastMessage: string;
  lastMessageTime: number;
  unreadCount: number;
  createdAt: number;
}

export interface NotificationItem {
  id: string;
  source: 'whatsapp' | 'messenger' | 'gmail' | 'system' | 'youtube';
  sender: string;
  message: string;
  timestamp: number;
  isRead: boolean;
  actionUrl?: string;
  replySuggestion?: string;
}


export interface ActionCard {
  appName: 'Spotify' | 'Google' | 'WhatsApp' | 'YouTube' | 'Gmail' | 'Messenger' | 'Phone' | 'Flashlight' | 'System';
  title: string;
  actionUrl: string;
  nativeUrl?: string;
  query?: string;
  buttonLabel?: string;
  contactName?: string;
  contactPhone?: string;
  extraDetails?: string;
}

export interface ContactItem {
  id: string;
  name: string;
  phone: string;
  email?: string;
  category?: 'VIP' | 'Famille' | 'Travail' | 'Général';
  isFavorite?: boolean;
}

export interface Conversation {
  id: string;
  title: string;
  createdAt: number;
  updatedAt: number;
  isPinned?: boolean;
  summary?: string;
  messages: Message[];
}

export interface Message {
  id: string;
  conversationId?: string;
  role: 'user' | 'assistant' | 'system';
  content: string;
  timestamp: number;
  toolName?: string | null;
  toolInput?: string | null;
  toolOutput?: string | null;
  imageUri?: string | null;
  isError?: boolean;
  actionCard?: ActionCard;
}

export interface Memory {
  id: string;
  key: string;
  content: string;
  category: 'General' | 'Preferences' | 'Work' | 'Identity' | 'System';
  timestamp?: number;
  isActive: boolean;
  createdAt?: number;
}

export interface Note {
  id: string;
  title: string;
  content: string;
  tags: string;
  createdAt: number;
  updatedAt: number;
}

export interface ToolLog {
  id: string;
  toolName: string;
  inputParams: string;
  outputResult: string;
  timestamp: number;
  durationMs: number;
  status: 'SUCCESS' | 'ERROR';
}

export interface UserAccount {
  id: string;
  email: string;
  phone: string;
  displayName: string;
  authProvider: 'google' | 'email' | 'phone' | 'guest';
  clearanceLevel: string;
  avatarUrl: string;
  createdAt: number;
  lastLoginAt: number;
}

export interface IncidentReport {
  id: string;
  incidentCode: string;
  errorCode: string;
  errorMessage: string;
  stackTrace: string;
  contextInfo: string;
  timestamp: number;
  emailRecipient: string;
  emailSent: boolean;
  aiCouncilStatus: 'CONVENED' | 'ANALYZING' | 'RESOLVED';
  aiCouncilDeliberation: string;
  aiCouncilHotfixCode: string;
}

export type AuthValidationStatus = 'NON_AUTHENTIFIE' | 'EN_ATTENTE_VALIDATION' | 'VALIDE';

export interface UserSettings {
  id: number;
  userName: string;
  userEmail: string;
  userPhone: string;
  userAvatarUrl?: string;
  userStatus?: string;
  authProvider: 'google' | 'email' | 'phone' | 'guest';
  isLoggedIn: boolean;
  authValidationStatus?: AuthValidationStatus;
  securityClearanceLevel: string;
  assistantName: string;
  voiceLanguage: string; // 'fr' | 'en' | 'auto'
  ttsEnabled: boolean;
  autoSpeakResponses: boolean;
  speechRate: number;
  speechPitch: number;
  aiModel: string;
  temperature: number;
  memoryEnabled: boolean;
  isDemoMode: boolean;
  personalityTone: string;
  activeAiProvider: 'gemini' | 'openai' | 'claude' | 'groq' | 'deepseek';
  customGeminiApiKey: string;
  customOpenAiApiKey: string;
  customClaudeApiKey: string;
  customGroqApiKey: string;
  customDeepSeekApiKey: string;
  voicePersonaName: string;
  voicePersonaDescription: string;
  voicePersonaPitch: number;
  voicePersonaRate: number;
  voicePersonaPromptStyle: string;
  isVoicePersonaActive: boolean;
  developerAlertEmail: string;
  autoSendErrorAlerts: boolean;
  voiceAnnounceNotifications: boolean;
  wakeWordEnabled: boolean;
  defaultWhatsappNumber: string;
  screenWakeLockEnabled?: boolean;
  conversationalLoopEnabled?: boolean;
  autoWakeOnVoice?: boolean;
}

export interface TaskItem {
  id: string;
  title: string;
  time: string;
  category: string; // 'Système' | 'Analyse' | 'Rappel'
  isCompleted: boolean;
  priority?: 'HIGH' | 'MEDIUM' | 'LOW' | string;
  createdAt?: number;
}

export interface ToolDefinition {
  name: string;
  description: string;
  parameters: string;
  iconName: string;
}

export interface ToolExecutionResult {
  toolName: string;
  input: string;
  result: string;
  isSuccess: boolean;
  executionTimeMs: number;
}
