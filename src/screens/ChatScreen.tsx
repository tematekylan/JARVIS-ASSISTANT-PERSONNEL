import React, { useRef, useEffect, useState } from 'react';
import { 
  Send, 
  Mic, 
  MicOff, 
  Volume2, 
  VolumeX, 
  Copy, 
  Check, 
  Wrench, 
  Sparkles, 
  Bot, 
  User, 
  AlertTriangle,
  RefreshCw,
  Square,
  ExternalLink,
  Music,
  Globe,
  PhoneCall,
  Flashlight,
  Maximize2
} from 'lucide-react';
import { Message, AssistantState, UserSettings, ActionCard } from '../types';
import { speakText, stopSpeaking } from '../utils/audio';
import { launchSpotifySearch, launchGoogleSearch, launchWhatsAppChat, launchPhoneCall, toggleFlashlight } from '../utils/phoneControl';

interface ChatScreenProps {
  messages: Message[];
  assistantState: AssistantState;
  onSendMessage: (text: string) => void;
  onToggleVoice: () => void;
  isListening: boolean;
  onStopOutput: () => void;
  settings: UserSettings;
}

export const ChatScreen: React.FC<ChatScreenProps> = ({
  messages,
  assistantState,
  onSendMessage,
  onToggleVoice,
  isListening,
  onStopOutput,
  settings
}) => {
  const [inputText, setInputText] = useState("");
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const [speakingMsgId, setSpeakingMsgId] = useState<string | null>(null);
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, assistantState]);

  const handleSubmit = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!inputText.trim() || assistantState === 'THINKING' || assistantState === 'PROCESSING') return;
    onSendMessage(inputText.trim());
    setInputText("");
  };

  const handleCopy = (id: string, text: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => setCopiedId(null), 2000);
  };

  const handleSpeakMessage = (msg: Message) => {
    if (speakingMsgId === msg.id) {
      stopSpeaking();
      setSpeakingMsgId(null);
    } else {
      setSpeakingMsgId(msg.id);
      speakText(msg.content, {
        rate: settings.speechRate,
        pitch: settings.speechPitch,
        language: settings.voiceLanguage,
        onEnd: () => setSpeakingMsgId(null)
      });
    }
  };

  return (
    <div className="flex-1 flex flex-col h-full overflow-hidden bg-[#030609]">
      {/* Messages Scroll Area */}
      <div className="flex-1 overflow-y-auto p-3 sm:p-4 space-y-3.5">
        {messages.length === 0 ? (
          <div className="h-full flex flex-col items-center justify-center text-center p-6 space-y-3 select-none">
            <div className="w-12 h-12 rounded-full bg-[#0A1219] border border-[#00E5FF]/30 flex items-center justify-center text-[#00E5FF] shadow-[0_0_15px_rgba(0,229,255,0.15)]">
              <Sparkles className="w-5 h-5 text-[#00E5FF]" />
            </div>
            <h2 className="text-base font-bold font-['Chakra_Petch',sans-serif] text-[#E5FCFF] tracking-wider">
              T-HACK AI
            </h2>
            <p className="text-xs text-[#6F9DA6] max-w-xs font-mono">
              Prêt pour vos directives et requêtes.
            </p>
          </div>
        ) : (
          messages.map((msg) => {
            const isUser = msg.role === 'user';
            const isSpeaking = speakingMsgId === msg.id;

            return (
              <div
                key={msg.id}
                className={`flex gap-2.5 ${isUser ? 'justify-end' : 'justify-start'}`}
              >
                {!isUser && (
                  <div className="w-7 h-7 rounded bg-[#0A1219] border border-[#00E5FF]/60 flex items-center justify-center text-[#00E5FF] shrink-0 mt-0.5 shadow-[0_0_6px_rgba(0,229,255,0.3)]">
                    <Bot className="w-4 h-4" />
                  </div>
                )}

                <div className={`max-w-[85%] sm:max-w-[75%] space-y-1.5`}>
                  {/* Tool execution badge if present */}
                  {msg.toolName && (
                    <div className="p-2 rounded bg-[#070D12] border border-[#007C91]/50 text-xs font-mono">
                      <div className="flex items-center space-x-1.5 text-[#00E5FF] font-bold">
                        <Wrench className="w-3.5 h-3.5" />
                        <span>OUTIL EXÉCUTÉ : {msg.toolName}</span>
                      </div>
                      {msg.toolInput && (
                        <div className="text-[11px] text-[#6F9DA6] mt-0.5 truncate">
                          Entrée : {msg.toolInput}
                        </div>
                      )}
                      {msg.toolOutput && (
                        <div className="text-[11px] text-[#31F5A3] mt-0.5 bg-[#030609] p-1.5 rounded border border-[#007C91]/30 whitespace-pre-wrap">
                          {msg.toolOutput}
                        </div>
                      )}
                    </div>
                  )}

                  {/* Message Bubble */}
                  <div
                    className={`p-3 rounded text-sm leading-relaxed ${
                      isUser
                        ? 'bg-[#007C91]/30 border border-[#00E5FF]/50 text-[#E5FCFF] rounded-tr-none'
                        : msg.isError
                        ? 'bg-[#FF4660]/15 border border-[#FF4660]/50 text-[#FF8095] rounded-tl-none'
                        : 'bg-[#0A1219] border border-[#007C91]/40 text-[#E5FCFF] rounded-tl-none shadow-[0_0_10px_rgba(0,124,145,0.15)]'
                    }`}
                  >
                    <div className="whitespace-pre-wrap font-['Rajdhani',sans-serif]">
                      {msg.content}
                    </div>

                    {/* Footer with actions */}
                    <div className="flex items-center justify-between pt-1.5 mt-1 border-t border-[#007C91]/20 text-[10px] font-mono text-[#6F9DA6]">
                      <span>
                        {new Date(msg.timestamp).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}
                      </span>
                      <div className="flex items-center space-x-2">
                        {!isUser && (
                          <button
                            onClick={() => handleSpeakMessage(msg)}
                            className="hover:text-[#00E5FF] transition-colors p-0.5 cursor-pointer"
                            title={isSpeaking ? "Arrêter la lecture" : "Lire vocalement"}
                          >
                            {isSpeaking ? <VolumeX className="w-3.5 h-3.5 text-[#FF4660]" /> : <Volume2 className="w-3.5 h-3.5" />}
                          </button>
                        )}
                        <button
                          onClick={() => handleCopy(msg.id, msg.content)}
                          className="hover:text-[#00E5FF] transition-colors p-0.5 cursor-pointer"
                          title="Copier le texte"
                        >
                          {copiedId === msg.id ? <Check className="w-3.5 h-3.5 text-[#31F5A3]" /> : <Copy className="w-3.5 h-3.5" />}
                        </button>
                      </div>
                    </div>

                    {/* Rich ActionCard for Phone & External Apps */}
                    {msg.actionCard && (
                      <div className="mt-2.5 p-3 rounded bg-[#070D12] border border-[#00E5FF]/40 space-y-2">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center space-x-2">
                            {msg.actionCard.appName === 'Spotify' && <Music className="w-4 h-4 text-[#1DB954]" />}
                            {msg.actionCard.appName === 'Google' && <Globe className="w-4 h-4 text-[#4285F4]" />}
                            {msg.actionCard.appName === 'Phone' && <PhoneCall className="w-4 h-4 text-[#FFDE00]" />}
                            {msg.actionCard.appName === 'Flashlight' && <Flashlight className="w-4 h-4 text-[#FFDE00]" />}
                            {msg.actionCard.appName === 'System' && <Maximize2 className="w-4 h-4 text-[#00E5FF]" />}
                            <span className="text-xs font-mono font-bold text-[#E5FCFF]">{msg.actionCard.title}</span>
                          </div>
                          <span className="text-[10px] font-mono px-1.5 py-0.5 rounded bg-[#00E5FF]/15 text-[#00E5FF] uppercase">
                            {msg.actionCard.appName}
                          </span>
                        </div>

                        {msg.actionCard.extraDetails && (
                          <div className="text-[11px] font-mono text-[#6F9DA6]">
                            {msg.actionCard.extraDetails}
                          </div>
                        )}

                        <div className="pt-1 flex gap-2">
                          <button
                            onClick={() => {
                              if (msg.actionCard?.appName === 'Spotify' && msg.actionCard.query) {
                                launchSpotifySearch(msg.actionCard.query);
                              } else if (msg.actionCard?.appName === 'Google' && msg.actionCard.query) {
                                launchGoogleSearch(msg.actionCard.query);
                              } else if (msg.actionCard?.actionUrl.startsWith('tel:')) {
                                launchPhoneCall(msg.actionCard.actionUrl);
                              } else if (msg.actionCard?.actionUrl === '#flashlight') {
                                toggleFlashlight();
                              } else if (msg.actionCard?.actionUrl) {
                                window.open(msg.actionCard.actionUrl, '_blank', 'noopener,noreferrer');
                              }
                            }}
                            className="flex-1 py-1.5 px-3 rounded font-mono text-xs font-bold flex items-center justify-center space-x-1.5 transition-all cursor-pointer bg-[#00E5FF]/20 hover:bg-[#00E5FF]/30 border border-[#00E5FF] text-[#00E5FF] shadow-[0_0_10px_rgba(0,229,255,0.2)]"
                          >
                            <span>{msg.actionCard.buttonLabel || "Exécuter l'action"}</span>
                            <ExternalLink className="w-3.5 h-3.5" />
                          </button>
                        </div>
                      </div>
                    )}
                  </div>
                </div>

                {isUser && (
                  <div className="w-7 h-7 rounded bg-[#007C91]/40 border border-[#00E5FF]/40 flex items-center justify-center text-[#78F7FF] shrink-0 mt-0.5">
                    <User className="w-4 h-4" />
                  </div>
                )}
              </div>
            );
          })
        )}

        {/* Thinking / Streaming Indicator */}
        {(assistantState === 'THINKING' || assistantState === 'PROCESSING') && (
          <div className="flex items-center space-x-2.5 text-xs font-mono text-[#00E5FF] p-2 bg-[#0A1219]/60 border border-[#007C91]/30 rounded w-fit">
            <RefreshCw className="w-3.5 h-3.5 animate-spin" />
            <span>T-HACK traite la requête neuronale...</span>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Input Bar */}
      <div className="p-3 bg-[#070D12] border-t border-[#007C91]/40">
        <form onSubmit={handleSubmit} className="flex items-center gap-2">
          <div className="relative flex-1 flex items-center bg-[#0A1219] border border-[#007C91]/50 focus-within:border-[#00E5FF] rounded-sm transition-all shadow-[inset_0_0_8px_rgba(0,124,145,0.2)]">
            <span className="pl-3 text-xs font-mono text-[#00E5FF] font-bold select-none">&gt;</span>
            <input
              type="text"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder={isListening ? "Écoute active... parlez" : "Écrire un message ou directive..."}
              className="w-full py-2.5 px-2 bg-transparent text-[#E5FCFF] placeholder-[#6F9DA6]/60 text-sm font-['Rajdhani',sans-serif] focus:outline-none"
              disabled={assistantState === 'THINKING' || assistantState === 'PROCESSING'}
            />
            {assistantState === 'SPEAKING' && (
              <button
                type="button"
                onClick={onStopOutput}
                className="mr-2 px-2 py-1 rounded bg-[#FF4660]/20 border border-[#FF4660]/50 text-[#FF4660] text-[11px] font-mono flex items-center gap-1 hover:bg-[#FF4660]/30 transition-all cursor-pointer"
              >
                <Square className="w-3 h-3 fill-current" /> STOP
              </button>
            )}
          </div>

          <button
            type="button"
            onClick={onToggleVoice}
            className={`p-2.5 rounded-sm border transition-all cursor-pointer flex items-center justify-center ${
              isListening
                ? 'bg-[#FF4660] border-[#FF4660] text-white shadow-[0_0_15px_rgba(255,70,96,0.6)] animate-pulse'
                : 'bg-[#0A1219] border-[#007C91]/50 text-[#00E5FF] hover:border-[#00E5FF]'
            }`}
            title={isListening ? "Arrêter l'écoute" : "Activer l'écoute vocale"}
          >
            {isListening ? <MicOff className="w-4 h-4" /> : <Mic className="w-4 h-4" />}
          </button>

          <button
            type="submit"
            disabled={!inputText.trim() || assistantState === 'THINKING' || assistantState === 'PROCESSING'}
            className={`p-2.5 rounded-sm border transition-all flex items-center justify-center ${
              inputText.trim() && assistantState !== 'THINKING' && assistantState !== 'PROCESSING'
                ? 'bg-[#00E5FF] border-[#00E5FF] text-[#030609] hover:bg-[#78F7FF] shadow-[0_0_10px_rgba(0,229,255,0.4)] cursor-pointer'
                : 'bg-[#0A1219] border-[#007C91]/30 text-[#6F9DA6]/40 cursor-not-allowed'
            }`}
          >
            <Send className="w-4 h-4" />
          </button>
        </form>
      </div>
    </div>
  );
};
