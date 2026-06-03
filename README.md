# Gestor de Siniestros — Backend

API REST para la gestión de siniestros de seguros. Proyecto de portfolio full-stack construido con Spring Boot 3, PostgreSQL y JWT.

🌐 **Demo**: https://gestor-siniestros-frontend.vercel.app  
⚙️ **API**: https://gestor-siniestros-backend-production.up.railway.app

---

## Stack

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17 | Lenguaje |
| Spring Boot | 3.3.5 | Framework principal |
| Spring Security | 6.x | Autenticación y autorización |
| PostgreSQL | 15+ | Base de datos |
| Liquibase | 4.27 | Migraciones de BD |
| jjwt | 0.12.5 | Generación y validación JWT |
| MapStruct | 1.5.5 | Mapeo entidad ↔ DTO |
| Lombok | — | Reducción de boilerplate |
| Maven | 3.9+ | Gestión de dependencias |

---

## Características

- **Autenticación JWT** con `accessToken` (1h) y `refreshToken` (24h)
- **Autorización por roles**: ADMIN · GESTOR · PERITO con `@PreAuthorize`
- **CRUD completo** de Clientes, Pólizas y Siniestros
- **Máquina de estados** para Siniestros: `ABIERTO → EN_PERITACION → RESUELTO/DENEGADO`
- **Log de auditoría** automático en cada cambio de estado
- **Dashboard KPIs**: conteos por estado, importe total indemnizado, media días resolución, distribución por tipo
- **Paginación y filtros** en todas las listas
- **Soft delete** en clientes (`activo = false`)
- **Validaciones de negocio**: importe indemnizado ≤ cobertura máxima, solo el perito asignado puede resolver
- **Migraciones versionadas** con Liquibase
- **Manejo global de errores** con formato homogéneo

---

## Arquitectura

```
src/main/java/com/portfolio/siniestros/
├── config/          # SecurityConfig, CorsConfig
├── security/        # JwtService, JwtFilter, UserDetailsServiceImpl
├── entity/          # Entidades JPA + enums
├── repository/      # Repositorios Spring Data JPA
├── dto/             # DTOs request/response
├── mapper/          # Mappers MapStruct
├── service/         # Lógica de negocio
├── controller/      # Controllers REST
└── exception/       # GlobalExceptionHandler, excepciones custom
```

**Flujo de una petición:**  
`Request → JwtFilter → Controller → Service → Repository → BD`

---

## Requisitos previos

- Java 17+
- Maven 3.9+
- PostgreSQL 15+

---

## Instalación local

### 1. Clonar el repositorio

```bash
git clone https://github.com/ale-dm/gestor-siniestros-backend.git
cd gestor-siniestros-backend
```

### 2. Crear la base de datos

```sql
CREATE DATABASE siniestros_db;
CREATE USER siniestros_user WITH PASSWORD 'siniestros123';
GRANT ALL PRIVILEGES ON DATABASE siniestros_db TO siniestros_user;
GRANT CREATE ON SCHEMA public TO siniestros_user;
```

### 3. Configurar variables de entorno

Copia `.env.example` a `.env` y rellena los valores:

```bash
cp .env.example .env
```

```env
DB_URL=jdbc:postgresql://localhost:5432/siniestros_db
DB_USER=siniestros_user
DB_PASSWORD=siniestros123
JWT_SECRET=unaClaveLargaDeAlMenos32Caracteres
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

> En IntelliJ: instala el plugin **EnvFile** y apunta al `.env` en la Run Configuration.

### 4. Arrancar

```bash
mvn spring-boot:run
```

Liquibase ejecutará automáticamente las 7 migraciones y creará los datos iniciales.  
La API queda disponible en `http://localhost:8080`.

---

## Variables de entorno

| Variable | Descripción | Ejemplo |
|---|---|---|
| `DB_URL` | JDBC URL de PostgreSQL | `jdbc:postgresql://localhost:5432/siniestros_db` |
| `DB_USER` | Usuario de la BD | `siniestros_user` |
| `DB_PASSWORD` | Contraseña de la BD | `siniestros123` |
| `JWT_SECRET` | Clave para firmar JWT (mín. 32 chars) | `xK9mP2vL8nQ4wR7...` |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos (separados por coma) | `https://mi-app.vercel.app` |
| `SPRING_PROFILES_ACTIVE` | Perfil activo | `dev` / `prod` |

---

## Endpoints

```
POST   /api/auth/login
POST   /api/auth/refresh

GET    /api/clientes?page=0&size=10&search=garcia
GET    /api/clientes/{id}
POST   /api/clientes
PUT    /api/clientes/{id}
PATCH  /api/clientes/{id}/desactivar

GET    /api/polizas?estado=ACTIVA&tipo=HOGAR&page=0
GET    /api/polizas/{id}
POST   /api/polizas
PUT    /api/polizas/{id}
PATCH  /api/polizas/{id}/estado

GET    /api/siniestros?estado=ABIERTO&peritoId=2&page=0
GET    /api/siniestros/{id}
POST   /api/siniestros
PATCH  /api/siniestros/{id}/estado
PATCH  /api/siniestros/{id}/asignar-perito
GET    /api/siniestros/{id}/log

GET    /api/dashboard/resumen

GET    /api/usuarios
GET    /api/usuarios/peritos
PATCH  /api/usuarios/{id}/activar
```

### Autenticación

Todas las rutas excepto `/api/auth/**` requieren el header:

```
Authorization: Bearer <accessToken>
```

### Roles

| Endpoint | ADMIN | GESTOR | PERITO |
|---|:---:|:---:|:---:|
| Clientes (lectura) | ✅ | ✅ | ✅ |
| Clientes (escritura) | ✅ | ✅ | ❌ |
| Siniestros (lectura) | ✅ | ✅ | ✅ |
| Siniestros (crear/asignar) | ✅ | ✅ | ❌ |
| Siniestros (resolver/denegar) | ✅ | ✅ | ✅* |
| Usuarios | ✅ | ❌ | ❌ |

> *El perito solo puede resolver/denegar los siniestros que tiene asignados.

### Formato de respuesta de error

```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "El importe indemnizado supera la cobertura máxima de la póliza",
  "timestamp": "2026-06-03T14:00:00"
}
```

---

## Base de datos

### Modelo de datos

```
usuarios ──< siniestros (perito)
polizas  ──< siniestros ──< log_siniestro
clientes ──< polizas
```

### Migraciones Liquibase

| Archivo | Contenido |
|---|---|
| `001-create-usuarios.xml` | Tabla `usuarios` |
| `002-create-clientes.xml` | Tabla `clientes` |
| `003-create-polizas.xml` | Tabla `polizas` |
| `004-create-siniestros.xml` | Tabla `siniestros` |
| `005-create-log-siniestro.xml` | Tabla `log_siniestro` |
| `006-insert-data-inicial.xml` | Datos de prueba |
| `007-fix-passwords.xml` | Fix hashes BCrypt |

### Credenciales de prueba

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `password123` | ADMIN |
| `gestor1` | `password123` | GESTOR |
| `perito1` | `password123` | PERITO |

---

## Deploy en Railway

1. Fork o clona este repo en GitHub
2. En [railway.app](https://railway.app) → New Project → Deploy from GitHub
3. Añade un servicio **PostgreSQL**
4. Configura las variables de entorno (ver tabla arriba), usando las reference variables de Railway:
   ```
   DB_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
   DB_USER=${{Postgres.PGUSER}}
   DB_PASSWORD=${{Postgres.PGPASSWORD}}
   SPRING_PROFILES_ACTIVE=prod
   ```
5. Railway detecta el `pom.xml` y despliega automáticamente

---

## Frontend

El frontend de este proyecto está en:  
🔗 https://github.com/ale-dm/gestor-siniestros-frontend
