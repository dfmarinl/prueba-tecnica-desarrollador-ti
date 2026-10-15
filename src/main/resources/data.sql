-- Datos de demostracion para la prueba tecnica. NO son datos reales.
--
-- Poliza 1: INDIVIDUAL ACTIVA con 1 riesgo (limite de negocio: maximo 1).
-- Poliza 2: COLECTIVA ACTIVA con 3 riesgos activos.
-- Poliza 3: COLECTIVA CANCELADA con 2 riesgos cancelados.

INSERT INTO polizas (id, tipo, estado, canon, meses, prima, fecha_inicio) VALUES
    (1, 'INDIVIDUAL', 'ACTIVA', 500000.00, 12, 6000000.00, '2026-01-01'),
    (2, 'COLECTIVA', 'ACTIVA', 300000.00, 24, 7200000.00, '2026-03-01'),
    (3, 'COLECTIVA', 'CANCELADA', 200000.00, 12, 2400000.00, '2025-06-01');

INSERT INTO riesgos (id, poliza_id, estado, descripcion) VALUES
    (1, 1, 'ACTIVO', 'Apartamento 302'),
    (2, 2, 'ACTIVO', 'Local comercial 101'),
    (3, 2, 'ACTIVO', 'Oficina 401'),
    (4, 2, 'ACTIVO', 'Bodega 7'),
    (5, 3, 'CANCELADO', 'Casa 15 cancelada'),
    (6, 3, 'CANCELADO', 'Apartamento 8 cancelado');

ALTER TABLE polizas ALTER COLUMN id RESTART WITH 4;
ALTER TABLE riesgos ALTER COLUMN id RESTART WITH 7;
