# API de Gestión de Pólizas de Arrendamiento

API REST para administrar pólizas de arrendamiento y sus riesgos asociados.
Proyecto desarrollado como Prueba Técnica (Módulo 2 – Hands-on Code Challenge).

## 1. Descripción y alcance

Permite consultar, renovar y cancelar pólizas, gestionar riesgos asociados y
recibir eventos de un CORE simulado.

**Alcance:** únicamente lo solicitado en el enunciado. No hay frontend, ni
integración con un CORE real, ni funcionalidades adicionales.

## 2. Tecnologías

- Java 21
- Spring Boot 3.5.5
- Maven
- Spring Web, Spring Data JPA, Bean Validation
- H2 en memoria (base de datos de ejecución local y pruebas)
- JUnit 5 + Spring Boot Test + MockMvc

## 3. Requisitos previos

- JDK 21
- Maven 3.8+

## 4. Ejecución

```bash
mvn spring-boot:run
```

La aplicación queda disponible en `http://localhost:8080`.

Al iniciar, se crea una base de datos H2 en memoria con datos de demostración
(ver sección 9).

## 5. Configuración

Propiedades en `src/main/resources/application.properties`:

| Propiedad | Valor por defecto | Descripción |
|---|---|---|
| `app.api-key` | `123456` | API key obligatoria en el header `x-api-key`. **Valor de prueba técnica, no apto para producción.** Sobreescribir con variable de entorno `APP_API_KEY`. |
| `app.ipc.porcentaje` | `1.5` | Porcentaje IPC aplicado en la renovación. **Valor demostrativo (1,5 %) para la prueba; no representa el IPC real vigente.** Sobreescribir con variable de entorno `APP_IPC_PORCENTAJE`. |

Base de datos: H2 en memoria (`jdbc:h2:mem:polizas`), creada y poblada al
arrancar mediante `schema.sql` y `data.sql`. No requiere configuración externa.

### Regla de redondeo monetario

Todos los cálculos usan `BigDecimal` con escala 2 y redondeo `HALF_UP`:

- Nuevo canon = canon anterior × (1 + IPC / 100)
- Nueva prima = nuevo canon × número de meses de vigencia

Ejemplo con IPC 1,5 %: canon 500 000,00 → 507 500,00; prima = 507 500,00 × 12 = 6 090 000,00.

## 6. Endpoints

Todas las rutas exigen el header `x-api-key: 123456` (401 si falta o es incorrecto).

| Método | Ruta | Propósito | Parámetros |
|---|---|---|---|
| GET | `/polizas` | Listar pólizas | Query opcionales: `tipo` (INDIVIDUAL\|COLECTIVA), `estado` (ACTIVA\|RENOVADA\|CANCELADA) |
| GET | `/polizas/{id}/riesgos` | Riesgos de una póliza | Path: `id` |
| POST | `/polizas/{id}/renovar` | Renovar póliza (+IPC) | Path: `id` |
| POST | `/polizas/{id}/cancelar` | Cancelar póliza y sus riesgos | Path: `id` |
| POST | `/polizas/{id}/riesgos` | Agregar riesgo (solo COLECTIVA) | Path: `id`, body JSON |
| POST | `/riesgos/{id}/cancelar` | Cancelar un riesgo | Path: `id` |
| POST | `/core-mock/evento` | Mock del CORE: registra el evento en logs | Body JSON |

Códigos HTTP: `200` OK, `201` creación de riesgo, `400` solicitud inválida,
`401` API key ausente/incorrecta, `404` recurso o ruta inexistente,
`405` método HTTP no permitido, `409` operación incompatible con el estado/tipo
del recurso, `500` error interno.

## 7. Ejemplos de solicitudes y respuestas

```bash
# Listar todas
curl -H "x-api-key: 123456" http://localhost:8080/polizas

# Filtrar
curl -H "x-api-key: 123456" "http://localhost:8080/polizas?tipo=COLECTIVA&estado=ACTIVA"

# Riesgos de una póliza
curl -H "x-api-key: 123456" http://localhost:8080/polizas/2/riesgos

# Renovar (IPC 1,5 %)
curl -X POST -H "x-api-key: 123456" http://localhost:8080/polizas/1/renovar
# => {"id":1,...,"estado":"RENOVADA","canon":507500.00,"meses":12,"prima":6090000.00,...}

# Cancelar póliza (cancela también todos sus riesgos)
curl -X POST -H "x-api-key: 123456" http://localhost:8080/polizas/2/cancelar

# Agregar riesgo (solo pólizas COLECTIVA no canceladas)
curl -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" \
  -d '{"descripcion":"Local 202"}' http://localhost:8080/polizas/2/riesgos
# => 201 Created

# Cancelar riesgo
curl -X POST -H "x-api-key: 123456" http://localhost:8080/riesgos/2/cancelar

# Mock del CORE
curl -X POST -H "x-api-key: 123456" -H "Content-Type: application/json" \
  -d '{"evento":"ACTUALIZACION","polizaId":555}' http://localhost:8080/core-mock/evento
# => {"mensaje":"Evento recibido por el mock del CORE","evento":"ACTUALIZACION","polizaId":555}

# Sin API key => 401
curl http://localhost:8080/polizas
# => {"status":401,"mensaje":"API key ausente o invalida"}
```

## 8. Reglas de negocio implementadas

1. Una póliza INDIVIDUAL solo puede tener 1 riesgo (se rechaza cualquier adición).
2. No se puede renovar una póliza CANCELADA (409 Conflict).
3. Cancelar una póliza cancela todos sus riesgos en una única transacción.
4. Agregar riesgos solo está permitido en pólizas COLECTIVA no canceladas (409 en caso contrario).
5. La renovación incrementa canon e IPC con redondeo HALF_UP a 2 decimales y recalcula la prima como nuevo canon × meses; la duración se conserva.
6. Cancelar una póliza o riesgo ya cancelado es **idempotente**: responde 200 sin cambios adicionales.
7. Las operaciones que modifican estados registran el intento de notificación al CORE simulado (solo log; no hay conexión real).

## 9. Datos de demostración

Poblados por `data.sql` al iniciar (datos de prueba, no reales):

| Póliza | Tipo | Estado | Canon | Meses | Prima | Riesgos |
|---|---|---|---|---|---|---|
| 1 | INDIVIDUAL | ACTIVA | 500 000,00 | 12 | 6 000 000,00 | 1 activo |
| 2 | COLECTIVA | ACTIVA | 300 000,00 | 24 | 7 200 000,00 | 3 activos |
| 3 | COLECTIVA | CANCELADA | 200 000,00 | 12 | 2 400 000,00 | 2 cancelados |

## 10. Pruebas

```bash
mvn test
```

26 pruebas de integración (MockMvc) cubren: consulta con y sin filtros,
consulta con filtros válidos sin coincidencias (lista vacía), filtros
inválidos, riesgos por póliza existente/inexistente, renovación con
verificación de cálculo (IPC 1,5 %), rechazo de renovación de póliza cancelada,
cancelación de póliza con cancelación en cascada de riesgos, idempotencia,
alta de riesgo en COLECTIVA, rechazo en INDIVIDUAL y en póliza cancelada,
cancelación de riesgo, seguridad sin/con API key incorrecta, endpoint del mock
y registro del intento de integración en el CORE simulado para las cuatro
operaciones de modificación (renovar, cancelar póliza, agregar riesgo y
cancelar riesgo).

## 11. Limitaciones conocidas

- La integración con el CORE está **simulada**: el componente `CoreClient` y el
  endpoint `/core-mock/evento` únicamente registran el evento en logs. No hay
  conexión con ningún sistema externo.
- H2 en memoria: los datos se pierden al reiniciar la aplicación.
- El IPC (1,5 %) es un valor demostrativo configurable, no el IPC real.
- La API key `123456` es exclusiva de la prueba técnica.
