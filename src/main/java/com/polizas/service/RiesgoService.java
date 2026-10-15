package com.polizas.service;

import com.polizas.dto.RiesgoResponse;
import com.polizas.entity.EstadoRiesgo;
import com.polizas.entity.Riesgo;
import com.polizas.exception.RecursoNoEncontradoException;
import com.polizas.integration.CoreClient;
import com.polizas.repository.RiesgoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiesgoService {

    private final RiesgoRepository riesgoRepository;
    private final CoreClient coreClient;

    public RiesgoService(RiesgoRepository riesgoRepository, CoreClient coreClient) {
        this.riesgoRepository = riesgoRepository;
        this.coreClient = coreClient;
    }

    @Transactional
    public RiesgoResponse cancelar(Long riesgoId) {
        Riesgo riesgo = riesgoRepository.findById(riesgoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Riesgo no encontrado con id " + riesgoId));
        if (riesgo.getEstado() != EstadoRiesgo.CANCELADO) {
            riesgo.setEstado(EstadoRiesgo.CANCELADO);
            riesgoRepository.save(riesgo);
            coreClient.registrarActualizacion(riesgo.getPoliza().getId());
        }
        return new RiesgoResponse(riesgo.getId(), riesgo.getPoliza().getId(),
                riesgo.getEstado(), riesgo.getDescripcion());
    }
}
