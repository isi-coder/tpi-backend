# 🚛 TPI – Backend Transportes de Contenedores
Sistema backend completo para una empresa transportista, desarrollado con **Spring Boot**, **Microservicios**, **API Gateway**, **Keycloak**, **PostgreSQL** y **OSRM**.

Implementa solicitudes de traslado de contenedores, rutas tentativas, asignación de camiones, seguimiento, depósitos y cálculo de costos reales.

---

## 📦 Arquitectura General

/tpi-backend
├── api-gateway (Spring Cloud Gateway)
├── ms-maestros (camiones, depósitos, contenedores, tarifas)
├── ms-operaciones (solicitudes, rutas, tramos, seguimiento)
├── docker-compose.yml (opcional – levantar todo junto)
├── README.md
└── postman_collection/


### 🧱 Microservicios

| Servicio         | Puerto | Función |
|------------------|--------|---------|
| API Gateway      | 8085   | Entry point – JWT validation + routing |
| ms-maestros      | 8092   | Catálogos: camiones, tipos, contenedores, depósitos, tarifas |
| ms-operaciones   | 8091   | Solicitudes, rutas, tramos, costos reales |
| Keycloak         | 8081   | Seguridad y gestión de usuarios |
| PostgreSQL       | 5433   | Base de datos |

---

# 🔐 Seguridad – Keycloak

### Roles utilizados
- `cliente`
- `operador`
- `transportista`
- `admin`

### Usuarios para pruebas
|      Usuario       |      Rol      |Password|
|--------------------|---------------|------|
| test-cliente       | cliente       | 1234 |
| test-operador      | operador      | 1234 |
| test-transportista | transportista | 1234 |

> El API Gateway valida tokens JWT emitidos por el realm:  
`http://localhost:8081/realms/tpi-backend`

---

# 🗂 Base de Datos

Cada micro usa su propio schema:

- `maestros`
- `operaciones`

Hibernate genera tablas automáticamente (`ddl-auto=update`).

---

# 🌍 API Externa – OSRM

El sistema consulta OSRM para obtener distancia/duración real entre coordenadas:

Ejemplo de request:
http://router.project-osrm.org/route/v1/driving/{lonA},{latA};{lonB},{latB}


Se usa para:
- estimar distancia origen→destino
- origen→depósito→destino
- calcular tarifas aproximadas
- calcular tiempos estimados

---

# 🚀 Cómo correr el proyecto (sin Docker)

### 1️⃣ Levantar Keycloak  
Importar el realm `tpi-backend` (archivo JSON si está en el repo).

### 2️⃣ Levantar PostgreSQL  
Puedes usar tu propio servidor o Docker.

### 3️⃣ Correr microservicios en este orden:
1. `api-gateway`
2. `ms-maestros`
3. `ms-operaciones`

---

# 🐳 Cómo correr todo con Docker Compose

> (Opcional — si agregás el `docker-compose.yml` final)

```bash
docker compose up --build
Esto levanta:

PostgreSQL

Keycloak

OSRM (opcional)

api-gateway

ms-maestros

ms-operaciones

postman_collection/
 ├── 1 - Maestros
 ├── 2 - Solicitudes
 ├── 3 - Tramos (operador)
 ├── 4 - Transportistas
 ├── 5 - Testing de flujos completos

Cada request ya tiene:

Auth con token JWT

Variables para URL base

Tests automáticos básicos

