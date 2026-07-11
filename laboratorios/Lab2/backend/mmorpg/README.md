# MMORPG - Grupo 3

## Stack
- **Backend**: Java 21 + Spring Boot 3 + Spring JDBC + BCrypt + JWT (jjwt 0.12.6)
- **Frontend**: React + Vite
- **Base de Datos**: PostgreSQL 16 en Docker
- **Puertos**: Backend:8080, Frontend:5173, PostgreSQL:5435

---

## Requisitos
- Docker + Docker Compose
- Java 21
- Node.js 18+

---

## Instalación y Ejecución

### 1. Levantar PostgreSQL
```bash
cd backend/mmorpg
docker compose up -d
Esto crea el contenedor postgres-mmorpg-aislado con la base db_laboratorio.

2. Inicializar la base de datos (schema + datos de prueba)

# Copia el schema.sql al contenedor y lo ejecuta
docker compose exec -T postgres psql -U admin_raid -d db_laboratorio < src/main/resources/schema.sql
(Opcionalmente Spring Boot lo ejecuta automático al iniciar con spring.sql.init.mode=always)

3. Compilar y ejecutar backend

cd backend/mmorpg
./mvnw clean package -DskipTests
java -jar target/mmorpg-0.0.1-SNAPSHOT.jar
El backend levanta en http://localhost:8080.

4. Instalar dependencias y ejecutar frontend

cd frontend
npm install
npm run dev
El frontend levanta en http://localhost:5173.

5. Usuarios de prueba
Usuario	Password	Rol
bruno	123456	Admin
xhuala	123456	Usuario
Error -9 (proceso zombie)
Si al ejecutar java -jar ... aparece:


Web server failed to start. Port 8080 was already in use.
O el proceso anterior queda zombie y no deja iniciar Spring Boot:

Solución 1: Matar el proceso por puerto


sudo fuser -k 8080/tcp
Solución 2: Buscar y matar el proceso


lsof -i :8080
# o
ps aux | grep java
kill -9 <PID>
Solución 3 (Docker): Si el proceso está dentro de otro contenedor:


docker ps
docker stop <container-id>
Este error ocurre cuando un proceso Java anterior no se cerró correctamente y el puerto 8080 queda ocupado.

Endpoints principales
Autenticación
POST /api/auth/login — Login (devuelve JWT)
POST /api/auth/register — Registrar nuevo usuario
CRUD
GET/POST /api/jugadores — Listar/Crear jugadores
GET/POST /api/personajes — Listar/Crear personajes
GET/POST /api/clanes — Listar/Crear clanes
GET/POST /api/raids — Listar/Crear raids
GET/POST /api/items — Listar/Crear items
Raids
POST /api/raids/{id}/inscribir?idPersonaje=X — Inscribir personaje a raid
Inventario
POST /api/personajes/{id}/inventario — Agregar item al inventario
