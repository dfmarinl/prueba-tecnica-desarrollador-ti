# API de Gestión de Pólizas de Arrendamiento

API REST para administrar pólizas de arrendamiento y sus riesgos asociados.
Proyecto de prueba técnica (Módulo 2).

## Tecnologías

- Java 21
- Spring Boot 3.5.5
- Maven
- Spring Web, Spring Data JPA, Bean Validation
- H2 en memoria
- JUnit 5 + Spring Boot Test + MockMvc

## Requisitos previos

- JDK 21
- Maven 3.8+

## Ejecución

```bash
mvn spring-boot:run
```

La aplicación queda disponible en `http://localhost:8080`.
Al iniciar se crea una base H2 en memoria con datos de demostración.

## Pruebas

```bash
mvn test
```

## Configuración

Propiedades en `src/main/resources/application.properties`:

| Propiedad | Valor por defecto | Descripción |
|---|---|---|
| `app.api-key` | `123456` | API key obligatoria en el header `x-api-key`. Valor de prueba técnica, no apto para producción. Sobreescribir con `APP_API_KEY`. |
| `app.ipc.porcentaje` | `1.5` | Porcentaje IPC aplicado en la renovación. Valor demostrativo, no representa el IPC real. Sobreescribir con `APP_IPC_PORCENTAJE`. |

## Endpoints

Todas las rutas exigen el header `x-api-key` (401 si falta o es incorrecto).

| Método | Ruta | Propósito |
|---|---|---|
| GET | `/polizas` | Listar pólizas con filtros opcionales `tipo` y `estado`. |
| GET | `/polizas/{id}/riesgos` | Consultar los riesgos de una póliza. |
| POST | `/polizas/{id}/renovar` | Renovar la póliza aplicando el IPC. |
| POST | `/polizas/{id}/cancelar` | Cancelar la póliza y sus riesgos. |
| POST | `/polizas/{id}/riesgos` | Agregar un riesgo (solo pólizas COLECTIVA). |
| POST | `/riesgos/{id}/cancelar` | Cancelar un riesgo. |
| POST | `/core-mock/evento` | Mock del CORE: registra el evento en logs. |

## Ejemplos de uso

```bash
# Listar pólizas (requiere API key)
curl -H "x-api-key: 123456" http://localhost:8080/polizas

# Filtrar por tipo y estado
curl -H "x-api-key: 123456" "http://localhost:8080/polizas?tipo=COLECTIVA&estado=ACTIVA"

# Renovar una póliza (aplica el IPC del 1,5 %)
curl -X POST -H "x-api-key: 123456" http://localhost:8080/polizas/1/renovar
# => {"id":1,...,"estado":"RENOVADA","canon":507500.00,"meses":12,"prima":6090000.00,...}

# Agregar un riesgo a una póliza COLECTIVA
curl -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" \
  -d '{"descripcion":"Local 202"}' http://localhost:8080/polizas/2/riesgos
# => 201 Created  {"id":7,"polizaId":2,"estado":"ACTIVO","descripcion":"Local 202"}

# Mock del CORE
curl -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" \
  -d '{"evento":"ACTUALIZACION","polizaId":555}' http://localhost:8080/core-mock/evento
# => {"mensaje":"Evento recibido por el mock del CORE","evento":"ACTUALIZACION","polizaId":555}
```

## Reglas de negocio

1. Una póliza INDIVIDUAL solo puede tener 1 riesgo.
2. Los riesgos solo se pueden agregar a pólizas COLECTIVA no canceladas.
3. No se puede renovar una póliza CANCELADA.
4. Cancelar una póliza cancela todos sus riesgos en una única transacción.
5. La renovación aplica el IPC al canon y recalcula la prima como nuevo canon × meses.
6. Cancelar una póliza o un riesgo ya cancelado es idempotente (responde 200). |
