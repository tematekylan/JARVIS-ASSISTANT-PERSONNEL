import React, { useState, useEffect } from 'react';
import { Cpu, Zap, Radio, Shield, Thermometer, Database, CheckCircle2, RotateCw } from 'lucide-react';
import { HudPanel } from '../components/HudPanel';
import { UserSettings } from '../types';

interface SystemScreenProps {
  settings: UserSettings;
}

export const SystemScreen: React.FC<SystemScreenProps> = ({ settings }) => {
  const [coreFreq, setCoreFreq] = useState(3.84);
  const [temp, setTemp] = useState(36.8);
  const [fluxPurity, setFluxPurity] = useState(99.4);
  const [isCalibrating, setIsCalibrating] = useState(false);

  useEffect(() => {
    const interval = setInterval(() => {
      setCoreFreq(prev => +(prev + (Math.random() * 0.08 - 0.04)).toFixed(2));
      setTemp(prev => +(prev + (Math.random() * 0.4 - 0.2)).toFixed(1));
    }, 2000);
    return () => clearInterval(interval);
  }, []);

  const handleCalibrate = () => {
    setIsCalibrating(true);
    setTimeout(() => {
      setFluxPurity(99.9);
      setIsCalibrating(false);
    }, 1500);
  };

  return (
    <div className="flex-1 p-3 sm:p-5 max-w-4xl mx-auto w-full space-y-4 overflow-y-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-[#007C91]/30 pb-3">
        <div>
          <h1 className="text-xl font-bold font-['Chakra_Petch',sans-serif] text-[#E5FCFF] tracking-wider flex items-center gap-2">
            <Zap className="w-5 h-5 text-[#00E5FF]" /> SYSTÈME & RÉACTEUR ARC STARK
          </h1>
          <p className="text-xs font-mono text-[#6F9DA6]">
            Télémétrie matérielle, confinement énergétique et intégrité du noyau
          </p>
        </div>

        <button
          onClick={handleCalibrate}
          disabled={isCalibrating}
          className="px-3.5 py-1.5 rounded bg-[#00E5FF] hover:bg-[#78F7FF] text-[#030609] text-xs font-mono font-bold flex items-center gap-1.5 transition-all cursor-pointer shadow-[0_0_10px_rgba(0,229,255,0.4)]"
        >
          <RotateCw className={`w-4 h-4 ${isCalibrating ? 'animate-spin' : ''}`} />
          {isCalibrating ? "CALIBRAGE EN COURS..." : "CALIBRER LE RÉACTEUR"}
        </button>
      </div>

      {/* Main Core Gauge Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
        <div className="p-3 bg-[#070D12] border border-[#007C91]/40 rounded hud-panel-corner">
          <div className="flex items-center space-x-1.5 text-xs font-mono text-[#6F9DA6]">
            <Cpu className="w-4 h-4 text-[#00E5FF]" />
            <span>FRÉQUENCE CORE</span>
          </div>
          <div className="text-xl font-bold font-mono text-[#E5FCFF] mt-1">
            {coreFreq} <span className="text-xs text-[#00E5FF]">GHz</span>
          </div>
          <div className="text-[10px] text-[#31F5A3] font-mono mt-1">Flux quantique synchrone</div>
        </div>

        <div className="p-3 bg-[#070D12] border border-[#007C91]/40 rounded hud-panel-corner">
          <div className="flex items-center space-x-1.5 text-xs font-mono text-[#6F9DA6]">
            <Thermometer className="w-4 h-4 text-[#FFB547]" />
            <span>TEMPÉRATURE</span>
          </div>
          <div className="text-xl font-bold font-mono text-[#E5FCFF] mt-1">
            {temp} <span className="text-xs text-[#FFB547]">°C</span>
          </div>
          <div className="text-[10px] text-[#31F5A3] font-mono mt-1">Refroidissement cryogénique</div>
        </div>

        <div className="p-3 bg-[#070D12] border border-[#007C91]/40 rounded hud-panel-corner">
          <div className="flex items-center space-x-1.5 text-xs font-mono text-[#6F9DA6]">
            <Zap className="w-4 h-4 text-[#31F5A3]" />
            <span>PURETÉ FLUX</span>
          </div>
          <div className="text-xl font-bold font-mono text-[#E5FCFF] mt-1">
            {fluxPurity}%
          </div>
          <div className="text-[10px] text-[#31F5A3] font-mono mt-1">Confinement magnétique</div>
        </div>

        <div className="p-3 bg-[#070D12] border border-[#007C91]/40 rounded hud-panel-corner">
          <div className="flex items-center space-x-1.5 text-xs font-mono text-[#6F9DA6]">
            <Shield className="w-4 h-4 text-[#00E5FF]" />
            <span>CHIFFREMENT</span>
          </div>
          <div className="text-xl font-bold font-mono text-[#E5FCFF] mt-1">
            AES-256
          </div>
          <div className="text-[10px] text-[#31F5A3] font-mono mt-1">Certifié Stark Industries</div>
        </div>
      </div>

      {/* Subsystem Matrix */}
      <HudPanel title="MATRICE DES SOUS-SYSTÈMES TACTIQUES" badge="100% OPÉRATIONNEL">
        <div className="space-y-2.5">
          {[
            { name: "Moteur Vocal (Web Audio Synthesizer & Speech Recognition)", status: "ACTIF", latency: "12ms", ok: true },
            { name: "Passerelle Multimodale Gemini-3.8-Flash (Serveur Express)", status: "EN LIGNE", latency: "42ms", ok: true },
            { name: "Base de Données Locale Chiffrée (Room / IndexedStore)", status: "VERROUILLÉ", latency: "2ms", ok: true },
            { name: "Canal de Délibération du Collège d'IA d'Incident", status: "VEILLE", latency: "0ms", ok: true },
            { name: "Module Holographique Canvas à 7 Niveaux", status: "60 FPS", latency: "16ms", ok: true }
          ].map((sub, i) => (
            <div key={i} className="p-2.5 bg-[#070D12] rounded border border-[#007C91]/30 flex items-center justify-between text-xs font-mono">
              <div className="flex items-center space-x-2">
                <CheckCircle2 className="w-4 h-4 text-[#31F5A3]" />
                <span className="text-[#E5FCFF]">{sub.name}</span>
              </div>
              <div className="flex items-center space-x-3 text-[#6F9DA6]">
                <span>{sub.latency}</span>
                <span className="px-2 py-0.5 rounded bg-[#31F5A3]/15 text-[#31F5A3] border border-[#31F5A3]/30 text-[10px]">
                  {sub.status}
                </span>
              </div>
            </div>
          ))}
        </div>
      </HudPanel>

      {/* Environment Hardware Parameters */}
      <HudPanel title="PARAMÈTRES D'EXÉCUTION" badge={settings.securityClearanceLevel}>
        <div className="text-xs font-mono space-y-1.5 text-[#6F9DA6]">
          <div className="flex justify-between border-b border-[#007C91]/20 py-1">
            <span>Architecture Cible :</span>
            <span className="text-[#E5FCFF]">Full-Stack Web (Express + Vite + React 18)</span>
          </div>
          <div className="flex justify-between border-b border-[#007C91]/20 py-1">
            <span>Port d'Écoute :</span>
            <span className="text-[#00E5FF]">3000 (0.0.0.0)</span>
          </div>
          <div className="flex justify-between border-b border-[#007C91]/20 py-1">
            <span>Fournisseur IA Actif :</span>
            <span className="text-[#31F5A3] uppercase">{settings.activeAiProvider} ({settings.aiModel})</span>
          </div>
          <div className="flex justify-between py-1">
            <span>Courriel Développeur Alerte :</span>
            <span className="text-[#78F7FF]">{settings.developerAlertEmail}</span>
          </div>
        </div>
      </HudPanel>
    </div>
  );
};
