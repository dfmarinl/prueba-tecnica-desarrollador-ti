package com.polizas.service;

import com.polizas.config.AppConfig;
import com.polizas.dto.PolizaResponse;
import com.polizas.dto.RiesgoRequest;
import com.polizas.dto.RiesgoResponse;
import com.polizas.entity.EstadoPoliza;
import com.polizas.entity.EstadoRiesgo;
import com.polizas.entity.Poliza;
import com.polizas.entity.Riesgo;
import com.polizas.entity.TipoPoliza;
import com.polizas.exception.RecursoNoEncontradoException;
import com.polizas.exception.ReglaNegocioException;
import com.polizas.integration.CoreClient;
import com.polizas.repository.PolizaRepository;
import com.polizas.repository.RiesgoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PolizaService {

    private static final int ESCALA_MONETARIA = 2;

    private final PolizaRepository polizaRepository;
    private final RiesgoRepository riesgoRepository;
    private final AppConfig appConfig;
    private final CoreClient coreClient;

    public PolizaService(PolizaRepository polizaRepository, RiesgoRepository riesgoRepository,
                         AppConfig appConfig, CoreClient coreClient) {
        this.polizaRepository = polizaRepository;
        this.riesgoRepository = riesgoRepository;
        this.appConfig = appConfig;
        this.coreClient = coreClient;
    }

    @Transactional(readOnly = true)
    public List<PolizaResponse> listar(TipoPoliza tipo, EstadoPoliza estado) {
        List<Poliza> polizas;
        if (tipo != null && estado != null) {
            polizas = polizaRepository.findByTipoAndEstado(tipo, estado);
        } else if (tipo != null) {
            polizas = polizaRepository.findByTipo(tipo);
        } else if (estado != null) {
            polizas = polizaRepository.findByEstado(estado);
        } else {
            polizas = polizaRepository.findAll();
        }
        return polizas.stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RiesgoResponse> listarRiesgos(Long polizaId) {
        Poliza poliza = buscarPoliza(polizaId);
        return riesgoRepository.findByPoliza(poliza).stream().map(this::aResponse).toList();
    }

    @Transactional
    public PolizaResponse renovar(Long polizaId) {
        Poliza poliza = buscarPoliza(polizaId);
        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("No se puede renovar una poliza cancelada");
        }

        BigDecimal ipc = BigDecimal.valueOf(appConfig.ipc().porcentaje());
        BigDecimal factor = BigDecimal.ONE.add(ipc.divide(BigDecimal.valueOf(100)));
        BigDecimal nuevoCanon = poliza.getCanon()
                .multiply(factor)
                .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);
        BigDecimal nuevaPrima = nuevoCanon
                .multiply(BigDecimal.valueOf(poliza.getMeses()))
                .setScale(ESCALA_MONETARIA, RoundingMode.HALF_UP);

        poliza.setCanon(nuevoCanon);
        poliza.setPrima(nuevaPrima);
        poliza.setEstado(EstadoPoliza.RENOVADA);
        Poliza renovada = polizaRepository.save(poliza);

        coreClient.registrarActualizacion(renovada.getId());
        return aResponse(renovada);
    }

    @Transactional
    public PolizaResponse cancelar(Long polizaId) {
        Poliza poliza = buscarPoliza(polizaId);
        if (poliza.getEstado() != EstadoPoliza.CANCELADA) {
            poliza.setEstado(EstadoPoliza.CANCELADA);
            polizaRepository.save(poliza);
            riesgoRepository.findByPoliza(poliza).forEach(riesgo -> {
                if (riesgo.getEstado() != EstadoRiesgo.CANCELADO) {
                    riesgo.setEstado(EstadoRiesgo.CANCELADO);
                    riesgoRepository.save(riesgo);
                }
            });
            coreClient.registrarActualizacion(poliza.getId());
        }
        return aResponse(poliza);
    }

    @Transactional
    public RiesgoResponse agregarRiesgo(Long polizaId, RiesgoRequest request) {
        Poliza poliza = buscarPoliza(polizaId);
        if (poliza.getTipo() != TipoPoliza.COLECTIVA) {
            throw new ReglaNegocioException(
                    "Solo se pueden agregar riesgos a polizas de tipo COLECTIVA");
        }
        if (poliza.getEstado() == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("No se pueden agregar riesgos a una poliza cancelada");
        }

        Riesgo riesgo = riesgoRepository.save(new Riesgo(poliza, EstadoRiesgo.ACTIVO, request.descripcion()));
        coreClient.registrarActualizacion(polizaId);
        return aResponse(riesgo);
    }

    private Poliza buscarPoliza(Long id) {
        return polizaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Poliza no encontrada con id " + id));
    }

    private PolizaResponse aResponse(Poliza poliza) {
        return new PolizaResponse(poliza.getId(), poliza.getTipo(), poliza.getEstado(),
                poliza.getCanon(), poliza.getMeses(), poliza.getPrima(), poliza.getFechaInicio());
    }

    private RiesgoResponse aResponse(Riesgo riesgo) {
        return new RiesgoResponse(riesgo.getId(), riesgo.getPoliza().getId(),
                riesgo.getEstado(), riesgo.getDescripcion());
    }
}
