Integrantes
Bruno Gutiérrez
Alejandra Silva
Emilio Poblete
Bastian Ramos
Jesús Ganga
# Sistema de Gestión de Tareas Georreferenciadas - Control 2

Este proyecto corresponde a la implementación de un sistema de gestión de tareas con funcionalidades espaciales, desarrollado con una arquitectura separada:
- **Frontend** en Vue.js
- **Backend** como API RESTful en Spring Boot
- **Base de Datos** en PostgreSQL con la extensión PostGIS para el almacenamiento y análisis de datos geoespaciales.


##  Requisitos Previos

Gracias a la contenedorización del proyecto, los requisitos para levantar el sistema en cualquier entorno local se han simplificado enormemente. Es estrictamente necesario contar con:

- **Docker** y **Docker Compose** instalados en el sistema.
- **Git** (para clonar el repositorio).

*(Nota para desarrollo local sin contenedores: Java 21, Maven, Node.js/npm y PostgreSQL 16 con PostGIS).*


##  Instrucciones de Despliegue (Ejecución Automática)

El proyecto está configurado para levantar la Base de Datos, el Backend y el Frontend de manera orquestada con un solo comando, creando las tablas y configuraciones de forma automática.


### Paso 1: Obtener el proyecto
Abra una terminal y clone el repositorio (o haga un pull si ya lo tiene):

git clone https://github.com/Bix0/TBD.git

### Paso 2: Posicionarse en la carpeta raíz del proyecto

cd "TBD/controles/CONTROL 2"

### Paso 3: Levantar los servicios
Ejecute el siguiente comando para construir las imágenes y levantar todos los contenedores en segundo plano:

 docker-compose up -d --build

### Paso 4: Acceder a la aplicación
Una vez que el comando termine y los contenedores estén en ejecución (puede verificarlo con docker ps), los servicios estarán disponibles en las siguientes rutas:
Frontend (Interfaz de Usuario): Ingrese desde su navegador a http://localhost:80 (o simplemente http://localhost).
Backend (API RESTful): Corriendo internamente y expuesto en http://localhost:8080.
Base de Datos (PostgreSQL/PostGIS): Corriendo en el puerto 5432 (Credenciales por defecto en docker-compose.yml: Usuario adminControl, Password Elpro123#, BD backend-control2).
## Credenciales y Acceso Inicial
Debido a que el sistema se levanta con una base de datos limpia, para comenzar a probar la aplicación siga estos pasos:
1. Ingrese a `http://localhost:80`
2. Diríjase a la vista de **Registro** en el frontend.
3. Cree un usuario nuevo proporcionando un nombre, contraseña y marcando su ubicación en el mapa.
4. Inicie sesión con las credenciales recién creadas para acceder al panel de tareas.

### Paso 5: Detener la aplicación
Para apagar el sistema y liberar los puertos sin perder la información estructurada, ejecute:

docker-compose down
