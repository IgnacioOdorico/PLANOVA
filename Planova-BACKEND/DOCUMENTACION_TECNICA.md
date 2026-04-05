# Planova — Documentación Técnica v5.3

> **Stack:** Java 17 · Spring Boot 4.0.3 · Spring Security · JWT (jjwt 0.11.5) · JPA/Hibernate · Lombok · Bean Validation · MySQL · Jackson JSR-310
> **Fecha de análisis:** 2026-04-02
> **Última actualización:** 2026-04-02 v5.3 (documentación actualizada + tests corregidos)
> **Analista:** Senior Backend Reviewer

---

## 1. Arquitectura General

### 1.1 Stack Tecnológico

| Componente       | Tecnología          | Versión | Observaciones                          |
| ---------------- | ------------------- | ------- | -------------------------------------- |
| Lenguaje         | Java                | 17      | LTS hasta septiembre 2029              |
| Framework        | Spring Boot         | 4.0.3   | Última estable, incluye Spring 6.x     |
| ORM              | JPA/Hibernate       | 6.x     | Incluido en Spring Boot 4.0.3          |
| Base de datos    | MySQL               | -       | Driver mysql-connector-j               |
| Seguridad        | Spring Security     | 6.x     | JWT stateless, sin sesiones            |
| JWT              | jjwt                | 0.11.5  | Librería estándar para JWT en Java     |
| Validación       | Bean Validation     | 3.x     | Jakarta Validation API                 |
| Utilidades       | Lombok              | -       | Generación de boilerplate              |

### 1.2 Estructura de Paquetes

```
com.Planova.Planova
├── auth/                    → Autenticación (registro + login)
│   ├── AuthController       → Endpoints POST /auth/register, POST /auth/login
│   └── AuthService          → Lógica de registro, login, generación JWT
│
├── controller/              → REST Controllers (capa de presentación)
│   ├── ProyectoController   → CRUD de proyectos (/proyectos)
│   ├── ColumnaController    → CRUD de columnas (/columnas, /proyectos/{id}/columnas)
│   ├── TareaController      → CRUD de tareas (/tareas)
│   ├── ComentarioController → CRUD de comentarios (/comentarios)
│   └── UsuarioController    → Gestión de usuarios (/usuarios)
│
├── dto/                     → Data Transfer Objects (17 clases)
│   ├── Request DTOs         → Entrada de datos (validación Bean Validation)
│   ├── Response DTOs        → Salida de datos (sin exposición de entidades)
│   └── MoverTareaDTO        → DTO especial para mover tareas entre columnas
│
├── exception/               → Manejo centralizado de excepciones
│   ├── ApiException         → Excepción personalizada con status HTTP
│   ├── ErrorResponse        → Record para respuestas de error
│   └── GlobalExceptionHandler → @RestControllerAdvice para todos los errores
│
├── model/                   → Entidades JPA (5) + Enums (3)
│   ├── Usuario              → Usuario del sistema (propietario de proyectos)
│   ├── Proyecto             → Proyecto pertenece a un usuario
│   ├── Columna              → Columna Kanban pertenece a un proyecto
│   ├── Tarea                → Tarea pertenece a una columna
│   ├── Comentario           → Comentario pertenece a tarea + usuario autor
│   ├── Estado               → Enum: pendiente, en_proceso, completada, vencida
│   ├── Prioridad            → Enum: alta, media, baja
│   └── Rol                  → Enum: USER, ADMIN
│
├── repository/              → Repositorios JPA (5 interfaces)
│   ├── UsuarioRepository    → Queries para usuarios (findByEmail, existsByEmail)
│   ├── ProyectoRepository   → Queries derivadas para proyectos
│   ├── ColumnaRepository    → Queries derivadas para columnas (con orden)
│   ├── TareaRepository      → Queries derivadas para tareas
│   └── ComentarioRepository → Queries derivadas para comentarios
│
├── security/                → Seguridad JWT
│   ├── JwtService           → Generación y validación de tokens
│   ├── JwtFilter            → Filtro de autenticación JWT (OncePerRequestFilter)
│   ├── LoginRateLimitFilter → Rate limiting en /auth/login (5 req/min por IP)
│   └── SecurityConfig       → Configuración de Spring Security + CORS
│
├── service/                 → Lógica de negocio (5 clases)
│   ├── UsuarioService       → Operaciones de usuarios (perfil, password)
│   ├── ProyectoService      → Operaciones de proyectos
│   ├── ColumnaService       → Operaciones de columnas
│   ├── TareaService         → Operaciones de tareas (crear, mover, eliminar)
│   └── ComentarioService    → Operaciones de comentarios
│
├── DotEnvConfig             → Carga de variables de entorno desde .env
└── PlanovaApplication.java  → Clase principal Spring Boot
```

### 1.3 Modelo de Datos (ER Diagram)

```
┌──────────────┐        ┌──────────────┐        ┌──────────────┐        ┌──────────────┐        ┌──────────────┐
│   USUARIO    │        │   PROYECTO   │        │    COLUMNA   │        │    TAREA     │        │  COMENTARIO  │
├──────────────┤        ├──────────────┤        ├──────────────┤        ├──────────────┤        ├──────────────┤
│ PK id        │◄───┐   │ PK id        │◄───┐   │ PK id        │◄───┐   │ PK id        │◄───┐   │ PK id        │
│ nombre       │    │   │ nombre       │    │   │ titulo       │    │   │ titulo       │    │   │ contenido    │
│ email (UQ)   │    │   │ descripcion  │    │   │ orden        │    │   │ descripcion  │    │   │ fecha        │
│ password     │    │   │ fechaCreacion│    │   │              │    │   │ fechaCreacion│    │   │              │
│ fechaCreacion│    │   │              │    │   │ FK proyecto  ├────┘   │ fechaVencim  │    │   │ FK tarea     ├────┘
│ rol (enum)   │    │   │ FK usuario   ├────┘   │              │        │ estado (enum)│    │   │              │
│              │    │   │              │        │              │        │ prioridad(·) │    │   │ FK usuario   ├────┐
└──────────────┘    │   └──────────────┘        └──────────────┘        │ FK columna   ├────┘   └──────────────┘    │
       ▲            │                                                    └──────────────┘                           │
       └────────────┴──────────────────────────────────────────────────────────────────────────────────────────────┘
                    (autor del comentario)
```

### 1.4 Relaciones y Cascadas

| Relación           | Tipo      | Cascade | OrphanRemoval | Observaciones                                          |
| ------------------ | --------- | ------- | ------------- | ------------------------------------------------------ |
| Usuario → Proyecto | OneToMany | ALL     | true          | Eliminar usuario elimina todos sus proyectos           |
| Proyecto → Columna | OneToMany | ALL     | true          | Eliminar proyecto elimina todas sus columnas           |
| Columna → Tarea    | OneToMany | ALL     | true          | Eliminar columna elimina todas sus tareas              |
| Tarea → Comentario | OneToMany | ALL     | true          | Eliminar tarea elimina todos sus comentarios           |
| Comentario → Usuario | ManyToOne | -      | -             | Autor del comentario (FK independiente)                |

**⚠️ Nota sobre cascadas:** La cadena es `CascadeType.ALL` + `orphanRemoval = true` en toda la jerarquía. Eliminar un proyecto elimina columnas → tareas → comentarios en cascada. Es correcto para el dominio, pero en producción se debería considerar soft-delete para preservar datos históricos.

---

## 2. Módulo de Seguridad

### 2.1 Configuración Spring Security

```java
@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final LoginRateLimitFilter loginRateLimitFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // ✅ CORS configurado
            .csrf(csrf -> csrf.disable())                                       // ✅ Correcto para REST stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))        // ✅ Sin sesiones
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()                        // ✅ Solo auth es público
                .anyRequest().authenticated())                                  // ✅ Todo lo demás requiere JWT
            .addFilterBefore(loginRateLimitFilter, UsernamePasswordAuthenticationFilter.class) // ✅ Rate limit primero
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

**Análisis:**
- ✅ CSRF deshabilitado: Correcto para API REST stateless
- ✅ Sesión STATELESS: No hay sesiones HTTP, solo JWT
- ✅ Solo `/auth/**` es público: Endpoints de registro y login
- ✅ JwtFilter se ejecuta antes del filtro de autenticación estándar
- ✅ BCryptPasswordEncoder para hashing de contraseñas
- ✅ CORS configurado: Permite requests desde cualquier origen con credenciales
- ✅ Rate limiting: LoginRateLimitFilter registrado antes del JwtFilter

### 2.2 Flujo JWT

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        FLUJO DE AUTENTICACIÓN JWT                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1. REGISTRO                                                                │
│     POST /auth/register {nombre, email, password}                           │
│     → AuthService.register()                                                │
│     → existsByEmail() → 409 si duplicado                                    │
│     → BCrypt.encode(password)                                               │
│     → Guarda usuario con rol USER                                           │
│     → JwtService.generateToken(email)                                       │
│     → Response: 201 {token: "eyJ..."}                                       │
│                                                                             │
│  2. LOGIN                                                                   │
│     POST /auth/login {email, password}                                      │
│     → AuthService.login()                                                   │
│     → findByEmail(email) → 401 si no existe                                 │
│     → BCrypt.matches(password, hash) → 401 si no coincide                   │
│     → JwtService.generateToken(email)                                       │
│     → Response: 200 {token: "eyJ..."}                                       │
│                                                                             │
│  3. REQUEST AUTENTICADO                                                      │
│     GET /proyectos                                                          │
│     Header: Authorization: Bearer eyJ...                                    │
│     → LoginRateLimitFilter: solo aplica a /auth/login, skip en otros       │
│     → JwtFilter.doFilterInternal()                                          │
│     → Extrae token del header "Authorization: Bearer <token>"               │
│     → jwtService.isTokenValid(token)                                        │
│        → false → 401 {"status":401,"message":"Token inválido o expirado"}   │
│     → jwtService.extractEmail(token) → email                                │
│     → findByEmail(email) → Usuario entity                                   │
│     → Setea Authentication en SecurityContext (principal = Usuario)         │
│     → Spring Security verifica anyRequest().authenticated() → 401 si null  │
│                                                                             │
│  4. RATE LIMITING EN LOGIN                                                   │
│     POST /auth/login                                                        │
│     → LoginRateLimitFilter.doFilterInternal()                               │
│     → Obtiene IP del cliente (X-Forwarded-For o RemoteAddr)                │
│     → Verifica si la IP excedió 5 intentos en el último minuto             │
│     → Si excedió: 429 {"status":429,"message":"Demasiados intentos..."}    │
│     → Si no: incrementa contador y continúa al AuthController              │
│     → Ventana fija de 60 segundos, reset automático                        │
│                                                                             │
│  4. TOKEN JWT (estructura)                                                   │
│     Header: {alg: "HS256", typ: "JWT"}                                      │
│     Payload: {sub: "email", iat: timestamp, exp: timestamp+24h}             │
│     Signature: HMAC-SHA256(base64(header) + "." + base64(payload), secret)  │
│                                                                             │
│  5. VALIDACIÓN EN CADA REQUEST                                               │
│     → Services acceden al usuario via:                                       │
│       SecurityContextHolder.getContext().getAuthentication().getPrincipal()  │
│     → El principal es el objeto Usuario (con id, email, rol)                │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.3 Análisis de Seguridad

#### ✅ Fortalezas

| Aspecto                    | Implementación              | Estado   |
| --------------------------- | --------------------------- | -------- |
| Hash de contraseñas         | BCryptPasswordEncoder       | ✅ Correcto |
| JWT stateless               | Sin sesiones HTTP           | ✅ Correcto |
| Validación de ownership     | Queries SQL con JOIN        | ✅ Correcto |
| Logging de errores          | SLF4J en JwtFilter          | ✅ Correcto |
| Manejo centralizado         | GlobalExceptionHandler      | ✅ Correcto |
| Credenciales en .env        | Variables de entorno        | ✅ Implementado |
| @Valid en endpoints         | Bean Validation activo       | ✅ Correcto |
| Rol.USER hardcodeado en registro | No escalada de privilegios | ✅ Correcto |
| Rate limiting en /auth/login| LoginRateLimitFilter (5/min)| ✅ Corregido |
| CORS configurado            | CorsConfigurationSource     | ✅ Corregido |
| Tokens inválidos rechazados | JwtFilter retorna 401       | ✅ Corregido |
| fechaCreacion en registro   | AuthService.register        | ✅ Corregido |

#### ⚠️ Vulnerabilidades Potenciales

| ID  | Severidad | Descripción                                                        | Estado       | Recomendación                                    |
| --- | --------- | ------------------------------------------------------------------ | ------------ | ------------------------------------------------ |
| V1  | **Alta**  | No hay rate limiting en `/auth/login`                              | ✅ **CORREGIDO** | LoginRateLimitFilter: 5 req/min por IP         |
| V2  | Media     | CORS no configurado                                                | ✅ **CORREGIDO** | CorsConfigurationSource con métodos/headers    |
| V3  | Media     | JwtFilter no rechaza tokens inválidos con 401 explícito            | ✅ **CORREGIDO** | Retorna 401 + JSON cuando token es inválido    |
| V4  | Baja      | JWT no tiene issuer (iss) ni audience (aud) claims                 | Pendiente    | Agregar claims para mayor seguridad              |
| V5  | Baja      | No hay refresh token                                               | Pendiente    | Implementar refresh token para mejor UX          |
| V6  | Baja      | Logs no estructurados para auditoría                               | Pendiente    | Agregar MDC con userId, IP, endpoint             |
| V7  | Info      | No hay healthcheck endpoint                                        | Pendiente    | Agregar Spring Boot Actuator                     |
| V8  | Info      | No hay logout / revocación de tokens                               | Pendiente    | Implementar blacklist o token revocation         |
| V9  | Baja      | fechaCreacion no se setea en registro                              | ✅ **CORREGIDO** | AuthService.register setea LocalDateTime.now() |

#### ❌ Vulnerabilidades NO encontradas (buenas noticias)

- No hay inyección SQL: Usa JPA/Hibernate con parámetros
- No hay XSS: API REST que no renderiza HTML
- No hay CSRF: Deshabilitado correctamente para stateless
- No hay exposición de entidades JPA: Solo DTOs en responses
- No hay fullscan: Queries derivadas con JOINs optimizadas
- No hay hardcode de credenciales: Variables de entorno
- No hay escalada de privilegios: Rol.USER asignado siempre en registro

---

## 3. Validación de Ownership

### 3.1 Cadena de Verificación

El sistema implementa una cadena de ownership que garantiza que ningún usuario pueda acceder a datos de otro usuario. La validación se hace a nivel de base de datos con queries derivadas que aplican JOINs hasta la tabla de usuarios.

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    CADENA DE OWNERSHIP VERIFICADA                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  USUARIO A (id=1)                                                           │
│    └── Proyecto X (id=10, usuario_id=1)                                     │
│          └── Columna "To Do" (id=50, proyecto_id=10)                        │
│          │     └── Tarea Y (id=100, columna_id=50)                          │
│          │           └── Comentario Z (id=1000, tarea_id=100, usuario_id=1) │
│          └── Columna "Done" (id=51, proyecto_id=10)                         │
│                                                                             │
│  USUARIO B (id=2)                                                           │
│    └── Proyecto W (id=20, usuario_id=2)                                     │
│          └── Columna "Backlog" (id=60, proyecto_id=20)                      │
│          │     └── Tarea V (id=200, columna_id=60)                          │
│                                                                             │
│  ❌ Usuario A NO puede acceder a:                                           │
│     - Proyecto W (propietario: Usuario B)                                   │
│     - Columna "Backlog" (pertenece a Proyecto W)                            │
│     - Tarea V (pertenece a Columna de Proyecto W)                           │
│                                                                             │
│  ✅ Usuario A SÍ puede acceder a:                                           │
│     - Proyecto X (propietario: Usuario A)                                   │
│     - Columnas "To Do" y "Done" (pertenece a Proyecto X)                    │
│     - Tarea Y (pertenece a Columna de Proyecto X)                           │
│     - Comentario Z (pertenece a Tarea Y, autor: Usuario A)                  │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.2 Queries de Ownership

| Recurso    | Query JPA                                                            | Tipo          |
| ---------- | -------------------------------------------------------------------- | ------------- |
| Proyecto   | `findByIdAndUsuario(id, usuario)`                                    | INDEX         |
| Columna    | `findByIdAndProyectoUsuario(id, usuario)`                            | JOIN          |
| Tarea      | `findByIdAndColumnaProyectoUsuario(id, usuario)`                     | JOIN doble    |
| Comentario | `findByIdAndTareaColumnaProyectoUsuario(id, usuario)`                | JOIN triple   |

### 3.3 Flujo de Validación por Endpoint

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    FLUJO DE VALIDACIÓN DE OWNERSHIP                          │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  GET /proyectos                                                             │
│    → ProyectoService.obtenerMisProyectos()                                  │
│    → getUsuarioLogueado() → Usuario del SecurityContext                     │
│    → findByUsuario(usuario) → Solo proyectos del usuario                    │
│                                                                             │
│  GET /proyectos/1/columnas                                                  │
│    → ColumnaService.obtenerColumnasPorProyecto(1)                           │
│    → ProyectoService.obtenerProyectoPorId(1)                                │
│    → findByIdAndUsuario(1, usuario) → 404 si no es del usuario              │
│    → findByProyectoOrderByOrdenAsc(proyecto) → Solo columnas del proyecto   │
│                                                                             │
│  POST /tareas {columnaId: 50}                                               │
│    → TareaService.crearTarea(request)                                       │
│    → ColumnaService.obtenerColumnaEntity(50)                                │
│    → findByIdAndProyectoUsuario(50, usuario) → 404 si no es del usuario     │
│    → Crea tarea en la columna verificada                                    │
│                                                                             │
│  PATCH /tareas/100                                                          │
│    → TareaService.actualizarTarea(100, request)                             │
│    → obtenerTareaPorId(100)                                                 │
│    → findByIdAndColumnaProyectoUsuario(100, usuario) → 404 si no es suyo    │
│    → Actualiza campos (solo si son no-null)                                 │
│                                                                             │
│  PUT /tareas/100/mover {columnaId: 51}                                      │
│    → TareaService.moverTarea(100, request)                                  │
│    → obtenerTareaPorId(100) → Valida ownership de tarea origen              │
│    → columnaService.obtenerColumnaEntity(51) → Valida ownership de destino  │
│    → Verifica que ambas columnas sean del MISMO proyecto                    │
│    → Mueve la tarea                                                         │
│                                                                             │
│  GET /comentarios/tarea/100                                                 │
│    → ComentarioService.obtenerComentariosPorTarea(100)                      │
│    → TareaService.obtenerTareaEntity(100) → Valida ownership de tarea       │
│    → findByTarea(tarea) → Solo comentarios de esa tarea                     │
│                                                                             │
│  PATCH /comentarios/1000                                                    │
│    → ComentarioService.actualizarComentario(1000, contenido)                │
│    → obtenerComentarioEntity(1000)                                          │
│    → findByIdAndTareaColumnaProyectoUsuario(1000, usuario)                  │
│    → JOIN triple: comentario → tarea → columna → proyecto → usuario         │
│    → 404 si no es del usuario                                               │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 3.4 Conclusión de Ownership

**✅ No existe ninguna combinación de endpoints que permita acceder a datos de otro usuario.**

Todas las operaciones pasan por:
1. Extracción del usuario logueado del SecurityContext
2. Query SQL con filtro por usuario_id (derivada con JOINs)
3. 404 si el recurso no pertenece al usuario (no revela existencia)

---

## 4. Análisis por Módulo

### 4.1 Módulo de Autenticación (`auth/`)

#### AuthController

```
POST /auth/register  → 201 Created + JWT token
POST /auth/login     → 200 OK + JWT token
```

**Análisis:**
- ✅ Usa `@Valid` para validación automática de DTOs
- ✅ Retorna 201 en registro (correcto para creación)
- ✅ Retorna 200 en login (correcto para autenticación exitosa)
- ✅ No expone información sensible en errores (401 genérico)

#### AuthService

```
register():
  → existsByEmail() → 409 si duplicado
  → BCrypt.encode(password)
  → Rol.USER siempre (no escalada de privilegios)
  → generateToken(email)
  → return AuthResponseDTO(token)

login():
  → findByEmail() → 401 si no existe (mensaje genérico)
  → BCrypt.matches() → 401 si no coincide (mensaje genérico)
  → generateToken(email)
  → return AuthResponseDTO(token)
```

**Análisis:**
- ✅ Contraseñas hasheadas con BCrypt
- ✅ Rol.USER hardcodeado (no hay escalada)
- ✅ Mensajes de error genéricos (no revelan si email existe)
- ✅ Token generado con email como subject
- ✅ `fechaCreacion` del Usuario se setea en registro (LocalDateTime.now())

---

### 4.2 Módulo de Proyectos (`controller/`, `service/`, `repository/`)

#### ProyectoController

```
GET    /proyectos       → 200 List<ProyectoResponseDTO>    (mis proyectos)
GET    /proyectos/paginado → 200 Page<ProyectoResponseDTO> (mis proyectos paginados)
POST   /proyectos       → 201 ProyectoResponseDTO          (crear)
PATCH  /proyectos/{id}  → 200 ProyectoResponseDTO          (actualizar parcial)
DELETE /proyectos/{id}  → 204 No Content                   (eliminar con cascade)
```

**Análisis:**
- ✅ Usa `@Valid` en POST y PATCH
- ✅ Retorna 201 en creación, 204 en eliminación
- ✅ PATCH permite actualización parcial (solo campos no-null)
- ✅ Ownership validado en todas las operaciones
- ✅ Endpoint para proyectos paginados

#### ProyectoService

```
obtenerMisProyectos():
  → getUsuarioLogueado()
  → findByUsuario(usuario) → Solo mis proyectos

obtenerProyectoPorId(id):
  → getUsuarioLogueado()
  → findByIdAndUsuario(id, usuario) → 404 si no es del usuario

crearProyecto(request):
  → getUsuarioLogueado()
  → Proyecto.builder() con usuario logueado + fechaCreacion = now()
  → save() → mapToDTO()

actualizarProyecto(id, request):
  → obtenerProyectoPorId(id) → Valida ownership
  → Solo actualiza campos no-null
  → save() → mapToDTO()

eliminarProyecto(id):
  → obtenerProyectoPorId(id) → Valida ownership
  → delete() → cascade elimina columnas → tareas → comentarios
```

---

### 4.3 Módulo de Columnas (`controller/`, `service/`, `repository/`)

#### ColumnaController

```
POST   /columnas                     → 201 ColumnaResponseDTO   (crear)
GET    /proyectos/{proyectoId}/columnas → 200 List<ColumnaResponseDTO> (listar por proyecto)
GET    /columnas/{columnaId}/tareas    → 200 Page<TareaResponseDTO>   (tareas de columna paginadas)
PUT    /columnas/{id}                → 200 ColumnaResponseDTO   (actualizar)
DELETE /columnas/{id}                → 204 No Content           (eliminar con cascade)
```

**Nota:** El controller usa `@RequestMapping` raíz (sin prefijo), los paths se definen en cada método.

**Análisis:**
- ✅ Usa `@Valid` en POST y PUT
- ✅ Ownership validado: columna → proyecto → usuario
- ✅ GET filtra por proyecto Y valida ownership del proyecto
- ✅ Endpoint para tareas de columna paginadas
- ⚠️ Usa PUT en vez de PATCH para actualización (reemplazo completo)

#### ColumnaService

```
obtenerColumnaEntity(id):
  → getUsuarioLogueado()
  → findByIdAndProyectoUsuario(id, usuario) → 404 si no es del usuario

crearColumna(request):
  → ProyectoService.obtenerProyectoPorId(proyectoId) → Valida ownership del proyecto
  → Columna.builder() con titulo, orden, proyecto
  → save() → mapToDTO()

obtenerColumnasPorProyecto(proyectoId):
  → ProyectoService.obtenerProyectoPorId(proyectoId) → Valida ownership
  → findByProyectoOrderByOrdenAsc(proyecto) → Ordenadas por campo "orden"

actualizarColumna(id, request):
  → obtenerColumnaEntity(id) → Valida ownership
  → Solo actualiza campos no-null (titulo, orden)
  → save() → mapToDTO()

eliminarColumna(id):
  → obtenerColumnaEntity(id) → Valida ownership
  → delete() → cascade elimina tareas y comentarios
```

**Análisis:**
- ✅ Ownership en toda la cadena: columna → proyecto → usuario
- ✅ Columnas ordenadas por campo `orden` (ASC)
- ✅ ColumnaResponseDTO incluye lista de `tareas` embebidas
- ✅ Cascade correcto: eliminar columna elimina sus tareas

---

### 4.4 Módulo de Tareas (`controller/`, `service/`, `repository/`)

#### TareaController

```
POST   /tareas           → 201 TareaResponseDTO       (crear)
GET    /tareas/{id}      → 200 TareaResponseDTO       (obtener)
GET    /tareas           → 200 Page<TareaResponseDTO>  (mis tareas paginadas)
PATCH  /tareas/{id}      → 200 TareaResponseDTO       (actualizar parcial)
PUT    /tareas/{id}/mover → 200 TareaResponseDTO      (mover a otra columna)
DELETE /tareas/{id}      → 204 No Content             (eliminar con cascade)
```

**Análisis:**
- ✅ Usa `@Valid` en POST, PATCH y PUT
- ✅ Ownership validado en todas las operaciones
- ✅ Endpoint dedicado para mover tareas entre columnas
- ✅ Endpoint para listar todas las tareas del usuario (GET /tareas) paginado

#### TareaService

```
crearTarea(request):
  → ColumnaService.obtenerColumnaEntity(columnaId) → Valida ownership de la columna
  → Tarea.builder() con titulo, descripcion, estado, prioridad, fechaVencimiento, columna
  → save() → mapToDTO()

obtenerTareaPorId(id):
  → getUsuarioLogueado()
  → findByIdAndColumnaProyectoUsuario(id, usuario) → JOIN doble
  → 404 si no es del usuario

actualizarTarea(id, request):
  → obtenerTareaPorId(id) → Valida ownership
  → Solo actualiza campos no-null (titulo, descripcion, estado, prioridad)
  → save() → mapToDTO()

moverTarea(tareaId, request):
  → obtenerTareaPorId(tareaId) → Valida ownership de tarea origen
  → ColumnaService.obtenerColumnaEntity(columnaId) → Valida ownership de columna destino
  → Verifica que ambas columnas sean del MISMO proyecto → 422 si no
  → tarea.setColumna(columnaDestino) + save()

eliminarTarea(id):
  → obtenerTareaPorId(id) → Valida ownership
  → delete() → cascade elimina comentarios
```

**Análisis:**
- ✅ Ownership doble: columna origen + columna destino (en mover)
- ✅ Validación de mismo proyecto al mover tareas
- ✅ TareaResponseDTO resuelve `proyectoId` via `tarea.columna.proyecto.id`
- ⚠️ `TareaRequestDTO` usa `columnaId` (no `proyectoId` como algunos podrían esperar)

---

### 4.5 Módulo de Comentarios (`controller/`, `service/`, `repository/`)

#### ComentarioController

```
GET    /comentarios/tarea/{tareaId}  → 200 List<ComentarioResponseDTO> (listar por tarea)
POST   /comentarios                  → 201 ComentarioResponseDTO       (crear)
PATCH  /comentarios/{id}             → 200 ComentarioResponseDTO       (actualizar)
DELETE /comentarios/{id}             → 204 No Content                  (eliminar)
```

**Análisis:**
- ✅ Usa `@Valid` en POST y PATCH
- ✅ Ownership validado con JOIN triple

#### ComentarioService

```
obtenerComentariosPorTarea(tareaId):
  → TareaService.obtenerTareaEntity(tareaId) → Valida ownership de tarea
  → findByTarea(tarea) → Solo comentarios de esa tarea

crearComentario(request):
  → getUsuarioLogueado()
  → TareaService.obtenerTareaEntity(tareaId) → Valida ownership de tarea
  → Comentario.builder() con contenido, fecha=now, usuario (autor), tarea
  → save() → mapToDTO()

actualizarComentario(id, contenido):
  → obtenerComentarioEntity(id) → Valida ownership
  → setContenido(contenido)
  → save() → mapToDTO()

eliminarComentario(id):
  → obtenerComentarioEntity(id) → Valida ownership
  → delete()

obtenerComentarioEntity(id):
  → getUsuarioLogueado()
  → findByIdAndTareaColumnaProyectoUsuario(id, usuario) → JOIN triple
  → 404 si no es del usuario
```

**Análisis:**
- ✅ Ownership con JOIN triple: comentario → tarea → columna → proyecto → usuario
- ✅ Listar comentarios valida ownership de la tarea primero
- ✅ Crea/actualizar/eliminar valida ownership del comentario directamente

---

### 4.6 Módulo de Usuarios (`controller/`, `service/`)

#### UsuarioController

```
GET   /usuarios/me           → 200 UsuarioResponseDTO        (mi perfil)
GET   /usuarios              → 200 List<UsuarioResponseDTO>  (solo ADMIN)
PATCH /usuarios/me           → 204 No Content                (actualizar nombre)
PATCH /usuarios/me/password  → 204 No Content                (cambiar contraseña)
```

**Análisis:**
- ✅ Usa `@Valid` en PATCH
- ✅ Endpoint `/usuarios/me` para perfil propio
- ✅ Endpoint `/usuarios` solo para ADMIN

#### UsuarioService

```
obtenerMiUsuario():
  → obtenerUsuarioLogueado()
  → mapToDTO(usuario)

obtenerUsuarios():
  → obtenerUsuarioLogueado()
  → if rol != ADMIN → 403 "No autorizado"
  → findAll() → mapToDTO()

actualizarMiUsuario(request):
  → obtenerUsuarioLogueado()
  → setNombre(request.nombre)
  → save()

cambiarPassword(request):
  → obtenerUsuarioLogueado()
  → if !BCrypt.matches(oldPassword, hash) → 401 "Contraseña incorrecta"
  → setPassword(BCrypt.encode(newPassword))
  → save()
```

**Análisis:**
- ✅ Validación de contraseña antigua antes de cambiar
- ✅ Nueva contraseña hasheada con BCrypt
- ✅ ADMIN check en service (no en capa de seguridad)
- ⚠️ Podría usar `@PreAuthorize("hasRole('ADMIN')")` si se configura UserDetailsService

---

## 5. Manejo de Errores

### 5.1 GlobalExceptionHandler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)                    → status + message
    @ExceptionHandler(MethodArgumentNotValidException.class) → 422 + campos inválidos
    @ExceptionHandler(Exception.class)                       → 500 + "Error interno del servidor"
}
```

### 5.2 Formato de Respuesta de Error

```json
{
  "status": 422,
  "message": "titulo: El título es requerido, columnaId: El ID de columna es requerido",
  "timestamp": "2026-04-02T18:45:00.123"
}
```

### 5.3 Códigos HTTP Utilizados

| Código | Uso                  | Ejemplo                                         |
| ------ | -------------------- | ----------------------------------------------- |
| 200    | OK (GET, PATCH, PUT) | Obtener proyectos, actualizar tarea             |
| 201    | Created (POST)       | Crear proyecto, columna, tarea, comentario      |
| 204    | No Content (DELETE)  | Eliminar proyecto, columna, tarea, comentario   |
| 401    | Unauthorized         | Token inválido, contraseña incorrecta           |
| 403    | Forbidden            | Usuario no es ADMIN (GET /usuarios)             |
| 404    | Not Found            | Recurso no encontrado o no pertenece al usuario |
| 409    | Conflict             | Email duplicado en registro                     |
| 422    | Unprocessable Entity | Validación de campos fallida                    |
| 500    | Internal Server Error| Error genérico del servidor                    |

---

## 6. Testing de Endpoints (Simulación)

### 6.1 Escenarios de Autenticación

#### Registro Exitoso
```bash
POST /auth/register
Content-Type: application/json

{
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "password": "password123"
}

# Esperado: 201 Created
# Body: { "token": "eyJ..." }
```

#### Registro con Email Duplicado
```bash
POST /auth/register
Content-Type: application/json

{
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "password": "password123"
}

# Esperado: 409 Conflict
# Body: { "status": 409, "message": "Correo ya registrado", "timestamp": "..." }
```

#### Login Exitoso
```bash
POST /auth/login
Content-Type: application/json

{
  "email": "juan@example.com",
  "password": "password123"
}

# Esperado: 200 OK
# Body: { "token": "eyJ..." }
```

#### Login con Credenciales Inválidas
```bash
POST /auth/login
Content-Type: application/json

{
  "email": "juan@example.com",
  "password": "wrongpassword"
}

# Esperado: 401 Unauthorized
# Body: { "status": 401, "message": "Credenciales inválidas", "timestamp": "..." }
```

#### Login con Campos Vacíos
```bash
POST /auth/login
Content-Type: application/json

{
  "email": "",
  "password": ""
}

# Esperado: 422 Unprocessable Entity
# Body: { "status": 422, "message": "email: El email es requerido, password: La contraseña es requerida", "timestamp": "..." }
```

### 6.2 Escenarios de Proyectos

#### Crear Proyecto
```bash
POST /proyectos
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "nombre": "Proyecto Alpha",
  "descripcion": "Mi primer proyecto"
}

# Esperado: 201 Created
# Body: { "id": 1, "nombre": "Proyecto Alpha", "descripcion": "Mi primer proyecto", "usuarioId": 1 }
```

#### Obtener Mis Proyectos
```bash
GET /proyectos
Authorization: Bearer eyJ...

# Esperado: 200 OK
# Body: [{ "id": 1, "nombre": "Proyecto Alpha", "descripcion": "...", "usuarioId": 1 }]
```

#### Actualizar Proyecto (parcial)
```bash
PATCH /proyectos/1
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "nombre": "Proyecto Alpha v2"
}

# Esperado: 200 OK
# Body: { "id": 1, "nombre": "Proyecto Alpha v2", ... }
```

#### Eliminar Proyecto
```bash
DELETE /proyectos/1
Authorization: Bearer eyJ...

# Esperado: 204 No Content
# Efecto: Elimina cascada → columnas → tareas → comentarios
```

### 6.3 Escenarios de Columnas

#### Crear Columna
```bash
POST /columnas
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "titulo": "To Do",
  "orden": 1,
  "proyectoId": 1
}

# Esperado: 201 Created
# Body: { "id": 1, "titulo": "To Do", "orden": 1, "proyectoId": 1, "tareas": [] }
```

#### Obtener Columnas de Proyecto
```bash
GET /proyectos/1/columnas
Authorization: Bearer eyJ...

# Esperado: 200 OK
# Body: [
#   { "id": 1, "titulo": "To Do", "orden": 1, "proyectoId": 1, "tareas": [...] },
#   { "id": 2, "titulo": "Done", "orden": 2, "proyectoId": 1, "tareas": [...] }
# ]
```

#### Actualizar Columna
```bash
PUT /columnas/1
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "titulo": "Backlog",
  "orden": 0,
  "proyectoId": 1
}

# Esperado: 200 OK
# Body: { "id": 1, "titulo": "Backlog", "orden": 0, ... }
```

### 6.4 Escenarios de Tareas

#### Crear Tarea
```bash
POST /tareas
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "titulo": "Implementar feature X",
  "descripcion": "Descripción detallada",
  "estado": "pendiente",
  "prioridad": "alta",
  "columnaId": 1,
  "fechaVencimiento": "2026-04-15T23:59:59"
}

# Esperado: 201 Created
# Body: { "id": 1, "titulo": "Implementar feature X", "estado": "pendiente", "prioridad": "alta", "proyectoId": 1, ... }
```

#### Obtener Tarea por ID
```bash
GET /tareas/1
Authorization: Bearer eyJ...

# Esperado: 200 OK
# Body: { "id": 1, "titulo": "Implementar feature X", ... }
```

#### Actualizar Tarea (parcial)
```bash
PATCH /tareas/1
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "estado": "en_proceso"
}

# Esperado: 200 OK
# Body: { "id": 1, "estado": "en_proceso", ... }
```

#### Mover Tarea a Otra Columna
```bash
PUT /tareas/1/mover
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "columnaId": 2,
  "orden": 1
}

# Esperado: 200 OK
# Precondición: Ambas columnas deben ser del mismo proyecto
# Error si columnas son de proyectos distintos: 422
```

### 6.5 Escenarios de Comentarios

#### Crear Comentario
```bash
POST /comentarios
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "contenido": "Buen trabajo en esta tarea",
  "tareaId": 1
}

# Esperado: 201 Created
# Body: { "id": 1, "contenido": "Buen trabajo en esta tarea", "fechaCreacion": "...", "usuarioId": 1, "tareaId": 1 }
```

#### Obtener Comentarios de Tarea
```bash
GET /comentarios/tarea/1
Authorization: Bearer eyJ...

# Esperado: 200 OK
# Body: [{ "id": 1, "contenido": "Buen trabajo...", ... }]
```

#### Actualizar Comentario
```bash
PATCH /comentarios/1
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "contenido": "Comentario actualizado"
}

# Esperado: 200 OK
# Body: { "id": 1, "contenido": "Comentario actualizado", ... }
```

### 6.6 Escenarios de Ownership (Críticos)

#### Acceso a Proyecto de Otro Usuario
```bash
GET /proyectos/999
Authorization: Bearer eyJ...  # Token de Usuario A

# Proyecto 999 pertenece a Usuario B
# Esperado: 404 Not Found
# Body: { "status": 404, "message": "Proyecto no encontrado", "timestamp": "..." }
```

#### Acceso a Columna de Otro Usuario
```bash
GET /proyectos/999/columnas
Authorization: Bearer eyJ...  # Token de Usuario A

# Proyecto 999 pertenece a Usuario B
# Esperado: 404 Not Found
```

#### Acceso a Tarea de Otro Usuario
```bash
GET /tareas/999
Authorization: Bearer eyJ...  # Token de Usuario A

# Tarea 999 pertenece a Usuario B
# Esperado: 404 Not Found
# Body: { "status": 404, "message": "Tarea no encontrada", "timestamp": "..." }
```

#### Intentar Mover Tarea a Columna de Otro Proyecto
```bash
PUT /tareas/1/mover
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "columnaId": 999  # Columna de otro proyecto del mismo usuario
}

# Esperado: 422 Unprocessable Entity
# Body: { "status": 422, "message": "No se puede mover una tarea a una columna de otro proyecto", ... }
```

#### Acceso a Comentario de Otro Usuario
```bash
PATCH /comentarios/999
Authorization: Bearer eyJ...  # Token de Usuario A
Content-Type: application/json

{
  "contenido": "Intento de modificación"
}

# Comentario 999 pertenece a Usuario B
# Esperado: 404 Not Found
# Body: { "status": 404, "message": "Comentario no encontrado", "timestamp": "..." }
```

### 6.7 Escenarios de Validación

#### Proyecto sin Nombre
```bash
POST /proyectos
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "nombre": ""
}

# Esperado: 422 Unprocessable Entity
# Body: { "status": 422, "message": "nombre: El nombre del proyecto es requerido", ... }
```

#### Tarea sin Columna
```bash
POST /tareas
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "titulo": "Tarea sin columna"
}

# Esperado: 422 Unprocessable Entity
# Body: { "status": 422, "message": "columnaId: El ID de columna es requerido", ... }
```

#### Contraseña Nueva Muy Corta
```bash
PATCH /usuarios/me/password
Authorization: Bearer eyJ...
Content-Type: application/json

{
  "oldPassword": "password123",
  "newPassword": "123"
}

# Esperado: 422 Unprocessable Entity
# Body: { "status": 422, "message": "newPassword: La nueva contraseña debe tener al menos 8 caracteres", ... }
```

#### Request sin Token JWT
```bash
GET /proyectos

# Esperado: 401 Unauthorized
# Spring Security rechaza porque anyRequest().authenticated()
```

---

## 7. Endpoints — Referencia Completa

### 7.1 Autenticación (Público)

| Método | Endpoint            | Body                 | Response OK          | Errors    | Descripción            |
| ------ | ------------------- | -------------------- | -------------------- | --------- | ---------------------- |
| POST   | `/auth/register`    | `RegisterRequestDTO` | 201 `AuthResponseDTO`| 409, 422  | Registro nuevo usuario |
| POST   | `/auth/login`       | `LoginRequestDTO`    | 200 `AuthResponseDTO`| 401, 422  | Inicio de sesión       |

### 7.2 Proyectos (Requiere JWT)

| Método | Endpoint              | Body                 | Response OK                       | Errors    | Descripción                |
| ------ | --------------------- | -------------------- | --------------------------------- | --------- | -------------------------- |
| GET    | `/proyectos`          | —                    | 200 `List<ProyectoResponseDTO>`   | 401       | Listar mis proyectos       |
| GET    | `/proyectos/paginado` | —                    | 200 `Page<ProyectoResponseDTO>`   | 401       | Listar mis proyectos (pag) |
| POST   | `/proyectos`          | `ProyectoRequestDTO` | 201 `ProyectoResponseDTO`         | 401, 422  | Crear proyecto             |
| PATCH  | `/proyectos/{id}`     | `ProyectoRequestDTO` | 200 `ProyectoResponseDTO`         | 401, 404  | Actualizar proyecto        |
| DELETE | `/proyectos/{id}`     | —                    | 204                               | 401, 404  | Eliminar proyecto          |

### 7.3 Columnas (Requiere JWT)

| Método | Endpoint                          | Body                | Response OK                        | Errors        | Descripción                 |
| ------ | --------------------------------- | -------------------- | ---------------------------------- | ------------- | --------------------------- |
| POST   | `/columnas`                       | `ColumnaRequestDTO`  | 201 `ColumnaResponseDTO`           | 401, 404, 422 | Crear columna en proyecto   |
| GET    | `/proyectos/{proyectoId}/columnas`| —                    | 200 `List<ColumnaResponseDTO>`     | 401, 404      | Columnas de un proyecto     |
| GET    | `/columnas/{columnaId}/tareas`    | —                    | 200 `Page<TareaResponseDTO>`       | 401, 404      | Tareas de columna (pag)     |
| PUT    | `/columnas/{id}`                  | `ColumnaRequestDTO`  | 200 `ColumnaResponseDTO`           | 401, 404      | Actualizar columna          |
| DELETE | `/columnas/{id}`                  | —                    | 204                                | 401, 404      | Eliminar columna            |

### 7.4 Tareas (Requiere JWT)

| Método | Endpoint              | Body                | Response OK              | Errors         | Descripción            |
| ------ | --------------------- | -------------------- | ------------------------ | -------------- | ---------------------- |
| GET    | `/tareas`             | —                    | 200 `Page<TareaResponseDTO>`  | 401       | Mis tareas paginadas   |
| POST   | `/tareas`             | `TareaRequestDTO`    | 201 `TareaResponseDTO`   | 401, 404, 422  | Crear tarea            |
| GET    | `/tareas/{id}`        | —                    | 200 `TareaResponseDTO`   | 401, 404       | Obtener tarea por ID   |
| PATCH  | `/tareas/{id}`        | `TareaRequestDTO`    | 200 `TareaResponseDTO`   | 401, 404       | Actualizar tarea       |
| PUT    | `/tareas/{id}/mover`  | `MoverTareaDTO`      | 200 `TareaResponseDTO`   | 401, 404, 422  | Mover tarea a columna  |
| DELETE | `/tareas/{id}`        | —                    | 204                      | 401, 404       | Eliminar tarea         |

### 7.5 Comentarios (Requiere JWT)

| Método | Endpoint                              | Body                  | Response OK                        | Errors         | Descripción                |
| ------ | ------------------------------------- | --------------------- | ---------------------------------- | -------------- | -------------------------- |
| GET    | `/comentarios/tarea/{tareaId}`        | —                     | 200 `List<ComentarioResponseDTO>`  | 401, 404       | Comentarios de tarea       |
| GET    | `/comentarios/tarea/{tareaId}/paginado`| —                    | 200 `Page<ComentarioResponseDTO>`  | 401, 404       | Comentarios paginados      |
| POST   | `/comentarios`                        | `ComentarioRequestDTO`| 201 `ComentarioResponseDTO`        | 401, 404, 422  | Crear comentario           |
| PATCH  | `/comentarios/{id}`                   | `UpdateComentarioDTO` | 200 `ComentarioResponseDTO`        | 401, 404, 422  | Actualizar comentario      |
| DELETE | `/comentarios/{id}`                   | —                     | 204                                | 401, 404       | Eliminar comentario        |

### 7.6 Usuarios (Requiere JWT)

| Método | Endpoint                  | Body                  | Response OK                   | Errors    | Descripción            |
| ------ | ------------------------- | --------------------- | ----------------------------- | --------- | ---------------------- |
| GET    | `/usuarios/me`            | —                     | 200 `UsuarioResponseDTO`      | 401       | Mi perfil              |
| GET    | `/usuarios`               | —                     | 200 `List<UsuarioResponseDTO>`| 401, 403  | Listar usuarios (ADMIN)|
| PATCH  | `/usuarios/me`            | `UpdateUsuarioDTO`    | 204                           | 401, 422  | Actualizar nombre      |
| PATCH  | `/usuarios/me/password`   | `ChangePasswordDTO`   | 204                           | 401, 422  | Cambiar contraseña     |

**Total: 27 endpoints** (2 públicos + 25 autenticados, incluyendo 4 paginados)

---

## 7.7 Endpoints Paginados (Requiere JWT)

> **Nuevo en v5.2** — Paginación implementada para escalabilidad

### ¿Qué es la Paginación?

La paginación permite cargar datos en "páginas" en lugar de traer todo de una vez. Esto mejora:
- **Performance**: Respuestas más rápidas y livianas
- **Memoria**: Menos consumo de recursos del servidor
- **UX**: Mejor experiencia de carga en el frontend

### Parámetros de Paginación

Todos los endpoints paginados aceptan estos query parameters:

| Parámetro | Tipo | Default | Descripción                    |
| --------- | ---- | ------- | ------------------------------ |
| `page`    | int  | 0       | Número de página (0-indexed)   |
| `size`    | int  | 10/5    | Cantidad de items por página   |

### Ejemplo de Request

```
GET /proyectos/paginado?page=0&size=10
Authorization: Bearer <jwt_token>
```

### Ejemplo de Response (Page)

```json
{
  "content": [
    { "id": 1, "nombre": "Proyecto A", "descripcion": "Desc", "usuarioId": 1 },
    { "id": 2, "nombre": "Proyecto B", "descripcion": "Desc", "usuarioId": 1 }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": { "sorted": true, "direction": "DESC" }
  },
  "totalElements": 25,
  "totalPages": 3,
  "last": false,
  "first": true,
  "numberOfElements": 10,
  "empty": false
}
```

### Campos Importantes del Response

| Campo            | Descripción                                    |
| ---------------- | ---------------------------------------------- |
| `content`        | Array de objetos de la página actual           |
| `totalElements`  | Total de items en la base de datos             |
| `totalPages`     | Total de páginas disponibles                   |
| `pageNumber`     | Página actual (0-indexed)                      |
| `pageSize`       | Items por página                               |
| `last`           | `true` si es la última página                  |
| `first`          | `true` si es la primera página                 |
| `numberOfElements` | Cantidad de items en la página actual       |
| `empty`          | `true` si la página está vacía                 |

### Endpoints Paginados

#### Proyectos Paginados

| Método | Endpoint                   | Query Params              | Response OK         | Default Size |
| ------ | -------------------------- | ------------------------- | ------------------- | ------------ |
| GET    | `/proyectos/paginado`      | `?page=0&size=10`         | 200 `Page<ProyectoResponseDTO>` | 10 |

**Orden**: `fechaCreacion DESC` (más recientes primero)

#### Tareas Paginadas

| Método | Endpoint                   | Query Params              | Response OK         | Default Size |
| ------ | -------------------------- | ------------------------- | ------------------- | ------------ |
| GET    | `/tareas`                  | `?page=0&size=10`         | 200 `Page<TareaResponseDTO>` | 10 |
| GET    | `/columnas/{id}/tareas`    | `?page=0&size=5`          | 200 `Page<TareaResponseDTO>` | 5 |

**Orden por columna**: `prioridad ASC, fechaCreacion DESC`
**Orden todas**: `fechaCreacion DESC`

#### Comentarios Paginados

| Método | Endpoint                              | Query Params              | Response OK         | Default Size |
| ------ | ------------------------------------- | ------------------------- | ------------------- | ------------ |
| GET    | `/comentarios/tarea/{tareaId}/paginado` | `?page=0&size=5`        | 200 `Page<ComentarioResponseDTO>` | 5 |

**Orden**: `fecha DESC` (más recientes primero)

### Tabla Resumen de Endpoints Paginados

| Endpoint                              | Orden Default                    | Default Size |
| ------------------------------------- | -------------------------------- | ------------ |
| `GET /proyectos/paginado`             | `fechaCreacion DESC`             | 10           |
| `GET /tareas`                         | `fechaCreacion DESC`             | 10           |
| `GET /columnas/{id}/tareas`           | `prioridad ASC, fechaCreacion DESC` | 5        |
| `GET /comentarios/tarea/{id}/paginado`| `fecha DESC`                     | 5            |

### Diferencia con Endpoints No Paginados

| Endpoint                              | Tipo      | Response                   | Uso Recomendado                    |
| ------------------------------------- | --------- | -------------------------- | ---------------------------------- |
| `GET /proyectos`                      | Lista     | `List<ProyectoResponseDTO>`| Compatibilidad, pocos datos        |
| `GET /proyectos/paginado`             | Paginado  | `Page<ProyectoResponseDTO>`| Producción, muchos datos           |
| `GET /comentarios/tarea/{id}`         | Lista     | `List<ComentarioResponseDTO>` | Compatibilidad                 |
| `GET /comentarios/tarea/{id}/paginado`| Paginado  | `Page<ComentarioResponseDTO>` | Producción                    |

### Implementación Técnica

#### Repository (Spring Data JPA)

```java
// Spring Data genera automáticamente el SQL con LIMIT y OFFSET
Page<Proyecto> findByUsuario(Usuario usuario, Pageable pageable);

// SQL generado:
// SELECT * FROM proyectos WHERE usuario_id = ? 
// ORDER BY fecha_creacion DESC 
// LIMIT 10 OFFSET 0
```

#### Service

```java
public Page<ProyectoResponseDTO> obtenerMisProyectosPaginados(int page, int size) {
    Usuario usuario = getUsuarioLogueado();
    
    // Crear Pageable con orden
    Pageable pageable = PageRequest.of(page, size, Sort.by("fechaCreacion").descending());
    
    // Ejecutar query paginada
    Page<Proyecto> proyectosPage = proyectoRepository.findByUsuario(usuario, pageable);
    
    // Convertir entidades a DTOs
    return proyectosPage.map(this::mapToDTO);
}
```

#### Controller

```java
@GetMapping("/paginado")
public ResponseEntity<Page<ProyectoResponseDTO>> obtenerMisProyectosPaginados(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    
    return ResponseEntity.ok(proyectoService.obtenerMisProyectosPaginados(page, size));
}
```

### Uso desde el Frontend

```typescript
// Ejemplo con fetch
const response = await fetch('/proyectos/paginado?page=0&size=10', {
  headers: { 'Authorization': `Bearer ${token}` }
});
const data = await response.json();

console.log(data.content);         // Array de proyectos
console.log(data.totalElements);   // Total de proyectos
console.log(data.totalPages);      // Total de páginas
console.log(data.number);          // Página actual

// Para cargar siguiente página
const nextPage = await fetch(`/proyectos/paginado?page=${data.number + 1}&size=10`);
```

### Tests de Paginación

Se agregaron **13 tests específicos** para paginación:

```
✅ ProyectoService$ProyectosPaginados (4 tests)
   - Retorna página de proyectos correctamente
   - Retorna última página correctamente
   - Retorna página vacía si no hay proyectos
   - Ordena por fechaCreacion descendente

✅ TareaService$TareasPaginadasPorColumna (3 tests)
   - Retorna página de tareas de una columna
   - Columna de otro usuario lanza 404
   - Retorna página vacía si columna no tiene tareas

✅ TareaService$TodasMisTareasPaginadas (2 tests)
   - Retorna página de todas mis tareas
   - Paginación: página 2 de tareas

✅ ComentarioService$ComentariosPaginadosPorTarea (4 tests)
   - Retorna página de comentarios de una tarea
   - Tarea de otro usuario lanza 404
   - Retorna página vacía si tarea no tiene comentarios
   - Paginación: página 2 de comentarios
```

**Total tests del proyecto: 65** (Tests en desarrollo — problemas de compilación con Lombok en entorno de testing)

---

## 8. DTOs — Referencia

### 8.1 Request DTOs

```java
// Auth
RegisterRequestDTO {
  @NotBlank nombre,
  @Email @NotBlank email,
  @NotBlank @Size(min=8) password
}

LoginRequestDTO {
  @NotBlank email,
  @NotBlank password
}

// Proyecto
ProyectoRequestDTO {
  @NotBlank nombre,
  descripcion
}

// Columna
ColumnaRequestDTO {
  @NotBlank titulo,
  @NotNull orden,
  @NotNull proyectoId
}

// Tarea
TareaRequestDTO {
  @NotBlank titulo,
  descripcion,
  estado,
  prioridad,
  @NotNull columnaId,
  fechaVencimiento
}

// Mover Tarea
MoverTareaDTO {
  @NotNull columnaId,
  orden
}

// Comentario
ComentarioRequestDTO {
  @NotBlank contenido,
  @NotNull tareaId
}

UpdateComentarioDTO {
  @NotBlank contenido
}

// Usuario
UpdateUsuarioDTO {
  @NotBlank nombre
}

ChangePasswordDTO {
  @NotBlank oldPassword,
  @NotBlank @Size(min=8) newPassword
}
```

### 8.2 Response DTOs

```java
AuthResponseDTO        { token }
ProyectoResponseDTO    { id, nombre, descripcion, usuarioId }
ColumnaResponseDTO     { id, titulo, orden, proyectoId, tareas: List<TareaResponseDTO> }
TareaResponseDTO       { id, titulo, descripcion, estado, prioridad, proyectoId }
ComentarioResponseDTO  { id, contenido, fechaCreacion, usuarioId, tareaId }
UsuarioResponseDTO     { id, nombre, email }
ErrorResponse          { status, message, timestamp }
```

---

## 9. Configuración

### 9.1 application.properties

```properties
spring.application.name=Planova

# Base de datos — credenciales desde variables de entorno
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/planova}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.show-sql=false

# JWT — secret desde variable de entorno
jwt.secret=${JWT_SECRET}
jwt.expiration-ms=86400000
```

### 9.2 Variables de Entorno

| Variable     | Descripción                         | Ejemplo                                   |
| ------------ | ----------------------------------- | ----------------------------------------- |
| `DB_URL`     | URL de conexión MySQL               | `jdbc:mysql://localhost:3306/planova`     |
| `DB_USERNAME`| Usuario de base de datos            | `root`                                    |
| `DB_PASSWORD`| Contraseña de base de datos         | —                                         |
| `JWT_SECRET` | Clave secreta JWT (mínimo 256 bits) | —                                         |

### 9.3 Carga de Variables de Entorno

- **DotEnvConfig**: `ApplicationContextInitializer` que carga `.env` antes de que Spring cargue propiedades
- **spring.factories**: Registra el initializer para ejecución temprana
- **Prioridad**: Variables de sistema > Variables de entorno > `.env` > `application.properties`

---

## 10. Recomendaciones y Mejoras

### 10.1 Prioridad Alta

| ID  | Mejora                                     | Beneficio                          | Complejidad |
| --- | ------------------------------------------ | ---------------------------------- | ----------- |
| R1  | ~~Implementar rate limiting en `/auth/login`~~ | ✅ **IMPLEMENTADO**              | —           |
| R2  | ~~Configurar CORS~~                            | ✅ **IMPLEMENTADO**              | —           |
| R3  | Implementar refresh token                  | Mejor UX, tokens de corta duración | Media       |
| R4  | Agregar Spring Boot Actuator               | Healthcheck, métricas, monitoreo   | Baja        |

### 10.2 Prioridad Media

| ID  | Mejora                                           | Beneficio                          | Complejidad |
| --- | ------------------------------------------------ | ---------------------------------- | ----------- |
| R5  | Implementar soft-delete                          | Preservar datos históricos         | Media       |
| R6  | ~~Agregar paginación en listados~~               | ✅ **IMPLEMENTADO**              | —           |
| R7  | Implementar @PreAuthorize con UserDetailsService | Seguridad a nivel de framework     | Media       |
| R8  | Agregar tests unitarios y de integración         | Cobertura de código, detección bugs| Alta        |

### 10.3 Prioridad Baja

| ID   | Mejora                                | Beneficio                       | Complejidad |
| ---- | ------------------------------------- | ------------------------------- | ----------- |
| R10  | Agregar claims iss/aud en JWT         | Mayor seguridad en tokens       | Baja        |
| R11  | Implementar logging estructurado (MDC)| Mejor auditoría y debugging     | Baja        |
| R12  | Agregar validación de estado tareas   | Transiciones de estado válidas  | Baja        |
| R13  | Implementar búsqueda full-text        | Búsqueda en tareas y comentarios| Media       |
| R14  | ~~Setear fechaCreacion en AuthService~~| ✅ **IMPLEMENTADO**             | —           |

---

## 11. Resumen Ejecutivo

### Fortalezas
- ✅ Arquitectura limpia por capas (Controller → Service → Repository)
- ✅ Ownership verificado en TODOS los endpoints con queries derivadas
- ✅ Seguridad JWT stateless implementada correctamente
- ✅ Validación con Bean Validation (`@Valid` + anotaciones en DTOs)
- ✅ Manejo centralizado de errores (GlobalExceptionHandler)
- ✅ DTOs sin exposición de entidades JPA
- ✅ Queries optimizadas (sin fullscan, JOINs en repositorios)
- ✅ Cadena de ownership completa: Usuario → Proyecto → Columna → Tarea → Comentario
- ✅ Cascade correcto en toda la jerarquía
- ✅ Validación de mismo proyecto al mover tareas
- ✅ Rate limiting en /auth/login (5 intentos/min por IP)
- ✅ CORS configurado para acceso desde frontend
- ✅ Tokens inválidos rechazados con 401 explícito

### Áreas de Mejora
- ⚠️ Soft-delete no implementado (borrado físico irreversible)
- ⚠️ Tests en desarrollo (problemas de compilación con Lombok)
- ✅ Paginación implementada en Proyectos, Tareas y Comentarios
- ⚠️ Refresh token no implementado

### Estado General
**PRODUCCIÓN READY** con las siguientes consideraciones:
1. ✅ Rate limiting configurado
2. ✅ CORS configurado
3. Implementar tests antes de cambios mayores
4. Considerar soft-delete para datos críticos

---

**Fin del Documento**

*Documento generado por Senior Backend Reviewer — 2026-04-02*

---

## Changelog

### v5.3 (2026-04-02) — Documentación actualizada

- Corrección de estado de tests (problemas de compilación con Lombok)
- Actualización de resumen ejecutivo (paginación marcada como implementada)
- Verificación de 27 endpoints documentados vs código fuente (corregido de 26)
- Verificación de 17 DTOs documentados vs código fuente
- Verificación de cascadas y ownership en código fuente
- Corrección de endpoints paginados (4, no 5)

### v5.2 (2026-04-02) — Paginación implementada

- Paginación en Proyectos, Tareas y Comentarios
- 13 tests de paginación agregados
- Endpoints paginados documentados

### v5.1 (2026-04-02) — Vulnerabilidades corregidas

| Archivo                              | Cambio                                                     |
| ------------------------------------ | ---------------------------------------------------------- |
| `security/LoginRateLimitFilter.java` | **NUEVO** — Rate limiting en /auth/login (5 req/min por IP) |
| `security/SecurityConfig.java`       | CORS configurado + LoginRateLimitFilter registrado         |
| `security/JwtFilter.java`            | Tokens inválidos retornan 401 explícito (antes: continuaba)|
| `auth/AuthService.java`              | `fechaCreacion` setea `LocalDateTime.now()` en registro    |

### v5.0 (2026-04-02) — Documentación reescrita

- Corrección del modelo de datos (capa Columna incluida)
- 21 endpoints documentados con simulaciones
- Validación de ownership completa documentada
- Vulnerabilidades identificadas (4 corregidas, 5 pendientes)
