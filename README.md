# Banco XYZ — Semana 8

## Objetivo
Continuar el proyecto de la semana 7 agregando seguridad con OAuth 2.0 y ejecutando todo con Docker.
Se mantienen PostgreSQL local, la mensajería JMS con ActiveMQ y la tolerancia a fallos con Resilience4j.

## Estructura

| Componente | Función | Puerto |
|---|---|---|
| auth-server | Entrega los tokens OAuth 2.0 | 9000 |
| config-server | Guarda la configuración de los servicios | 8888 |
| discovery-server | Registro de servicios (Eureka) | 8761 |
| banco-servicios | API bancaria y solicitudes de retiro | 8080 (HTTPS) |
| ms-cuentas | Saldos, débitos y reintegros | 8091 |
| ms-notificaciones | Notificaciones y auditoría | 8093 |
| ActiveMQ | Broker de mensajes JMS | 8161 |
| PostgreSQL (local, fuera de Docker) | Base de datos `banco_xyz` | 5432 |

Docker Compose levanta las seis aplicaciones y ActiveMQ. PostgreSQL sigue instalado en el computador, con los datos de las semanas anteriores.

## Propuesta técnica

- **Seguridad:** `auth-server` entrega tokens con el flujo `client_credentials`, pensado para aplicaciones y no para personas. Los servicios validan el token y revisan el permiso: `banco.read` para consultar y `banco.write` para pedir retiros.
- **Docker:** cada aplicación tiene su `Dockerfile` (se construye con Java 21 y Maven Wrapper). El `docker-compose.yaml` define el orden de arranque, de modo que cada servicio espera a los que necesita. PostgreSQL queda fuera de Docker para conservar los datos existentes.
- **Mensajería:** se usa JMS con ActiveMQ, como en la semana 7. Un retiro pasa por `banco-servicios`, `ms-cuentas` y `ms-notificaciones`. Si el cajero falla, el dinero se devuelve a la cuenta. El encabezado `Idempotency-Key` evita descontar dos veces el mismo retiro.
- **Resiliencia:** Resilience4j usa Circuit Breaker en la base de datos y, en el broker, Retry más Circuit Breaker. Si ActiveMQ se cae, el retiro queda guardado como `REGISTRADA` y se envía solo cuando el broker vuelve.
- **Límites:** son credenciales y certificado de prueba, solo para la actividad. Al reiniciar `auth-server` hay que pedir tokens nuevos. Cada servicio corre en una sola instancia.

## Requisitos
- Docker Desktop abierto, con contenedores Linux.
- PostgreSQL local funcionando en el puerto 5432, con la base `banco_xyz` de las semanas anteriores.
- Puertos 8080, 8091, 8093, 8161, 8761, 8888 y 9000 libres.
- Internet la primera vez, para descargar las dependencias.


## Cómo ejecutar

```powershell
Copy-Item .env.example .env
notepad .env
```

Escribe en `.env` los datos de tu PostgreSQL (`DB_NAME`, `DB_USER` y `DB_PASSWORD`). No subas este archivo a GitHub.

Luego inicia todo:

```powershell
docker compose up -d --build --wait --wait-timeout 300
```

 Para ver el estado:

```powershell
docker compose ps
docker compose images
```

## Obtener un token

```powershell
$respuesta = curl.exe -s -u "cajero:cajero-demo-2026" -d "grant_type=client_credentials" --data-urlencode "scope=banco.read banco.write" http://localhost:9000/oauth2/token
$token = ($respuesta | ConvertFrom-Json).access_token
curl.exe -k -i -H "Authorization: Bearer $token" https://localhost:8080/api/intereses
```

| Cliente | Secreto | Permisos |
|---|---|---|
| cajero | cajero-demo-2026 | Lectura y escritura |
| consulta | consulta-demo-2026 | Solo lectura |

Sin token la API responde `401`. Con el cliente `consulta`, una escritura responde `403`.
La opción `-k` es porque el certificado es de prueba.

## Pruebas

**Retiro.**

```powershell
$datos = '{\"cuentaId\":101,\"monto\":1,\"simularFallaCajero\":false}'
curl.exe -k -i -X POST -H "Authorization: Bearer $token" -H "Idempotency-Key: retiro-101-001" -H "Content-Type: application/json" -d $datos https://localhost:8080/api/retiros
```

Responde `202` con un `retiroId`. Para ver el resultado (debería llegar a `APROBADA`):

```powershell
curl.exe -k -s -H "Authorization: Bearer $token" https://localhost:8080/api/retiros/RETIRO_ID
```

- **Idempotencia:** repetir el mismo POST con el mismo `Idempotency-Key` responde `200` y no crea otro retiro.
- **Compensación:** con `"simularFallaCajero":true` y otro `Idempotency-Key`, el retiro termina en `REVERTIDA` y el saldo se reintegra.
- **Mensajes:** `docker compose logs --since 15m ms-cuentas ms-notificaciones`.

**Caída del broker:**

```powershell
docker compose stop activemq
```

Pide un retiro con otro `Idempotency-Key`: queda en `REGISTRADA`. Luego:

```powershell
docker compose start activemq
```

En menos de un minuto el retiro pasa solo a `APROBADA`.

## Consolas
- Eureka: http://localhost:8761 (usuario `eureka`, contraseña `eureka2026`)
- ActiveMQ: http://localhost:8161/admin (usuario `admin`, contraseña `admin`)

## Detener

```powershell
docker compose down
```
