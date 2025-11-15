# TPI Backend (skeleton)

Multi-módulo con `gateway`, `ms-maestros`, `ms-operaciones` y `common`.
- Java 17, Spring Boot 3.3.x, Spring Cloud 2024.0.x
- Security: Resource Server (JWT) contra Keycloak
- OSRM: `http://localhost:5000`

## Dev rápido
```bash
cd docker
docker compose up -d
cd ..
./mvnw -q -DskipTests package
# Levantar módulos (puertos: gateway:8080, ops:8091, maestros:8092)
```
