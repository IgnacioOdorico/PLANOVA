/**
 * TareaModal Component for Planova Kanban Board
 * Glass modal for task create/edit form
 */

import { useState, useEffect } from 'react';
import { Tarea, TareaCreate, Prioridad, PrioridadLabels, PRIORIDADES, EstadoLabels, ESTADOS, Estado } from '../../types';
import GlassCard from '../common/GlassCard';
import GlassButton from '../common/GlassButton';
import GlassInput from '../common/GlassInput';

interface TareaModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (data: TareaCreate, tareaId?: number) => Promise<void>;
  tarea?: Tarea | null;
  isLoading?: boolean;
}

const TareaModal: React.FC<TareaModalProps> = ({
  isOpen,
  onClose,
  onSave,
  tarea,
  isLoading = false,
}) => {
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [prioridad, setPrioridad] = useState<Prioridad>('media');
  const [estado, setEstado] = useState<Estado>('pendiente');
  const [fechaVencimiento, setFechaVencimiento] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    if (tarea) {
      setTitulo(tarea.titulo);
      setDescripcion(tarea.descripcion || '');
      setPrioridad(tarea.prioridad);
      setEstado(tarea.estado || 'pendiente');
      
      // format date from ISO to YYYY-MM-DDThh:mm
      if (tarea.fechaVencimiento) {
        setFechaVencimiento(tarea.fechaVencimiento.slice(0, 16));
      } else {
        setFechaVencimiento('');
      }
    } else {
      setTitulo('');
      setDescripcion('');
      setPrioridad('media');
      setEstado('pendiente');
      setFechaVencimiento('');
    }
    setError('');
  }, [tarea, isOpen]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!titulo.trim()) {
      setError('El título es requerido');
      return;
    }

    const data: TareaCreate = {
      titulo: titulo.trim(),
      descripcion: descripcion.trim() || undefined,
      prioridad,
      estado,
      ...(fechaVencimiento ? { fechaVencimiento: new Date(fechaVencimiento).toISOString() } : { fechaVencimiento: null })
    };

    try {
      await onSave(data, tarea?.id);
      onClose();
    } catch (err) {
      setError('Error al guardar la tarea');
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div 
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* Modal */}
      <GlassCard variant="elevated" className="w-full max-w-md relative">
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-white/10">
          <h2 className="text-xl font-semibold text-white">
            {tarea ? 'Editar Tarea' : 'Nueva Tarea'}
          </h2>
          <button
            onClick={onClose}
            className="p-2 text-white/50 hover:text-white hover:bg-glass-200 rounded-lg transition-colors"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-4 space-y-4">
          {error && (
            <div className="p-3 bg-red-500/20 border border-red-400/30 rounded-lg text-red-300 text-sm">
              {error}
            </div>
          )}

          <GlassInput
            label="Título"
            value={titulo}
            onChange={(e) => setTitulo(e.target.value)}
            placeholder="Nombre de la tarea"
            required
          />

          <div>
            <label className="block text-sm font-medium text-white/80 mb-1.5">
              Descripción
            </label>
            <textarea
              value={descripcion}
              onChange={(e) => setDescripcion(e.target.value)}
              placeholder="Descripción opcional de la tarea"
              rows={3}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-white placeholder-white/30 resize-none"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-white/80 mb-1.5">
              Prioridad
            </label>
            <div className="flex gap-2">
              {PRIORIDADES.map((p) => (
                <button
                  key={p}
                  type="button"
                  onClick={() => setPrioridad(p)}
                  className={`
                    flex-1 py-2 px-3 rounded-lg text-sm font-medium border transition-all
                    ${prioridad === p 
                      ? p === 'alta' 
                        ? 'bg-red-500/30 border-red-400/50 text-red-200'
                        : p === 'media'
                          ? 'bg-yellow-500/30 border-yellow-400/50 text-yellow-200'
                          : 'bg-blue-500/30 border-blue-400/50 text-blue-200'
                      : 'bg-glass-100 border-white/10 text-white/60 hover:bg-glass-150 hover:border-white/20'
                    }
                  `}
                >
                  {PrioridadLabels[p]}
                </button>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-white/80 mb-1.5">
              Estado
            </label>
            <div className="flex gap-2">
              {ESTADOS.map((e) => (
                <button
                  key={e}
                  type="button"
                  onClick={() => setEstado(e)}
                  className={`
                    flex-1 py-2 px-1 rounded-lg text-[0.7rem] font-medium text-center border transition-all
                    ${estado === e 
                      ? 'bg-purple-500/30 border-purple-400/50 text-purple-200'
                      : 'bg-glass-50 border-white/10 text-white/40 hover:bg-glass-150 hover:border-white/20'
                    }
                  `}
                >
                  {EstadoLabels[e]}
                </button>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-white/80 mb-1.5">
              Fecha de Vencimiento (Opcional)
            </label>
            <input
              type="datetime-local"
              value={fechaVencimiento}
              onChange={(e) => setFechaVencimiento(e.target.value)}
              className="w-full glass-input rounded-xl px-4 py-2.5 text-white bg-transparent border border-white/20 hover:border-white/40 focus:border-white/60 focus:outline-none"
              style={{ colorScheme: 'dark' }}
            />
          </div>

          {/* Actions */}
          <div className="flex gap-3 pt-2">
            <GlassButton
              type="button"
              variant="secondary"
              fullWidth
              onClick={onClose}
            >
              Cancelar
            </GlassButton>
            <GlassButton
              type="submit"
              variant="primary"
              fullWidth
              isLoading={isLoading}
            >
              {tarea ? 'Guardar' : 'Crear'}
            </GlassButton>
          </div>
        </form>
      </GlassCard>
    </div>
  );
};

export default TareaModal;