/**
 * Tarea Service for Planova
 * CRUD operations for tareas (tasks), including move between columns
 */

import { Tarea, TareaCreate, TareaUpdate, MoverTarea } from '../types';
import { tareaApi } from '../services/api';

export const tareaService = {
  /**
   * Get all tasks for a column
   */
  getByColumna: async (columnaId: number): Promise<Tarea[]> => {
    return tareaApi.getByColumna(columnaId);
  },

  /**
   * Get a task by ID
   */
  getById: async (id: number): Promise<Tarea> => {
    return tareaApi.getById(id);
  },

  /**
   * Get all tasks for a project
   */
  getByProyecto: async (proyectoId: number): Promise<Tarea[]> => {
    return tareaApi.getByProyecto(proyectoId);
  },

  /**
   * Create a new task in a column
   */
  create: async (columnaId: number, data: TareaCreate): Promise<Tarea> => {
    return tareaApi.create(columnaId, data);
  },

  /**
   * Update an existing task
   */
  update: async (id: number, data: TareaUpdate): Promise<Tarea> => {
    return tareaApi.update(id, data);
  },

  /**
   * Delete a task
   */
  delete: async (id: number): Promise<void> => {
    return tareaApi.delete(id);
  },

  /**
   * Move a task to another column (with new position)
   */
  mover: async (id: number, data: MoverTarea): Promise<void> => {
    return tareaApi.mover(id, data);
  },
};

export default tareaService;