/**
 * TareaNotes Panel for Planova Kanban
 * Glass panel that slides in from right showing comments for a selected task
 */

import { useState, useEffect, useCallback } from 'react';
import { Tarea, Comentario, ComentarioCreate } from '../../types';
import { comentarioService } from '../../services/comentarioService';
import { useAuth } from '../../context/AuthContext';
import CommentItem from './CommentItem';
import GlassCard from '../common/GlassCard';
import GlassButton from '../common/GlassButton';

interface TareaNotesProps {
  tarea: Tarea | null;
  isOpen: boolean;
  onClose: () => void;
}

const TareaNotes: React.FC<TareaNotesProps> = ({ tarea, isOpen, onClose }) => {
  const { user } = useAuth();
  const [comentarios, setComentarios] = useState<Comentario[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [newComment, setNewComment] = useState('');
  const [error, setError] = useState<string | null>(null);

  const loadComentarios = useCallback(async () => {
    if (!tarea) return;

    setIsLoading(true);
    setError(null);
    try {
      const data = await comentarioService.getByTarea(tarea.id);
      setComentarios(data);
    } catch (err) {
      console.error('Error loading comments:', err);
      setError('Error al cargar los comentarios');
    } finally {
      setIsLoading(false);
    }
  }, [tarea]);

  useEffect(() => {
    if (isOpen && tarea) {
      loadComentarios();
    } else {
      setComentarios([]);
      setNewComment('');
    }
  }, [isOpen, tarea, loadComentarios]);

  const handleAddComment = async () => {
    if (!tarea || !newComment.trim()) return;

    setIsSaving(true);
    try {
      const commentData: ComentarioCreate = {
        contenido: newComment.trim(),
      };
      const newCommentObj = await comentarioService.create(tarea.id, commentData);
      setComentarios((prev) => [...prev, newCommentObj]);
      setNewComment('');
    } catch (err) {
      console.error('Error adding comment:', err);
      alert('Error al agregar el comentario');
    } finally {
      setIsSaving(false);
    }
  };

  const handleEditComment = async (id: number, contenido: string) => {
    try {
      const updated = await comentarioService.update(id, contenido);
      setComentarios((prev) =>
        prev.map((c) => (c.id === id ? { ...c, contenido: updated.contenido } : c))
      );
    } catch (err) {
      console.error('Error editing comment:', err);
      alert('Error al editar el comentario');
    }
  };

  const handleDeleteComment = async (id: number) => {
    if (!confirm('¿Estás seguro de que quieres eliminar este comentario?')) return;

    try {
      await comentarioService.delete(id);
      setComentarios((prev) => prev.filter((c) => c.id !== id));
    } catch (err) {
      console.error('Error deleting comment:', err);
      alert('Error al eliminar el comentario');
    }
  };

  if (!isOpen || !tarea) return null;

  return (
    <>
      {/* Backdrop */}
      <div
        className="fixed inset-0 z-40 bg-black/50 backdrop-blur-sm transition-opacity"
        onClick={onClose}
      />

      {/* Slide-in Panel */}
      <div className="fixed right-0 top-0 h-full w-full max-w-md z-50 flex">
        <div className="w-full h-full">
          <GlassCard
            variant="default"
            className="h-full rounded-none flex flex-col"
          >
            {/* Glossy highlight */}
            <div className="absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-white/20 to-transparent" />

            {/* Header */}
            <div className="flex-shrink-0 border-b border-white/10 p-4">
              <div className="flex items-center justify-between">
                <div className="flex-1 min-w-0 pr-4">
                  <h2 className="text-lg font-semibold text-white truncate">
                    Notas de Tarea
                  </h2>
                  <p className="text-sm text-white/50 truncate mt-0.5">
                    {tarea.titulo}
                  </p>
                </div>
                <button
                  onClick={onClose}
                  className="flex-shrink-0 p-2 text-white/60 hover:text-white hover:bg-glass-200 rounded-lg transition-colors"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            </div>

            {/* Comments List */}
            <div className="flex-1 overflow-y-auto p-4 space-y-3">
              {isLoading ? (
                <div className="flex items-center justify-center py-8">
                  <svg className="animate-spin h-6 w-6 text-white/50" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                  </svg>
                </div>
              ) : error ? (
                <div className="text-center py-8 text-red-300">
                  <p>{error}</p>
                  <button
                    onClick={loadComentarios}
                    className="mt-2 text-sm underline hover:text-red-200"
                  >
                    Reintentar
                  </button>
                </div>
              ) : comentarios.length === 0 ? (
                <div className="text-center py-8 text-white/40">
                  <svg className="w-12 h-12 mx-auto mb-3 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                  </svg>
                  <p>No hay comentarios todavía</p>
                  <p className="text-sm mt-1">Sé el primero en agregar uno</p>
                </div>
              ) : (
                comentarios.map((comentario) => (
                  <CommentItem
                    key={comentario.id}
                    comentario={comentario}
                    currentUserId={user?.id}
                    onEdit={handleEditComment}
                    onDelete={handleDeleteComment}
                  />
                ))
              )}
            </div>

            {/* Add Comment Form */}
            <div className="flex-shrink-0 border-t border-white/10 p-4">
              <div className="space-y-3">
                <textarea
                  value={newComment}
                  onChange={(e) => setNewComment(e.target.value)}
                  placeholder="Escribe un comentario..."
                  className="w-full glass-input rounded-xl px-4 py-3 text-white placeholder-white/40 resize-none min-h-[100px]"
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' && e.ctrlKey && !isSaving) {
                      handleAddComment();
                    }
                  }}
                />
                <div className="flex items-center justify-between">
                  <span className="text-xs text-white/40">
                    Ctrl + Enter para enviar
                  </span>
                  <GlassButton
                    variant="primary"
                    size="sm"
                    onClick={handleAddComment}
                    isLoading={isSaving}
                    disabled={!newComment.trim()}
                  >
                    Agregar Comentario
                  </GlassButton>
                </div>
              </div>
            </div>
          </GlassCard>
        </div>
      </div>
    </>
  );
};

export default TareaNotes;
