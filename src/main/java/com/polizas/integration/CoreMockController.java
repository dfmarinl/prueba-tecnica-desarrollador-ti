package com.polizas.integration;

import com.polizas.dto.CoreEventoRequest;
import com.polizas.dto.CoreEventoResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint obligatorio del mock del CORE. Su unico proposito es registrar en
 * logs que el evento fue recibido. No se conecta a un CORE real.
 */
@RestController
@RequestMapping("/core-mock")
public class CoreMockController {

    private final CoreClient coreClient;

    public CoreMockController(CoreClient coreClient) {
        this.coreClient = coreClient;
    }

    @PostMapping("/evento")
    public CoreEventoResponse recibirEvento(@Valid @RequestBody CoreEventoRequest request) {
        coreClient.registrarEvento(request);
        return new CoreEventoResponse("Evento recibido por el mock del CORE",
                request.evento(), request.polizaId());
    }
}
