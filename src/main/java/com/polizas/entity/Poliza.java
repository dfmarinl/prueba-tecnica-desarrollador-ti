package com.polizas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "polizas")
public class Poliza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoPoliza tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPoliza estado;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal canon;

    @Column(nullable = false)
    private Integer meses;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal prima;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    protected Poliza() {
    }

    public Poliza(TipoPoliza tipo, EstadoPoliza estado, BigDecimal canon, Integer meses,
                  BigDecimal prima, LocalDate fechaInicio) {
        this.tipo = tipo;
        this.estado = estado;
        this.canon = canon;
        this.meses = meses;
        this.prima = prima;
        this.fechaInicio = fechaInicio;
    }

    public Long getId() {
        return id;
    }

    public TipoPoliza getTipo() {
        return tipo;
    }

    public EstadoPoliza getEstado() {
        return estado;
    }

    public void setEstado(EstadoPoliza estado) {
        this.estado = estado;
    }

    public BigDecimal getCanon() {
        return canon;
    }

    public void setCanon(BigDecimal canon) {
        this.canon = canon;
    }

    public Integer getMeses() {
        return meses;
    }

    public BigDecimal getPrima() {
        return prima;
    }

    public void setPrima(BigDecimal prima) {
        this.prima = prima;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }
}
