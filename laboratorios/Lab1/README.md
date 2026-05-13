###### Grupo e integrantes:

Grupo 3 

# Integrantes
- Bruno Gutiérrez
- Alejandra Silva
- Emilio Poblete
- Bastian Ramos
- Jesús Ganga

###### Descripción del proyecto

Gestor de Clanes y Raids - MMORPG que administra:
- Clanes
- Personajes
- Raids
- Inventario
- Distribución de botín

# Estructura
Implementa una arquitectura en capas con la lógica de negocios manejada por pgSQL.

# Tecnologías usadas
- Base de datos: PostgreSQL 15.3
  - Alternativa: Docker corriendo PostgreSQL
- Backend: Java 21 + Spring Boot 4.0.5 (Con JdbcTemplate)
  - API REST: Jdbc (Tecnología no ORM)
- Frontend: React 19 + Vite + Axios
- Seguridad: JWT + BCrypt


###### Guía de despliegue

### 1.- Requisitos previos
- Java JDK 21 o superior
- Maven 3.9+
- Node.js 18 o superior
- Npm 9 o superior
- Docker 24 (Con imágen PostgreSQL)
ó
- PostgreSQL 15+

### 2.- Descargar el archivo lab1.zip, descomprimir y comprobar la existencia de:
- README.md (este archivo)
- /backend (directorio)
- /frontend (directorio)

### 3.- Iniciar la base de datos, hay dos opciones:
- Opción A - Docker: Ejecutar los siguientes comandos en terminal (abierto en el directorio /Lab1):
  - Asegurarse de que Docker Desktop esta corriendo.
  - Inicializar:
cd backend/mmorpg
docker compose up -d

  - Verificar que esté corriendo:
docker ps
<<Debería mostrar: postgres-mmorpg-aislado up (healthy)>>

- Opción B - PostgreSQL nativo (psql):
  - Abre psql o pgAdmin con credenciales locales
  - Abrir servidor 16
  - Click derecho en DataBase, seleccionar Crear DataBase
  - asignar nombre: db_guild
  - obtener el data source url
  - Conectarse a la base de datos
  - Comprobar en 'application.properties' que las credenciales y el puerto coincidan

# Posibles errores
- Puerto 5432 está siendo usado por otro proceso, puede ser docker o psql nativo
Solución: Cerrar docker o psql
- 'failed to connect to the docker API', docker no está abierto
Solución: Abrir Docker Desktop

### 4.- Iniciar backend (Spring Boot):
<<Abrir terminal en /lab1>>

# Situarse en el directorio de mvn
cd backend/mmorpg

# Dar permisos a wrapper
chmod +x ./mvnw

# Ejecutar
./mvnw spring-boot:run

El backend estará disponible en 'http://localhost:8080'

Al iniciarse el backend:
- Se ejecuta automáticamente el script 'schema.sql' que crea tablas, indices, triggers, SPs y vistas.
- 'DataSeeder' que comprueba si la BD esta vacía e inserta datos de prueba:

Usuario | Contraseña | Rol

tato_admin| 123456 | Admin
jugador2  | 123456 | Usuario
jugador3  | 123456 | Usuario

# Posibles errores
- 'port 8080 already in use', el puerto 8080 esta siendo usado por otro proceso:
Solución: Matar el proceso ejecutando el/los siguientes comandos en terminal:
lsof -ti :8080 | xargs kill -9  //MacOS

sudo fuser -k 8080/tcp          //Linux

netstat -ano | findstr :8080    //Windows -> obtener PID de la linea que dice LISTENING
taskkill /PID [PID] /F          //Windows -> [PID] obtenido con el comando anterior

### 5.- Iniciar frontend (React + Vite)
<<Abrir terminal en /lab1>>

cd frontend

# Instalar dependencias
npm install

# Iniciar
npm run dev

El frontend estará disponible en 'http://localhost:5173'

### 6.- Probar la aplicación

1. Abrir 'http://localhost:5173' en el navegador.
2. Iniciar sesión con 'tato_admin' / '123456' para usuario ADMIN
3. Iniciar sesión con 'jugador2' o 'jugador3' / '123456' para usuario jugador.
