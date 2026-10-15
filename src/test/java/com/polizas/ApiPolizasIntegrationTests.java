package com.polizas;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"app.api-key=123456", "app.ipc.porcentaje=1.5"})
@Sql(scripts = {"/test-schema.sql", "/test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ApiPolizasIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    private static final String API_KEY = "123456";

    @FunctionalInterface
    private interface AccionHttp {
        void ejecutar() throws Exception;
    }

    private List<ILoggingEvent> capturarLogsCore(AccionHttp accion) throws Exception {
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        Logger logger = (Logger) LoggerFactory.getLogger(com.polizas.integration.CoreClient.class);
        logger.addAppender(appender);
        try {
            accion.ejecutar();
        } finally {
            logger.detachAppender(appender);
        }
        return appender.list;
    }

    private void verificaRegistroCore(List<ILoggingEvent> logs, long polizaId) {
        assertThat(logs)
                .extracting(ILoggingEvent::getFormattedMessage)
                .anyMatch(mensaje -> mensaje.contains("ACTUALIZACION")
                        && mensaje.contains("polizaId=" + polizaId));
    }

    // --- Seguridad ---

    @Test
    void rechazaSolicitudSinApiKey() throws Exception {
        mockMvc.perform(get("/polizas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rechazaSolicitudConApiKeyIncorrecta() throws Exception {
        mockMvc.perform(get("/polizas").header("x-api-key", "incorrecta"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rechazaCoreMockSinApiKey() throws Exception {
        mockMvc.perform(post("/core-mock/evento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"evento\":\"ACTUALIZACION\",\"polizaId\":555}"))
                .andExpect(status().isUnauthorized());
    }

    // --- Consulta de polizas ---

    @Test
    void consultaPolizasSinFiltros() throws Exception {
        mockMvc.perform(get("/polizas").header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void filtraPolizasPorTipoYEstado() throws Exception {
        mockMvc.perform(get("/polizas").param("tipo", "COLECTIVA").param("estado", "ACTIVA")
                        .header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].tipo").value("COLECTIVA"))
                .andExpect(jsonPath("$[0].estado").value("ACTIVA"));
    }

    @Test
    void filtroInvalidoDevuelve400() throws Exception {
        mockMvc.perform(get("/polizas").param("tipo", "INVALIDO")
                        .header("x-api-key", API_KEY))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filtraPolizasSinCoincidenciasDevuelveListaVacia() throws Exception {
        mockMvc.perform(get("/polizas").param("tipo", "INDIVIDUAL").param("estado", "CANCELADA")
                        .header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // --- Consulta de riesgos ---

    @Test
    void consultaRiesgosDePolizaExistente() throws Exception {
        mockMvc.perform(get("/polizas/2/riesgos").header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].estado").value("ACTIVO"));
    }

    @Test
    void consultaRiesgosDePolizaInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/polizas/999/riesgos").header("x-api-key", API_KEY))
                .andExpect(status().isNotFound());
    }

    // --- Renovacion ---

    @Test
    void renuevaPolizaActivaYCalculaCanonYPrima() throws Exception {
        mockMvc.perform(post("/polizas/1/renovar").header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RENOVADA"))
                .andExpect(jsonPath("$.canon").value(507500.00))
                .andExpect(jsonPath("$.prima").value(6090000.00))
                .andExpect(jsonPath("$.meses").value(12));
    }

    @Test
    void rechazaRenovacionDePolizaCancelada() throws Exception {
        mockMvc.perform(post("/polizas/3/renovar").header("x-api-key", API_KEY))
                .andExpect(status().isConflict());
    }

    @Test
    void renuevaPolizaInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/polizas/999/renovar").header("x-api-key", API_KEY))
                .andExpect(status().isNotFound());
    }

    // --- Cancelacion de poliza ---

    @Test
    void cancelarPolizaCancelaTodosSusRiesgos() throws Exception {
        mockMvc.perform(post("/polizas/2/cancelar").header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));

        mockMvc.perform(get("/polizas/2/riesgos").header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].estado").value("CANCELADO"))
                .andExpect(jsonPath("$[1].estado").value("CANCELADO"))
                .andExpect(jsonPath("$[2].estado").value("CANCELADO"));
    }

    @Test
    void cancelarPolizaInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/polizas/999/cancelar").header("x-api-key", API_KEY))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelarPolizaYaCanceladaEsIdempotente() throws Exception {
        mockMvc.perform(post("/polizas/3/cancelar").header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADA"));
    }

    // --- Agregar riesgo ---

    @Test
    void agregaRiesgoAPolizaColectiva() throws Exception {
        mockMvc.perform(post("/polizas/2/riesgos").header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descripcion\":\"Local 202\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.polizaId").value(2))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.descripcion").value("Local 202"));
    }

    @Test
    void rechazaRiesgoEnPolizaIndividual() throws Exception {
        mockMvc.perform(post("/polizas/1/riesgos").header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descripcion\":\"Segundo riesgo\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void rechazaRiesgoEnPolizaCancelada() throws Exception {
        mockMvc.perform(post("/polizas/3/riesgos").header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"descripcion\":\"Nuevo riesgo\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void rechazaRiesgoSinDescripcion() throws Exception {
        mockMvc.perform(post("/polizas/2/riesgos").header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // --- Cancelar riesgo ---

    @Test
    void cancelaRiesgoExistente() throws Exception {
        mockMvc.perform(post("/riesgos/2/cancelar").header("x-api-key", API_KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }

    @Test
    void cancelarRiesgoInexistenteDevuelve404() throws Exception {
        mockMvc.perform(post("/riesgos/999/cancelar").header("x-api-key", API_KEY))
                .andExpect(status().isNotFound());
    }

    // --- Mock del CORE ---

    @Test
    void coreMockRecibeEvento() throws Exception {
        mockMvc.perform(post("/core-mock/evento").header("x-api-key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"evento\":\"ACTUALIZACION\",\"polizaId\":555}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje").value("Evento recibido por el mock del CORE"))
                .andExpect(jsonPath("$.evento").value("ACTUALIZACION"))
                .andExpect(jsonPath("$.polizaId").value(555));
    }

    @Test
    void operacionDeModificacionRegistraIntentoEnElCore() throws Exception {
        List<ILoggingEvent> logs = capturarLogsCore(() ->
                mockMvc.perform(post("/polizas/1/renovar").header("x-api-key", API_KEY))
                        .andExpect(status().isOk()));
        verificaRegistroCore(logs, 1);
    }

    @Test
    void cancelarPolizaRegistraIntentoEnElCore() throws Exception {
        List<ILoggingEvent> logs = capturarLogsCore(() ->
                mockMvc.perform(post("/polizas/2/cancelar").header("x-api-key", API_KEY))
                        .andExpect(status().isOk()));
        verificaRegistroCore(logs, 2);
    }

    @Test
    void agregarRiesgoRegistraIntentoEnElCore() throws Exception {
        List<ILoggingEvent> logs = capturarLogsCore(() ->
                mockMvc.perform(post("/polizas/2/riesgos").header("x-api-key", API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"descripcion\":\"Local 202\"}"))
                        .andExpect(status().isCreated()));
        verificaRegistroCore(logs, 2);
    }

    @Test
    void cancelarRiesgoRegistraIntentoEnElCore() throws Exception {
        List<ILoggingEvent> logs = capturarLogsCore(() ->
                mockMvc.perform(post("/riesgos/2/cancelar").header("x-api-key", API_KEY))
                        .andExpect(status().isOk()));
        verificaRegistroCore(logs, 2);
    }
}
