# Pedidos360

Sistema local compuesto por un frontend Angular y dos microservicios Spring Boot:

- `pedidos360-frontend` — Angular, `http://localhost:4200`
- `servicio-producto` — API de productos, `http://localhost:8081`
- `servicio-pedido` — API de pedidos, `http://localhost:8082`

La autenticación se realiza con Microsoft Entra ID mediante MSAL. Las bases locales usan H2 en memoria.

## Requisitos

- Node.js y npm
- Java 21 o superior
- Acceso a una App Registration de Microsoft Entra ID

## Configuración de Entra ID

En `pedidos360-frontend/src/environments/environment.development.ts` verifica:

```ts
clientId: 'TU_CLIENT_ID'
tenantId: 'TU_TENANT_ID'
redirectUri: 'http://localhost:4200'
postLogoutRedirectUri: 'http://localhost:4200'
apiScopes: ['api://TU_API_ID/.default']
```

En Microsoft Entra ID registra `http://localhost:4200` como URI de redirección de tipo **Single-page application**. El valor de `apiScopes` debe coincidir con el App ID URI y los permisos expuestos por tu API.

## Levantar el proyecto

Abre tres terminales desde la carpeta raíz `Pedidos360`.

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

### 3. Frontend

```powershell
cd pedidos360-frontend
npm install
npm start
```

Abre [http://localhost:4200](http://localhost:4200), inicia sesión con una cuenta del tenant configurado y entra al workspace.

## Configuración opcional de APIs

Los backends funcionan con valores locales por defecto. Se pueden sobrescribir mediante variables de entorno:

```text
MS_ENTRA_ISSUER_URI
MS_ENTRA_AUDIENCE
MS_ENTRA_REQUIRED_SCOPE
APP_CORS_ALLOWED_ORIGIN
PRODUCT_DB_URL / ORDER_DB_URL
PRODUCT_DB_USERNAME / ORDER_DB_USERNAME
PRODUCT_DB_PASSWORD / ORDER_DB_PASSWORD
```

Por defecto, CORS permite `http://localhost:4200` y las APIs requieren un Bearer JWT válido.

## Validación

Frontend:

```powershell
cd pedidos360-frontend
npm run build
npm test -- --watch=false
```

Backends:

```powershell
cd servicio-producto
.\mvnw.cmd test

cd ..\servicio-pedido
.\mvnw.cmd test
```

Sin token, los endpoints protegidos deben responder `401 Unauthorized`:

```text
GET http://localhost:8081/api/productos
GET http://localhost:8082/api/pedidos
```

> H2 es temporal y en memoria: los datos se reinician al detener los microservicios. El despliegue en AWS, RDS, API Gateway y HTTPS corresponde a la siguiente etapa.
