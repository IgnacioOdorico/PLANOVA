/**
 * CreateProjectModal Component for Planova
 * Glass modal for creating new proyecto with form
 */

import React, { useState, useEffect } from 'react';
import GlassCard from '../common/GlassCard';
import GlassButton from '../common/GlassButton';
import GlassInput from '../common/GlassInput';
import { Proyecto, ProyectoCreate } from '../../types';
import { getApiErrorMessage } from '../../services/api';

interface CreateProjectModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (data: ProyectoCreate, id?: number) => Promise<void>;
  project?: Proyecto | null;
}

export const CreateProjectModal: React.FC<CreateProjectModalProps> = ({
  isOpen,
  onClose,
  onSubmit,
  project,
}) => {
  const [nombre, setNombre] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (project) {
      setNombre(project.nombre);
      setDescripcion(project.descripcion || '');
    } else {
      setNombre('');
      setDescripcion('');
    }
    setError('');
  }, [project, isOpen]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!nombre.trim()) {
      setError('El nombre del proyecto es obligatorio');
      return;
    }

    setError('');
    setIsLoading(true);

    try {
      await onSubmit({
        nombre: nombre.trim(),
        descripcion: descripcion.trim() || undefined,
      }, project?.id);
      
      // Reset form and close
      if (!project) {
        setNombre('');
        setDescripcion('');
      }
      onClose();
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    if (!project) {
      setNombre('');
      setDescripcion('');
    }
    setError('');
    onClose();
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div 
        className="absolute inset-0 bg-black/50 backdrop-blur-sm"
        onClick={handleClose}
      />
      
      {/* Modal Content */}
      <GlassCard 
        variant="elevated" 
        className="w-full max-w-md relative z-10"
      >
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-semibold text-white">
            {project ? 'Editar Proyecto' : 'Nuevo Proyecto'}
          </h2>
          <button
            onClick={handleClose}
            className="p-2 rounded-lg text-white/60 hover:text-white hover:bg-white/10 transition-colors"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit}>
          <div className="space-y-4">
            <GlassInput
              label="Nombre del proyecto"
              placeholder="Mi nuevo proyecto"
              value={nombre}
              onChange={(e) => setNombre(e.target.value)}
              required
              autoFocus
              maxLength={50}
            />

            <div className="w-full">
              <label className="block text-sm font-medium text-white/80 mb-1.5">
                Descripción (opcional)
                <span className="text-[10px] ml-2 opacity-40">({descripcion.length}/150)</span>
              </label>
              <textarea
                className="w-full glass-input px-4 py-2.5 text-base rounded-xl bg-glass-100 border border-white/10 text-white placeholder-white/30 focus:outline-none focus:ring-2 focus:ring-white/20 focus:border-white/20 transition-all duration-300 resize-none font-sans"
                placeholder="Describe tu proyecto..."
                rows={3}
                value={descripcion}
                onChange={(e) => setDescripcion(e.target.value)}
                maxLength={150}
              />
            </div>

            {error && (
              <p className="text-sm text-red-300 flex items-center gap-1">
                <svg className="w-4 h-4 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1-1|v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                </svg>
                {error}
              </p>
            )}
          </div>

          {/* Actions */}
          <div className="flex gap-3 mt-6">
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
              {project ? 'Guardar Cambios' : 'Crear Proyecto'}
            </GlassButton>
          </div>
        </form>
      </GlassCard>
    </div>
  );
};;

export default CreateProjectModal;