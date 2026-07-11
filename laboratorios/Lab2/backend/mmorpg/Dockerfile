# ============================================
# Dockerfile para backend MMORPG (Lab2)
# ============================================
# Build:   docker build -t lab2-backend .
# Run:     docker run -p 8080:8080 lab2-backend
# ============================================

# Etapa 1: Build con Maven + JDK 21
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar pom.xml y descargar dependencias (cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn package -DskipTests -B

# Etapa 2: Ejecución con JDK 21
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copiar el JAR desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Puerto de la aplicación
EXPOSE 8080

# Ejecutar
ENTRYPOINT ["java", "-jar", "app.jar"]
