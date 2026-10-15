-- Reconstruye el esquema antes de cada test para garantizar aislamiento.
DROP TABLE IF EXISTS riesgos;
DROP TABLE IF EXISTS polizas;

CREATE TABLE polizas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    canon DECIMAL(14,2) NOT NULL,
    meses INT NOT NULL,
    prima DECIMAL(14,2) NOT NULL,
    fecha_inicio DATE NOT NULL
);

CREATE TABLE riesgos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    poliza_id BIGINT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    descripcion VARCHAR(200) NOT NULL,
    CONSTRAINT fk_riesgo_poliza FOREIGN KEY (poliza_id) REFERENCES polizas(id)
);
