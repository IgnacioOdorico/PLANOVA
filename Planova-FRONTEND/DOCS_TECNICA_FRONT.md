# Planova — Documentación Técnica (Frontend) v1.0

> **Stack:** React 18 · TypeScript · Vite · TailwindCSS · @dnd-kit · Axios · React Router 6

---

## 1. Arquitectura de la Aplicación

El frontend está diseñado como una SPA (Single Page Application) modular, utilizando TypeScript para asegurar la integridad de los datos que viajan desde el backend.

### 1.1 Estructura de Proyecto
- `src/components/`: Componentes reutilizables.
  - `common/`: Componentes básicos con estilo Glassmorphism (Buttons, Cards, Inputs).
  - `kanban/`: Lógica específica del tablero (Columnas, Tareas, Modales).
  - `proyectos/`: Componentes para la gestión de la lista de proyectos.
- `src/context/`: Manejo de estado global (Autenticación).
- `src/hooks/`: Hooks personalizados para lógica compartida.
- `src/pages/`: Vistas principales (Home, Kanban, Login, Perfil, Register).
- `src/services/`: Capa de abstracción para llamadas a la API (Axios).
- `src/types/`: Definiciones de interfaces que espejan los DTOs del backend.

---

## 2. Gestión de Estado y Datos

### 2.1 Autenticación (AuthContext)
Se utiliza un `AuthContext` para centralizar el estado del usuario logueado. 
- El sistema persiste el JWT y los datos básicos del usuario en **LocalStorage**.
- Un `ProtectedRoute` intercepta las rutas privadas y redirige al `/login` si no hay un token válido.

### 2.2 Integración con API (Axios)
Ubicado en `src/services/api.ts`, el cliente Axios cuenta con interceptores:
- **Request Interceptor:** Inyecta automáticamente el token JWT en el header `Authorization` de cada petición.
- **Response Interceptor:** Detecta errores 401 para limpiar el estado local si el token expiró.

---

## 3. Tablero Kanban (Drag & Drop)

El tablero Kanban utiliza la librería `@dnd-kit`, elegida por su accesibilidad y modularidad.

- **DndContext:** Envuelve el tablero y maneja los eventos de inicio (`onDragStart`), movimiento (`onDragOver`) y fin (`onDragEnd`).
- **SortableContext:** Permite el reordenamiento vertical de las tareas dentro de una columna.
- **Optimistic UI:** Al mover una tarea entre columnas, el estado local se actualiza de inmediato para una sensación de fluidez, mientras se envía la petición de movimiento al backend.
- **Auto-Sorting:** Las columnas implementan un ordenamiento automático basado en la prioridad de la tarea.

---

## 4. Diseño Visual (Glassmorphism)

El estilo visual de Planova se basa en **Glassmorphism**, implementado mediante TailwindCSS:
- Fondos con `backdrop-blur`.
- Bordes semitransparentes.
- Gradientes sutiles y sombras suaves.
- Animaciones de entrada y hover para una experiencia premium.

---

## 5. Puntos de Mejora Sugeridos

1.  **Vitest + Testing Library:** Actualmente no existe una suite de tests automatizados para los componentes. Implementar tests de unidad para la lógica del `AuthContext` y `api.ts` es prioridad.
2.  **Manejo de Imágenes Mejorado:** Utilizar una librería como `react-dropzone` para permitir a los usuarios subir imágenes de perfil o adjuntos a las tareas.
3.  **Skeleton Screens:** En lugar de spinners genéricos, usar Skeletons para mejorar la percepción de velocidad en la carga del Kanban.
4.  **Zustand o Signals:** Para una aplicación con muchos estados cruzados, migrar de Context API a una librería de estado más liviana y reactiva podría mejorar el performance en tableros muy grandes.

---
*Última actualización: 2026-04-12*
