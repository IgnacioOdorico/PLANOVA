/**
 * CommentItem Component for Planova Kanban
 * Glass card displaying a single comment with author, date, and content
 */

import { useState } from 'react';
import { Comentario } from '../../types';
import GlassCard from '../common/GlassCard';
import GlassButton from '../common/GlassButton';
import { format } from 'date-fns';
import { es } from 'date-fns/locale';

interface CommentItemProps {
  comentario: Comentario;
  currentUserId?: number;
  onEdit: (id: number, contenido: string) => void;
  onDelete: (id: number) => void;
}

const CommentItem: React.FC<CommentItemProps> = ({
  comentario,
  currentUserId,
  onEdit,
  onDelete,
}) => {
  const [isEditing, setIsEditing] = useState(false);
  const [editContent, setEditContent] = useState(comentario.contenido);
  const [isDeleting, setIsDeleting] = useState(false);

  const isOwner = currentUserId === comentario.usuarioId;

  const formattedDate = (() => {
    try {
      return format(new Date(comentario.fechaCreacion), "d MMM yyyy, HH:mm", { locale: es });
    } catch {
      return comentario.fechaCreacion;
    }
  })();

  const handleSaveEdit = () => {
    if (editContent.trim() && editContent !== comentario.contenido) {
      onEdit(comentario.id, editContent.trim());
    }
    setIsEditing(false);
  };

  const handleCancelEdit = () => {
    setEditContent(comentario.contenido);
    setIsEditing(false);
  };

  const handleDelete = () => {
    setIsDeleting(true);
    onDelete(comentario.id);
    setIsDeleting(false);
  };

  return (
    <GlassCard variant="subtle" className="p-4">
      <div className="flex gap-3">
        {/* Avatar */}
        <div className="flex-shrink-0">
          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-white/20 to-white/5 border border-white/20 flex items-center justify-center">
            <span className="text-white font-medium text-sm">
              {comentario.usuario?.nombre?.charAt(0).toUpperCase() || '?'}
            </span>
          </div>
        </div>

        {/* Content */}
        <div className="flex-1 min-w-0">
          {/* Header */}
          <div className="flex items-center justify-between gap-2 mb-2">
            <div className="flex items-center gap-2">
              <span className="text-white font-medium text-sm">
                {comentario.usuario?.nombre || 'Usuario'}
              </span>
              <span className="text-white/40 text-xs">•</span>
              <span className="text-white/50 text-xs">{formattedDate}</span>
            </div>

            {/* Actions - only for owner */}
            {isOwner && !isEditing && (
              <div className="flex items-center gap-1">
                <button
                  onClick={() => setIsEditing(true)}
                  className="p-1.5 text-white/40 hover:text-white hover:bg-glass-200 rounded-lg transition-colors"
                  title="Editar comentario"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                  </svg>
                </button>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    e.preventDefault();
                    handleDelete();
                  }}
                  disabled={isDeleting}
                  className="p-1.5 text-white/40 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-colors disabled:opacity-50"
                  title="Eliminar comentario"
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                </button>
              </div>
            )}
          </div>

          {/* Body */}
          {isEditing ? (
            <div className="space-y-2">
              <textarea
                value={editContent}
                onChange={(e) => setEditContent(e.target.value)}
                className="w-full glass-input rounded-lg px-3 py-2 text-sm text-white resize-none min-h-[80px]"
                autoFocus
              />
              <div className="flex gap-2 justify-end">
                <GlassButton
                  variant="ghost"
                  size="sm"
                  onClick={handleCancelEdit}
                >
                  Cancelar
                </GlassButton>
                <GlassButton
                  variant="primary"
                  size="sm"
                  onClick={handleSaveEdit}
                  disabled={!editContent.trim()}
                >
                  Guardar
                </GlassButton>
              </div>
            </div>
          ) : (
            <p className="text-white/80 text-sm whitespace-pre-wrap break-words">
              {comentario.contenido}
            </p>
          )}
        </div>
      </div>
    </GlassCard>
  );
};

export default CommentItem;
