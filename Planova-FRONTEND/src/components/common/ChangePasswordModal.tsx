/**
 * ChangePasswordModal Component for Planova
 * Glass modal for changing user password
 */

import { useState } from 'react';
import GlassCard from './GlassCard';
import GlassButton from './GlassButton';
import GlassInput from './GlassInput';
import { usuarioService } from '../../services/usuarioService';
import { getApiErrorMessage } from '../../services/api';

interface ChangePasswordModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
}

export const ChangePasswordModal: React.FC<ChangePasswordModalProps> = ({
  isOpen,
  onClose,
  onSuccess,
}) => {
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);

    // Validate passwords
    if (newPassword !== confirmPassword) {
      setError('Las contraseñas no coinciden');
      return;
    }

    if (newPassword.length < 6) {
      setError('La contraseña debe tener al menos 6 caracteres');
      return;
    }

    setIsLoading(true);

    try {
      await usuarioService.changePassword(currentPassword, newPassword);
      setSuccess(true);
      setCurrentPassword('');
      setNewPassword('');
      setConfirmPassword('');
      
      // Close after success
      setTimeout(() => {
        onSuccess?.();
        onClose();
      }, 1500);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    setCurrentPassword('');
    setNewPassword('');
    setConfirmPassword('');
    setError(null);
    setSuccess(false);
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div 
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={handleClose}
      />
      
      {/* Modal */}
      <GlassCard 
        variant="floating" 
        className="w-full max-w-md p-6 relative z-10"
      >
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-semibold text-white">
            Cambiar Contraseña
          </h2>
          <button
            onClick={handleClose}
            className="text-white/40 hover:text-white transition-colors"
          >
            <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Success Message */}
        {success && (
          <div className="mb-4 p-3 rounded-xl bg-green-500/20 border border-green-400/30 text-green-200 text-sm">
            Contraseña cambiada exitosamente
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <GlassInput
            label="Contraseña Actual"
            type="password"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            placeholder="Ingresa tu contraseña actual"
            required
          />

          <GlassInput
            label="Nueva Contraseña"
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            placeholder="Ingresa tu nueva contraseña"
            hint="Mínimo 6 caracteres"
            required
          />

          <GlassInput
            label="Confirmar Contraseña"
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            placeholder="Confirma tu nueva contraseña"
            required
          />

          {/* Error Message */}
          {error && (
            <div className="p-3 rounded-xl bg-red-500/20 border border-red-400/30 text-red-200 text-sm">
              {error}
            </div>
          )}

          {/* Actions */}
          <div className="flex gap-3 pt-2">
            <GlassButton
              type="button"
              variant="secondary"
              onClick={handleClose}
              className="flex-1"
            >
              Cancelar
            </GlassButton>
            <GlassButton
              type="submit"
              variant="primary"
              isLoading={isLoading}
              className="flex-1"
            >
              Cambiar
            </GlassButton>
          </div>
        </form>
      </GlassCard>
    </div>
  );
};

export default ChangePasswordModal;
