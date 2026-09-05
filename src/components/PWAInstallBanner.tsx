import React, { useState } from 'react';
import { Download, Smartphone, X, Check, Share, ShieldCheck } from 'lucide-react';
import { usePWAInstall } from '../utils/usePWAInstall';

interface PWAInstallBannerProps {
  onDismiss?: () => void;
}

export const PWAInstallBanner: React.FC<PWAInstallBannerProps> = ({ onDismiss }) => {
  const { isInstallable, isInstalled, isIOS, install } = usePWAInstall();
  const [showIOSModal, setShowIOSModal] = useState(false);
  const [dismissed, setDismissed] = useState(false);

  if (isInstalled || dismissed) {
    return null;
  }

  // If installable via beforeinstallprompt
  if (isInstallable) {
    return (
      <div className="bg-[#050D14] border-b border-[#00E5FF]/40 px-3 py-2 flex items-center justify-between gap-2 z-20 select-none shadow-[0_2px_12px_rgba(0,229,255,0.15)]">
        <div className="flex items-center space-x-2.5 min-w-0">
          <div className="w-8 h-8 rounded-lg bg-[#00E5FF]/10 border border-[#00E5FF]/50 flex items-center justify-center shrink-0">
            <Smartphone className="w-4 h-4 text-[#00E5FF]" />
          </div>
          <div className="truncate">
            <div className="text-xs font-mono font-bold text-[#E5FCFF] flex items-center gap-1.5">
              <span>APPLICATION MOBILE T-HACK AI</span>
              <span className="text-[9px] px-1 bg-[#31F5A3]/20 text-[#31F5A3] border border-[#31F5A3]/40 rounded">OFFICIEL</span>
            </div>
            <p className="text-[11px] text-[#6F9DA6] truncate">
              Installez l'application directement sur votre téléphone pour un accès instantané hors-ligne.
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-2 shrink-0">
          <button
            onClick={install}
            className="flex items-center space-x-1.5 px-3 py-1.5 bg-[#00E5FF] hover:bg-[#78F7FF] text-[#030609] rounded text-xs font-mono font-bold transition-all shadow-[0_0_10px_rgba(0,229,255,0.5)] cursor-pointer"
          >
            <Download className="w-3.5 h-3.5" />
            <span>INSTALLER SUR MON TÉLÉPHONE</span>
          </button>
          <button
            onClick={() => {
              setDismissed(true);
              if (onDismiss) onDismiss();
            }}
            className="p-1 text-[#6F9DA6] hover:text-[#E5FCFF] cursor-pointer"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      </div>
    );
  }

  // iOS Safari flow
  if (isIOS) {
    return (
      <>
        <div className="bg-[#050D14] border-b border-[#00E5FF]/30 px-3 py-2 flex items-center justify-between gap-2 z-20 select-none">
          <div className="flex items-center space-x-2 truncate">
            <Smartphone className="w-4 h-4 text-[#00E5FF] shrink-0" />
            <span className="text-xs font-mono text-[#E5FCFF] truncate">
              Installer l'application mobile T-HACK AI sur votre iPhone / iPad
            </span>
          </div>
          <div className="flex items-center space-x-2 shrink-0">
            <button
              onClick={() => setShowIOSModal(true)}
              className="flex items-center space-x-1 px-2.5 py-1 bg-[#00E5FF]/20 hover:bg-[#00E5FF]/30 border border-[#00E5FF]/50 text-[#00E5FF] rounded text-xs font-mono transition-all cursor-pointer"
            >
              <Share className="w-3 h-3" />
              <span>Guide d'installation</span>
            </button>
            <button
              onClick={() => {
                setDismissed(true);
                if (onDismiss) onDismiss();
              }}
              className="p-1 text-[#6F9DA6] hover:text-[#E5FCFF] cursor-pointer"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>

        {showIOSModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 p-4 backdrop-blur-sm">
            <div className="w-full max-w-sm rounded-xl bg-[#070D12] border border-[#00E5FF]/60 p-5 text-[#E5FCFF] shadow-2xl">
              <div className="flex items-center justify-between mb-3 border-b border-[#007C91]/30 pb-2">
                <div className="flex items-center space-x-2">
                  <Smartphone className="w-5 h-5 text-[#00E5FF]" />
                  <span className="font-mono font-bold text-sm">INSTALLER SUR iOS</span>
                </div>
                <button
                  onClick={() => setShowIOSModal(false)}
                  className="p-1 text-[#6F9DA6] hover:text-white cursor-pointer"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>
              <p className="text-xs text-[#6F9DA6] mb-4">
                Suivez ces 2 étapes simples pour ajouter l'application mobile sur votre écran d'accueil sans passer par l'App Store :
              </p>
              <div className="space-y-3 font-mono text-xs">
                <div className="flex items-start space-x-2 bg-[#0A1219] p-2.5 rounded border border-[#007C91]/30">
                  <span className="w-5 h-5 rounded-full bg-[#00E5FF]/20 text-[#00E5FF] flex items-center justify-center font-bold text-xs shrink-0">1</span>
                  <p>Appuyez sur le bouton <strong>Partager</strong> <Share className="w-3.5 h-3.5 inline mx-1 text-[#00E5FF]" /> dans la barre de Safari.</p>
                </div>
                <div className="flex items-start space-x-2 bg-[#0A1219] p-2.5 rounded border border-[#007C91]/30">
                  <span className="w-5 h-5 rounded-full bg-[#00E5FF]/20 text-[#00E5FF] flex items-center justify-center font-bold text-xs shrink-0">2</span>
                  <p>Faites défiler vers le bas et sélectionnez <strong>Sur l'écran d'accueil</strong>.</p>
                </div>
              </div>
              <button
                onClick={() => setShowIOSModal(false)}
                className="mt-5 w-full py-2 bg-[#00E5FF] text-[#030609] font-mono font-bold text-xs rounded hover:bg-[#78F7FF] transition-all cursor-pointer"
              >
                COMPRIS
              </button>
            </div>
          </div>
        )}
      </>
    );
  }

  return null;
};
