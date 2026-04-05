/**
 * GlassButton Component for Planova
 * Glass button with variants (primary, secondary, danger)
 */

import { ReactNode, ButtonHTMLAttributes } from 'react';

export type GlassButtonVariant = 'primary' | 'secondary' | 'danger' | 'ghost';
export type GlassButtonSize = 'sm' | 'md' | 'lg';

interface GlassButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  children: ReactNode;
  variant?: GlassButtonVariant;
  size?: GlassButtonSize;
  isLoading?: boolean;
  leftIcon?: ReactNode;
  rightIcon?: ReactNode;
  fullWidth?: boolean;
}

export const GlassButton: React.FC<GlassButtonProps> = ({
  children,
  variant = 'primary',
  size = 'md',
  isLoading = false,
  leftIcon,
  rightIcon,
  fullWidth = false,
  className = '',
  disabled,
  ...props
}) => {
  const baseClasses = 'relative inline-flex items-center justify-center font-medium transition-all duration-300 focus:outline-none focus:ring-2 focus:ring-white/20 disabled:opacity-50 disabled:cursor-not-allowed';
  
  const variantClasses: Record<GlassButtonVariant, string> = {
    primary: `
      bg-gradient-to-br from-white/20 to-white/10
      border border-white/20
      text-white
      hover:from-white/25 hover:to-white/15
      hover:border-white/30
      hover:shadow-[0_0_20px_rgba(255,255,255,0.1)]
      active:from-white/15 active:to-white/5
    `,
    secondary: `
      bg-glass-100
      border border-white/10
      text-white/80
      hover:bg-glass-150
      hover:border-white/15
      hover:text-white
      active:bg-glass-200
    `,
    danger: `
      bg-gradient-to-br from-red-500/20 to-red-500/10
      border border-red-400/20
      text-red-100
      hover:from-red-500/30 hover:to-red-500/15
      hover:border-red-400/30
      hover:shadow-[0_0_20px_rgba(239,68,68,0.2)]
      active:from-red-500/15 active:to-red-500/5
    `,
    ghost: `
      bg-transparent
      border border-transparent
      text-white/60
      hover:bg-glass-100
      hover:text-white
      hover:border-white/10
      active:bg-glass-150
    `,
  };

  const sizeClasses: Record<GlassButtonSize, string> = {
    sm: 'px-3 py-1.5 text-sm rounded-lg gap-1.5',
    md: 'px-4 py-2 text-base rounded-xl gap-2',
    lg: 'px-6 py-3 text-lg rounded-xl gap-2.5',
  };

  const widthClass = fullWidth ? 'w-full' : '';

  const isDisabled = disabled || isLoading;

  return (
    <button
      className={`
        ${baseClasses}
        ${variantClasses[variant]}
        ${sizeClasses[size]}
        ${widthClass}
        ${className}
      `}
      disabled={isDisabled}
      {...props}
    >
      {/* Glossy highlight */}
      <div className="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-white/30 to-transparent" />
      
      {/* Content */}
      <span className="relative z-10 flex items-center gap-2">
        {isLoading ? (
          <>
            <svg 
              className="animate-spin h-4 w-4" 
              xmlns="http://www.w3.org/2000/svg" 
              fill="none" 
              viewBox="0 0 24 24"
            >
              <circle 
                className="opacity-25" 
                cx="12" 
                cy="12" 
                r="10" 
                stroke="currentColor" 
                strokeWidth="4"
              />
              <path 
                className="opacity-75" 
                fill="currentColor" 
                d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
              />
            </svg>
            <span>Cargando...</span>
          </>
        ) : (
          <>
            {leftIcon && <span className="flex-shrink-0">{leftIcon}</span>}
            {children}
            {rightIcon && <span className="flex-shrink-0">{rightIcon}</span>}
          </>
        )}
      </span>
    </button>
  );
};

export default GlassButton;
