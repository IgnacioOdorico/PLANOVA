/**
 * Auth Service for Planova
 * Authentication functions that use the API
 */

import { LoginRequest, RegisterRequest, Usuario } from '../types';
import { authApi, setToken, setUser, removeToken, removeUser } from '../services/api';

export interface LoginResult {
  token: string;
  user: Usuario;
}

export const authService = {
  /**
   * Login user with email and password
   * Backend now returns { token, usuario } — no extra roundtrip needed
   */
  login: async (credentials: LoginRequest): Promise<LoginResult> => {
    const response = await authApi.login(credentials);
    const { token, usuario } = response;
    
    // Store token
    setToken(token);
    
    // Use usuario from response directly — no need for getPerfil
    const user: Usuario = {
      id: usuario.id,
      nombre: usuario.nombre,
      email: usuario.email,
    };
    setUser(user);
    
    return { token, user };
  },

  /**
   * Register a new user
   * Backend now returns { token, usuario } — no extra roundtrip needed
   */
  register: async (userData: RegisterRequest): Promise<LoginResult> => {
    const response = await authApi.register(userData);
    const { token, usuario } = response;
    
    // Store token
    setToken(token);
    
    // Use usuario from response directly
    const user: Usuario = {
      id: usuario.id,
      nombre: usuario.nombre,
      email: usuario.email,
    };
    setUser(user);
    
    return { token, user };
  },

  /**
   * Logout user
   */
  logout: async (): Promise<void> => {
    try {
      await authApi.logout();
    } finally {
      removeToken();
      removeUser();
    }
  },

  /**
   * Get current user profile
   */
  getProfile: async (): Promise<Usuario> => {
    const { usuarioApi } = await import('../services/api');
    return usuarioApi.getPerfil();
  },

  /**
   * Update user profile — PATCH /usuarios/me now returns 200 + user
   */
  updateProfile: async (data: { nombre?: string; email?: string }): Promise<Usuario> => {
    const { usuarioApi } = await import('../services/api');
    const user = await usuarioApi.updatePerfil(data);
    setUser(user);
    return user;
  },

  /**
   * Change password
   */
  changePassword: async (oldPassword: string, newPassword: string): Promise<void> => {
    const { usuarioApi } = await import('../services/api');
    await usuarioApi.changePassword(oldPassword, newPassword);
  },
};

export default authService;
