/**
 * Proyecto Service for Planova
 * CRUD operations for proyectos
 */

import { Proyecto, ProyectoCreate } from '../types';
import { proyectoApi } from '../services/api';

export interface ProyectoWithFecha extends Proyecto {
  fechaCreacion?: string;
}

export const proyectoService = {
  /**
   * Get all projects for the current user
   */
  getAll: async (): Promise<Proyecto[]> => {
    return proyectoApi.getAll();
  },

  /**
   * Get a project by ID
   */
  getById: async (id: number): Promise<Proyecto> => {
    return proyectoApi.getById(id);
  },

  /**
   * Create a new project
   */
  create: async (data: ProyectoCreate): Promise<Proyecto> => {
    return proyectoApi.create(data);
  },

  /**
   * Update an existing project
   */
  update: async (id: number, data: Partial<ProyectoCreate>): Promise<Proyecto> => {
    return proyectoApi.update(id, data);
  },

  /**
   * Delete a project
   */
  delete: async (id: number): Promise<void> => {
    return proyectoApi.delete(id);
  },
};

export default proyectoService;