package com.polizas.integration;

import com.polizas.dto.CoreEventoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Componente de integracion con el CORE. En esta prueba tecnica la integracion
 * esta simulada: unicamente registra en logs el intento de envio.
 * No se conecta a ningun sistema externo.
 */
@Component
public class CoreClient {

    private static final Logger log = LoggerFactory.getLogger(CoreClient.class);

    public void registrarActualizacion(Long polizaId) {
        registrarEvento("ACTUALIZACION", polizaId);
    }

    public void registrarEvento(String evento, Long polizaId) {
        log.info("[CORE-MOCK] Intento de envio al CORE: evento={}, polizaId={}", evento, polizaId);
    }

    public void registrarEvento(CoreEventoRequest request) {
        registrarEvento(request.evento(), request.polizaId());
    }
}
