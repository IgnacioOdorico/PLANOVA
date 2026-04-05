# AI Design Guidelines

## Propósito
Definir qué partes del sistema deben ser replicadas exactamente y cuáles permiten libertad creativa controlada.

---

## 🔒 Zonas estrictas (NO MODIFICAR)

Las siguientes vistas deben replicarse EXACTAMENTE según las imágenes en `/docs/assets`:

- Login
- Home (Proyectos)
- Vista de Proyecto (Kanban)

No se permite:
- Cambiar layout
- Cambiar colores
- Cambiar estilo glass
- Reorganizar elementos

---

## 🔓 Zonas flexibles (IA puede diseñar)

Las siguientes vistas NO tienen diseño definido y pueden ser creadas libremente:

- Perfil de usuario
- Vista de notas de tarea

---

## 🎨 Reglas para zonas flexibles

Aunque son libres, DEBEN respetar el estilo global:

### Estilo obligatorio
- Glassmorphism (vidrio líquido)
- Alto uso de blur
- Bordes suaves y brillantes
- Glow dinámico
- Sensación flotante

### Inspiración visual
- Deben parecer parte del mismo sistema
- Mantener coherencia con `/docs/assets`

### UX
- Minimalista
- Limpio
- Enfocado en legibilidad

---

## 🚫 Restricciones

Incluso en zonas flexibles, NO se permite:
- UI plana (sin glass)
- Colores fuera de la paleta
- Diseños tipo material UI estándar
- Interfaces genéricas

---

## 🧠 Directiva final

La IA debe priorizar:
→ Estética visual
→ Coherencia con el sistema
→ Sensación premium

Por encima de simplicidad técnica.