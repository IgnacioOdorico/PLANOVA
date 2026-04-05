/**
 * Column Component for Planova Kanban Board
 * Glass column with title, task list, drop zone, and add task button
 */

import { useDroppable } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy } from '@dnd-kit/sortable';
import { Columna, Tarea } from '../../types';
import TareaCard from './TareaCard';

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
  const { setNodeRef, isOver } = useDroppable({
    id: `column-${column.id}`,
    data: { type: 'column', columnId: column.id },
  });

  return (
    <div className="flex-shrink-0 w-[320px]">
      <div 
        className="h-full flex flex-col rounded-3xl backdrop-blur-lg"
        style={{
          border: '4px solid rgba(255,255,255,0.15)',
          background: 'rgba(255,255,255,0.02)',
          boxShadow: '0 8px 32px 0 rgba(0, 0, 0, 0.1)'
        }}
      >
        {/* Column Header */}
        <div className="p-4 border-b-4 border-white/10 text-center">
          <div className="flex items-center justify-between">
            <div className="flex flex-col items-center justify-center w-full">
              <h3 className="text-2xl font-light text-white uppercase tracking-wider" style={{ fontFamily: 'Kalam, cursive' }}>{column.titulo}</h3>
            </div>
            <div className="absolute right-4 flex items-center gap-1">
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

        {/* Task List - Drop Zone */}
        <div
          ref={setNodeRef}
          className={`
            flex-1 p-3 overflow-y-auto space-y-3 min-h-[200px]
            transition-colors duration-200
            ${isOver ? 'bg-white/5' : ''}
          `}
        >
          <SortableContext
            items={tareas.map(t => `tarea-${t.id}`)}
            strategy={verticalListSortingStrategy}
          >
            {tareas.map((tarea) => (
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
              <p className="text-xs mt-1">Arrastra tareas aqui o crea una nueva</p>
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
      </div>
    </div>
  );
};

export default Column;