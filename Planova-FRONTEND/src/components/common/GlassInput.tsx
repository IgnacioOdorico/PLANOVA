/**
 * GlassInput Component for Planova
 * Glass input with label, error state, and icon support
 */

import { ReactNode, InputHTMLAttributes, forwardRef } from 'react';

interface GlassInputProps extends Omit<InputHTMLAttributes<HTMLInputElement>, 'size'> {
  label?: string;
  error?: string;
  hint?: string;
  leftIcon?: ReactNode;
  rightIcon?: ReactNode;
  inputSize?: 'sm' | 'md' | 'lg';
}

export const GlassInput = forwardRef<HTMLInputElement, GlassInputProps>(({
  label,
  error,
  hint,
  leftIcon,
  rightIcon,
  inputSize = 'md',
  className = '',
  id,
  ...props
}, ref) => {
  const inputId = id || `input-${Math.random().toString(36).substr(2, 9)}`;
  
  const baseClasses = 'w-full glass-input transition-all duration-300';
  
  const sizeClasses = {
    sm: 'px-3 py-2 text-sm rounded-lg',
    md: 'px-4 py-2.5 text-base rounded-xl',
    lg: 'px-4 py-3 text-lg rounded-xl',
  };

  const hasError = !!error;
  const errorClasses = hasError 
    ? 'border-red-400/50 focus:border-red-400/70 focus:ring-red-400/20' 
    : '';

  const leftIconClasses = leftIcon ? 'pl-10' : '';
  const rightIconClasses = rightIcon ? 'pr-10' : '';

  return (
    <div className="w-full">
      {label && (
        <label 
          htmlFor={inputId}
          className="block text-sm font-medium text-white/80 mb-1.5"
        >
          {label}
        </label>
      )}
      
      <div className="relative">
        {/* Left Icon */}
        {leftIcon && (
          <div className="absolute left-3 top-1/2 -translate-y-1/2 text-white/40 pointer-events-none">
            {leftIcon}
          </div>
        )}
        
        {/* Input */}
        <input
          ref={ref}
          id={inputId}
          className={`
            ${baseClasses}
            ${sizeClasses[inputSize]}
            ${leftIconClasses}
            ${rightIconClasses}
            ${errorClasses}
            ${className}
          `}
          {...props}
        />
        
        {/* Right Icon */}
        {rightIcon && (
          <div className="absolute right-3 top-1/2 -translate-y-1/2 text-white/40 pointer-events-none">
            {rightIcon}
          </div>
        )}
      </div>
      
      {/* Error Message */}
      {error && (
        <p className="mt-1.5 text-sm text-red-300 flex items-center gap-1">
          <svg 
            className="w-4 h-4 flex-shrink-0" 
            fill="currentColor" 
            viewBox="0 0 20 20"
          >
            <path 
              fillRule="evenodd" 
              d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" 
              clipRule="evenodd" 
            />
          </svg>
          {error}
        </p>
      )}
      
      {/* Hint Text */}
      {!error && hint && (
        <p className="mt-1.5 text-sm text-white/40">
          {hint}
        </p>
      )}
    </div>
  );
});

GlassInput.displayName = 'GlassInput';

export default GlassInput;
