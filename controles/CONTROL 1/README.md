# Controles_TBD
Control 1, GRUPO 3 (Base de datos de peluquerías)
Integrantes:
-Bruno Gutiérrez
-Alejandra Silva
-Emilio Poblete
-Bastian Ramos
-Jesús Ganga

https://www.youtube.com/watch?v=5MujiN69IAw&feature=youtu.be

MMORPG Gestión de Clanes
Este proyecto es un sistema integral de información diseñado para la gestión de datos complejos dentro de un entorno MMORPG (Massively Multiplayer Online Role-Playing Game). Implementa una arquitectura desacoplada que incluye un Backend (API REST), un Frontend (React) y una Base de Datos relacional robusta (PostgreSQL).

🚀 Arquitectura y Tecnologías
Base de Datos: PostgreSQL (Lógica centralizada mediante PL/pgSQL).

Backend: Java / Spring Boot (Implementado con JdbcTemplate, garantizando CERO uso de ORMs según los requerimientos técnicos).

Frontend: React.js / Vite (Interfaz de usuario interactiva).

Seguridad: JSON Web Tokens (JWT) con Control de Acceso Basado en Roles (RBAC).

⚙️ Proceso de Despliegue (Instalación y Ejecución)
Paso 1: Configuración de la Base de Datos (PostgreSQL)
Para garantizar la correcta compilación de los objetos programables (Triggers y Stored Procedures), la base de datos debe ser inicializada directamente en el motor:

Abre pgAdmin y crea una nueva base de datos llamada dbd_guild.

Haz clic derecho sobre dbd_guild y selecciona Query Tool.

Abre el archivo src/main/resources/schema.sql (incluido en este repositorio), copia todo su contenido y pégalo en el Query Tool.

Presiona el botón de Ejecutar (Play). Esto creará automáticamente todas las tablas normalizadas, 3 índices de rendimiento, 1 vista materializada, 2 procedimientos almacenados y 6 disparadores (Triggers).

Paso 2: Despliegue del Backend (Spring Boot)
Asegúrate de tener instalado Java 17 (o superior) y Maven.

Abre el proyecto backend en tu IDE (ej. IntelliJ IDEA o VS Code).

Verifica que el archivo src/main/resources/application.properties tenga las credenciales correctas de tu servidor local de PostgreSQL (usuario y contraseña).

Ejecuta la clase principal de la aplicación (MmorpgApplication.java) o corre el siguiente comando en la terminal desde la raíz del proyecto backend:

Bash
mvn spring-boot:run
El servidor se levantará de forma segura en http://localhost:8080.

Paso 3: Despliegue del Frontend (React)
Asegúrate de tener instalado Node.js.

Abre una nueva terminal y navega hasta la carpeta del proyecto frontend.

Instala las dependencias necesarias ejecutando:

Bash
npm install
Inicia el servidor de desarrollo ejecutando:

Bash
npm run dev
Abre el navegador en la ruta indicada por Vite (usualmente http://localhost:5173) para comenzar a interactuar con la plataforma.

🧠 Lógica de Negocio en el Servidor (Base de Datos)
Este proyecto delega el peso de las validaciones y auditorías críticas directamente al motor de la base de datos para asegurar la integridad de la información:

Procedimientos Almacenados (Transacciones Atómicas)
sp_distribuir_botin: Gestiona de forma transaccional el descuento de DKP, inserción en el historial de loot, entrega del ítem al inventario, cierre de la Raid y asignación de asistencia masiva.

sp_crear_raid_e_invitar: Crea una instancia de Raid e inscribe automáticamente y de forma masiva a todos los personajes que posean el rol de "Raider".

Disparadores Críticos (Triggers)
Auditoría de Liderazgo: Registra automáticamente cualquier cambio en la jerarquía del clan, almacenando al antiguo líder, al nuevo y la estampa de tiempo exacta del cambio.

Reglas de Negocio: Validación estricta del Item Level (Poder) para bloquear inscripciones a Raids de alto nivel, sistema anti-trampas de equipo único en el inventario, y auto-cálculo matemático del poder total del personaje basado en sus armas equipadas.
