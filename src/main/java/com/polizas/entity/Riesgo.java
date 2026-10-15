package com.polizas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "riesgos")
public class Riesgo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poliza_id", nullable = false)
    private Poliza poliza;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRiesgo estado;

    @Column(nullable = false, length = 200)
    private String descripcion;

    protected Riesgo() {
    }

    public Riesgo(Poliza poliza, EstadoRiesgo estado, String descripcion) {
        this.poliza = poliza;
        this.estado = estado;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public Poliza getPoliza() {
        return poliza;
    }

    public EstadoRiesgo getEstado() {
        return estado;
    }

    public void setEstado(EstadoRiesgo estado) {
        this.estado = estado;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
