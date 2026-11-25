# Pedidos360

Sistema compuesto por un frontend Angular y tres microservicios Spring Boot:

- `pedidos360-frontend` — Angular, `http://localhost:4200`
- `servicio-producto` — API de productos, `http://localhost:8081`
- `servicio-pedido` — API de pedidos, `http://localhost:8082`
- `servicio-rabbitmq-admin` — API de administración de RabbitMQ (colas, exchanges, bindings), `http://localhost:8083`

La autenticación de `servicio-producto` y `servicio-pedido` se realiza con Microsoft Entra ID mediante MSAL/OAuth2. Las bases de datos usan PostgreSQL y la comunicación asíncrona entre microservicios usa RabbitMQ; ambos se levantan localmente con Docker Compose. `servicio-rabbitmq-admin` no requiere autenticación (es una herramienta de administración interna).

## Requisitos

- Node.js y npm
- Java 21 o superior
- Docker y Docker Compose
- Acceso a una App Registration de Microsoft Entra ID

## Infraestructura local (Docker)

RabbitMQ y PostgreSQL se levantan con el `docker-compose.yml` de la raíz del proyecto:

```powershell
docker compose up -d
```

Esto expone:

- PostgreSQL en `localhost:5432` (db `pedidos360`, usuario/clave `pedidos360`)
- RabbitMQ (AMQP) en `localhost:5672`
- RabbitMQ Management UI en [http://localhost:15672](http://localhost:15672) (usuario/clave `guest`)

Los tres microservicios Spring Boot apuntan a esta infraestructura por defecto; no se necesita configuración adicional para desarrollo local.

## Configuración de Entra ID

En `pedidos360-frontend/src/environments/environment.development.ts` verifica:

```ts
clientId: 'TU_CLIENT_ID'
tenantId: 'TU_TENANT_ID'
redirectUri: 'http://localhost:4200'
postLogoutRedirectUri: 'http://localhost:4200'
apiScopes: ['api://TU_API_ID/access_as_user']
```

En Microsoft Entra ID registra `http://localhost:4200` como URI de redirección de tipo **Single-page application**. En **Expose an API**, crea o verifica el scope delegado `access_as_user`. El valor de `apiScopes` debe coincidir exactamente con el App ID URI y el scope expuesto por tu API.

## Levantar el proyecto

Desde la carpeta raíz `Pedidos360`, primero levanta la infraestructura y luego cada servicio en su propia terminal.

### 0. Infraestructura (RabbitMQ + PostgreSQL)

```powershell
docker compose up -d
```

### 1. Microservicio de productos

```powershell
cd servicio-producto
.\mvnw.cmd spring-boot:run
```

### 2. Microservicio de pedidos

```powershell
cd servicio-pedido
.\mvnw.cmd spring-boot:run
```

### 3. Microservicio de administración de RabbitMQ

```powershell
cd servicio-rabbitmq-admin
.\mvnw.cmd spring-boot:run
```

### 4. Frontend

```powershell
cd pedidos360-frontend
npm install
npm start
```

Abre [http://localhost:4200](http://localhost:4200), inicia sesión con una cuenta del tenant configurado y entra al workspace.

## Mensajería (RabbitMQ)

Al crear un pedido (`POST /api/pedidos`), `servicio-pedido` publica un evento en el exchange fanout `pedidos.events.exchange`, que se reparte a dos colas consumidas por ambos microservicios:

- `ticket-generation.queue` — consumida por `servicio-pedido` (`TicketGenerationListener`), simula la generación de un comprobante.
- `notifications.queue` — consumida por `servicio-producto` (`NotificationListener`), simula el envío de una notificación.

Cada consumidor usa **ACK/NACK manual**: si el procesamiento falla, el mensaje se rechaza sin reencolar (`nack(requeue=false)`) y, gracias al argumento `x-dead-letter-exchange` de cada cola, termina en el exchange y cola compartidos `pedidos360.dlx.exchange` / `pedidos360.dlq`. Un `DeadLetterListener` en `servicio-pedido` registra en el log todo lo que llega a la DLQ.

Los nombres de exchanges, colas y bindings están centralizados en `application.properties` de cada servicio (propiedades `rabbitmq.*`) y declarados como beans en clases `RabbitMQConfig` dedicadas, separadas de la lógica de negocio.

Puedes inspeccionar colas, mensajes y la DLQ desde la Management UI: [http://localhost:15672](http://localhost:15672).

## Administración de RabbitMQ (API REST)

`servicio-rabbitmq-admin` (puerto 8083) expone endpoints para gestionar colas, exchanges y bindings en tiempo de ejecución, con validación de entrada:

```text
POST   /api/admin/queues          { "name": "...", "durable": true }
DELETE /api/admin/queues/{nombre}
GET    /api/admin/queues/{nombre}

POST   /api/admin/exchanges       { "name": "...", "type": "direct|fanout|topic|headers", "durable": true }
DELETE /api/admin/exchanges/{nombre}

POST   /api/admin/bindings        { "queue": "...", "exchange": "...", "routingKey": "" }
DELETE /api/admin/bindings        (mismo body que el POST)
```

Nombres vacíos o con caracteres inválidos, y tipos de exchange no soportados, responden `400 Bad Request` con el detalle del error. Este servicio no exige JWT: está pensado como herramienta de administración interna, pensada para que el frontend la consuma directamente.

## Configuración opcional de APIs

Los backends funcionan con valores locales por defecto. Se pueden sobrescribir mediante variables de entorno:

```text
MS_ENTRA_ISSUER_URI
MS_ENTRA_AUDIENCE (por defecto, el client ID GUID de la API)
MS_ENTRA_REQUIRED_SCOPE
APP_CORS_ALLOWED_ORIGIN
PRODUCT_DB_URL / ORDER_DB_URL
PRODUCT_DB_USERNAME / ORDER_DB_USERNAME
PRODUCT_DB_PASSWORD / ORDER_DB_PASSWORD
RABBITMQ_HOST / RABBITMQ_PORT
RABBITMQ_USERNAME / RABBITMQ_PASSWORD
```

Por defecto, CORS permite `http://localhost:4200` y las APIs de productos y pedidos requieren un Bearer JWT válido.

## Swagger / OpenAPI

Los microservicios de productos y pedidos incluyen Swagger UI y documentan los endpoints REST existentes:

- Productos: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- Pedidos: [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)

También puedes consultar el documento OpenAPI directamente:

- `http://localhost:8081/v3/api-docs`
- `http://localhost:8082/v3/api-docs`

La documentación es pública para poder abrir la interfaz, pero las operaciones `/api/**` continúan protegidas. En Swagger pulsa **Authorize**, selecciona `bearerAuth` y pega únicamente el access token JWT, sin escribir manualmente `Bearer`. Swagger agregará el encabezado automáticamente.

La sesión iniciada en Angular no se comparte automáticamente con Swagger porque utilizan puertos distintos. El login de Microsoft Entra ID del frontend no se modifica.

## Validación

Frontend:

```powershell
cd pedidos360-frontend
npm run build
npm test -- --watch=false
```

Backends (requieren la infraestructura Docker arriba para los tests que levantan el contexto completo):

```powershell
cd servicio-producto
.\mvnw.cmd test

cd ..\servicio-pedido
.\mvnw.cmd test

cd ..\servicio-rabbitmq-admin
.\mvnw.cmd test
```

Sin token, los endpoints protegidos deben responder `401 Unauthorized`:

```text
GET http://localhost:8081/api/productos
GET http://localhost:8082/api/pedidos
```

> PostgreSQL y RabbitMQ corren localmente vía Docker Compose para esta etapa del curso. El despliegue en AWS (RDS, API Gateway, HTTPS) corresponde a una etapa anterior/posterior y no es requisito de esta evaluación.
