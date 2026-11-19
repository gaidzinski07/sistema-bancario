# syntax=docker/dockerfile:1
#
# Imagem única da aplicação: a API Spring Boot servindo também o build do Angular
# (mesmo domínio, sem CORS). Três estágios: front, back e a imagem final só com o JRE.
#
#   docker build -t banco .
#   docker run -p 8080:8080 -e DB_URL=... -e DB_USER=... -e DB_PASSWORD=... -e JWT_SECRET=... banco
#
# Por padrão sobe com o profile `prod` (sem seed, sem Swagger). Para criar o primeiro administrador
# num banco vazio, defina BANCO_ADMIN_INICIAL_CPF, BANCO_ADMIN_INICIAL_NOME e BANCO_ADMIN_INICIAL_SENHA.

# ---------------------------------------------------------------- frontend
FROM node:22-alpine AS frontend
WORKDIR /build
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY frontend/ ./
RUN npx ng build

# ---------------------------------------------------------------- backend
FROM eclipse-temurin:21-jdk-alpine AS backend
WORKDIR /build
# Dependências numa camada própria: só baixa de novo quando o pom.xml mudar.
COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY backend/src src
RUN ./mvnw -B -q -DskipTests package && cp target/banco-backend-*.jar app.jar

# ---------------------------------------------------------------- imagem final
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S banco && adduser -S -G banco banco
WORKDIR /app
COPY --from=backend /build/app.jar app.jar
COPY --from=frontend /build/dist/frontend/browser ./frontend
USER banco

ENV SPRING_PROFILES_ACTIVE=prod \
    BANCO_FRONTEND_DIRETORIO=/app/frontend \
    TZ=UTC
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"UP"' || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
