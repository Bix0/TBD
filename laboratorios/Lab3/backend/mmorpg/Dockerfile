
# Etapa 1: Build con Maven + JDK 21
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copiar pom.xml y código fuente
COPY pom.xml .
COPY src ./src

# Compilar sin ejecutar tests
RUN mvn clean package -DskipTests -B

# Etapa 2: Ejecución con JDK 21
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copiar el JAR desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Puerto de la aplicación
EXPOSE 8080

# Ejecutar
ENTRYPOINT ["java", "-jar", "app.jar"]