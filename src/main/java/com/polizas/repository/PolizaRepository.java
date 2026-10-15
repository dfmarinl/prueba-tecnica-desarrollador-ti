package com.polizas.repository;

import com.polizas.entity.Poliza;
import com.polizas.entity.EstadoPoliza;
import com.polizas.entity.TipoPoliza;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PolizaRepository extends JpaRepository<Poliza, Long> {

    List<Poliza> findByTipo(TipoPoliza tipo);

    List<Poliza> findByEstado(EstadoPoliza estado);

    List<Poliza> findByTipoAndEstado(TipoPoliza tipo, EstadoPoliza estado);
}
