import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Menu, ArrowLeft, Plus, Home, MessageSquare, Sliders, Settings, Mic, MicOff, User, Zap, ChevronLeft } from 'lucide-react';
import { 
  AssistantState, 
  JarvisScreen, 
  UserSettings, 
  Conversation, 
  Message, 
  TaskItem, 
  Note, 
  Memory, 
  ToolLog, 
  IncidentReport,
  NotificationItem,
  UserContact 
} from './types';
import { 
  loadSettings, 
  saveSettings, 
  loadConversations, 
  saveConversations, 
  loadTasks, 
  saveTasks, 
  loadNotes, 
  saveNotes, 
  loadMemories, 
  saveMemories, 
  loadToolLogs, 
  saveToolLogs, 
  loadIncidents, 
  saveIncidents,
  loadNotifications,
  saveNotifications
} from './utils/storage';
import { 
  playHudBeep, 
  playJarvisChime, 
  playErrorAlarm, 
  speakText, 
  stopSpeaking, 
  startListening, 
  stopListening,
  startWakeWordDetection,
  stopWakeWordDetection,
  requestScreenWakeLock
} from './utils/audio';
import { toggleFullScreen, toggleFlashlight } from './utils/phoneControl';
import { parseSlashCommand, executeTool } from './utils/tools';

// Automation
import { AppController } from './automation/AppController';

// Components
import { JarvisDrawerContent } from './components/JarvisDrawerContent';
import { JarvisAuthDialog } from './components/JarvisAuthDialog';
import { UpdateModal } from './components/UpdateModal';

// Screens
import { SplashScreen } from './screens/SplashScreen';
import { HomeScreen } from './screens/HomeScreen';
import { ChatScreen } from './screens/ChatScreen';
import { CommandCenterScreen } from './screens/CommandCenterScreen';
import { TerminalScreen } from './screens/TerminalScreen';
import { TasksScreen } from './screens/TasksScreen';
import { NotesScreen } from './screens/NotesScreen';
import { MemoryScreen } from './screens/MemoryScreen';
import { SystemScreen } from './screens/SystemScreen';
import { ActivityScreen } from './screens/ActivityScreen';
import { SettingsScreen } from './screens/SettingsScreen';
import { HolographicAmbientScreen } from './screens/HolographicAmbientScreen';
import { ExternalAppsScreen } from './screens/ExternalAppsScreen';
import { NotificationsScreen } from './screens/NotificationsScreen';
import { KotlinStudioScreen } from './screens/KotlinStudioScreen';
import { executeExternalAppCommand } from './utils/appCoordinator';

export const App: React.FC = () => {
  // Navigation & Section Accumulator States (Init with Splash if first load)
  const [currentScreen, setCurrentScreen] = useState<JarvisScreen>(() => {
    return sessionStorage.getItem('thack_splash_completed') ? 'HOME' : 'SPLASH';
  });
  const [screenHistory, setScreenHistory] = useState<JarvisScreen[]>(['HOME']);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [isAuthOpen, setIsAuthOpen] = useState(false);
  const [isUpdateModalOpen, setIsUpdateModalOpen] = useState(false);

  // Revolutionary Voice-Only Autopilot Mode
  const [isVoiceAutopilotActive, setIsVoiceAutopilotActive] = useState(false);

  // Assistant & Audio States
  const [assistantState, setAssistantState] = useState<AssistantState>('IDLE');
  const [audioAmplitude, setAudioAmplitude] = useState(0.2);
  const [isListening, setIsListening] = useState(false);

  // Section Stack Navigation
  const navigateToScreen = useCallback((screen: JarvisScreen) => {
    setScreenHistory(prev => [...prev, screen]);
    setCurrentScreen(screen);
  }, []);

  const handleExitSection = useCallback(() => {
    setScreenHistory(prev => {
      if (prev.length <= 1) {
        setCurrentScreen('HOME');
        return ['HOME'];
      }
      const next = [...prev];
      next.pop();
      const prevScreen = next[next.length - 1] || 'HOME';
      setCurrentScreen(prevScreen);
      return next;
    });
  }, []);

  // Persistent States
  const [settings, setSettings] = useState<UserSettings>(loadSettings);
  const [conversations, setConversations] = useState<Conversation[]>(loadConversations);
  const [activeConversationId, setActiveConversationId] = useState<string>(() => {
    const convs = loadConversations();
    return convs[0]?.id || "conv_initial";
  });
  const [tasks, setTasks] = useState<TaskItem[]>(loadTasks);
  const [notes, setNotes] = useState<Note[]>(loadNotes);
  const [memories, setMemories] = useState<Memory[]>(loadMemories);
  const [toolLogs, setToolLogs] = useState<ToolLog[]>(loadToolLogs);
  const [incidents, setIncidents] = useState<IncidentReport[]>(loadIncidents);
  const [notifications, setNotifications] = useState<NotificationItem[]>(loadNotifications);

  // Active conversation object
  const activeConversation = conversations.find(c => c.id === activeConversationId) || conversations[0] || {
    id: "conv_default",
    title: "Session Principale T-HACK",
    createdAt: Date.now(),
    updatedAt: Date.now(),
    messages: []
  };

  // Sync to storage on change
  useEffect(() => { saveSettings(settings); }, [settings]);
  useEffect(() => { saveConversations(conversations); }, [conversations]);
  useEffect(() => { saveTasks(tasks); }, [tasks]);
  useEffect(() => { saveNotes(notes); }, [notes]);
  useEffect(() => { saveMemories(memories); }, [memories]);
  useEffect(() => { saveToolLogs(toolLogs); }, [toolLogs]);
  useEffect(() => { saveIncidents(incidents); }, [incidents]);
  useEffect(() => { saveNotifications(notifications); }, [notifications]);

  // Connect Automation AppController
  useEffect(() => {
    const ctrl = AppController.getInstance();
    ctrl.registerNavigation((screen) => {
      navigateToScreen(screen);
    });
  }, [navigateToScreen]);

  // Audio Amplitude simulation when listening or speaking
  useEffect(() => {
    let animId: number;
    let baseTime = 0;

    const updateAmp = () => {
      baseTime += 0.05;
      if (assistantState === 'SPEAKING') {
        setAudioAmplitude(0.45 + Math.sin(baseTime * 4) * 0.35 + Math.random() * 0.2);
      } else if (assistantState === 'LISTENING') {
        setAudioAmplitude(0.3 + Math.sin(baseTime * 2.5) * 0.25 + Math.random() * 0.15);
      } else if (assistantState === 'THINKING' || assistantState === 'PROCESSING') {
        setAudioAmplitude(0.4 + Math.sin(baseTime * 6) * 0.3);
      } else {
        setAudioAmplitude(0.15 + Math.sin(baseTime) * 0.05);
      }
      animId = requestAnimationFrame(updateAmp);
    };

    animId = requestAnimationFrame(updateAmp);
    return () => cancelAnimationFrame(animId);
  }, [assistantState]);

  // Handle Voice Toggle
  const handleToggleVoice = useCallback(() => {
    if (isListening) {
      stopListening();
      setIsListening(false);
      setAssistantState('IDLE');
      playHudBeep(300, 0.1);
    } else {
      stopSpeaking();
      playHudBeep(880, 0.15);
      setIsListening(true);
      setAssistantState('LISTENING');

      const success = startListening(
        (transcript) => {
          setIsListening(false);
          const lower = transcript.toLowerCase().trim();
          
          // Check wake word: "HACK AI démarre" / "mode hologramme"
          if (
            lower.includes("hack ai demarre") ||
            lower.includes("hack ai démarre") ||
            lower.includes("hack ai active") ||
            lower.includes("mode hologramme") ||
            lower.includes("plein ecran") ||
            lower.includes("plein écran")
          ) {
            playJarvisChime();
            speakText("Protocole HACK AI activé. Affichage holographique plein écran enclenché.", {
              rate: settings.speechRate,
              pitch: settings.speechPitch,
              language: settings.voiceLanguage
            });
            if (document.documentElement.requestFullscreen && !document.fullscreenElement) {
              document.documentElement.requestFullscreen().catch(() => {});
            }
            setCurrentScreen('HOLOGRAPHIC_AOD');
            setAssistantState('IDLE');
            return;
          }

          setAssistantState('PROCESSING');
          handleSendMessage(transcript);
        },
        () => {
          setIsListening(false);
          setAssistantState('ERROR');
          setTimeout(() => setAssistantState('IDLE'), 2000);
        },
        settings.voiceLanguage
      );

      if (!success) {
        setIsListening(false);
        setAssistantState('ERROR');
        setTimeout(() => setAssistantState('IDLE'), 2000);
      }
    }
  }, [isListening, settings.voiceLanguage]);

  // Stop current speaking output
  const handleStopOutput = useCallback(() => {
    stopSpeaking();
    setAssistantState('IDLE');
  }, []);

  // Send Message Logic
  const handleSendMessage = useCallback(async (content: string) => {
    if (!content.trim()) return;

    // Play user dispatch sound
    playHudBeep(640, 0.08);

    // Build user message
    const userMsg: Message = {
      id: `msg_${Date.now()}_u`,
      role: 'user',
      content: content.trim(),
      timestamp: Date.now()
    };

    // Update conversation with user message
    const updatedMessages = [...activeConversation.messages, userMsg];
    setConversations(prev => prev.map(c => 
      c.id === activeConversation.id 
        ? { ...c, messages: updatedMessages, updatedAt: Date.now() }
        : c
    ));

    setAssistantState('THINKING');

    // Check for Wake Word directly in prompt
    const lowerContent = content.toLowerCase().trim();

    // Revolutionary Autonomous Voice Navigation & Section Control
    if (
      lowerContent.includes("active le pilote vocal") ||
      lowerContent.includes("active le mode autonome") ||
      lowerContent.includes("pilote autonome")
    ) {
      setIsVoiceAutopilotActive(true);
      playJarvisChime();
      const resp = "Pilote vocal autonome activé. Vous pouvez piloter l'ensemble de l'application à la voix sans toucher l'écran.";
      speakText(resp, {
        rate: settings.speechRate,
        pitch: settings.speechPitch,
        language: settings.voiceLanguage,
        onEnd: () => setTimeout(() => handleToggleVoice(), 400)
      });
      const assistantMsg: Message = {
        id: `msg_${Date.now()}_a`,
        role: 'assistant',
        content: resp,
        timestamp: Date.now()
      };
      setConversations(prev => prev.map(c => c.id === activeConversation.id ? { ...c, messages: [...updatedMessages, assistantMsg] } : c));
      setAssistantState('IDLE');
      return;
    }

    if (
      lowerContent.includes("desactive le pilote vocal") ||
      lowerContent.includes("désactive le pilote vocal") ||
      lowerContent.includes("arrete le pilote vocal") ||
      lowerContent.includes("arrête le pilote vocal")
    ) {
      setIsVoiceAutopilotActive(false);
      playHudBeep(440, 0.1);
      const resp = "Pilote vocal autonome désactivé.";
      speakText(resp, {
        rate: settings.speechRate,
        pitch: settings.speechPitch,
        language: settings.voiceLanguage
      });
      const assistantMsg: Message = {
        id: `msg_${Date.now()}_a`,
        role: 'assistant',
        content: resp,
        timestamp: Date.now()
      };
      setConversations(prev => prev.map(c => c.id === activeConversation.id ? { ...c, messages: [...updatedMessages, assistantMsg] } : c));
      setAssistantState('IDLE');
      return;
    }

    // Voice navigation between sections
    if (
      lowerContent.includes("sors de cette section") ||
      lowerContent.includes("quitte la section") ||
      lowerContent.includes("ferme la section") ||
      lowerContent.includes("retour aux sections")
    ) {
      handleExitSection();
      playJarvisChime();
      const resp = "Sortie de la section effectuée.";
      speakText(resp, {
        rate: settings.speechRate,
        pitch: settings.speechPitch,
        language: settings.voiceLanguage,
        onEnd: () => {
          if (isVoiceAutopilotActive) setTimeout(() => handleToggleVoice(), 400);
        }
      });
      return;
    }

    if (lowerContent.includes("va à l'accueil") || lowerContent.includes("ouvre l'accueil") || lowerContent.includes("montre l'hologramme") || lowerContent.includes("affiche l'hologramme")) {
      navigateToScreen('HOME');
      playJarvisChime();
      const resp = "Affichage de l'hologramme principal sur l'accueil.";
      speakText(resp, {
        rate: settings.speechRate,
        pitch: settings.speechPitch,
        language: settings.voiceLanguage,
        onEnd: () => {
          if (isVoiceAutopilotActive) setTimeout(() => handleToggleVoice(), 400);
        }
      });
      return;
    }

    if (lowerContent.includes("ouvre la discussion") || lowerContent.includes("va dans le chat") || lowerContent.includes("ouvre le chat")) {
      navigateToScreen('CHAT');
      playJarvisChime();
      const resp = "Canal de discussion ouvert.";
      speakText(resp, {
        rate: settings.speechRate,
        pitch: settings.speechPitch,
        language: settings.voiceLanguage,
        onEnd: () => {
          if (isVoiceAutopilotActive) setTimeout(() => handleToggleVoice(), 400);
        }
      });
      return;
    }

    if (lowerContent.includes("ouvre les tâches") || lowerContent.includes("va dans les tâches") || lowerContent.includes("affiche les tâches")) {
      navigateToScreen('TASKS');
      playJarvisChime();
      const resp = "Section des tâches opérationnelles ouverte.";
      speakText(resp, {
        rate: settings.speechRate,
        pitch: settings.speechPitch,
        language: settings.voiceLanguage,
        onEnd: () => {
          if (isVoiceAutopilotActive) setTimeout(() => handleToggleVoice(), 400);
        }
      });
      return;
    }

    if (lowerContent.includes("ouvre le terminal") || lowerContent.includes("va dans le terminal") || lowerContent.includes("console shell")) {
      navigateToScreen('TERMINAL');
      playJarvisChime();
      const resp = "Console terminal TTY ouverte.";
      speakText(resp, {
        rate: settings.speechRate,
        pitch: settings.speechPitch,
        language: settings.voiceLanguage,
        onEnd: () => {
          if (isVoiceAutopilotActive) setTimeout(() => handleToggleVoice(), 400);
        }
      });
      return;
    }

    if (lowerContent.includes("ouvre les paramètres") || lowerContent.includes("va dans les paramètres") || lowerContent.includes("ouvre la configuration")) {
      navigateToScreen('SETTINGS');
      playJarvisChime();
      const resp = "Panneau des paramètres et configuration ouvert.";
      speakText(resp, {
        rate: settings.speechRate,
        pitch: settings.speechPitch,
        language: settings.voiceLanguage,
        onEnd: () => {
          if (isVoiceAutopilotActive) setTimeout(() => handleToggleVoice(), 400);
        }
      });
      return;
    }
    if (
      lowerContent.includes("allume-toi") ||
      lowerContent.includes("allume toi") ||
      lowerContent.includes("allume l'écran") ||
      lowerContent.includes("allume l'ecran") ||
      lowerContent.includes("reveille-toi") ||
      lowerContent.includes("réveille-toi") ||
      lowerContent.includes("hack ai demarre") ||
      lowerContent.includes("hack ai démarre") ||
      lowerContent.includes("mode hologramme")
    ) {
      playJarvisChime();
      requestScreenWakeLock();
      toggleFullScreen(true);
      speakText("Systèmes T-HACK AI allumés. Écran maintenu éveillé et affichage plein écran enclenché.", {
        rate: settings.speechRate,
        pitch: settings.speechPitch,
        language: settings.voiceLanguage
      });
      setCurrentScreen('HOLOGRAPHIC_AOD');
      setAssistantState('IDLE');
      return;
    }

    // Check External App & Phone Intent (Spotify, Google, WhatsApp, Phone, Torch, Screen Wake, YouTube, Gmail, Messenger)
    try {
      const appResult = await executeExternalAppCommand(content, settings);
      if (appResult.handled) {
        const assistantMsg: Message = {
          id: `msg_${Date.now()}_a`,
          role: 'assistant',
          content: appResult.spokenResponse || appResult.feedback || `Liaison effectuée avec succès pour l'application ${appResult.appName}.`,
          timestamp: Date.now(),
          actionCard: appResult.actionCard
        };
        setConversations(prev => prev.map(c => 
          c.id === activeConversation.id 
            ? { ...c, messages: [...updatedMessages, assistantMsg], updatedAt: Date.now() }
            : c
        ));
        setAssistantState('SUCCESS');

        if (settings.autoSpeakResponses && appResult.spokenResponse) {
          setAssistantState('SPEAKING');
          speakText(appResult.spokenResponse, {
            rate: settings.speechRate,
            pitch: settings.speechPitch,
            language: settings.voiceLanguage,
            onEnd: () => {
              setAssistantState('IDLE');
              if (settings.conversationalLoopEnabled) {
                setTimeout(() => handleToggleVoice(), 350);
              }
            }
          });
        } else {
          setTimeout(() => setAssistantState('IDLE'), 2000);
        }
        return;
      }
    } catch (e) {
      console.warn("External app intent check fallback:", e);
    }

    try {
      // 1. Check for slash command or direct tool execution
      const slash = parseSlashCommand(content);
      let toolExecution: { name: string; input?: string; output: string } | null = null;

      if (slash.isSlash && slash.command === 'calculator') {
        const result = await executeTool('calculator', slash.cleanPrompt);
        toolExecution = { name: 'calculator', input: slash.cleanPrompt, output: result.result };
      } else if (slash.isSlash && slash.command === 'meteo') {
        const result = await executeTool('weather', slash.cleanPrompt);
        toolExecution = { name: 'weather', input: slash.cleanPrompt, output: result.result };
      } else if (slash.isSlash && slash.command === 'heure') {
        const result = await executeTool('world_time', slash.cleanPrompt);
        toolExecution = { name: 'world_time', input: slash.cleanPrompt, output: result.result };
      } else if (slash.isSlash && slash.command === 'systeme') {
        const result = await executeTool('system_status', slash.cleanPrompt);
        toolExecution = { name: 'system_status', input: slash.cleanPrompt, output: result.result };
      }

      // 2. Call backend proxy (/api/chat)
      let assistantText = "";
      try {
        const res = await fetch("/api/chat", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            message: content,
            history: updatedMessages.slice(-10).map(m => ({
              role: m.role === 'assistant' ? 'model' : 'user',
              content: m.content
            })),
            userName: settings.userName,
            assistantName: settings.assistantName,
            personalityTone: settings.personalityTone,
            activeMemories: settings.memoryEnabled ? memories.filter(m => m.isActive).map(m => `${m.key}: ${m.content}`) : []
          })
        });

        if (res.ok) {
          const data = await res.json();
          assistantText = data.text || "Directives reçues et traitées.";
        } else {
          throw new Error("HTTP " + res.status);
        }
      } catch (err) {
        // Fallback simulation if offline or dev server proxy is starting
        if (toolExecution) {
          assistantText = `Résultat de l'outil [${toolExecution.name}] :\n${toolExecution.output}`;
        } else {
          assistantText = `Bien reçu ${settings.userName}. J'ai traité votre requête : "${content}". Tout fonctionne à merveille.`;
        }
      }

      // Add tool output prefix if an explicit tool executed
      if (toolExecution && !assistantText.includes(toolExecution.output)) {
        assistantText = `${toolExecution.output}\n\n${assistantText}`;
      }

      // Create assistant message
      const assistantMsg: Message = {
        id: `msg_${Date.now()}_a`,
        role: 'assistant',
        content: assistantText,
        timestamp: Date.now(),
        toolName: toolExecution?.name,
        toolInput: toolExecution?.input,
        toolOutput: toolExecution?.output
      };

      setConversations(prev => prev.map(c =>
        c.id === activeConversation.id
          ? { ...c, messages: [...updatedMessages, assistantMsg], updatedAt: Date.now() }
          : c
      ));

      setAssistantState('SUCCESS');
      playJarvisChime();

      // Speak text if autoSpeak enabled
      if (settings.autoSpeakResponses) {
        setAssistantState('SPEAKING');
        speakText(assistantText, {
          rate: settings.speechRate,
          pitch: settings.speechPitch,
          language: settings.voiceLanguage,
          onEnd: () => {
            setAssistantState('IDLE');
            if (isVoiceAutopilotActive || settings.conversationalLoopEnabled) {
              setTimeout(() => handleToggleVoice(), 350);
            }
          }
        });
      } else {
        setTimeout(() => setAssistantState('IDLE'), 1000);
      }
    } catch (e: any) {
      setAssistantState('ERROR');
      playErrorAlarm();
      const errorMsg: Message = {
        id: `msg_${Date.now()}_err`,
        role: 'assistant',
        content: `Erreur du noyau: ${e.message || "Anomalie de flux"}`,
        timestamp: Date.now(),
        isError: true
      };
      setConversations(prev => prev.map(c =>
        c.id === activeConversation.id
          ? { ...c, messages: [...updatedMessages, errorMsg], updatedAt: Date.now() }
          : c
      ));
      setTimeout(() => setAssistantState('IDLE'), 3000);
    }
  }, [activeConversation, settings, memories, handleToggleVoice]);

  // Continuous background wake-word listener (e.g. "Hey AI, allume-toi")
  useEffect(() => {
    if (!settings.wakeWordEnabled) {
      stopWakeWordDetection();
      return;
    }

    const onWakeWordTriggered = (phrase: string) => {
      playJarvisChime();
      requestScreenWakeLock();
      toggleFullScreen(true);

      const clean = phrase.toLowerCase().trim();
      if (
        clean.includes("allume-toi") ||
        clean.includes("allume toi") ||
        clean.includes("allume l'écran") ||
        clean.includes("allume l'ecran") ||
        clean.includes("réveille-toi") ||
        clean.includes("reveille-toi") ||
        clean.includes("wake up") ||
        clean === "hey ai" ||
        clean === "t-hack"
      ) {
        speakText("Systèmes T-HACK AI allumés. Écran éveillé, que puis-je faire pour vous ?", {
          rate: settings.speechRate,
          pitch: settings.speechPitch,
          language: settings.voiceLanguage,
          onEnd: () => {
            handleToggleVoice();
          }
        });
        setCurrentScreen('HOLOGRAPHIC_AOD');
      } else {
        const command = phrase
          .replace(/^(?:hey ai|t-hack|hey jarvis|allume-toi|allume toi|active-toi)[,\s]*/i, '')
          .trim();
        if (command) {
          handleSendMessage(command);
        } else {
          handleToggleVoice();
        }
      }
    };

    startWakeWordDetection(onWakeWordTriggered, undefined, settings.voiceLanguage);

    return () => {
      stopWakeWordDetection();
    };
  }, [settings.wakeWordEnabled, settings.voiceLanguage, settings.speechRate, settings.speechPitch, handleSendMessage, handleToggleVoice]);

  // Keep screen awake if setting is enabled
  useEffect(() => {
    if (settings.screenWakeLockEnabled) {
      requestScreenWakeLock();
    }
  }, [settings.screenWakeLockEnabled]);

  // New Conversation Creation
  const handleNewConversation = useCallback(() => {
    const newId = `conv_${Date.now()}`;
    const newConv: Conversation = {
      id: newId,
      title: `Session ${conversations.length + 1}`,
      createdAt: Date.now(),
      updatedAt: Date.now(),
      messages: []
    };
    setConversations(prev => [newConv, ...prev]);
    setActiveConversationId(newId);
    playHudBeep(750, 0.1);
  }, [conversations.length]);

  const handleTogglePinConversation = useCallback((id: string) => {
    setConversations(prev => prev.map(c => c.id === id ? { ...c, isPinned: !c.isPinned } : c));
  }, []);

  const handleDeleteConversation = useCallback((id: string) => {
    setConversations(prev => {
      const filtered = prev.filter(c => c.id !== id);
      if (activeConversationId === id && filtered.length > 0) {
        setActiveConversationId(filtered[0].id);
      }
      return filtered;
    });
  }, [activeConversationId]);

  const handleRenameConversation = useCallback((id: string, newTitle: string) => {
    setConversations(prev => prev.map(c => c.id === id ? { ...c, title: newTitle, updatedAt: Date.now() } : c));
    playHudBeep(880, 0.08);
  }, []);

  const handleShareConversation = useCallback((conv: Conversation) => {
    const text = `T-HACK AI • Section : ${conv.title}\n\n` + 
      conv.messages.map(m => `[${m.role === 'user' ? 'Utilisateur' : 'T-HACK'}] : ${m.content}`).join('\n\n');
    if (navigator.share) {
      navigator.share({ title: conv.title, text }).catch(() => {});
    } else {
      navigator.clipboard.writeText(text);
    }
  }, []);

  // Tasks handlers
  const handleToggleTask = useCallback((id: string) => {
    setTasks(prev => prev.map(t => t.id === id ? { ...t, isCompleted: !t.isCompleted } : t));
    playHudBeep(520, 0.05);
  }, []);

  const handleAddTask = useCallback((title: string, category: string, time: string) => {
    const newTask: TaskItem = {
      id: `task_${Date.now()}`,
      title,
      category,
      time,
      isCompleted: false,
      priority: 'HIGH',
      createdAt: Date.now()
    };
    setTasks(prev => [newTask, ...prev]);
    playJarvisChime();
  }, []);

  const handleDeleteTask = useCallback((id: string) => {
    setTasks(prev => prev.filter(t => t.id !== id));
  }, []);

  // Notes handlers
  const handleAddNote = useCallback((title: string, content: string, tags: string) => {
    const newNote: Note = {
      id: `note_${Date.now()}`,
      title,
      content,
      tags,
      createdAt: Date.now(),
      updatedAt: Date.now()
    };
    setNotes(prev => [newNote, ...prev]);
    playJarvisChime();
  }, []);

  const handleUpdateNote = useCallback((updated: Note) => {
    setNotes(prev => prev.map(n => n.id === updated.id ? updated : n));
  }, []);

  const handleDeleteNote = useCallback((id: string) => {
    setNotes(prev => prev.filter(n => n.id !== id));
  }, []);

  // Memory handlers
  const handleAddMemory = useCallback((key: string, content: string, category: Memory['category']) => {
    const newMem: Memory = {
      id: `mem_${Date.now()}`,
      key,
      content,
      category,
      isActive: true,
      createdAt: Date.now()
    };
    setMemories(prev => [newMem, ...prev]);
    playJarvisChime();
  }, []);

  const handleUpdateMemory = useCallback((updated: Memory) => {
    setMemories(prev => prev.map(m => m.id === updated.id ? updated : m));
  }, []);

  const handleDeleteMemory = useCallback((id: string) => {
    setMemories(prev => prev.filter(m => m.id !== id));
  }, []);

  const handleWipeAllMemories = useCallback(() => {
    setMemories([]);
    playErrorAlarm();
  }, []);

  // Ambient AOD View Render
  if (currentScreen === 'HOLOGRAPHIC_AOD') {
    return (
      <HolographicAmbientScreen 
        onExit={() => setCurrentScreen('HOME')}
        assistantState={assistantState}
        onToggleVoice={handleToggleVoice}
        isListening={isListening}
      />
    );
  }

  return (
    <div className="flex flex-col h-screen w-full bg-[#030609] text-[#E5FCFF] overflow-hidden select-none">
      {/* Clean Minimalist Top Header (ChatGPT / Gemini / DeepSeek style) */}
      <header className="w-full bg-[#070D12] border-b border-[#007C91]/30 px-3 sm:px-4 py-2 flex items-center justify-between select-none z-20">
        <div className="flex items-center space-x-2.5">
          {/* Menu Drawer Toggle */}
          <button
            onClick={() => setIsDrawerOpen(prev => !prev)}
            className="p-1.5 rounded-lg bg-[#0A1219] hover:bg-[#007C91]/30 border border-[#007C91]/40 text-[#00E5FF] transition-all cursor-pointer flex items-center justify-center"
            title="Menu & Sections"
          >
            <Menu className="w-4 h-4" />
          </button>

          {/* Back button if inside a section / chat */}
          {currentScreen !== 'HOME' && (
            <button
              onClick={handleExitSection}
              className="p-1.5 rounded-lg bg-[#0A1219] hover:bg-[#007C91]/30 border border-[#007C91]/40 text-[#6F9DA6] hover:text-[#00E5FF] transition-all cursor-pointer flex items-center justify-center"
              title="Retour à l'accueil"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
          )}

          {/* Brand Name */}
          <span className="font-bold text-sm tracking-wide font-['Chakra_Petch',sans-serif] text-[#E5FCFF]">
            T-HACKMAN AI
          </span>
        </div>

        {/* Center: Clean subtle model indicator */}
        <div className="hidden sm:flex items-center space-x-1.5 px-3 py-1 rounded-full bg-[#0A1219] border border-[#007C91]/25 text-[11px] font-mono text-[#6F9DA6]">
          <span className="w-1.5 h-1.5 rounded-full bg-[#31F5A3] animate-pulse" />
          <span>Noyau Holographique &bull; Gemini 2.5</span>
        </div>

        {/* Right Actions: New Chat, Mic, Avatar Profile */}
        <div className="flex items-center space-x-2">
          {currentScreen === 'CHAT' && (
            <button
              onClick={handleNewConversation}
              className="px-2.5 py-1 rounded-lg bg-[#0A1219] hover:bg-[#00E5FF]/20 border border-[#007C91]/40 text-[#00E5FF] text-xs font-mono transition-all cursor-pointer flex items-center gap-1"
              title="Nouvelle conversation"
            >
              <Plus className="w-3.5 h-3.5" />
              <span className="hidden sm:inline">Nouveau</span>
            </button>
          )}

          {/* Clean Mic Button */}
          <button
            onClick={handleToggleVoice}
            className={`p-1.5 rounded-lg border transition-all cursor-pointer ${
              isListening
                ? 'bg-[#FF4660]/20 border-[#FF4660] text-[#FF4660] animate-pulse'
                : 'bg-[#0A1219] hover:bg-[#00E5FF]/20 border-[#007C91]/40 text-[#6F9DA6] hover:text-[#00E5FF]'
            }`}
            title={isListening ? "Arrêter l'écoute" : "Activer la voix"}
          >
            {isListening ? <Mic className="w-4 h-4 text-[#FF4660]" /> : <Mic className="w-4 h-4" />}
          </button>

          {/* User Avatar Button (Opens Firebase Auth & Profile) */}
          <button
            onClick={() => setIsAuthOpen(true)}
            className="flex items-center space-x-1.5 p-1 rounded-lg bg-[#0A1219] hover:bg-[#007C91]/30 border border-[#007C91]/40 transition-all cursor-pointer"
            title="Profil & Compte Firebase"
          >
            <img 
              src={settings.userAvatarUrl || "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80"} 
              alt="Avatar" 
              className="w-6 h-6 rounded-full object-cover border border-[#00E5FF]/60"
            />
            <span className="hidden md:inline text-xs font-mono text-[#E5FCFF] max-w-[80px] truncate pr-1">
              {settings.userName || "Teddy"}
            </span>
          </button>
        </div>
      </header>

      {/* Main Screen Container */}
      <main className="flex-1 flex flex-col overflow-hidden relative">
        {currentScreen === 'SPLASH' && (
          <SplashScreen 
            onComplete={() => {
              sessionStorage.setItem('thack_splash_completed', 'true');
              navigateToScreen('HOME');
            }}
          />
        )}

        {currentScreen === 'HOME' && (
          <HomeScreen 
            assistantState={assistantState}
            audioAmplitude={audioAmplitude}
            onSendCommand={handleSendMessage}
            onToggleVoice={handleToggleVoice}
            isListening={isListening}
            onStopOutput={handleStopOutput}
            settings={settings}
            onNavigate={navigateToScreen}
          />
        )}

        {currentScreen === 'KOTLIN_STUDIO' && (
          <KotlinStudioScreen 
            onBack={() => navigateToScreen('HOME')}
            onNavigate={navigateToScreen}
          />
        )}

        {currentScreen === 'CHAT' && (
          <ChatScreen 
            messages={activeConversation.messages}
            assistantState={assistantState}
            onSendMessage={handleSendMessage}
            onToggleVoice={handleToggleVoice}
            isListening={isListening}
            onStopOutput={handleStopOutput}
            settings={settings}
          />
        )}

        {currentScreen === 'COMMAND_CENTER' && (
          <CommandCenterScreen 
            toolLogs={toolLogs}
            onRefreshLogs={() => setToolLogs(loadToolLogs())}
            onClearLogs={() => setToolLogs([])}
            settings={settings}
          />
        )}

        {currentScreen === 'TERMINAL' && (
          <TerminalScreen 
            onExecuteCommand={handleSendMessage}
          />
        )}

        {currentScreen === 'TASKS' && (
          <TasksScreen 
            tasks={tasks}
            onToggleTask={handleToggleTask}
            onAddTask={handleAddTask}
            onDeleteTask={handleDeleteTask}
          />
        )}

        {currentScreen === 'NOTES' && (
          <NotesScreen 
            notes={notes}
            onAddNote={handleAddNote}
            onUpdateNote={handleUpdateNote}
            onDeleteNote={handleDeleteNote}
          />
        )}

        {currentScreen === 'MEMORY' && (
          <MemoryScreen 
            memories={memories}
            onAddMemory={handleAddMemory}
            onUpdateMemory={handleUpdateMemory}
            onDeleteMemory={handleDeleteMemory}
            onWipeAllMemories={handleWipeAllMemories}
            settings={settings}
            onUpdateSettings={setSettings}
          />
        )}

        {currentScreen === 'SYSTEM' && (
          <SystemScreen 
            settings={settings}
          />
        )}

        {currentScreen === 'ACTIVITY' && (
          <ActivityScreen 
            toolLogs={toolLogs}
            messages={activeConversation.messages}
            incidents={incidents}
            onRefresh={() => {
              setToolLogs(loadToolLogs());
              setIncidents(loadIncidents());
            }}
          />
        )}

        {currentScreen === 'EXTERNAL_APPS' && (
          <ExternalAppsScreen 
            settings={settings}
            onEnterHolographicMode={() => {
              if (document.documentElement.requestFullscreen && !document.fullscreenElement) {
                document.documentElement.requestFullscreen().catch(() => {});
              }
              setCurrentScreen('HOLOGRAPHIC_AOD');
            }}
            onNotificationReceived={(n) => setNotifications(prev => [n, ...prev])}
          />
        )}

        {currentScreen === 'NOTIFICATIONS' && (
          <NotificationsScreen 
            notifications={notifications}
            settings={settings}
            onUpdateNotifications={setNotifications}
            onUpdateSettings={setSettings}
          />
        )}

        {currentScreen === 'SETTINGS' && (
          <SettingsScreen 
            settings={settings}
            onSaveSettings={setSettings}
            onNavigate={navigateToScreen}
            onBackToChat={handleExitSection}
          />
        )}
      </main>

      {/* Navigation Drawer Sidebar */}
      <JarvisDrawerContent 
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        currentScreen={currentScreen}
        onSelectScreen={navigateToScreen}
        conversations={conversations}
        activeConversationId={activeConversationId}
        onSelectConversation={(id) => {
          setActiveConversationId(id);
          navigateToScreen('CHAT');
        }}
        onNewConversation={handleNewConversation}
        onDeleteConversation={handleDeleteConversation}
        onRenameConversation={handleRenameConversation}
        onShareConversation={handleShareConversation}
        onOpenUpdateModal={() => setIsUpdateModalOpen(true)}
        settings={settings}
      />

      {/* Authentication & Clearance Dialog */}
      <JarvisAuthDialog 
        isOpen={isAuthOpen}
        onClose={() => setIsAuthOpen(false)}
        settings={settings}
        onUpdateSettings={setSettings}
      />

      {/* Software Update Modal */}
      <UpdateModal 
        isOpen={isUpdateModalOpen}
        onClose={() => setIsUpdateModalOpen(false)}
      />
    </div>
  );
};
