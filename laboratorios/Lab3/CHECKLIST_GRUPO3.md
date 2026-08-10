# Checklist - Grupo 3: Gestor de Clanes y Raids para MMORPG

Esta lista contiene los 6 requerimientos técnicos avanzados de MongoDB que deben implementarse en el backend del proyecto.

- [X] **1. Modelado de Datos**
  - **Objetivo:** Justificar e implementar la decisión de diseño para los *Personajes*.
  - **Detalle:** Decidir si embeber los personajes dentro del documento del Jugador o referenciarlos en una colección propia, considerando que cada personaje participa en múltiples raids de forma independiente.

- [X] **2. Validación de Esquema ($jsonSchema)**
  - **Objetivo:** Regla de validación para la asignación de botín (Loot) y validación de esquemas global.
  - **Detalle:** Se implementaron validadores `$jsonSchema` a nivel de colección para todas las colecciones del sistema (`historial_loot`, `jugadores`, `personajes`, `clanes`, `raids`, `items`, `inscripciones_raid`, `inventarios`, `auditoria_liderazgo`), garantizando tipos BSON, campos obligatorios, valores enum y rangos válidos con nivel estricto (`validationLevel: strict`, `validationAction: error`).


- [ ] **3. Transacción Multi-Documento (ACID)**
  - **Objetivo:** Distribución segura de Loot en eventos de Raid.
  - **Detalle:** Diseñar una transacción que distribuya el Loot entre varios personajes de forma atómica. Debe evitar que un mismo ítem quede asignado a más de un personaje si hay solicitudes concurrentes (al mismo tiempo).

- [ ] **4. Aggregation Pipeline**
  - **Objetivo:** Cálculo del Ranking de Clanes.
  - **Detalle:** Construir un pipeline (usando `$group`, `$lookup` y `$sort`) que calcule el ranking de clanes basándose en su desempeño en las raids: tiempo de finalización, asistencia y daño total.

- [ ] **5. Índices**
  - **Objetivo:** Optimización de consultas frecuentes.
  - **Detalle:** 
    - Diseñar un índice compuesto para filtrar rápidamente personajes disponibles por clase y rol (ej. "Healer") dentro de un clan.
    - Diseñar un índice único sobre el nombre del personaje para evitar duplicados.

- [ ] **6. Change Streams (Reactividad)**
  - **Objetivo:** Automatización tras la muerte de un Boss.
  - **Detalle:** Implementar un listener que detecte la inserción/actualización del evento "muerte del Boss" en la colección de Raids. Al detectarlo, debe disparar automáticamente el proceso de distribución de Loot y actualizar una colección materializada de "clanes mejor rankeados" (usando `$merge`).
