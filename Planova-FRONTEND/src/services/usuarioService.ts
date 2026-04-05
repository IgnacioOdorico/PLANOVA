/**
 * Usuario Service for Planova
 * User profile operations
 */

import { Usuario, UsuarioUpdate } from '../types';
import { usuarioApi, setUser } from '../services/api';

export const usuarioService = {
  /**
   * Get current user profile
   * Endpoint: GET /usuarios/me
   */
  getPerfil: async (): Promise<Usuario> => {
    return usuarioApi.getMe();
  },

  /**
   * Update current user profile
   * Endpoint: PATCH /usuarios/me — returns updated user (200 OK)
   */
  updatePerfil: async (data: UsuarioUpdate): Promise<Usuario> => {
    const user = await usuarioApi.updateMe(data);
    setUser(user);
    return user;
  },

  /**
   * Change password
   * Endpoint: PATCH /usuarios/me/password
   */
  changePassword: async (oldPassword: string, newPassword: string): Promise<void> => {
    return usuarioApi.changePasswordMe(oldPassword, newPassword);
  },
};

export default usuarioService;
