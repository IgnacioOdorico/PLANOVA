/**
 * TareaCard Component for Planova Kanban Board
 * Glass card showing task info, priority badge, and drag handle
 */

import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Tarea, PrioridadLabels, EstadoLabels, Estado } from '../../types';

interface TareaCardProps {
  tarea: Tarea;
  onEdit: () => void;
  onDelete: () => void;
  onViewNotes?: () => void;
}

const estadoColors: Record<Estado, string> = {
  completada: 'bg-[rgba(16,185,129,0.2)] border-emerald-400/40 text-emerald-100',
  vencida: 'bg-[rgba(244,63,94,0.2)] border-rose-400/40 text-rose-100',
  en_proceso: 'bg-[rgba(59,130,246,0.2)] border-blue-400/40 text-blue-100',
  pendiente: 'bg-[rgba(245,158,11,0.2)] border-amber-400/40 text-amber-100',
};

const TareaCard: React.FC<TareaCardProps> = ({ tarea, onEdit, onDelete, onViewNotes }) => {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id: `tarea-${tarea.id}`,
    data: { type: 'tarea', tarea },
  });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
  };

  const formattedDate = tarea.fechaVencimiento
    ? new Date(tarea.fechaVencimiento).toLocaleDateString('es-AR', {
        day: '2-digit',
        month: 'short',
        hour: '2-digit',
        minute: '2-digit'
      })
    : null;

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={`relative ${isDragging ? 'opacity-50' : ''}`}
    >
      {/* Sticky Tape Effect */}
      <div className="absolute -top-2 left-1/2 -translate-x-1/2 w-12 h-5 bg-white/20 backdrop-blur-md rounded-sm rotate-2 shadow-sm z-10" />

      <div 
        className={`p-4 cursor-pointer transition-all hover:scale-[1.02] group rounded-xl border ${estadoColors[tarea.estado]} backdrop-blur-md`}
        onClick={onEdit}
        style={{
          boxShadow: '0 4px 15px rgba(0,0,0,0.1)'
        }}
      >
        <div className="flex items-start gap-3">
          {/* Drag Handle */}
          <button
            {...attributes}
            {...listeners}
            className="flex-shrink-0 mt-1 text-white/30 hover:text-white/60 cursor-grab active:cursor-grabbing"
            onClick={(e) => e.stopPropagation()}
          >
            <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
              <path d="M8 6a2 2 0 1 1-4 0 2 2 0 0 1 4 0zM8 12a2 2 0 1 1-4 0 2 2 0 0 1 4 0zM8 18a2 2 0 1 1-4 0 2 2 0 0 1 4 0zM14 6a2 2 0 1 1-4 0 2 2 0 0 1 4 0zM14 12a2 2 0 1 1-4 0 2 2 0 0 1 4 0zM14 18a2 2 0 1 1-4 0 2 2 0 0 1 4 0z" />
            </svg>
          </button>

          {/* Content */}
          <div className="flex-1 min-w-0">
            <h4 className="text-sm font-medium text-white truncate">{tarea.titulo}</h4>
            
            {tarea.descripcion && (
              <p className="text-xs text-white/50 mt-1 line-clamp-2">{tarea.descripcion}</p>
            )}

            {formattedDate && (
              <div className="text-[0.7rem] font-medium mt-1 flex items-center gap-1 opacity-80" style={{
                color: tarea.estado === 'vencida' ? '#fecdd3' : 'inherit'
              }}>
                <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                {formattedDate}
              </div>
            )}

            {/* Badges */}
            <div className="mt-2 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className={`px-2 py-0.5 text-[0.65rem] font-medium rounded-md border ${estadoColors[tarea.estado]}`}>
                  {EstadoLabels[tarea.estado]}
                </span>
                <span className="px-2 py-0.5 text-[0.65rem] font-medium rounded-md border border-white/20 text-white/60">
                  {PrioridadLabels[tarea.prioridad]}
                </span>
              </div>
              
              {/* Actions */}
              <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                {/* Notes Button */}
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onViewNotes?.();
                  }}
                  className="p-1 text-white/40 hover:text-white hover:bg-glass-200 rounded transition-all"
                  title="Ver notas y comentarios"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
                  </svg>
                </button>
                
                {/* Delete Button */}
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    e.preventDefault();
                    onDelete();
                  }}
                  className="p-1 text-white/40 hover:text-red-400 hover:bg-red-500/10 rounded transition-all relative z-10"
                  title="Eliminar tarea"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TareaCard;