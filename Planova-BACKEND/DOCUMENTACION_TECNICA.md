# Planova — Documentación Técnica (Backend) v6.0

> **Stack:** Java 17 · Spring Boot 4.0.3 · Spring Security 6.x · JWT (jjwt) · JPA/Hibernate 6.x · MySQL · Lombok

---

## 1. Arquitectura del Sistema

El backend de Planova está construido siguiendo una arquitectura de capas estándar de Spring Boot, optimizada para una API REST stateless.

### 1.1 Estructura de Paquetes
- `auth/`: Controladores y servicios específicos para el flujo de autenticación (Registro/Login).
- `controller/`: Capa de entrada que maneja las peticiones HTTP y la validación de DTOs.
- `service/`: Contiene la lógica de negocio central, incluyendo la validación de reglas y el manejo de excepciones.
- `repository/`: Interfaces JPA para la persistencia de datos con queries personalizadas para validación de propiedad.
- `model/`: Entidades JPA que representan la base de datos.
- `dto/`: Objetos de transferencia de datos para separar la persistencia de la presentación.
- `security/`: Configuración de Spring Security, filtros JWT y Rate Limiting.
- `exception/`: Manejo global de excepciones.

---

## 2. Modelo de Datos y Relaciones

El sistema maneja una jerarquía estricta de propiedad:
`Usuario` ➔ `Proyecto` ➔ `Columna` ➔ `Tarea` ➔ `Comentario`.

| Entidad | Descripción | Relación Principal |
| :--- | :--- | :--- |
| **Usuario** | Usuario del sistema con rol (USER/ADMIN). | Propietario de múltiples proyectos. |
| **Proyecto** | Contenedor de tableros Kanban. | Pertenece a un Usuario. |
| **Columna** | Representa un estado o etapa (ej: "To Do"). | Pertenece a un Proyecto. |
| **Tarea** | Unidad de trabajo con estado y prioridad. | Pertenece a una Columna. |
| **Comentario** | Notas adicionales en una tarea. | Pertenece a una Tarea y un Usuario (autor). |

---

## 3. Seguridad y Control de Acceso

### 3.1 Autenticación JWT
- Flujo **stateless**: No se mantienen sesiones en el servidor.
- El token JWT se genera con el email del usuario como `subject` y expira en 24h.
- Se utiliza `BCryptPasswordEncoder` para el resguardo de contraseñas.

### 3.2 Validación de Ownership (Propiedad)
Este es el pilar de seguridad de Planova. Cada petición valida que el recurso solicitado pertenezca efectivamente al usuario logueado mediante **Queries con JOINs** en la base de datos:

```java
// Ejemplo en TareaRepository
Optional<Tarea> findByIdAndColumnaProyectoUsuario(Long id, Usuario usuario);
```
Esto garantiza que, aunque un usuario conozca un ID de tarea, no podrá acceder a él si no es el dueño del proyecto asociado.

### 3.3 Rate Limiting
- Se implementó un `LoginRateLimitFilter` que bloquea IPs tras 5 intentos fallidos en un minuto para prevenir ataques de fuerza bruta.

---

## 4. Lógica de Tareas

### 4.1 Auto-vencimiento
El sistema cuenta con un mecanismo de "verificación de vencimiento" en el `TareaService`. Cada vez que se consulta una tarea o se lista una columna, el backend verifica si la `fechaVencimiento` ya pasó y, de ser así, actualiza automáticamente el estado a `vencida`.

### 4.2 Paginación
Todos los listados de tareas (por columna o generales) están **paginados** para asegurar el rendimiento con grandes volúmenes de datos.
- Endpoint: `GET /tareas?page=0&size=10`
- Orden: Por defecto las tareas se ordenan por `prioridad` y `fechaCreacion`.

---

## 5. Puntos de Mejora Sugeridos

1.  **Swagger/OpenAPI:** Actualmente la documentación es manual. Integrar `springdoc-openapi` permitiría tener una UI interactiva para probar los endpoints.
2.  **Soft Delete:** Implementar eliminación lógica (marcar como borrado) en lugar de física para poder recuperar datos por error del usuario.
3.  **Audit Logs:** Utilizar `MDC` para registrar el ID del usuario en todos los logs de error y facilitar el debugging en producción.
4.  **Tests de Integración:** Si bien hay tests, aumentar la cobertura en los casos de borde del Rate Limiting y Ownership.

---
*Última actualización: 2026-04-12*
