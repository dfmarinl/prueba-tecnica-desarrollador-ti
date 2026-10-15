package com.polizas.repository;

import com.polizas.entity.Poliza;
import com.polizas.entity.Riesgo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RiesgoRepository extends JpaRepository<Riesgo, Long> {

    List<Riesgo> findByPoliza(Poliza poliza);
}
