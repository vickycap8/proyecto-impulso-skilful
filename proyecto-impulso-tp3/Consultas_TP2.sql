USE proyecto_impulso;

-- INSERT de prueba. La transacción permite deshacer los datos de ejemplo.
START TRANSACTION;

INSERT INTO socio (
    id_sede_habitual,
    dni,
    nombre,
    apellido,
    fecha_nacimiento,
    telefono,
    telefono_emergencia,
    correo,
    domicilio,
    activo
) VALUES (
    1,
    '99999999',
    'Socio',
    'Prueba TP2',
    '2000-01-01',
    '3420000000',
    '3420000001',
    'prueba.tp2@example.com',
    'Domicilio de prueba',
    TRUE
);

-- SELECT para comprobar la inserción y la relación con la sede.
SELECT
    so.id_socio,
    so.dni,
    CONCAT(so.apellido, ', ', so.nombre) AS socio,
    se.nombre AS sede_habitual
FROM socio so
INNER JOIN sede se ON se.id_sede = so.id_sede_habitual
WHERE so.dni = '99999999';

-- DELETE del registro de prueba.
DELETE FROM socio
WHERE dni = '99999999';

ROLLBACK;

-- Consulta de cobros por sede y fecha.
SELECT
    p.fecha_pago,
    se.nombre AS sede,
    so.dni,
    CONCAT(so.apellido, ', ', so.nombre) AS socio,
    mp.nombre AS medio_pago,
    p.importe_final,
    p.estado
FROM pago p
INNER JOIN cuota c ON c.id_cuota = p.id_cuota
INNER JOIN membresia m ON m.id_membresia = c.id_membresia
INNER JOIN socio so ON so.id_socio = m.id_socio
INNER JOIN sede se ON se.id_sede = p.id_sede
INNER JOIN medio_pago mp ON mp.id_medio_pago = p.id_medio_pago
ORDER BY p.fecha_pago DESC;

-- Consulta de rechazos pendientes de notificación.
SELECT
    ic.id_intento,
    so.dni,
    CONCAT(so.apellido, ', ', so.nombre) AS socio,
    ic.numero_intento,
    ic.fecha_intento,
    ic.importe
FROM intento_cobro ic
INNER JOIN suscripcion su
    ON su.id_suscripcion = ic.id_suscripcion
INNER JOIN membresia m ON m.id_membresia = su.id_membresia
INNER JOIN socio so ON so.id_socio = m.id_socio
LEFT JOIN notificacion n ON n.id_intento = ic.id_intento
WHERE ic.estado = 'RECHAZADO'
  AND n.id_notificacion IS NULL
ORDER BY ic.fecha_intento;
