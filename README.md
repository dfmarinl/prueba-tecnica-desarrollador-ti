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
