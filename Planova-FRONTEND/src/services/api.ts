/**
 * API Service Layer for Planova
 * Axios instance with JWT authentication interceptors
 */

import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig, AxiosResponse } from 'axios';
import { 
  LoginRequest, 
  RegisterRequest, 
  AuthResponse,
  Usuario, 
  UsuarioUpdate,
  Proyecto, 
  ProyectoCreate,
  Columna,
  ColumnaCreate,
  Tarea,
  TareaCreate,
  TareaUpdate,
  MoverTarea,
  Comentario,
  ComentarioCreate,
} from '../types';

// ==================== Constants ====================

const API_BASE_URL = '/api'; // Proxied to localhost:8080 in vite.config.ts

const TOKEN_KEY = 'planova_token';
const USER_KEY = 'planova_user';

// ==================== Axios Instance ====================

const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ==================== Request Interceptor ====================

api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// ==================== Response Interceptor ====================

api.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  (error: AxiosError<{ message?: string; error?: string }>) => {
    // Handle 401 errors but don't auto-redirect - let the caller handle it
    if (error.response?.status === 401) {
      // Token expired or invalid - clear local storage
      // Don't auto-redirect here - let the components handle auth state
      console.warn('Auth error detected - token may be expired');
    }
    return Promise.reject(error);
  }
);

// ==================== Token Management ====================

export const getToken = (): string | null => {
  if (typeof window === 'undefined') return null;
  return localStorage.getItem(TOKEN_KEY);
};

export const setToken = (token: string): void => {
  localStorage.setItem(TOKEN_KEY, token);
};

export const removeToken = (): void => {
  localStorage.removeItem(TOKEN_KEY);
};

export const getUser = (): Usuario | null => {
  if (typeof window === 'undefined') return null;
  const userStr = localStorage.getItem(USER_KEY);
  if (!userStr) return null;
  try {
    return JSON.parse(userStr) as Usuario;
  } catch {
    return null;
  }
};

export const setUser = (user: Usuario): void => {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
};

export const removeUser = (): void => {
  localStorage.removeItem(USER_KEY);
};

export const isAuthenticated = (): boolean => {
  return !!getToken();
};

// ==================== Auth API ====================

export const authApi = {
  login: async (credentials: LoginRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/auth/login', credentials);
    return response.data;
  },

  register: async (userData: RegisterRequest): Promise<AuthResponse> => {
    const response = await api.post<AuthResponse>('/auth/register', userData);
    return response.data;
  },

  logout: async (): Promise<void> => {
    try {
      await api.post('/auth/logout');
    } finally {
      removeToken();
      removeUser();
    }
  },
};

// ==================== User API ====================

export const usuarioApi = {
  getMe: async (): Promise<Usuario> => {
    const response = await api.get<Usuario>('/usuarios/me');
    return response.data;
  },

  updateMe: async (data: UsuarioUpdate): Promise<Usuario> => {
    const response = await api.patch<Usuario>('/usuarios/me', data);
    return response.data;
  },

  changePasswordMe: async (oldPassword: string, newPassword: string): Promise<void> => {
    await api.patch('/usuarios/me/password', { oldPassword, newPassword });
  },

  getPerfil: async (): Promise<Usuario> => {
    const response = await api.get<Usuario>('/usuarios/me');
    return response.data;
  },

  updatePerfil: async (data: UsuarioUpdate): Promise<Usuario> => {
    const response = await api.patch<Usuario>('/usuarios/me', data);
    return response.data;
  },

  changePassword: async (oldPassword: string, newPassword: string): Promise<void> => {
    await api.patch('/usuarios/me/password', { oldPassword, newPassword });
  },

  getById: async (id: number): Promise<Usuario> => {
    const response = await api.get<Usuario>(`/usuarios/${id}`);
    return response.data;
  },

  getAll: async (): Promise<Usuario[]> => {
    const response = await api.get<Usuario[]>('/usuarios');
    return response.data;
  },
};

// ==================== Project API ====================

export const proyectoApi = {
  getAll: async (): Promise<Proyecto[]> => {
    const response = await api.get<Proyecto[]>('/proyectos');
    return response.data;
  },

  getById: async (id: number): Promise<Proyecto> => {
    const response = await api.get<Proyecto>(`/proyectos/${id}`);
    return response.data;
  },

  create: async (data: ProyectoCreate): Promise<Proyecto> => {
    const response = await api.post<Proyecto>('/proyectos', data);
    return response.data;
  },

  update: async (id: number, data: Partial<ProyectoCreate>): Promise<Proyecto> => {
    const response = await api.patch<Proyecto>(`/proyectos/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/proyectos/${id}`);
  },
};

// ==================== Column API ====================

export const columnaApi = {
  getByProyecto: async (proyectoId: number): Promise<Columna[]> => {
    const response = await api.get<Columna[]>(`/proyectos/${proyectoId}/columnas`);
    return response.data;
  },

  getById: async (id: number): Promise<Columna> => {
    const response = await api.get<Columna>(`/columnas/${id}`);
    return response.data;
  },

  create: async (proyectoId: number, data: ColumnaCreate): Promise<Columna> => {
    const response = await api.post<Columna>(`/columnas`, { ...data, proyectoId });
    return response.data;
  },

  update: async (id: number, data: Partial<ColumnaCreate>): Promise<Columna> => {
    const response = await api.put<Columna>(`/columnas/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/columnas/${id}`);
  },

  reorder: async (id: number, orden: number): Promise<Columna> => {
    const response = await api.put<Columna>(`/columnas/${id}`, { titulo: '', orden });
    return response.data;
  },
};

// ==================== Task API ====================

export const tareaApi = {
  getByColumna: async (columnaId: number): Promise<Tarea[]> => {
    // Note: Backend returns Page<TareaResponseDTO> which contains { content: [...] }
    const response = await api.get<{content: Tarea[]}>(`/columnas/${columnaId}/tareas?page=0&size=100`);
    return response.data.content || [];
  },

  getById: async (id: number): Promise<Tarea> => {
    const response = await api.get<Tarea>(`/tareas/${id}`);
    return response.data;
  },

  getByProyecto: async (_proyectoId: number): Promise<Tarea[]> => {
    // This endpoint doesn't exist directly on the backend, returning empty for safety
    console.warn('getByProyecto in tareaApi requires mapping through columnas logic. Returning [] temporarily');
    return [];
  },

  create: async (columnaId: number, data: TareaCreate): Promise<Tarea> => {
    const response = await api.post<Tarea>(`/tareas`, { ...data, columnaId });
    return response.data;
  },

  update: async (id: number, data: TareaUpdate): Promise<Tarea> => {
    const response = await api.patch<Tarea>(`/tareas/${id}`, data);
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/tareas/${id}`);
  },

  mover: async (id: number, data: MoverTarea): Promise<void> => {
    await api.put(`/tareas/${id}/mover`, data);
  },
};

// ==================== Comment API ====================

export const comentarioApi = {
  getByTarea: async (tareaId: number): Promise<Comentario[]> => {
    const response = await api.get<Comentario[]>(`/comentarios/tarea/${tareaId}`);
    return response.data;
  },

  getById: async (id: number): Promise<Comentario> => {
    const response = await api.get<Comentario>(`/comentarios/${id}`);
    return response.data;
  },

  create: async (tareaId: number, data: ComentarioCreate): Promise<Comentario> => {
    const response = await api.post<Comentario>(`/comentarios`, { ...data, tareaId });
    return response.data;
  },

  update: async (id: number, contenido: string): Promise<Comentario> => {
    const response = await api.patch<Comentario>(`/comentarios/${id}`, { contenido });
    return response.data;
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/comentarios/${id}`);
  },
};

// ==================== Error Handling Helper ====================

export const getApiErrorMessage = (error: unknown): string => {
  if (axios.isAxiosError(error)) {
    const axiosError = error as AxiosError<{ message?: string; error?: string;Mensaje?: string }>;
    
    // Try to get message from various response formats
    const data = axiosError.response?.data;
    if (data) {
      if (typeof data === 'string') {
        return data;
      }
      if (data.message) return data.message;
      if (data.Mensaje) return data.Mensaje;
      if (data.error) return data.error;
    }
    
    if (axiosError.message) {
      return axiosError.message;
    }
  }
  return 'Ha ocurrido un error inesperado';
};

// ==================== Export ====================

export default api;
export { api };
export { TOKEN_KEY, USER_KEY };