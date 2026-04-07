/**
 * GlassCard Component for Planova
 * Reusable glass container with variants and hover effects
 */

import { ReactNode } from 'react';
import GlassSparkles from './GlassSparkles';

export type GlassCardVariant = 'default' | 'floating' | 'elevated' | 'subtle' | 'extreme';

interface GlassCardProps {
  children: ReactNode;
  variant?: GlassCardVariant;
  hoverable?: boolean;
  className?: string;
  sparkleCount?: number;
  style?: React.CSSProperties;
  onClick?: () => void;
}

export const GlassCard: React.FC<GlassCardProps> = ({
  children,
  variant = 'default',
  hoverable = false,
  className = '',
  sparkleCount,
  style,
  onClick,
}) => {
  const baseClasses = 'relative overflow-hidden';
  
  const variantClasses: Record<GlassCardVariant, string> = {
    default: 'glass-card',
    floating: 'glass-float',
    elevated: `
      bg-glass-150
      backdrop-blur-glass
      border border-white/20
      rounded-glass-lg
      shadow-glass-float
    `,
    subtle: `
      bg-glass-50
      backdrop-blur-glass-light
      border border-white/5
      rounded-glass
      shadow-glass
    `,
    extreme: 'glass-extreme',
  };

  const hoverClasses = hoverable 
    ? 'glass-hover cursor-pointer transition-all duration-300 hover:translate-y-[-2px]' 
    : '';

  const clickableClasses = onClick ? 'cursor-pointer' : '';

  return (
    <div
      className={`
        ${baseClasses}
        ${variantClasses[variant]}
        ${hoverClasses}
        ${clickableClasses}
        ${variant === 'extreme' ? '' : 'overflow-hidden'} /* Extreme card handles its own overflow for borders */
        ${className}
      `}
      style={style}
      onClick={onClick}
    >
      {/* Glossy highlight effect */}
      <div 
        className="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-white/20 to-transparent" 
      />
      
      {/* Inner glow */}
      <div 
        className="absolute inset-0 pointer-events-none rounded-glass-lg opacity-50"
        style={{
          background: 'linear-gradient(135deg, rgba(255,255,255,0.1) 0%, transparent 50%, rgba(255,255,255,0.05) 100%)'
        }}
      />
      
      {/* Content */}
      <div className="relative z-10 w-full h-full">
        {variant === 'extreme' && <GlassSparkles count={sparkleCount || 8} />}
        {children}
      </div>
    </div>
  );
};

export default GlassCard;
