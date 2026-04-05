/**
 * Perfil Page for Planova
 * User profile settings with glassmorphism design
 */

import { useState, useEffect } from 'react';
import GlassCard from '../components/common/GlassCard';
import GlassButton from '../components/common/GlassButton';
import GlassInput from '../components/common/GlassInput';
import ChangePasswordModal from '../components/common/ChangePasswordModal';
import { usuarioService } from '../services/usuarioService';
import { getApiErrorMessage } from '../services/api';
import { Usuario } from '../types';

export const PerfilPage: React.FC = () => {
  const [user, setUser] = useState<Usuario | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Edit form state
  const [nombre, setNombre] = useState('');

  // Fetch user profile on mount
  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    setIsLoading(true);
    try {
      const profile = await usuarioService.getPerfil();
      setUser(profile);
      setNombre(profile.nombre);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  };

  const handleSaveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    if (!nombre.trim()) {
      setError('El nombre no puede estar vacío');
      return;
    }

    setIsSaving(true);
    try {
      const updatedUser = await usuarioService.updatePerfil({ nombre: nombre.trim() });
      setUser(updatedUser);
      setSuccess('Perfil actualizado correctamente');
      setIsEditing(false);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setIsSaving(false);
    }
  };

  const handleCancelEdit = () => {
    setNombre(user?.nombre || '');
    setIsEditing(false);
    setError(null);
    setSuccess(null);
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return 'No disponible';
    const date = new Date(dateString);
    return date.toLocaleDateString('es-ES', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  if (isLoading) {
    return (
      <div className="p-8 flex items-center justify-center h-full">
        <div className="glass-card p-8 rounded-2xl">
          <div className="flex items-center gap-3">
            <svg className="animate-spin h-6 w-6 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
            <span className="text-white/80">Cargando perfil...</span>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="p-8">
      <div className="max-w-2xl mx-auto">
        <h1 className="text-3xl font-bold text-white mb-8">Mi Perfil</h1>
        
        {/* Error Message */}
        {error && (
          <div className="mb-4 p-4 rounded-xl bg-red-500/20 border border-red-400/30 text-red-200">
            {error}
          </div>
        )}

        {/* Success Message */}
        {success && (
          <div className="mb-4 p-4 rounded-xl bg-green-500/20 border border-green-400/30 text-green-200">
            {success}
          </div>
        )}

        <GlassCard variant="floating" className="p-8">
          {isEditing ? (
            /* Edit Profile Form */
            <form onSubmit={handleSaveProfile} className="space-y-6">
              <h2 className="text-xl font-semibold text-white mb-4">Editar Perfil</h2>
              
              <GlassInput
                label="Nombre"
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                placeholder="Tu nombre"
                required
              />

              <div className="flex gap-3">
                <GlassButton
                  type="button"
                  variant="secondary"
                  onClick={handleCancelEdit}
                  className="flex-1"
                >
                  Cancelar
                </GlassButton>
                <GlassButton
                  type="submit"
                  variant="primary"
                  isLoading={isSaving}
                  className="flex-1"
                >
                  Guardar
                </GlassButton>
              </div>
            </form>
          ) : (
            /* User Info Display */
            <>
              {/* Avatar and Basic Info */}
              <div className="flex items-center gap-6 mb-8">
                <div 
                  className="w-24 h-24 rounded-full flex items-center justify-center text-3xl font-bold"
                  style={{
                    background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.4), rgba(139, 92, 246, 0.4))',
                    border: '1px solid rgba(255, 255, 255, 0.2)',
                    boxShadow: '0 0 30px rgba(99, 102, 241, 0.3)',
                  }}
                >
                  {user?.nombre?.charAt(0).toUpperCase() || 'U'}
                </div>
                <div>
                  <h2 className="text-2xl font-semibold text-white">{user?.nombre || 'Usuario'}</h2>
                  <p className="text-white/60">{user?.email || 'email@ejemplo.com'}</p>
                  <span className="inline-block mt-2 px-3 py-1 text-xs font-medium rounded-full bg-gradient-to-r from-indigo-500/30 to-purple-500/30 border border-white/10 text-white/80">
                    {user?.rol || 'Usuario'}
                  </span>
                </div>
              </div>

              {/* User Details */}
              <div className="space-y-4 mb-8">
                <div className="flex justify-between items-center py-3 border-b border-white/10">
                  <span className="text-white/60">Nombre</span>
                  <span className="text-white font-medium">{user?.nombre}</span>
                </div>
                <div className="flex justify-between items-center py-3 border-b border-white/10">
                  <span className="text-white/60">Email</span>
                  <span className="text-white font-medium">{user?.email}</span>
                </div>
                <div className="flex justify-between items-center py-3 border-b border-white/10">
                  <span className="text-white/60">Rol</span>
                  <span className="text-white font-medium">{user?.rol || 'Usuario'}</span>
                </div>
                <div className="flex justify-between items-center py-3">
                  <span className="text-white/60">Fecha de Registro</span>
                  <span className="text-white font-medium">{formatDate(user?.fechaCreacion)}</span>
                </div>
              </div>

              {/* Actions */}
              <div className="space-y-3">
                <GlassButton 
                  variant="secondary" 
                  fullWidth 
                  onClick={() => setIsEditing(true)}
                >
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                  </svg>
                  Editar Perfil
                </GlassButton>
                <GlassButton 
                  variant="secondary" 
                  fullWidth 
                  onClick={() => setShowPasswordModal(true)}
                >
                  <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
                  </svg>
                  Cambiar Contraseña
                </GlassButton>
              </div>
            </>
          )}
        </GlassCard>
      </div>

      {/* Change Password Modal */}
      <ChangePasswordModal
        isOpen={showPasswordModal}
        onClose={() => setShowPasswordModal(false)}
      />
    </div>
  );
};

export default PerfilPage;
