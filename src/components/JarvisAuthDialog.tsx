import React, { useState } from 'react';
import { X, ShieldCheck, Mail, KeyRound, CheckCircle2, RefreshCw, Send, ArrowRight, Lock, User, Sparkles } from 'lucide-react';
import { UserSettings } from '../types';
import { playHudBeep, playJarvisChime, playErrorAlarm, speakText } from '../utils/audio';

interface JarvisAuthDialogProps {
  isOpen: boolean;
  onClose: () => void;
  settings: UserSettings;
  onUpdateSettings: (newSettings: UserSettings) => void;
}

export const JarvisAuthDialog: React.FC<JarvisAuthDialogProps> = ({
  isOpen,
  onClose,
  settings,
  onUpdateSettings
}) => {
  const [authMode, setAuthMode] = useState<'register' | 'login'>('register');
  const [step, setStep] = useState<'form' | 'verify' | 'success'>('form');
  
  const [name, setName] = useState(settings.userName || "Teddy");
  const [email, setEmail] = useState(settings.userEmail || "temateteddy@gmail.com");
  const [password, setPassword] = useState("");
  
  const [verificationCode, setVerificationCode] = useState("");
  const [sentCodePreview, setSentCodePreview] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [successInfo, setSuccessInfo] = useState<{ token: string; email: string } | null>(null);

  if (!isOpen) return null;

  const handleSendConfirmationCode = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage("");

    if (!email || !email.includes("@")) {
      setErrorMessage("Veuillez saisir une adresse email valide.");
      playErrorAlarm();
      return;
    }

    setIsLoading(true);
    playHudBeep(880, 0.1);

    try {
      const res = await fetch("/api/auth/send-code", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: email.trim(),
          name: name.trim() || "Commandant",
          mode: authMode
        })
      });

      const data = await res.json();
      if (!res.ok || !data.success) {
        throw new Error(data.error || "Échec de l'envoi du message de confirmation");
      }

      setSentCodePreview(data.code);
      setStep('verify');
      playJarvisChime();
      speakText(`Message de confirmation et code de sécurité transmis à ${email.split('@')[0]}.`, {
        rate: settings.speechRate,
        pitch: settings.speechPitch,
        language: settings.voiceLanguage
      });
    } catch (err: any) {
      setErrorMessage(err.message || "Erreur réseau lors de l'envoi.");
      playErrorAlarm();
    } finally {
      setIsLoading(false);
    }
  };

  const handleVerifyCode = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage("");

    if (!verificationCode || verificationCode.trim().length < 4) {
      setErrorMessage("Veuillez saisir le code de confirmation complet à 6 chiffres.");
      playErrorAlarm();
      return;
    }

    setIsLoading(true);

    try {
      const res = await fetch("/api/auth/verify-code", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: email.trim(),
          code: verificationCode.trim(),
          name: name.trim() || "Teddy"
        })
      });

      const data = await res.json();
      if (!res.ok || !data.success) {
        throw new Error(data.error || "Code invalide");
      }

      // Successful verification
      setSuccessInfo({
        token: data.token,
        email: data.user.email
      });
      setStep('success');
      playJarvisChime();

      onUpdateSettings({
        ...settings,
        userName: data.user.name || name,
        userEmail: data.user.email || email,
        isLoggedIn: true,
        authProvider: 'email',
        securityClearanceLevel: "LEVEL 5 (COMMANDER)"
      });

      speakText(`Accréditation accordée avec succès. Bienvenue, Commandant ${name}. Tous les protocoles T-HACK sont déverrouillés.`, {
        rate: settings.speechRate,
        pitch: settings.speechPitch,
        language: settings.voiceLanguage
      });
    } catch (err: any) {
      setErrorMessage(err.message || "Code incorrect.");
      playErrorAlarm();
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = () => {
    onUpdateSettings({
      ...settings,
      userName: "Sir",
      userEmail: "",
      isLoggedIn: false,
      securityClearanceLevel: "LEVEL 1 (RESTRICTED)"
    });
    setStep('form');
    setVerificationCode("");
    setSentCodePreview(null);
    playHudBeep(440, 0.1);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-4 animate-in fade-in duration-200">
      <div className="relative w-full max-w-lg bg-[#070D12] border border-[#00E5FF]/40 rounded-sm p-6 hud-panel-corner shadow-[0_0_40px_rgba(0,229,255,0.25)]">
        
        {/* Close Button */}
        <button 
          onClick={onClose}
          className="absolute top-4 right-4 text-[#6F9DA6] hover:text-[#00E5FF] p-1 transition-colors cursor-pointer"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Dialog Header */}
        <div className="flex items-center space-x-3 pb-4 border-b border-[#007C91]/40">
          <div className="p-2 bg-[#00E5FF]/10 border border-[#00E5FF]/40 rounded">
            <ShieldCheck className="w-6 h-6 text-[#00E5FF]" />
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <h3 className="text-lg font-bold font-['Chakra_Petch',sans-serif] tracking-wider text-[#E5FCFF]">
                PORTAIL D'AUTHENTIFICATION T-HACK CORE
              </h3>
            </div>
            <p className="text-xs font-mono text-[#6F9DA6]">
              Système de vérification d'identité & émission de jetons sécurisés
            </p>
          </div>
        </div>

        {/* Session Status Banner */}
        {settings.isLoggedIn && step !== 'success' && (
          <div className="my-4 p-3 bg-[#0A1A1E] border border-[#31F5A3]/50 rounded flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <span className="w-2 h-2 rounded-full bg-[#31F5A3] animate-pulse"></span>
              <div>
                <div className="text-xs font-mono text-[#31F5A3] font-bold">SESSION COMMANDANT ACTIVE</div>
                <div className="text-sm font-semibold text-[#E5FCFF]">{settings.userName} ({settings.userEmail || "Connecté"})</div>
                <div className="text-[10px] font-mono text-[#6F9DA6]">{settings.securityClearanceLevel}</div>
              </div>
            </div>
            <button
              onClick={handleLogout}
              className="px-3 py-1.5 text-xs font-mono rounded bg-[#FF4660]/20 border border-[#FF4660]/50 text-[#FF4660] hover:bg-[#FF4660]/30 transition-all cursor-pointer"
            >
              Déconnexion
            </button>
          </div>
        )}

        {/* STEP 1: FORM (Inscription ou Connexion) */}
        {step === 'form' && (
          <div>
            {/* Mode Switcher */}
            <div className="grid grid-cols-2 gap-2 my-4 bg-[#0A1219] p-1 rounded border border-[#007C91]/40">
              <button
                type="button"
                onClick={() => { setAuthMode('register'); setErrorMessage(""); }}
                className={`py-2 text-xs font-mono font-bold tracking-wider rounded transition-all cursor-pointer ${
                  authMode === 'register' 
                    ? 'bg-[#00E5FF] text-[#030609] shadow-[0_0_10px_rgba(0,229,255,0.4)]' 
                    : 'text-[#6F9DA6] hover:text-[#E5FCFF]'
                }`}
              >
                INSCRIPTION (CRÉER UN COMPTE)
              </button>
              <button
                type="button"
                onClick={() => { setAuthMode('login'); setErrorMessage(""); }}
                className={`py-2 text-xs font-mono font-bold tracking-wider rounded transition-all cursor-pointer ${
                  authMode === 'login' 
                    ? 'bg-[#00E5FF] text-[#030609] shadow-[0_0_10px_rgba(0,229,255,0.4)]' 
                    : 'text-[#6F9DA6] hover:text-[#E5FCFF]'
                }`}
              >
                CONNEXION
              </button>
            </div>

            <form onSubmit={handleSendConfirmationCode} className="space-y-4">
              {authMode === 'register' && (
                <div>
                  <label className="block text-xs font-mono text-[#6F9DA6] mb-1 flex items-center space-x-1.5">
                    <User className="w-3.5 h-3.5 text-[#00E5FF]" />
                    <span>Identité / Nom complet :</span>
                  </label>
                  <input
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="Ex: Teddy Hackman"
                    required
                    className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-3 py-2 text-sm text-[#E5FCFF] placeholder-[#41626A] focus:outline-none focus:border-[#00E5FF]"
                  />
                </div>
              )}

              <div>
                <label className="block text-xs font-mono text-[#6F9DA6] mb-1 flex items-center space-x-1.5">
                  <Mail className="w-3.5 h-3.5 text-[#00E5FF]" />
                  <span>Adresse Email (envoi du message de confirmation) :</span>
                </label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="Ex: temateteddy@gmail.com"
                  required
                  className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-3 py-2 text-sm text-[#E5FCFF] placeholder-[#41626A] focus:outline-none focus:border-[#00E5FF]"
                />
              </div>

              <div>
                <label className="block text-xs font-mono text-[#6F9DA6] mb-1 flex items-center space-x-1.5">
                  <Lock className="w-3.5 h-3.5 text-[#00E5FF]" />
                  <span>Mot de passe de sécurité :</span>
                </label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••••••"
                  required
                  className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-3 py-2 text-sm text-[#E5FCFF] placeholder-[#41626A] focus:outline-none focus:border-[#00E5FF]"
                />
              </div>

              {errorMessage && (
                <div className="p-3 bg-[#FF4660]/10 border border-[#FF4660]/40 rounded text-xs text-[#FF4660] font-mono">
                  {errorMessage}
                </div>
              )}

              <button
                type="submit"
                disabled={isLoading}
                className="w-full py-3 bg-[#00E5FF] hover:bg-[#33EAFF] text-[#030609] font-mono font-bold text-sm tracking-wider rounded transition-all shadow-[0_0_15px_rgba(0,229,255,0.4)] flex items-center justify-center space-x-2 cursor-pointer disabled:opacity-50"
              >
                {isLoading ? (
                  <>
                    <RefreshCw className="w-4 h-4 animate-spin" />
                    <span>ENVOI DU MESSAGE EN COURS...</span>
                  </>
                ) : (
                  <>
                    <Send className="w-4 h-4" />
                    <span>
                      {authMode === 'register' 
                        ? "ENVOYER LE CODE DE CONFIRMATION (INSCRIPTION)" 
                        : "ENVOYER LE CODE DE SÉCURITÉ (CONNEXION)"}
                    </span>
                  </>
                )}
              </button>
            </form>
          </div>
        )}

        {/* STEP 2: CODE VERIFICATION */}
        {step === 'verify' && (
          <div className="space-y-4 my-2">
            <div className="p-4 bg-[#0A1B22] border border-[#00E5FF]/40 rounded">
              <div className="flex items-start space-x-3">
                <Mail className="w-5 h-5 text-[#00E5FF] shrink-0 mt-0.5 animate-bounce" />
                <div>
                  <h4 className="text-sm font-bold font-mono text-[#E5FCFF]">
                    MESSAGE DE CONFIRMATION TRANSMIS
                  </h4>
                  <p className="text-xs text-[#6F9DA6] mt-1">
                    Un code de sécurité à 6 chiffres a été expédié à <strong className="text-[#00E5FF]">{email}</strong>.
                  </p>
                </div>
              </div>

              {/* Code Preview Helper Card */}
              {sentCodePreview && (
                <div className="mt-3 p-3 bg-[#04080D] border border-[#00E5FF]/50 rounded flex items-center justify-between">
                  <div>
                    <div className="text-[10px] font-mono text-[#6F9DA6] uppercase">Code de sécurité reçu :</div>
                    <div className="text-xl font-mono font-bold tracking-widest text-[#31F5A3]">
                      {sentCodePreview}
                    </div>
                  </div>
                  <button
                    type="button"
                    onClick={() => {
                      setVerificationCode(sentCodePreview);
                      playHudBeep(900, 0.05);
                    }}
                    className="px-2.5 py-1 text-xs font-mono bg-[#00E5FF]/20 border border-[#00E5FF]/40 text-[#00E5FF] rounded hover:bg-[#00E5FF]/30 transition-all cursor-pointer"
                  >
                    Remplir automatiquement
                  </button>
                </div>
              )}
            </div>

            <form onSubmit={handleVerifyCode} className="space-y-4">
              <div>
                <label className="block text-xs font-mono text-[#6F9DA6] mb-1 flex items-center space-x-1.5">
                  <KeyRound className="w-3.5 h-3.5 text-[#00E5FF]" />
                  <span>Saisissez le code de confirmation (6 chiffres) :</span>
                </label>
                <input
                  type="text"
                  maxLength={6}
                  value={verificationCode}
                  onChange={(e) => setVerificationCode(e.target.value.replace(/\D/g, ''))}
                  placeholder="Ex: 849201"
                  autoFocus
                  required
                  className="w-full text-center tracking-[0.5em] font-mono text-2xl font-bold bg-[#0A1219] border border-[#00E5FF]/50 rounded py-2.5 text-[#00E5FF] placeholder-[#304B54] focus:outline-none focus:border-[#31F5A3]"
                />
              </div>

              {errorMessage && (
                <div className="p-3 bg-[#FF4660]/10 border border-[#FF4660]/40 rounded text-xs text-[#FF4660] font-mono">
                  {errorMessage}
                </div>
              )}

              <div className="flex space-x-2">
                <button
                  type="button"
                  onClick={() => { setStep('form'); setErrorMessage(""); }}
                  className="w-1/3 py-2.5 border border-[#007C91]/50 text-[#6F9DA6] hover:text-[#E5FCFF] font-mono text-xs rounded transition-all cursor-pointer"
                >
                  MODIFIER L'EMAIL
                </button>
                <button
                  type="submit"
                  disabled={isLoading}
                  className="w-2/3 py-2.5 bg-[#31F5A3] hover:bg-[#4EFAAF] text-[#030609] font-mono font-bold text-sm tracking-wider rounded transition-all shadow-[0_0_15px_rgba(49,245,163,0.4)] flex items-center justify-center space-x-2 cursor-pointer disabled:opacity-50"
                >
                  {isLoading ? (
                    <RefreshCw className="w-4 h-4 animate-spin" />
                  ) : (
                    <>
                      <CheckCircle2 className="w-4 h-4" />
                      <span>VALIDER LE CODE</span>
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        )}

        {/* STEP 3: SUCCESS */}
        {step === 'success' && (
          <div className="space-y-4 my-3 text-center">
            <div className="w-16 h-16 mx-auto rounded-full bg-[#31F5A3]/20 border border-[#31F5A3] flex items-center justify-center animate-pulse">
              <CheckCircle2 className="w-9 h-9 text-[#31F5A3]" />
            </div>

            <div>
              <h4 className="text-base font-bold font-['Chakra_Petch',sans-serif] tracking-wider text-[#E5FCFF]">
                ACCRÉDITATION COMMANDANT DÉLIVRÉE
              </h4>
              <p className="text-xs text-[#6F9DA6] mt-1 font-mono">
                Identité validée et clé de session chiffrée assignée au profil.
              </p>
            </div>

            {successInfo && (
              <div className="p-3 bg-[#0A1A1E] border border-[#31F5A3]/30 rounded text-left font-mono text-xs space-y-1">
                <div className="text-[#6F9DA6]">Compte : <span className="text-[#E5FCFF] font-bold">{email}</span></div>
                <div className="text-[#6F9DA6]">Accréditation : <span className="text-[#31F5A3] font-bold">LEVEL 5 (COMMANDER)</span></div>
                <div className="text-[#6F9DA6] truncate">Jeton de session : <span className="text-[#00E5FF]">{successInfo.token}</span></div>
              </div>
            )}

            <button
              type="button"
              onClick={onClose}
              className="w-full py-3 bg-[#00E5FF] hover:bg-[#33EAFF] text-[#030609] font-mono font-bold text-sm tracking-wider rounded transition-all shadow-[0_0_20px_rgba(0,229,255,0.4)] flex items-center justify-center space-x-2 cursor-pointer"
            >
              <span>ACCÉDER À LA PASSERELLE DE COMMANDE</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        )}

        {/* Footer info */}
        <div className="mt-5 pt-3 border-t border-[#007C91]/30 flex items-center justify-between text-[10px] font-mono text-[#41626A]">
          <span>CHIFFREMENT QUANTIQUE T-HACK AES-256</span>
          <span>STARK PROTOCOL LEVEL 5</span>
        </div>
      </div>
    </div>
  );
};
