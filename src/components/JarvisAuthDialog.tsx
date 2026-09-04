import React, { useState } from 'react';
import { X, ShieldCheck, Mail, Phone, KeyRound, UserCheck } from 'lucide-react';
import { UserSettings } from '../types';

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
  const [method, setMethod] = useState<'google' | 'email' | 'phone' | 'guest'>('google');
  const [emailInput, setEmailInput] = useState(settings.userEmail || "");
  const [phoneInput, setPhoneInput] = useState(settings.userPhone || "");
  const [nameInput, setNameInput] = useState(settings.userName || "Commandant");

  if (!isOpen) return null;

  const handleSave = (provider: 'google' | 'email' | 'phone' | 'guest') => {
    onUpdateSettings({
      ...settings,
      userName: nameInput.trim() || "Commandant",
      userEmail: emailInput.trim(),
      userPhone: phoneInput.trim(),
      authProvider: provider,
      isLoggedIn: true,
      securityClearanceLevel: provider === 'guest' ? "LEVEL 3 (GUEST)" : "LEVEL 5 (COMMANDER)"
    });
    onClose();
  };

  const handleLogout = () => {
    onUpdateSettings({
      ...settings,
      userName: "Sir",
      userEmail: "",
      userPhone: "",
      authProvider: "guest",
      isLoggedIn: false,
      securityClearanceLevel: "LEVEL 1 (RESTRICTED)"
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4">
      <div className="relative w-full max-w-md bg-[#070D12] border border-[#00E5FF]/40 rounded-sm p-5 hud-panel-corner shadow-[0_0_30px_rgba(0,229,255,0.2)]">
        {/* Close Button */}
        <button 
          onClick={onClose}
          className="absolute top-3 right-3 text-[#6F9DA6] hover:text-[#00E5FF] p-1 transition-colors cursor-pointer"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Dialog Header */}
        <div className="flex items-center space-x-2 pb-3 border-b border-[#007C91]/40">
          <ShieldCheck className="w-5 h-5 text-[#00E5FF]" />
          <div>
            <h3 className="text-base font-bold font-['Chakra_Petch',sans-serif] tracking-wider text-[#E5FCFF]">
              AUTHENTIFICATION T-HACK CORE
            </h3>
            <p className="text-[11px] font-mono text-[#6F9DA6]">
              Attribution du niveau d'accréditation de sécurité
            </p>
          </div>
        </div>

        {/* Current status if logged in */}
        {settings.isLoggedIn && (
          <div className="my-3 p-3 bg-[#0A1219] border border-[#31F5A3]/40 rounded flex items-center justify-between">
            <div>
              <div className="text-xs font-mono text-[#31F5A3] font-bold">SESSION ACTIVE</div>
              <div className="text-sm text-[#E5FCFF]">{settings.userName} ({settings.securityClearanceLevel})</div>
              {settings.userEmail && <div className="text-xs text-[#6F9DA6]">{settings.userEmail}</div>}
            </div>
            <button
              onClick={handleLogout}
              className="px-2.5 py-1 text-xs font-mono rounded bg-[#FF4660]/20 border border-[#FF4660]/50 text-[#FF4660] hover:bg-[#FF4660]/30 transition-all cursor-pointer"
            >
              Déconnexion
            </button>
          </div>
        )}

        {/* Tabs */}
        <div className="grid grid-cols-4 gap-1 my-3 bg-[#0A1219] p-1 rounded border border-[#007C91]/30">
          <button
            onClick={() => setMethod('google')}
            className={`py-1.5 text-[11px] font-mono rounded transition-all cursor-pointer ${
              method === 'google' ? 'bg-[#00E5FF] text-[#030609] font-bold' : 'text-[#6F9DA6] hover:text-[#E5FCFF]'
            }`}
          >
            Google
          </button>
          <button
            onClick={() => setMethod('email')}
            className={`py-1.5 text-[11px] font-mono rounded transition-all cursor-pointer ${
              method === 'email' ? 'bg-[#00E5FF] text-[#030609] font-bold' : 'text-[#6F9DA6] hover:text-[#E5FCFF]'
            }`}
          >
            Email
          </button>
          <button
            onClick={() => setMethod('phone')}
            className={`py-1.5 text-[11px] font-mono rounded transition-all cursor-pointer ${
              method === 'phone' ? 'bg-[#00E5FF] text-[#030609] font-bold' : 'text-[#6F9DA6] hover:text-[#E5FCFF]'
            }`}
          >
            Téléphone
          </button>
          <button
            onClick={() => setMethod('guest')}
            className={`py-1.5 text-[11px] font-mono rounded transition-all cursor-pointer ${
              method === 'guest' ? 'bg-[#00E5FF] text-[#030609] font-bold' : 'text-[#6F9DA6] hover:text-[#E5FCFF]'
            }`}
          >
            Invité
          </button>
        </div>

        {/* Form Body */}
        <div className="space-y-3">
          <div>
            <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">IDENTIFIANT COMMANDANT</label>
            <input
              type="text"
              value={nameInput}
              onChange={(e) => setNameInput(e.target.value)}
              placeholder="Ex: Tony Stark, Commandant..."
              className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded px-3 py-2 text-sm text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
            />
          </div>

          {method === 'google' && (
            <div className="p-3 bg-[#0A1219] border border-[#007C91]/30 rounded text-center">
              <UserCheck className="w-8 h-8 text-[#00E5FF] mx-auto mb-2" />
              <p className="text-xs text-[#6F9DA6] mb-3">
                Connexion rapide via compte Google sécurisé avec accréditation automatique Niveau 5.
              </p>
              <button
                onClick={() => handleSave('google')}
                className="w-full py-2 bg-[#00E5FF] hover:bg-[#78F7FF] text-[#030609] font-bold text-xs font-mono rounded transition-all cursor-pointer"
              >
                CONNECTER AVEC GOOGLE
              </button>
            </div>
          )}

          {method === 'email' && (
            <div className="space-y-2">
              <div>
                <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">ADRESSE EMAIL</label>
                <div className="relative flex items-center">
                  <Mail className="w-4 h-4 text-[#00E5FF] absolute left-2.5" />
                  <input
                    type="email"
                    value={emailInput}
                    onChange={(e) => setEmailInput(e.target.value)}
                    placeholder="commandant@starkindustries.com"
                    className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded pl-9 pr-3 py-2 text-sm text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
                  />
                </div>
              </div>
              <button
                onClick={() => handleSave('email')}
                className="w-full py-2 bg-[#00E5FF] hover:bg-[#78F7FF] text-[#030609] font-bold text-xs font-mono rounded transition-all cursor-pointer mt-2"
              >
                CONFIRMER L'AUTHENTIFICATION EMAIL
              </button>
            </div>
          )}

          {method === 'phone' && (
            <div className="space-y-2">
              <div>
                <label className="block text-[11px] font-mono text-[#6F9DA6] mb-1">NUMÉRO SÉCURISÉ</label>
                <div className="relative flex items-center">
                  <Phone className="w-4 h-4 text-[#00E5FF] absolute left-2.5" />
                  <input
                    type="tel"
                    value={phoneInput}
                    onChange={(e) => setPhoneInput(e.target.value)}
                    placeholder="+33 6 12 34 56 78"
                    className="w-full bg-[#0A1219] border border-[#007C91]/40 rounded pl-9 pr-3 py-2 text-sm text-[#E5FCFF] focus:outline-none focus:border-[#00E5FF]"
                  />
                </div>
              </div>
              <button
                onClick={() => handleSave('phone')}
                className="w-full py-2 bg-[#00E5FF] hover:bg-[#78F7FF] text-[#030609] font-bold text-xs font-mono rounded transition-all cursor-pointer mt-2"
              >
                VALIDER PAR TÉLÉPHONE
              </button>
            </div>
          )}

          {method === 'guest' && (
            <div className="p-3 bg-[#0A1219] border border-[#007C91]/30 rounded text-center">
              <KeyRound className="w-8 h-8 text-[#FFB547] mx-auto mb-2" />
              <p className="text-xs text-[#6F9DA6] mb-3">
                Accès invité avec accréditation restreinte (Niveau 3). Données conservées localement dans votre navigateur.
              </p>
              <button
                onClick={() => handleSave('guest')}
                className="w-full py-2 bg-[#FFB547] hover:bg-[#FFC97A] text-[#030609] font-bold text-xs font-mono rounded transition-all cursor-pointer"
              >
                CONTINUER EN MODE INVITÉ
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
