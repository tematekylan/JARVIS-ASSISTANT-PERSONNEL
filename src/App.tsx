import React, { useState, useEffect, useRef, useCallback } from 'react';
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
  NotificationItem 
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
  stopListening 
} from './utils/audio';
import { parseSlashCommand, executeTool } from './utils/tools';

// Components
import { FuturisticHeader } from './components/FuturisticHeader';
import { JarvisTopBar } from './components/JarvisTopBar';
import { JarvisDrawerContent } from './components/JarvisDrawerContent';
import { JarvisAuthDialog } from './components/JarvisAuthDialog';

// Screens
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
import { executeExternalAppCommand } from './utils/appCoordinator';

export const App: React.FC = () => {
  // Navigation & UI States
  const [currentScreen, setCurrentScreen] = useState<JarvisScreen>('HOME');
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [isAuthOpen, setIsAuthOpen] = useState(false);

  // Assistant & Audio States
  const [assistantState, setAssistantState] = useState<AssistantState>('IDLE');
  const [audioAmplitude, setAudioAmplitude] = useState(0.2);
  const [isListening, setIsListening] = useState(false);

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
    if (
      lowerContent.includes("hack ai demarre") ||
      lowerContent.includes("hack ai démarre") ||
      lowerContent.includes("mode hologramme")
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

    // Check External App Intent (YouTube, WhatsApp, Gmail, Messenger)
    try {
      const appResult = await executeExternalAppCommand(content, settings);
      if (appResult.handled) {
        const assistantMsg: Message = {
          id: `msg_${Date.now()}_a`,
          role: 'assistant',
          content: appResult.spokenResponse || appResult.feedback || `Liaison effectuée avec succès pour l'application ${appResult.appName}.`,
          timestamp: Date.now()
        };
        setConversations(prev => prev.map(c => 
          c.id === activeConversation.id 
            ? { ...c, messages: [...updatedMessages, assistantMsg], updatedAt: Date.now() }
            : c
        ));
        setAssistantState('SUCCESS');
        setTimeout(() => setAssistantState('IDLE'), 2000);
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
          assistantText = `[T-HACK LOCAL SIMULATION] Directive traitée pour ${settings.userName}. Tous les paramètres du réacteur Arc sont nominaux.`;
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
          onEnd: () => setAssistantState('IDLE')
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
  }, [activeConversation, settings, memories]);

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
      {/* Top Futuristic Stark Header */}
      <FuturisticHeader 
        settings={settings}
        assistantState={assistantState}
        onOpenSettings={() => setCurrentScreen('SETTINGS')}
        onOpenAmbient={() => setCurrentScreen('HOLOGRAPHIC_AOD')}
      />

      {/* Screen Top Bar with Breadcrumbs & Actions */}
      <JarvisTopBar 
        currentScreen={currentScreen}
        onNavigate={setCurrentScreen}
        onToggleDrawer={() => setIsDrawerOpen(prev => !prev)}
        onOpenAuth={() => setIsAuthOpen(true)}
        onNewSession={handleNewConversation}
        settings={settings}
      />

      {/* Main Screen Container */}
      <main className="flex-1 flex flex-col overflow-hidden relative">
        {currentScreen === 'HOME' && (
          <HomeScreen 
            assistantState={assistantState}
            audioAmplitude={audioAmplitude}
            onSendCommand={handleSendMessage}
            onToggleVoice={handleToggleVoice}
            isListening={isListening}
            onStopOutput={handleStopOutput}
            settings={settings}
            messages={activeConversation.messages}
            activeConversation={activeConversation}
            onNavigate={setCurrentScreen}
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
          />
        )}
      </main>

      {/* Navigation Drawer Sidebar */}
      <JarvisDrawerContent 
        isOpen={isDrawerOpen}
        onClose={() => setIsDrawerOpen(false)}
        currentScreen={currentScreen}
        onSelectScreen={setCurrentScreen}
        conversations={conversations}
        activeConversationId={activeConversationId}
        onSelectConversation={setActiveConversationId}
        onNewConversation={handleNewConversation}
        onTogglePinConversation={handleTogglePinConversation}
        onDeleteConversation={handleDeleteConversation}
        settings={settings}
      />

      {/* Authentication & Clearance Dialog */}
      <JarvisAuthDialog 
        isOpen={isAuthOpen}
        onClose={() => setIsAuthOpen(false)}
        settings={settings}
        onUpdateSettings={setSettings}
      />
    </div>
  );
};
