/**
 * TypeScript types for Planova API entities
 * Based on backend DTOs — enums match Java enum values exactly (lowercase)
 */

// ==================== Auth Types ====================

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  nombre: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  usuario: {
    id: number;
    nombre: string;
    email: string;
  };
}

// ==================== User Types ====================

export interface Usuario {
  id: number;
  nombre: string;
  email: string;
  rol?: string;
  fechaCreacion?: string;
}

export interface UsuarioUpdate {
  nombre?: string;
  email?: string;
}

export interface ChangePassword {
  oldPassword: string;
  newPassword: string;
}

// ==================== Project Types ====================

export interface Proyecto {
  id: number;
  nombre: string;
  descripcion: string;
  usuarioId: number;
  fechaCreacion?: string;
}

export interface ProyectoCreate {
  nombre: string;
  descripcion?: string;
}

// ==================== Column Types ====================

export interface Columna {
  id: number;
  titulo: string;
  orden: number;
  proyectoId: number;
  tareas: Tarea[];
}

export interface ColumnaCreate {
  titulo: string;
  orden?: number;
}

// ==================== Task Types ====================

// ⚠️ IMPORTANT: estos valores deben coincidir EXACTAMENTE con el enum Java
// Backend: pendiente | en_proceso | completada | vencida
export type Estado = 'pendiente' | 'en_proceso' | 'completada' | 'vencida';

// Backend: alta | media | baja
export type Prioridad = 'alta' | 'media' | 'baja';

export interface Tarea {
  id: number;
  titulo: string;
  descripcion?: string;
  estado: Estado;
  prioridad: Prioridad;
  fechaVencimiento?: string | null;
  columnaId: number;
  proyectoId: number;
}

export interface TareaCreate {
  titulo: string;
  descripcion?: string;
  estado?: Estado;
  prioridad?: Prioridad;
  fechaVencimiento?: string | null;
}

export interface TareaUpdate {
  titulo?: string;
  descripcion?: string;
  estado?: Estado;
  prioridad?: Prioridad;
  fechaVencimiento?: string | null;
}

export interface MoverTarea {
  columnaId: number;
  orden: number;
}

// ==================== Comment Types ====================

export interface Comentario {
  id: number;
  contenido: string;
  fechaCreacion: string; // ISO date string
  usuarioId: number;
  tareaId: number;
  usuario?: {
    id: number;
    nombre: string;
  };
}

export interface ComentarioCreate {
  contenido: string;
}

// ==================== API Response Types ====================

export interface ApiError {
  message: string;
  errors?: Record<string, string[]>;
}

export interface ApiResponse<T> {
  data?: T;
  error?: ApiError;
}

// ==================== Pagination Types ====================

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// ==================== Enum Utilities ====================

export const ESTADOS: Estado[] = ['pendiente', 'en_proceso', 'completada', 'vencida'];
export const PRIORIDADES: Prioridad[] = ['alta', 'media', 'baja'];

export const EstadoLabels: Record<Estado, string> = {
  pendiente: 'Pendiente',
  en_proceso: 'En Progreso',
  completada: 'Completada',
  vencida: 'Vencida',
};

export const PrioridadLabels: Record<Prioridad, string> = {
  alta: 'Alta',
  media: 'Media',
  baja: 'Baja',
};