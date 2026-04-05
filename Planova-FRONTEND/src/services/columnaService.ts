/**
 * Columna Service for Planova
 * CRUD operations for columnas (columns)
 */

import { Columna, ColumnaCreate } from '../types';
import { columnaApi } from '../services/api';

export const columnaService = {
  /**
   * Get all columns for a project
   */
  getByProyecto: async (proyectoId: number): Promise<Columna[]> => {
    return columnaApi.getByProyecto(proyectoId);
  },

  /**
   * Get a column by ID
   */
  getById: async (id: number): Promise<Columna> => {
    return columnaApi.getById(id);
  },

  /**
   * Create a new column
   */
  create: async (proyectoId: number, data: ColumnaCreate): Promise<Columna> => {
    return columnaApi.create(proyectoId, data);
  },

  /**
   * Update an existing column
   */
  update: async (id: number, data: Partial<ColumnaCreate>): Promise<Columna> => {
    return columnaApi.update(id, data);
  },

  /**
   * Delete a column
   */
  delete: async (id: number): Promise<void> => {
    return columnaApi.delete(id);
  },

  /**
   * Reorder a column
   */
  reorder: async (id: number, orden: number): Promise<void> => {
    return columnaApi.reorder(id, orden);
  },
};

export default columnaService;