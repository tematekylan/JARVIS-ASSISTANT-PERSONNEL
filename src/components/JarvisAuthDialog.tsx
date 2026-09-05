import React, { useState, useEffect } from 'react';
import { 
  X, 
  Mail, 
  KeyRound, 
  CheckCircle2, 
  RefreshCw, 
  Lock, 
  User, 
  ExternalLink, 
  Camera, 
  Image as ImageIcon,
  Clock,
  ShieldCheck,
  Download,
  AlertTriangle
} from 'lucide-react';
import { UserSettings } from '../types';
import { playHudBeep, playJarvisChime, playErrorAlarm, speakText } from '../utils/audio';
import { 
  saveUserProfileToDb, 
  registerFirebaseUser, 
  loginFirebaseUser, 
  resendFirebaseVerificationEmail, 
  updateUserAuthStatusInDb,
  auth
} from '../lib/firebase';

interface JarvisAuthDialogProps {
  isOpen: boolean;
  onClose: () => void;
  settings: UserSettings;
  onUpdateSettings: (newSettings: UserSettings) => void;
}

const DEFAULT_AVATARS = [
  'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&auto=format&fit=crop&q=80',
  'https://images.unsplash.com/photo-1628157582853-a796fa650a6a?w=150&auto=format&fit=crop&q=80'
];

export const JarvisAuthDialog: React.FC<JarvisAuthDialogProps> = ({
  isOpen,
  onClose,
  settings,
  onUpdateSettings
}) => {
  const [authMode, setAuthMode] = useState<'login' | 'register'>('login');
  
  // Explicit state management: 'FORM' | 'EN_ATTENTE_VALIDATION' | 'VALIDE'
  const [step, setStep] = useState<'FORM' | 'EN_ATTENTE_VALIDATION' | 'VALIDE'>(() => {
    if (settings.authValidationStatus === 'EN_ATTENTE_VALIDATION') return 'EN_ATTENTE_VALIDATION';
    if (settings.isLoggedIn) return 'VALIDE';
    return 'FORM';
  });
  
  const [name, setName] = useState(settings.userName || "Teddy");
  const [email, setEmail] = useState(settings.userEmail || "teddykylan@gmail.com");
  const [password, setPassword] = useState("");
  const [avatarUrl, setAvatarUrl] = useState(settings.userAvatarUrl || DEFAULT_AVATARS[0]);
  const [userStatus, setUserStatus] = useState(settings.userStatus || "En mission cybernétique");
  
  // Verification code strictly entered manually by user
  const [verificationCode, setVerificationCode] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [resendCooldown, setResendCooldown] = useState(0);

  // Sync state if settings change
  useEffect(() => {
    if (settings.authValidationStatus === 'EN_ATTENTE_VALIDATION') {
      setStep('EN_ATTENTE_VALIDATION');
    }
  }, [settings.authValidationStatus]);

  // Cooldown timer for resend
  useEffect(() => {
    if (resendCooldown <= 0) return;
    const t = setTimeout(() => setResendCooldown(c => c - 1), 1000);
    return () => clearTimeout(t);
  }, [resendCooldown]);

  if (!isOpen) return null;

  const handleStartAuthFlow = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage("");

    if (!email || !email.includes("@")) {
      setErrorMessage("Veuillez saisir une adresse e-mail valide.");
      playErrorAlarm();
      return;
    }

    if (password.length < 6) {
      setErrorMessage("Le mot de passe doit comporter au moins 6 caractères.");
      playErrorAlarm();
      return;
    }

    setIsLoading(true);
    playHudBeep(880, 0.1);

    try {
      // 1. Firebase Auth initialization
      if (authMode === 'register') {
        const firebaseRes = await registerFirebaseUser(
          email.trim(),
          password,
          name.trim() || "Teddy",
          avatarUrl
        );
        if (firebaseRes.error && !firebaseRes.error.includes("email-already-in-use")) {
          console.warn("[Firebase] Inscription notice:", firebaseRes.error);
        }
      } else {
        const loginRes = await loginFirebaseUser(email.trim(), password);
        if (loginRes.error && !loginRes.error.includes("wrong-password")) {
          console.warn("[Firebase] Connexion notice:", loginRes.error);
        }
      }

      // 2. Dispatch secure confirmation code email via backend (Strictly NO code preview in response)
      const res = await fetch("/api/auth/send-code", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: email.trim(),
          name: name.trim() || "Teddy",
          mode: authMode
        })
      });

      const data = await res.json();
      if (!res.ok || !data.success) {
        throw new Error(data.error || "Échec de l'expédition de l'e-mail de confirmation");
      }

      // 3. Move state to EN_ATTENTE_VALIDATION
      setStep('EN_ATTENTE_VALIDATION');
      setResendCooldown(60);
      playJarvisChime();

      onUpdateSettings({
        ...settings,
        userEmail: email.trim(),
        userName: name.trim() || settings.userName,
        userAvatarUrl: avatarUrl,
        userStatus: userStatus.trim(),
        authValidationStatus: 'EN_ATTENTE_VALIDATION'
      });

      speakText(`Votre compte est placé en état d'attente de validation. Veuillez consulter votre boîte e-mail et saisir manuellement le code reçu.`, {
        rate: settings.speechRate,
        pitch: settings.speechPitch,
        language: settings.voiceLanguage
      });
    } catch (err: any) {
      setErrorMessage(err.message || "Erreur lors de l'initialisation de l'authentification.");
      playErrorAlarm();
    } finally {
      setIsLoading(false);
    }
  };

  const handleManualCodeSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage("");

    const cleanCode = verificationCode.trim();
    if (!cleanCode || cleanCode.length < 4) {
      setErrorMessage("Veuillez saisir manuellement le code complet reçu dans votre boîte e-mail.");
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
          code: cleanCode,
          name: name.trim() || "Teddy"
        })
      });

      const data = await res.json();
      if (!res.ok || !data.success) {
        throw new Error(data.error || "Code invalide. Vérifiez votre boîte e-mail.");
      }

      // 4. Update status in Firestore Database to VALIDE
      const currentUid = auth.currentUser?.uid || `user_${Date.now()}`;
      await updateUserAuthStatusInDb(currentUid, 'VALIDE', true);
      await saveUserProfileToDb({
        uid: currentUid,
        email: email.trim().toLowerCase(),
        displayName: name.trim() || "Teddy",
        photoURL: avatarUrl,
        status: userStatus.trim() || "En mission cybernétique",
        authStatus: 'VALIDE',
        emailVerified: true,
        isOnline: true,
        lastSeen: Date.now(),
        role: "commander",
        updatedAt: Date.now()
      });

      // 5. Update UI to VALIDE state
      setStep('VALIDE');
      playJarvisChime();

      onUpdateSettings({
        ...settings,
        userName: data.user?.name || name,
        userEmail: data.user?.email || email,
        userAvatarUrl: avatarUrl,
        userStatus: userStatus.trim() || "En mission cybernétique",
        isLoggedIn: true,
        authProvider: 'email',
        authValidationStatus: 'VALIDE',
        securityClearanceLevel: "COMMANDANT (AUTORISÉ)"
      });

      speakText(`Code validé avec succès. État du compte validé. Accréditation commandant octroyée.`, {
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

  const handleResendEmail = async () => {
    if (resendCooldown > 0) return;
    setIsLoading(true);
    setErrorMessage("");

    try {
      await resendFirebaseVerificationEmail();
      const res = await fetch("/api/auth/send-code", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: email.trim(),
          name: name.trim() || "Teddy",
          mode: authMode
        })
      });

      const data = await res.json();
      if (!res.ok || !data.success) {
        throw new Error(data.error || "Erreur lors du renvoi");
      }

      setResendCooldown(60);
      playHudBeep(700, 0.1);
      speakText("Nouvel e-mail de confirmation envoyé à votre adresse.", {
        rate: settings.speechRate,
        pitch: settings.speechPitch,
        language: settings.voiceLanguage
      });
    } catch (err: any) {
      setErrorMessage(err.message || "Impossible de renvoyer l'e-mail.");
      playErrorAlarm();
    } finally {
      setIsLoading(false);
    }
  };

  const handlePhotoUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (uploadEvent) => {
        if (uploadEvent.target?.result) {
          setAvatarUrl(uploadEvent.target.result as string);
        }
      };
      reader.readAsDataURL(file);
    }
  };

  const handleLogout = () => {
    onUpdateSettings({
      ...settings,
      userName: "Teddy",
      userEmail: "",
      isLoggedIn: false,
      authValidationStatus: 'NON_AUTHENTIFIE',
      securityClearanceLevel: "RESTREINT"
    });
    setStep('FORM');
    setVerificationCode("");
    playHudBeep(440, 0.1);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-4 animate-in fade-in duration-150">
      <div className="relative w-full max-w-md mx-auto">
        
        {/* Close Button */}
        <button 
          onClick={onClose}
          className="absolute -top-10 right-0 text-[#8b949e] hover:text-white p-1 transition-colors cursor-pointer"
          title="Fermer"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Cyber Header */}
        <div className="flex flex-col items-center mb-4 text-center">
          <div className="w-12 h-12 rounded-xl bg-[#161b22] border border-[#00E5FF]/40 flex items-center justify-center text-[#00E5FF] mb-2 shadow-[0_0_15px_rgba(0,229,255,0.2)]">
            <ShieldCheck className="w-7 h-7 text-[#00E5FF]" />
          </div>
          <h2 className="text-xl font-bold text-[#f0f6fc] tracking-tight font-mono">
            {step === 'EN_ATTENTE_VALIDATION' 
              ? "VALIDATION DU COMPTE" 
              : authMode === 'login' ? "CONNEXION T-HACKMAN AI" : "CRÉATION DE COMPTE"}
          </h2>
          <p className="text-xs text-[#8b949e] mt-1">
            Sécurité Firebase Auth &bull; Firestore Database &bull; Confirmation e-mail
          </p>
        </div>

        {/* Active Session Callout */}
        {settings.isLoggedIn && step !== 'VALIDE' && (
          <div className="mb-4 bg-[#161b22] border border-[#30363d] rounded-md p-3 flex items-center justify-between text-xs">
            <div className="flex items-center space-x-2 text-[#7ee787]">
              <span className="w-2 h-2 rounded-full bg-[#238636]" />
              <span className="text-[#c9d1d9]">Connecté en tant que <strong>{settings.userName}</strong> ({settings.userEmail})</span>
            </div>
            <button
              onClick={handleLogout}
              className="text-[#f85149] hover:underline cursor-pointer ml-2"
            >
              Déconnexion
            </button>
          </div>
        )}

        {/* Main Card */}
        <div className="bg-[#161b22] border border-[#30363d] rounded-xl p-5 shadow-2xl text-left">

          {/* ==================================================== */}
          {/* STEP 1: FORMULAIRE INITIAL (LOGIN / INSCRIPTION)      */}
          {/* ==================================================== */}
          {step === 'FORM' && (
            <form onSubmit={handleStartAuthFlow} className="space-y-4">
              {authMode === 'register' && (
                <>
                  <div>
                    <label className="block text-xs font-medium text-[#c9d1d9] mb-1.5 flex items-center gap-1.5">
                      <User className="w-3.5 h-3.5 text-[#00E5FF]" />
                      <span>Nom ou Prénom</span>
                    </label>
                    <input
                      type="text"
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      placeholder="Teddy"
                      required
                      className="w-full bg-[#0d1117] border border-[#30363d] focus:border-[#00E5FF] focus:ring-1 focus:ring-[#00E5FF] rounded-md px-3 py-1.5 text-sm text-[#c9d1d9] placeholder-[#484f58] outline-none transition-colors"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-medium text-[#c9d1d9] mb-1.5 flex items-center gap-1.5">
                      <User className="w-3.5 h-3.5 text-[#00E5FF]" />
                      <span>Statut personnel</span>
                    </label>
                    <input
                      type="text"
                      value={userStatus}
                      onChange={(e) => setUserStatus(e.target.value)}
                      placeholder="Ex: En mission cybernétique, Disponible..."
                      className="w-full bg-[#0d1117] border border-[#30363d] focus:border-[#00E5FF] focus:ring-1 focus:ring-[#00E5FF] rounded-md px-3 py-1.5 text-sm text-[#c9d1d9] placeholder-[#484f58] outline-none transition-colors"
                    />
                  </div>

                  {/* Photo de profil */}
                  <div>
                    <label className="block text-xs font-medium text-[#c9d1d9] mb-1.5 flex items-center gap-1.5">
                      <Camera className="w-3.5 h-3.5 text-[#00E5FF]" />
                      <span>Photo de profil</span>
                    </label>
                    <div className="flex items-center space-x-3">
                      <img 
                        src={avatarUrl} 
                        alt="Avatar" 
                        className="w-12 h-12 rounded-full object-cover border-2 border-[#00E5FF]"
                      />
                      <div className="flex-1 space-y-1">
                        <label className="cursor-pointer inline-flex items-center px-2.5 py-1 text-xs bg-[#21262d] hover:bg-[#30363d] text-[#c9d1d9] rounded border border-[#30363d] transition-colors">
                          <ImageIcon className="w-3.5 h-3.5 mr-1" />
                          <span>Choisir une photo</span>
                          <input 
                            type="file" 
                            accept="image/*" 
                            onChange={handlePhotoUpload} 
                            className="hidden" 
                          />
                        </label>
                        <div className="flex space-x-1 mt-1">
                          {DEFAULT_AVATARS.slice(0, 4).map((url, i) => (
                            <img 
                              key={i} 
                              src={url} 
                              alt="Avatar" 
                              onClick={() => setAvatarUrl(url)}
                              className={`w-6 h-6 rounded-full cursor-pointer hover:scale-110 transition-transform ${avatarUrl === url ? 'ring-2 ring-[#00E5FF]' : 'opacity-60'}`}
                            />
                          ))}
                        </div>
                      </div>
                    </div>
                  </div>
                </>
              )}

              <div>
                <label className="block text-xs font-medium text-[#c9d1d9] mb-1.5 flex items-center gap-1.5">
                  <Mail className="w-3.5 h-3.5 text-[#00E5FF]" />
                  <span>Adresse e-mail</span>
                </label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="teddykylan@gmail.com"
                  required
                  className="w-full bg-[#0d1117] border border-[#30363d] focus:border-[#00E5FF] focus:ring-1 focus:ring-[#00E5FF] rounded-md px-3 py-1.5 text-sm text-[#c9d1d9] placeholder-[#484f58] outline-none transition-colors"
                />
              </div>

              <div>
                <label className="block text-xs font-medium text-[#c9d1d9] mb-1.5 flex items-center gap-1.5">
                  <Lock className="w-3.5 h-3.5 text-[#00E5FF]" />
                  <span>Mot de passe</span>
                </label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••••••"
                  required
                  className="w-full bg-[#0d1117] border border-[#30363d] focus:border-[#00E5FF] focus:ring-1 focus:ring-[#00E5FF] rounded-md px-3 py-1.5 text-sm text-[#c9d1d9] placeholder-[#484f58] outline-none transition-colors"
                />
              </div>

              {errorMessage && (
                <div className="p-2.5 bg-[#f85149]/15 border border-[#f85149]/40 rounded-md text-xs text-[#ff7b72] flex items-center gap-2">
                  <AlertTriangle className="w-4 h-4 shrink-0" />
                  <span>{errorMessage}</span>
                </div>
              )}

              <button
                type="submit"
                disabled={isLoading}
                className="w-full py-2.5 px-4 bg-[#00E5FF]/20 hover:bg-[#00E5FF]/30 active:bg-[#00E5FF]/40 border border-[#00E5FF] text-[#00E5FF] font-semibold text-sm rounded-md transition-all flex items-center justify-center space-x-2 cursor-pointer disabled:opacity-50 shadow-[0_0_15px_rgba(0,229,255,0.15)] mt-2 font-mono"
              >
                {isLoading ? (
                  <>
                    <RefreshCw className="w-4 h-4 animate-spin" />
                    <span>Traitement Firebase Auth...</span>
                  </>
                ) : (
                  <span>
                    {authMode === 'login' 
                      ? "SE CONNECTER ET VALIDER PAR E-MAIL" 
                      : "CRÉER LE COMPTE ET ENVOYER LE CODE"}
                  </span>
                )}
              </button>
            </form>
          )}

          {/* ==================================================== */}
          {/* STEP 2: ÉTAT 'EN_ATTENTE_VALIDATION' (AUCUN CODE DANS L'UI) */}
          {/* ==================================================== */}
          {step === 'EN_ATTENTE_VALIDATION' && (
            <div className="space-y-4">
              
              {/* Status Badge 'EN_ATTENTE_VALIDATION' */}
              <div className="flex items-center justify-between p-3 rounded-lg bg-[#d29922]/15 border border-[#d29922]/50 text-[#f2cc60]">
                <div className="flex items-center space-x-2.5">
                  <span className="relative flex h-3 w-3">
                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-[#d29922] opacity-75"></span>
                    <span className="relative inline-flex rounded-full h-3 w-3 bg-[#d29922]"></span>
                  </span>
                  <span className="font-mono text-xs font-bold uppercase tracking-wider">
                    ÉTAT : EN_ATTENTE_VALIDATION
                  </span>
                </div>
                <span className="text-[10px] px-2 py-0.5 rounded bg-[#d29922]/20 font-mono border border-[#d29922]/40">
                  Firebase &bull; Saisie manuelle
                </span>
              </div>

              {/* Security Directive Box */}
              <div className="p-3 bg-[#0d1117] border border-[#30363d] rounded-lg text-xs space-y-2">
                <div className="flex items-center gap-1.5 text-[#58a6ff] font-semibold">
                  <Mail className="w-4 h-4 text-[#00E5FF]" />
                  <span>Code expédié à votre boîte mail :</span>
                </div>
                <div className="font-mono text-sm font-bold text-[#E5FCFF] bg-[#161b22] p-2 rounded border border-[#30363d] break-all">
                  {email}
                </div>
                <div className="text-[#8b949e] leading-relaxed pt-1">
                  <strong className="text-[#f2cc60]">&#9888; Règle de sécurité :</strong> Pour des raisons de confidentialité, 
                  <strong> aucun code n'est affiché dans l'interface</strong>. 
                  Veuillez ouvrir votre boîte de réception, relever le code de sécurité reçu, 
                  et le <strong>saisir manuellement</strong> dans le champ ci-dessous.
                </div>
                <div className="pt-1 flex justify-end">
                  <a 
                    href="https://mail.google.com" 
                    target="_blank" 
                    rel="noreferrer"
                    className="inline-flex items-center gap-1 text-[11px] text-[#00E5FF] hover:underline"
                  >
                    Ouvrir Gmail directement <ExternalLink className="w-3 h-3" />
                  </a>
                </div>
              </div>

              {/* Manual Code Input Form */}
              <form onSubmit={handleManualCodeSubmit} className="space-y-3">
                <div>
                  <div className="flex items-center justify-between mb-1.5">
                    <label className="text-xs font-medium text-[#c9d1d9] flex items-center gap-1.5">
                      <KeyRound className="w-3.5 h-3.5 text-[#00E5FF]" />
                      <span>Saisissez manuellement le code reçu :</span>
                    </label>
                    <span className="text-[10px] text-[#8b949e] font-mono">6 chiffres</span>
                  </div>
                  <input
                    type="text"
                    maxLength={6}
                    value={verificationCode}
                    onChange={(e) => setVerificationCode(e.target.value.replace(/\D/g, ''))}
                    placeholder="••••••"
                    autoFocus
                    required
                    className="w-full text-center tracking-[0.4em] font-mono text-2xl font-bold bg-[#0d1117] border border-[#00E5FF]/40 focus:border-[#00E5FF] focus:ring-1 focus:ring-[#00E5FF] rounded-lg py-3 text-[#31F5A3] placeholder-[#484f58] outline-none shadow-inner"
                  />
                </div>

                {errorMessage && (
                  <div className="p-2.5 bg-[#f85149]/15 border border-[#f85149]/40 rounded-md text-xs text-[#ff7b72] flex items-center gap-2">
                    <AlertTriangle className="w-4 h-4 shrink-0" />
                    <span>{errorMessage}</span>
                  </div>
                )}

                <div className="flex gap-2 pt-1">
                  <button
                    type="button"
                    onClick={() => { setStep('FORM'); setErrorMessage(""); }}
                    className="w-1/3 py-2 text-xs text-[#8b949e] hover:text-[#c9d1d9] bg-[#21262d] hover:bg-[#30363d] border border-[#30363d] rounded-lg transition-colors cursor-pointer"
                  >
                    Modifier l'email
                  </button>
                  <button
                    type="submit"
                    disabled={isLoading || verificationCode.length < 4}
                    className="w-2/3 py-2 bg-[#238636] hover:bg-[#2ea043] disabled:opacity-40 text-white font-medium text-xs rounded-lg transition-colors flex items-center justify-center gap-1.5 cursor-pointer shadow-md font-mono"
                  >
                    {isLoading ? <RefreshCw className="w-3.5 h-3.5 animate-spin" /> : <CheckCircle2 className="w-3.5 h-3.5" />}
                    <span>VALIDER MANUELLEMENT</span>
                  </button>
                </div>
              </form>

              {/* Resend Action */}
              <div className="pt-2 border-t border-[#30363d] flex items-center justify-between text-xs text-[#8b949e]">
                <span>Vous n'avez pas reçu l'e-mail ?</span>
                <button
                  type="button"
                  onClick={handleResendEmail}
                  disabled={isLoading || resendCooldown > 0}
                  className="text-[#00E5FF] hover:underline disabled:opacity-40 cursor-pointer font-medium"
                >
                  {resendCooldown > 0 ? `Renvoyer (${resendCooldown}s)` : "Renvoyer l'e-mail"}
                </button>
              </div>

            </div>
          )}

          {/* ==================================================== */}
          {/* STEP 3: SUCCÈS & ACCRÉDITATION VALIDE                */}
          {/* ==================================================== */}
          {step === 'VALIDE' && (
            <div className="text-center py-4 space-y-4">
              <div className="w-14 h-14 bg-[#238636]/20 border border-[#238636] rounded-full flex items-center justify-center mx-auto text-[#7ee787] shadow-[0_0_20px_rgba(49,245,163,0.3)]">
                <CheckCircle2 className="w-8 h-8" />
              </div>
              <div>
                <div className="inline-block px-2.5 py-0.5 rounded bg-[#31F5A3]/15 border border-[#31F5A3]/40 text-[#31F5A3] font-mono text-xs font-bold mb-1">
                  ÉTAT DU COMPTE : VALIDÉ
                </div>
                <h3 className="text-base font-semibold text-[#f0f6fc]">Accréditation Octroyée</h3>
                <p className="text-xs text-[#8b949e] mt-1">
                  Le code a été validé manuellement. Votre profil est actif dans Firestore.
                </p>
              </div>

              <div className="p-3 bg-[#0d1117] border border-[#30363d] rounded-lg flex items-center space-x-3 text-left">
                <img 
                  src={avatarUrl} 
                  alt="Avatar utilisateur" 
                  className="w-10 h-10 rounded-full object-cover border-2 border-[#00E5FF]"
                />
                <div className="text-xs">
                  <div className="text-[#f0f6fc] font-semibold">{name}</div>
                  <div className="text-[#8b949e]">{email}</div>
                  <div className="text-[#31F5A3] text-[10px] mt-0.5 font-mono">Niveau : Commandant Autorisé</div>
                </div>
              </div>

              <button
                onClick={onClose}
                className="w-full py-2.5 bg-[#238636] hover:bg-[#2ea043] text-white font-medium text-xs rounded-lg transition-colors cursor-pointer font-mono shadow-md"
              >
                ACCÉDER À T-HACKMAN AI
              </button>
            </div>
          )}

        </div>

        {/* APK & Mobile Installation Box */}
        <div className="mt-3 p-3 bg-[#070D12] border border-[#007C91]/40 rounded-xl space-y-2 text-xs">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2 text-[#6F9DA6]">
              <Download className="w-4 h-4 text-[#31F5A3]" />
              <span>Package Android complet (27.3 Mo) :</span>
            </div>
            <a
              href="/api/download/apk"
              download="t-hackman-ai-v2.5.0.apk"
              className="px-2.5 py-1 rounded bg-[#31F5A3]/15 hover:bg-[#31F5A3]/25 border border-[#31F5A3]/40 text-[#31F5A3] text-[11px] font-mono font-bold transition-all flex items-center gap-1"
            >
              <Download className="w-3 h-3" />
              <span>Télécharger APK (27 Mo)</span>
            </a>
          </div>
          <div className="text-[11px] text-[#8CA0A8] pt-1 border-t border-[#007C91]/20">
            <span className="text-[#00E5FF] font-semibold">&#9733; Sur smartphone :</span> Vous pouvez aussi installer l'application en 1 clic sans télécharger d'APK via le menu de votre navigateur Chrome (<strong>Installer l'application</strong>).
          </div>
        </div>

        {/* Footer Toggle Login/Register (only in FORM step) */}
        {step === 'FORM' && (
          <div className="mt-3 p-3 bg-[#161b22] border border-[#30363d] rounded-xl text-center text-xs text-[#8b949e]">
            {authMode === 'login' ? (
              <span>
                Nouveau sur la plateforme ?{" "}
                <button
                  onClick={() => { setAuthMode('register'); setErrorMessage(""); }}
                  className="text-[#00E5FF] hover:underline font-medium cursor-pointer"
                >
                  Créer un compte
                </button>
              </span>
            ) : (
              <span>
                Vous possédez déjà un compte ?{" "}
                <button
                  onClick={() => { setAuthMode('login'); setErrorMessage(""); }}
                  className="text-[#00E5FF] hover:underline font-medium cursor-pointer"
                >
                  Se connecter
                </button>
              </span>
            )}
          </div>
        )}

      </div>
    </div>
  );
};
