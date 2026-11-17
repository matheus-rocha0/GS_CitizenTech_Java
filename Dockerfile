# Multi-stage build para a aplicacao UPA Agora (Java 21 + Spring Boot)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Otimizacao de cache para dependencias Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copia codigo-fonte e compila gerando o JAR executavel
COPY src ./src
RUN mvn clean package -DskipTests

# Stage final: imagem minima com Alpine e JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia artefato compilado
COPY --from=build /app/target/*.jar app.jar

# Variaveis de ambiente padrao para cloud (Render.com)
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
