import React from 'react';

interface HudPanelProps {
  children: React.ReactNode;
  title?: string;
  badge?: string;
  className?: string;
  accentColor?: string;
}

export const HudPanel: React.FC<HudPanelProps> = ({
  children,
  title,
  badge,
  className = "",
  accentColor
}) => {
  return (
    <div className={`relative bg-[#0A1219]/80 border border-[#007C91]/40 rounded-sm p-3.5 hud-panel-corner backdrop-blur-md ${className}`}>
      {(title || badge) && (
        <div className="flex items-center justify-between pb-2 mb-2 border-b border-[#007C91]/30">
          {title && (
            <div className="flex items-center space-x-2">
              <span className="w-1.5 h-1.5 bg-[#00E5FF] rounded-none rotate-45 inline-block" />
              <span className="text-xs font-mono font-bold tracking-wider text-[#00E5FF] uppercase">
                {title}
              </span>
            </div>
          )}
          {badge && (
            <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-[#00E5FF]/10 text-[#78F7FF] border border-[#00E5FF]/30">
              {badge}
            </span>
          )}
        </div>
      )}
      {children}
    </div>
  );
};
