/**
 * Kanban Page for Planova
 * Project board with columns and tasks using @dnd-kit for drag-and-drop
 */

import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  DndContext,
  DragOverlay,
  closestCorners,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors,
  DragStartEvent,
  DragEndEvent,
  DragOverEvent,
} from '@dnd-kit/core';
import { sortableKeyboardCoordinates } from '@dnd-kit/sortable';
import { Columna, Tarea, TareaCreate, MoverTarea } from '../types';
import { proyectoService } from '../services/proyectoService';
import { columnaService } from '../services/columnaService';
import { tareaService } from '../services/tareaService';
import Column from '../components/kanban/Column';
import TareaCard from '../components/kanban/TareaCard';
import TareaModal from '../components/kanban/TareaModal';
import TareaNotes from '../components/kanban/TareaNotes';
import GlassCard from '../components/common/GlassCard';
import GlassButton from '../components/common/GlassButton';
import GlassInput from '../components/common/GlassInput';

interface ColumnaWithTareas extends Columna {
  tareas: Tarea[];
}

export const KanbanPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const proyectoId = parseInt(id || '0', 10);

  const [proyecto, setProyecto] = useState<{ id: number; nombre: string } | null>(null);
  const [columnas, setColumnas] = useState<ColumnaWithTareas[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Modal states
  const [isTareaModalOpen, setIsTareaModalOpen] = useState(false);
  const [selectedTarea, setSelectedTarea] = useState<Tarea | null>(null);
  const [selectedColumnId, setSelectedColumnId] = useState<number | null>(null);
  const [isSavingTarea, setIsSavingTarea] = useState(false);

  // Column modal
  const [isColumnModalOpen, setIsColumnModalOpen] = useState(false);
  const [newColumnTitle, setNewColumnTitle] = useState('');
  const [isSavingColumn, setIsSavingColumn] = useState(false);

  // Drag state
  const [activeTarea, setActiveTarea] = useState<Tarea | null>(null);

  // Notes panel state
  const [isNotesPanelOpen, setIsNotesPanelOpen] = useState(false);
  const [notesTarea, setNotesTarea] = useState<Tarea | null>(null);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    }),
    useSensor(KeyboardSensor, {
      coordinateGetter: sortableKeyboardCoordinates,
    })
  );

  // Load project and columns
  const loadData = useCallback(async () => {
    if (!proyectoId) return;

    try {
      setIsLoading(true);
      setError(null);

      const proyectoData = await proyectoService.getById(proyectoId);
      setProyecto(proyectoData);

      const columnasData = await columnaService.getByProyecto(proyectoId);
      
      // Load tasks for each column
      const columnsWithTasks = await Promise.all(
        columnasData.map(async (col) => {
          const tareas = await tareaService.getByColumna(col.id);
          return { ...col, tareas };
        })
      );

      setColumnas(columnsWithTasks);
    } catch (err) {
      setError('Error al cargar el proyecto');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  }, [proyectoId]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // Task CRUD
  const handleSaveTarea = async (data: TareaCreate, tareaId?: number) => {
    setIsSavingTarea(true);
    try {
      if (tareaId) {
        // Update existing task
        await tareaService.update(tareaId, data);
      } else if (selectedColumnId) {
        // Create new task
        await tareaService.create(selectedColumnId, data);
      }
      await loadData();
    } finally {
      setIsSavingTarea(false);
    }
  };

  const handleDeleteTarea = async (tareaId: number) => {
    if (!confirm('¿Estás seguro de que quieres eliminar esta tarea?')) return;
    
    try {
      await tareaService.delete(tareaId);
      await loadData();
    } catch (err) {
      alert('Error al eliminar la tarea');
    }
  };

  const handleAddTask = (columnId: number) => {
    setSelectedTarea(null);
    setSelectedColumnId(columnId);
    setIsTareaModalOpen(true);
  };

  const handleEditTask = (tarea: Tarea) => {
    setSelectedTarea(tarea);
    setSelectedColumnId(tarea.id); // Used as indicator for edit mode
    setIsTareaModalOpen(true);
  };

  const handleViewNotes = (tarea: Tarea) => {
    setNotesTarea(tarea);
    setIsNotesPanelOpen(true);
  };

  // Column CRUD
  const handleAddColumn = async () => {
    if (!newColumnTitle.trim() || !proyectoId) return;

    setIsSavingColumn(true);
    try {
      await columnaService.create(proyectoId, {
        titulo: newColumnTitle.trim(),
        orden: columnas.length,
      });
      setNewColumnTitle('');
      setIsColumnModalOpen(false);
      await loadData();
    } catch (err) {
      alert('Error al crear la columna');
    } finally {
      setIsSavingColumn(false);
    }
  };

  const handleDeleteColumn = async (columnId: number) => {
    if (!confirm('¿Estás seguro de que quieres eliminar esta columna y todas sus tareas?')) return;

    try {
      await columnaService.delete(columnId);
      await loadData();
    } catch (err) {
      alert('Error al eliminar la columna');
    }
  };

  // Drag and Drop handlers
  const handleDragStart = (event: DragStartEvent) => {
    const { active } = event;
    const tareaId = active.id.toString().replace('tarea-', '');
    
    // Find the task in columns
    for (const col of columnas) {
      const tarea = col.tareas.find(t => t.id === parseInt(tareaId, 10));
      if (tarea) {
        setActiveTarea(tarea);
        break;
      }
    }
  };

  const handleDragOver = (event: DragOverEvent) => {
    const { active, over } = event;
    if (!over) return;

    const activeId = active.id.toString();
    const overId = over.id.toString();

    // Only handle tarea-to-tarea or tarea-to-column dragging
    if (!activeId.startsWith('tarea-')) return;

    const activeTareaId = parseInt(activeId.replace('tarea-', ''), 10);
    const overTareaId = overId.startsWith('tarea-') ? parseInt(overId.replace('tarea-', ''), 10) : null;
    const overColumnId = overId.startsWith('column-') ? parseInt(overId.replace('column-', ''), 10) : null;

    // Find source column
    let sourceColumnIndex = -1;
    let sourceTareaIndex = -1;
    for (let i = 0; i < columnas.length; i++) {
      const idx = columnas[i].tareas.findIndex(t => t.id === activeTareaId);
      if (idx !== -1) {
        sourceColumnIndex = i;
        sourceTareaIndex = idx;
        break;
      }
    }

    if (sourceColumnIndex === -1) return;

    // Determine target column and position
    let targetColumnIndex = -1;
    let targetTareaIndex = 0;

    if (overColumnId) {
      // Dropping on a column
      targetColumnIndex = columnas.findIndex(c => c.id === overColumnId);
    } else if (overTareaId !== null) {
      // Dropping on a task - find which column it's in
      for (let i = 0; i < columnas.length; i++) {
        const idx = columnas[i].tareas.findIndex(t => t.id === overTareaId);
        if (idx !== -1) {
          targetColumnIndex = i;
          targetTareaIndex = idx;
          break;
        }
      }
    }

    if (targetColumnIndex === -1) return;

    // If same column and same position, no need to update state
    if (sourceColumnIndex === targetColumnIndex && sourceTareaIndex === targetTareaIndex) return;

    // Optimistic update - move within state
    const newColumnas = [...columnas];
    const sourceColumn = { ...newColumnas[sourceColumnIndex] };
    const tarea = sourceColumn.tareas[sourceTareaIndex];
    
    sourceColumn.tareas = [
      ...sourceColumn.tareas.slice(0, sourceTareaIndex),
      ...sourceColumn.tareas.slice(sourceTareaIndex + 1),
    ];

    if (sourceColumnIndex === targetColumnIndex) {
      // Moving within same column
      const insertIndex = targetTareaIndex > sourceTareaIndex ? targetTareaIndex : targetTareaIndex;
      sourceColumn.tareas = [
        ...sourceColumn.tareas.slice(0, insertIndex),
        tarea,
        ...sourceColumn.tareas.slice(insertIndex),
      ];
      newColumnas[sourceColumnIndex] = sourceColumn;
    } else {
      // Moving to different column
      newColumnas[sourceColumnIndex] = sourceColumn;
      
      const targetColumn = { ...newColumnas[targetColumnIndex] };
      targetColumn.tareas = [
        ...targetColumn.tareas.slice(0, targetTareaIndex),
        tarea,
        ...targetColumn.tareas.slice(targetTareaIndex),
      ];
      newColumnas[targetColumnIndex] = targetColumn;
    }

    setColumnas(newColumnas);
  };

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;
    setActiveTarea(null);

    if (!over) return;

    const activeId = active.id.toString();
    const overId = over.id.toString();

    if (!activeId.startsWith('tarea-')) return;

    const activeTareaId = parseInt(activeId.replace('tarea-', ''), 10);
    const overTareaId = overId.startsWith('tarea-') ? parseInt(overId.replace('tarea-', ''), 10) : null;
    const overColumnId = overId.startsWith('column-') ? parseInt(overId.replace('column-', ''), 10) : null;

    // Find source column
    let sourceColumn: ColumnaWithTareas | null = null;
    for (let i = 0; i < columnas.length; i++) {
      const idx = columnas[i].tareas.findIndex(t => t.id === activeTareaId);
      if (idx !== -1) {
        sourceColumn = columnas[i];
        break;
      }
    }

    if (!sourceColumn) return;

    // Determine target column and new position
    let targetColumnId: number | null = null;
    let newOrden: number | null = null;

    if (overColumnId) {
      targetColumnId = overColumnId;
      newOrden = 0;
    } else if (overTareaId !== null) {
      for (let i = 0; i < columnas.length; i++) {
        const idx = columnas[i].tareas.findIndex(t => t.id === overTareaId);
        if (idx !== -1) {
          targetColumnId = columnas[i].id;
          newOrden = idx;
          break;
        }
      }
    } else {
      return;
    }

    if (targetColumnId === null || newOrden === null) return;

    // If moved within same column, we already updated state optimistically
    // Now we need to sync with backend
    try {
      const moveData: MoverTarea = {
        columnaId: targetColumnId,
        orden: newOrden,
      };
      await tareaService.mover(activeTareaId, moveData);
      // Reload to get proper order from server
      await loadData();
    } catch (err) {
      console.error('Error moving task:', err);
      // Revert to server state
      await loadData();
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-full p-8">
        <div className="glass-card p-8 rounded-2xl">
          <div className="flex items-center gap-3">
            <svg className="animate-spin h-6 w-6 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
            </svg>
            <span className="text-white/80">Cargando proyecto...</span>
          </div>
        </div>
      </div>
    );
  }

  if (error || !proyecto) {
    return (
      <div className="flex items-center justify-center h-full p-8">
        <GlassCard variant="elevated" className="p-8 text-center">
          <h2 className="text-xl font-semibold text-white mb-2">Error</h2>
          <p className="text-white/60 mb-4">{error || 'Proyecto no encontrado'}</p>
          <GlassButton onClick={() => navigate('/home')}>
            Volver a proyectos
          </GlassButton>
        </GlassCard>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <header className="glass-header border-b border-white/10 px-6 py-4 flex-shrink-0">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <button
              onClick={() => navigate('/home')}
              className="p-2 text-white/60 hover:text-white hover:bg-glass-200 rounded-lg transition-colors"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
              </svg>
            </button>
            <div className="flex flex-col min-w-0">
              <p className="text-sm font-medium text-white/50 tracking-wider uppercase">Tablero Kanban</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
             <GlassButton
              variant="danger"
              className="!bg-red-500/20 hover:!bg-red-500/40 text-red-100 border-red-500/30"
              onClick={async (e) => {
                e.stopPropagation();
                e.preventDefault();
                if (confirm('¿Estás seguro de que quieres eliminar este proyecto y todo su contenido?')) {
                  try {
                    await proyectoService.delete(proyectoId);
                    navigate('/home');
                  } catch (err) {
                    alert('Error al borrar el proyecto.');
                  }
                }
              }}
              leftIcon={
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                </svg>
              }
            >
              Borrar Proyecto
            </GlassButton>
            <GlassButton
              variant="primary"
              onClick={() => setIsColumnModalOpen(true)}
              leftIcon={
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                </svg>
              }
            >
              Nueva Columna
            </GlassButton>
          </div>
        </div>
      </header>

      {/* Kanban Board */}
      <main className="flex-1 overflow-x-auto p-6 flex flex-col">
        
        {/* HUGE Centered Project Title & Description */}
        <div className="w-full flex flex-col items-center justify-center mb-8 shrink-0">
          <h1 className="text-4xl md:text-5xl font-bold text-white tracking-wide uppercase text-center drop-shadow-xl" style={{ fontFamily: 'Kalam, cursive' }}>
            {proyecto.nombre}
          </h1>
          {/* @ts-ignore */}
          {proyecto.descripcion && (
            <p className="mt-3 text-white/70 text-center max-w-3xl text-sm md:text-base leading-relaxed drop-shadow-md">
              {/* @ts-ignore */}
              {(proyecto as any).descripcion}
            </p>
          )}
        </div>

        <div className="flex gap-6 flex-1 min-h-[500px]">
          <DndContext
            sensors={sensors}
            collisionDetection={closestCorners}
            onDragStart={handleDragStart}
            onDragOver={handleDragOver}
            onDragEnd={handleDragEnd}
          >
            {columnas.map((column) => (
              <Column
                key={column.id}
                column={column}
                tareas={column.tareas}
                onAddTask={handleAddTask}
                onEditTask={handleEditTask}
                onDeleteTask={handleDeleteTarea}
                onDeleteColumn={handleDeleteColumn}
                onViewNotes={handleViewNotes}
              />
            ))}

            {/* Add Column Button */}
            <div className="flex-shrink-0 w-[320px]">
              <GlassCard
                variant="subtle"
                className="h-full flex items-center justify-center cursor-pointer hover:bg-glass-100 transition-colors"
                onClick={() => setIsColumnModalOpen(true)}
              >
                <div className="text-center text-white/50">
                  <svg className="w-12 h-12 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 4v16m8-8H4" />
                  </svg>
                  <p className="font-medium">Agregar Columna</p>
                </div>
              </GlassCard>
            </div>

            <DragOverlay>
              {activeTarea && (
                <div className="w-[288px]">
                  <TareaCard
                    tarea={activeTarea}
                    onEdit={() => {}}
                    onDelete={() => {}}
                  />
                </div>
              )}
            </DragOverlay>
          </DndContext>
        </div>
      </main>

      {/* Task Modal */}
      <TareaModal
        isOpen={isTareaModalOpen}
        onClose={() => {
          setIsTareaModalOpen(false);
          setSelectedTarea(null);
          setSelectedColumnId(null);
        }}
        onSave={handleSaveTarea}
        tarea={selectedTarea}
        isLoading={isSavingTarea}
      />

      {/* Column Modal */}
      {isColumnModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div 
            className="absolute inset-0 bg-black/60 backdrop-blur-sm"
            onClick={() => setIsColumnModalOpen(false)}
          />
          <GlassCard variant="elevated" className="w-full max-w-sm relative">
            <div className="p-4 border-b border-white/10">
              <h2 className="text-xl font-semibold text-white">Nueva Columna</h2>
            </div>
            <div className="p-4">
              <GlassInput
                label="Nombre de la columna"
                value={newColumnTitle}
                onChange={(e) => setNewColumnTitle(e.target.value)}
                placeholder="Ej: Por hacer, En progreso, Completado"
                onKeyDown={(e) => e.key === 'Enter' && handleAddColumn()}
              />
              <div className="flex gap-3 mt-4">
                <GlassButton
                  variant="secondary"
                  fullWidth
                  onClick={() => setIsColumnModalOpen(false)}
                >
                  Cancelar
                </GlassButton>
                <GlassButton
                  variant="primary"
                  fullWidth
                  onClick={handleAddColumn}
                  isLoading={isSavingColumn}
                >
                  Crear
                </GlassButton>
              </div>
              </div>
            </GlassCard>
        </div>
      )}

      {/* Notes Panel */}
      <TareaNotes
        tarea={notesTarea}
        isOpen={isNotesPanelOpen}
        onClose={() => {
          setIsNotesPanelOpen(false);
          setNotesTarea(null);
        }}
      />
    </div>
  );
};

export default KanbanPage;