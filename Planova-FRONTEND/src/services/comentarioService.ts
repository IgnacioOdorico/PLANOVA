/**
 * Comentario Service for Planova
 * CRUD operations for comentarios (comments on tasks)
 */

import { Comentario, ComentarioCreate } from '../types';
import { comentarioApi } from '../services/api';

export const comentarioService = {
  /**
   * Get all comments for a task
   */
  getByTarea: async (tareaId: number): Promise<Comentario[]> => {
    return comentarioApi.getByTarea(tareaId);
  },

  /**
   * Get a comment by ID
   */
  getById: async (id: number): Promise<Comentario> => {
    return comentarioApi.getById(id);
  },

  /**
   * Create a new comment on a task
   */
  create: async (tareaId: number, data: ComentarioCreate): Promise<Comentario> => {
    return comentarioApi.create(tareaId, data);
  },

  /**
   * Update a comment's content
   */
  update: async (id: number, contenido: string): Promise<Comentario> => {
    return comentarioApi.update(id, contenido);
  },

  /**
   * Delete a comment
   */
  delete: async (id: number): Promise<void> => {
    return comentarioApi.delete(id);
  },
};

export default comentarioService;
