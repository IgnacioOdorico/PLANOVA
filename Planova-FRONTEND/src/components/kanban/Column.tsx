import { useState, useMemo, useEffect } from 'react';
import { useDroppable } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { Columna, Tarea, Prioridad, Estado } from '../../types';
import TareaCard from './TareaCard';
import { columnaService } from '../../services/columnaService';
import GlassCard from '../common/GlassCard';

interface ColumnProps {
  column: Columna;
  tareas: Tarea[];
  onAddTask: (columnId: number) => void;
  onEditTask: (tarea: Tarea) => void;
  onDeleteTask: (tareaId: number) => void;
  onDeleteColumn: (columnId: number) => void;
  onEditColumn?: (column: Columna) => void;
  onViewNotes?: (tarea: Tarea) => void;
}

type SortCriteria = 'prioridad' | 'estado' | 'fecha';

const Column: React.FC<ColumnProps> = ({
  column,
  tareas,
  onAddTask,
  onEditTask,
  onDeleteTask,
  onDeleteColumn,
  onEditColumn,
  onViewNotes,
}) => {
  const [sortBy, setSortBy] = useState<SortCriteria>((column.sortingMode as SortCriteria) || 'prioridad');

  // Sync with prop when data reloads (e.g. after task move)
  useEffect(() => {
    if (column.sortingMode && column.sortingMode !== sortBy) {
      setSortBy(column.sortingMode as SortCriteria);
    }
  }, [column.sortingMode]);

  const { setNodeRef, isOver } = useDroppable({
    id: `column-${column.id}`,
    data: { type: 'column', columnId: column.id },
  });

  const sortedTareas = useMemo(() => {
    const priorityOrder: Record<Prioridad, number> = { alta: 3, media: 2, baja: 1 };
    const statusOrder: Record<Estado, number> = { vencida: 4, en_proceso: 3, pendiente: 2, completada: 1 };

    return [...tareas].sort((a, b) => {
      if (sortBy === 'prioridad') {
        const diff = priorityOrder[b.prioridad] - priorityOrder[a.prioridad];
        if (diff !== 0) return diff;
        // Fallback to title
        return a.titulo.localeCompare(b.titulo);
      }
      if (sortBy === 'estado') {
        const diff = statusOrder[b.estado] - statusOrder[a.estado];
        if (diff !== 0) return diff;
        return a.titulo.localeCompare(b.titulo);
      }
      if (sortBy === 'fecha') {
        if (!a.fechaVencimiento && !b.fechaVencimiento) return a.titulo.localeCompare(b.titulo);
        if (!a.fechaVencimiento) return 1;
        if (!b.fechaVencimiento) return -1;
        const diff = new Date(a.fechaVencimiento).getTime() - new Date(b.fechaVencimiento).getTime();
        if (diff !== 0) return diff;
        return a.titulo.localeCompare(b.titulo);
      }
      return 0;
    });
  }, [tareas, sortBy]);

  const toggleSort = async () => {
    const sequence: SortCriteria[] = ['prioridad', 'fecha', 'estado'];
    const currentIndex = sequence.indexOf(sortBy);
    const nextIndex = (currentIndex + 1) % sequence.length;
    const nextSort = sequence[nextIndex];
    
    // Optimistic update
    setSortBy(nextSort);
    
    // Persist to backend
    try {
      await columnaService.update(column.id, { sortingMode: nextSort });
    } catch (err) {
      console.error('Error saving sort preference:', err);
    }
  };

  const getSortIcon = () => {
    switch(sortBy) {
      case 'prioridad': return (
        <svg className="w-4 h-4 text-yellow-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
        </svg>
      );
      case 'estado': return (
        <svg className="w-4 h-4 text-purple-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      );
      case 'fecha': return (
        <svg className="w-4 h-4 text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      );
    }
  };

  const getSortTitle = () => {
    switch(sortBy) {
      case 'prioridad': return "Ordenando por: Prioridad (Alta a Baja)";
      case 'fecha': return "Ordenando por: Fecha de Vencimiento (Más cercana)";
      case 'estado': return "Ordenando por: Estado (Vencida a Completada)";
    }
  };

  return (
    <div 
      ref={setNodeRef}
      className={`flex-shrink-0 w-[320px] h-full transition-colors duration-200 ${isOver ? 'bg-white/5' : ''} rounded-3xl`}
    >
      <GlassCard 
        variant="extreme"
        sparkleCount={10}
        className="h-full flex flex-col rounded-3xl"
      >
        {/* Column Header */}
        <div className="p-4 border-b-4 border-white/10">
          <div className="flex items-center justify-between gap-2">
            <h3 
              className="text-xl font-light text-white uppercase tracking-wider line-clamp-2 break-words flex-1 text-center" 
              style={{ fontFamily: 'Kalam, cursive' }} 
              title={column.titulo}
            >
              {column.titulo}
            </h3>
            
            <div className="flex items-center gap-1">
              {/* Sort Cycle Button */}
              <button
                onClick={toggleSort}
                className="p-1.5 rounded-lg transition-all bg-white/10 shadow-lg hover:bg-white/20"
                title={getSortTitle()}
              >
                {getSortIcon()}
              </button>

              {onEditColumn && (
                <button
                  onClick={() => onEditColumn(column)}
                  className="p-1.5 text-white/50 hover:text-white/80 hover:bg-glass-200 rounded-lg transition-colors"
                  title="Editar columna"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                  </svg>
                </button>
              )}

              <button
                onClick={(e) => {
                  e.stopPropagation();
                  e.preventDefault();
                  onDeleteColumn(column.id);
                }}
                className="p-1.5 text-white/50 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-colors relative z-50"
                title="Eliminar columna"
              >
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
              </button>
            </div>
          </div>
        </div>

        {/* Task List - Drop Zone (Inside) */}
        <div
          className="flex-1 p-3 overflow-y-auto space-y-3 min-h-[200px]"
        >
          <SortableContext
            items={sortedTareas.map(t => `tarea-${t.id}`)}
            strategy={verticalListSortingStrategy}
          >
            {sortedTareas.map((tarea) => (
              <TareaCard
                key={tarea.id}
                tarea={tarea}
                onEdit={() => onEditTask(tarea)}
                onDelete={() => onDeleteTask(tarea.id)}
                onViewNotes={() => onViewNotes?.(tarea)}
              />
            ))}
          </SortableContext>

          {tareas.length === 0 && (
            <div className="flex flex-col items-center justify-center py-8 text-white/40">
              <svg className="w-12 h-12 mb-2 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
              </svg>
              <p className="text-sm">No hay tareas</p>
              <p className="text-xs mt-1">Arrastra tareas aqui para cambiar de columna</p>
            </div>
          )}
        </div>

        {/* Add Task Button */}
        <div 
          className="p-4 border-t-4 border-white/10 flex justify-center cursor-pointer hover:bg-white/5 transition-colors rounded-b-3xl"
          onClick={() => onAddTask(column.id)}
        >
          <span className="text-white/60 text-4xl font-light hover:text-white transition-colors" style={{ lineHeight: '0.8' }}>+</span>
        </div>
      </GlassCard>
    </div>
  );
};



export default Column;