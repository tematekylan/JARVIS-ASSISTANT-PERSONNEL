import React, { useState } from 'react';
import { X, Download, CheckCircle2, Sparkles, RefreshCw, Smartphone, ArrowRight } from 'lucide-react';
import { playJarvisChime, playHudBeep } from '../utils/audio';

interface UpdateModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export const UpdateModal: React.FC<UpdateModalProps> = ({ isOpen, onClose }) => {
  const [downloadStep, setDownloadStep] = useState<'info' | 'downloading' | 'ready'>('info');
  const [downloadProgress, setDownloadProgress] = useState(0);

  if (!isOpen) return null;

  const handleStartDownload = () => {
    setDownloadStep('downloading');
    setDownloadProgress(0);
    playHudBeep(700, 0.1);

    const interval = setInterval(() => {
      setDownloadProgress((prev) => {
        if (prev >= 100) {
          clearInterval(interval);
          setDownloadStep('ready');
          playJarvisChime();
          return 100;
        }
        return prev + Math.floor(Math.random() * 15) + 10;
      });
    }, 200);
  };

  const handleInstallUpdate = () => {
    playJarvisChime();
    localStorage.setItem('thack_app_version', '2.5.0');
    setTimeout(() => {
      window.location.reload();
    }, 800);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4 animate-in fade-in duration-150">
      <div className="relative w-full max-w-md bg-[#0A1219] border border-[#007C91]/50 rounded-lg p-5 shadow-2xl text-left font-mono">
        
        {/* Close Button */}
        <button 
          onClick={onClose}
          className="absolute top-4 right-4 text-[#6F9DA6] hover:text-[#00E5FF] p-1 transition-colors cursor-pointer"
          title="Fermer"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Title */}
        <div className="flex items-center space-x-3 mb-4">
          <div className="p-2.5 rounded bg-[#31F5A3]/15 border border-[#31F5A3]/40 text-[#31F5A3]">
            <Download className="w-6 h-6" />
          </div>
          <div>
            <div className="text-sm font-bold text-[#E5FCFF]">MISE À JOUR LOGICIELLE</div>
            <div className="text-xs text-[#31F5A3]">T-HACK AI v2.5 DISPONIBLE</div>
          </div>
        </div>

        {/* Content based on step */}
        {downloadStep === 'info' && (
          <div className="space-y-4">
            <div className="p-3 bg-[#070D12] border border-[#007C91]/30 rounded text-xs text-[#8CA0A8] space-y-2">
              <div className="text-[#00E5FF] font-semibold text-xs flex items-center gap-1.5">
                <Sparkles className="w-3.5 h-3.5" />
                <span>Nouveautés de la version 2.5 :</span>
              </div>
              <ul className="list-disc list-inside space-y-1 text-[11px] text-[#C0DDE3]">
                <li>Épure intégrale de l'interface façon ChatGPT / Gemini / DeepSeek.</li>
                <li>Hologramme réacteur Arc seul au centre de la page d'accueil.</li>
                <li>Suppression des codes de confirmation affichés (envoi direct à votre boîte Gmail).</li>
                <li>Gestion des sections par appui long : Modifier, Partager, Supprimer.</li>
                <li>Base de données Firebase avec modification de la photo de profil.</li>
              </ul>
              <div className="text-[10px] text-[#6F9DA6] pt-1">
                Taille du paquet : 18.4 Mo &bull; Plateforme : Web / Mobile PWA
              </div>
            </div>

            <button
              onClick={handleStartDownload}
              className="w-full py-2.5 px-4 bg-[#00E5FF]/20 hover:bg-[#00E5FF]/30 border border-[#00E5FF] text-[#00E5FF] font-bold text-xs rounded transition-all flex items-center justify-center space-x-2 cursor-pointer shadow-[0_0_12px_rgba(0,229,255,0.25)]"
            >
              <Download className="w-4 h-4" />
              <span>Télécharger la mise à jour</span>
            </button>
          </div>
        )}

        {downloadStep === 'downloading' && (
          <div className="space-y-4 py-3">
            <div className="text-xs text-[#E5FCFF] flex justify-between">
              <span>Téléchargement depuis le serveur cloud...</span>
              <span className="text-[#00E5FF] font-bold">{Math.min(downloadProgress, 100)}%</span>
            </div>

            {/* Progress Bar */}
            <div className="w-full h-3 bg-[#070D12] border border-[#007C91]/50 rounded-full overflow-hidden p-0.5">
              <div 
                className="h-full bg-linear-to-r from-[#00E5FF] to-[#31F5A3] rounded-full transition-all duration-200"
                style={{ width: `${Math.min(downloadProgress, 100)}%` }}
              />
            </div>

            <div className="text-[10px] text-[#6F9DA6] text-center">
              Vérification des signatures cryptographiques du paquet...
            </div>
          </div>
        )}

        {downloadStep === 'ready' && (
          <div className="space-y-4 text-center py-2">
            <div className="w-12 h-12 rounded-full bg-[#31F5A3]/20 border border-[#31F5A3] flex items-center justify-center mx-auto text-[#31F5A3]">
              <CheckCircle2 className="w-7 h-7" />
            </div>

            <div>
              <div className="text-sm font-bold text-[#E5FCFF]">Téléchargement terminé !</div>
              <p className="text-xs text-[#8CA0A8] mt-1">
                La version v2.5 est prête. Veuillez cliquer ci-dessous pour l'installer et actualiser le noyau.
              </p>
            </div>

            <button
              onClick={handleInstallUpdate}
              className="w-full py-2.5 px-4 bg-[#31F5A3]/20 hover:bg-[#31F5A3]/30 border border-[#31F5A3] text-[#31F5A3] font-bold text-xs rounded transition-all flex items-center justify-center space-x-2 cursor-pointer shadow-[0_0_12px_rgba(49,245,163,0.3)]"
            >
              <span>Installer la nouvelle version maintenant</span>
              <ArrowRight className="w-4 h-4" />
            </button>
          </div>
        )}

      </div>
    </div>
  );
};
