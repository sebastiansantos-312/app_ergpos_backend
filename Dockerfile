# Etapa 1: construir el JAR
FROM eclipse-temurin:21-jdk AS builder

# Establecer directorio de trabajo
WORKDIR /app

# Copiar los archivos de configuración de Maven o Gradle
COPY pom.xml ./
COPY mvnw ./
COPY .mvn .mvn

# Descargar dependencias (para cache más eficiente)
RUN ./mvnw dependency:go-offline

# Copiar el código fuente
COPY src ./src

# Construir el proyecto
RUN ./mvnw clean package -DskipTests

# Etapa 2: imagen final ligera
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiar el .jar desde la etapa anterior
COPY --from=builder /app/target/*.jar app.jar

# Exponer el puerto (Render usa 8080 por defecto)
EXPOSE 8080

# Comando para ejecutar el JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
