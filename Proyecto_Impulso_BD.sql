-- =============================================================
-- PROYECTO IMPULSO: TRANSFORMACION DIGITAL EN SKILFUL
-- Autora: Victoria Capozzolo
-- =============================================================

SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS proyecto_impulso
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE proyecto_impulso;

-- =============================================================
-- 1. ACCESO, USUARIOS Y SEDES
-- =============================================================

CREATE TABLE IF NOT EXISTS rol (
    id_rol          TINYINT UNSIGNED AUTO_INCREMENT,
    nombre          VARCHAR(30) NOT NULL,
    descripcion     VARCHAR(150),
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_rol PRIMARY KEY (id_rol),
    CONSTRAINT uk_rol_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS sede (
    id_sede         SMALLINT UNSIGNED AUTO_INCREMENT,
    nombre          VARCHAR(60) NOT NULL,
    direccion       VARCHAR(120) NOT NULL,
    categoria       VARCHAR(20) NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_sede PRIMARY KEY (id_sede),
    CONSTRAINT uk_sede_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario          INT UNSIGNED AUTO_INCREMENT,
    id_rol              TINYINT UNSIGNED NOT NULL,
    nombre_completo     VARCHAR(100) NOT NULL,
    nombre_usuario      VARCHAR(50) NOT NULL,
    clave_hash          VARCHAR(100) NOT NULL,
    clave_salt          VARCHAR(50) NOT NULL,
    clave_iteraciones   INT UNSIGNED NOT NULL DEFAULT 120000,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
    CONSTRAINT uk_usuario_nombre UNIQUE (nombre_usuario),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol)
        REFERENCES rol (id_rol)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_usuario_iteraciones CHECK (clave_iteraciones >= 100000)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS usuario_sede (
    id_usuario      INT UNSIGNED NOT NULL,
    id_sede         SMALLINT UNSIGNED NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_asignacion DATE NOT NULL DEFAULT (CURRENT_DATE),
    CONSTRAINT pk_usuario_sede PRIMARY KEY (id_usuario, id_sede),
    CONSTRAINT fk_usuario_sede_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_usuario_sede_sede FOREIGN KEY (id_sede)
        REFERENCES sede (id_sede)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

-- =============================================================
-- 2. SOCIOS, PLANES, TARIFAS Y BENEFICIOS
-- =============================================================

CREATE TABLE IF NOT EXISTS socio (
    id_socio               INT UNSIGNED AUTO_INCREMENT,
    id_sede_habitual       SMALLINT UNSIGNED NOT NULL,
    dni                    VARCHAR(15) NOT NULL,
    nombre                 VARCHAR(60) NOT NULL,
    apellido               VARCHAR(60) NOT NULL,
    fecha_nacimiento       DATE NOT NULL,
    telefono               VARCHAR(30) NOT NULL,
    telefono_emergencia    VARCHAR(30) NOT NULL,
    correo                 VARCHAR(120),
    domicilio              VARCHAR(180) NOT NULL,
    ruta_fotografia        VARCHAR(255),
    activo                 BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                           ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_socio PRIMARY KEY (id_socio),
    CONSTRAINT uk_socio_dni UNIQUE (dni),
    INDEX idx_socio_apellido_nombre (apellido, nombre),
    CONSTRAINT fk_socio_sede FOREIGN KEY (id_sede_habitual)
        REFERENCES sede (id_sede)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS plan (
    id_plan            SMALLINT UNSIGNED AUTO_INCREMENT,
    nombre             VARCHAR(50) NOT NULL,
    categoria_acceso   VARCHAR(20) NOT NULL,
    descripcion        VARCHAR(180),
    activo             BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_plan PRIMARY KEY (id_plan),
    CONSTRAINT uk_plan_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS tipo_beneficio (
    id_tipo_beneficio  SMALLINT UNSIGNED AUTO_INCREMENT,
    nombre             VARCHAR(50) NOT NULL,
    vigencia_meses     SMALLINT UNSIGNED,
    requiere_empresa   BOOLEAN NOT NULL DEFAULT FALSE,
    activo             BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_tipo_beneficio PRIMARY KEY (id_tipo_beneficio),
    CONSTRAINT uk_tipo_beneficio_nombre UNIQUE (nombre),
    CONSTRAINT ck_tipo_beneficio_vigencia
        CHECK (vigencia_meses IS NULL OR vigencia_meses > 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS empresa_convenio (
    id_empresa      SMALLINT UNSIGNED AUTO_INCREMENT,
    nombre          VARCHAR(100) NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_empresa_convenio PRIMARY KEY (id_empresa),
    CONSTRAINT uk_empresa_convenio_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS tarifa_plan (
    id_tarifa              INT UNSIGNED AUTO_INCREMENT,
    id_plan                SMALLINT UNSIGNED NOT NULL,
    id_tipo_beneficio      SMALLINT UNSIGNED,
    codigo_tarifa          VARCHAR(30) NOT NULL,
    es_suscripcion         BOOLEAN NOT NULL DEFAULT FALSE,
    monto                  DECIMAL(12,2) NOT NULL,
    vigencia_desde         DATE NOT NULL,
    vigencia_hasta         DATE,
    activo                 BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_tarifa_plan PRIMARY KEY (id_tarifa),
    CONSTRAINT uk_tarifa_plan
        UNIQUE (id_plan, codigo_tarifa, vigencia_desde),
    INDEX idx_tarifa_plan_busqueda
        (id_plan, codigo_tarifa, activo, vigencia_desde, vigencia_hasta),
    CONSTRAINT fk_tarifa_plan_plan FOREIGN KEY (id_plan)
        REFERENCES plan (id_plan)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_tarifa_plan_beneficio FOREIGN KEY (id_tipo_beneficio)
        REFERENCES tipo_beneficio (id_tipo_beneficio)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_tarifa_plan_monto CHECK (monto > 0),
    CONSTRAINT ck_tarifa_plan_vigencia
        CHECK (vigencia_hasta IS NULL OR vigencia_hasta >= vigencia_desde)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS membresia (
    id_membresia    INT UNSIGNED AUTO_INCREMENT,
    id_socio        INT UNSIGNED NOT NULL,
    id_plan         SMALLINT UNSIGNED NOT NULL,
    fecha_inicio    DATE NOT NULL,
    fecha_fin       DATE,
    estado          VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    CONSTRAINT pk_membresia PRIMARY KEY (id_membresia),
    INDEX idx_membresia_socio_estado (id_socio, estado),
    CONSTRAINT fk_membresia_socio FOREIGN KEY (id_socio)
        REFERENCES socio (id_socio)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_membresia_plan FOREIGN KEY (id_plan)
        REFERENCES plan (id_plan)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_membresia_fechas
        CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_inicio),
    CONSTRAINT ck_membresia_estado
        CHECK (estado IN ('ACTIVA', 'SUSPENDIDA', 'FINALIZADA'))
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS beneficio_socio (
    id_beneficio_socio     INT UNSIGNED AUTO_INCREMENT,
    id_socio               INT UNSIGNED NOT NULL,
    id_tipo_beneficio      SMALLINT UNSIGNED NOT NULL,
    id_empresa             SMALLINT UNSIGNED,
    fecha_presentacion     DATE NOT NULL,
    fecha_vencimiento      DATE,
    estado                 VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    observacion            VARCHAR(200),
    CONSTRAINT pk_beneficio_socio PRIMARY KEY (id_beneficio_socio),
    INDEX idx_beneficio_socio_vencimiento (estado, fecha_vencimiento),
    INDEX idx_beneficio_socio_activo (id_socio, estado),
    CONSTRAINT fk_beneficio_socio_socio FOREIGN KEY (id_socio)
        REFERENCES socio (id_socio)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_beneficio_socio_tipo FOREIGN KEY (id_tipo_beneficio)
        REFERENCES tipo_beneficio (id_tipo_beneficio)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_beneficio_socio_empresa FOREIGN KEY (id_empresa)
        REFERENCES empresa_convenio (id_empresa)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_beneficio_socio_fechas
        CHECK (fecha_vencimiento IS NULL OR fecha_vencimiento >= fecha_presentacion),
    CONSTRAINT ck_beneficio_socio_estado
        CHECK (estado IN ('ACTIVO', 'VENCIDO', 'FINALIZADO'))
) ENGINE = InnoDB;

-- =============================================================
-- 3. CUOTAS, PAGOS Y SALDOS A FAVOR
-- =============================================================

CREATE TABLE IF NOT EXISTS medio_pago (
    id_medio_pago       SMALLINT UNSIGNED AUTO_INCREMENT,
    nombre              VARCHAR(50) NOT NULL,
    recargo_porcentaje  DECIMAL(5,2) NOT NULL DEFAULT 0,
    admite_beneficio    BOOLEAN NOT NULL DEFAULT FALSE,
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_medio_pago PRIMARY KEY (id_medio_pago),
    CONSTRAINT uk_medio_pago_nombre UNIQUE (nombre),
    CONSTRAINT ck_medio_pago_recargo
        CHECK (recargo_porcentaje >= 0 AND recargo_porcentaje <= 100)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS cuota (
    id_cuota            INT UNSIGNED AUTO_INCREMENT,
    id_membresia        INT UNSIGNED NOT NULL,
    id_tarifa_base      INT UNSIGNED NOT NULL,
    periodo             DATE NOT NULL,
    fecha_emision       DATE NOT NULL,
    fecha_vencimiento   DATE NOT NULL,
    importe_original    DECIMAL(12,2) NOT NULL,
    estado              VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT pk_cuota PRIMARY KEY (id_cuota),
    CONSTRAINT uk_cuota_periodo UNIQUE (id_membresia, periodo),
    INDEX idx_cuota_estado_vencimiento (estado, fecha_vencimiento),
    CONSTRAINT fk_cuota_membresia FOREIGN KEY (id_membresia)
        REFERENCES membresia (id_membresia)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_cuota_tarifa FOREIGN KEY (id_tarifa_base)
        REFERENCES tarifa_plan (id_tarifa)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_cuota_importe CHECK (importe_original > 0),
    CONSTRAINT ck_cuota_fechas
        CHECK (fecha_vencimiento >= fecha_emision),
    CONSTRAINT ck_cuota_estado
        CHECK (estado IN ('PENDIENTE', 'PAGADA', 'VENCIDA', 'ANULADA'))
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS pago (
    id_pago                 BIGINT UNSIGNED AUTO_INCREMENT,
    id_cuota                INT UNSIGNED NOT NULL,
    id_medio_pago           SMALLINT UNSIGNED NOT NULL,
    id_tarifa_aplicada      INT UNSIGNED NOT NULL,
    id_usuario              INT UNSIGNED NOT NULL,
    id_sede                 SMALLINT UNSIGNED NOT NULL,
    fecha_pago              DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    precio_original         DECIMAL(12,2) NOT NULL,
    descuento               DECIMAL(12,2) NOT NULL DEFAULT 0,
    recargo                 DECIMAL(12,2) NOT NULL DEFAULT 0,
    saldo_aplicado          DECIMAL(12,2) NOT NULL DEFAULT 0,
    importe_final           DECIMAL(12,2) NOT NULL,
    importe_abonado         DECIMAL(12,2) NOT NULL,
    saldo_generado          DECIMAL(12,2) NOT NULL DEFAULT 0,
    estado                  VARCHAR(20) NOT NULL DEFAULT 'REGISTRADO',
    id_pago_corregido       BIGINT UNSIGNED,
    motivo_anulacion        VARCHAR(250),
    CONSTRAINT pk_pago PRIMARY KEY (id_pago),
    INDEX idx_pago_fecha_sede (fecha_pago, id_sede),
    INDEX idx_pago_usuario (id_usuario, fecha_pago),
    CONSTRAINT fk_pago_cuota FOREIGN KEY (id_cuota)
        REFERENCES cuota (id_cuota)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_pago_medio FOREIGN KEY (id_medio_pago)
        REFERENCES medio_pago (id_medio_pago)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_pago_tarifa FOREIGN KEY (id_tarifa_aplicada)
        REFERENCES tarifa_plan (id_tarifa)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_pago_usuario_sede FOREIGN KEY (id_usuario, id_sede)
        REFERENCES usuario_sede (id_usuario, id_sede)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_pago_corregido FOREIGN KEY (id_pago_corregido)
        REFERENCES pago (id_pago)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_pago_importes CHECK (
        precio_original > 0
        AND descuento >= 0
        AND recargo >= 0
        AND saldo_aplicado >= 0
        AND importe_final >= 0
        AND importe_abonado >= 0
        AND saldo_generado >= 0
    ),
    CONSTRAINT ck_pago_estado
        CHECK (estado IN ('REGISTRADO', 'ANULADO'))
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS movimiento_saldo (
    id_movimiento    BIGINT UNSIGNED AUTO_INCREMENT,
    id_socio         INT UNSIGNED NOT NULL,
    id_pago          BIGINT UNSIGNED NOT NULL,
    tipo             VARCHAR(10) NOT NULL,
    monto            DECIMAL(12,2) NOT NULL,
    fecha_movimiento DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    descripcion      VARCHAR(200),
    CONSTRAINT pk_movimiento_saldo PRIMARY KEY (id_movimiento),
    INDEX idx_movimiento_saldo_socio (id_socio, fecha_movimiento),
    CONSTRAINT fk_movimiento_saldo_socio FOREIGN KEY (id_socio)
        REFERENCES socio (id_socio)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_movimiento_saldo_pago FOREIGN KEY (id_pago)
        REFERENCES pago (id_pago)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_movimiento_saldo_tipo
        CHECK (tipo IN ('CREDITO', 'DEBITO')),
    CONSTRAINT ck_movimiento_saldo_monto CHECK (monto > 0)
) ENGINE = InnoDB;

-- =============================================================
-- 4. SUSCRIPCIONES, INTENTOS Y NOTIFICACIONES
-- =============================================================

CREATE TABLE IF NOT EXISTS suscripcion (
    id_suscripcion       INT UNSIGNED AUTO_INCREMENT,
    id_membresia         INT UNSIGNED NOT NULL,
    fecha_inicio         DATE NOT NULL,
    permanencia_hasta    DATE NOT NULL,
    dia_cobro            TINYINT UNSIGNED NOT NULL,
    estado               VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    fecha_baja           DATE,
    CONSTRAINT pk_suscripcion PRIMARY KEY (id_suscripcion),
    INDEX idx_suscripcion_estado_dia (estado, dia_cobro),
    CONSTRAINT fk_suscripcion_membresia FOREIGN KEY (id_membresia)
        REFERENCES membresia (id_membresia)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_suscripcion_dia CHECK (dia_cobro BETWEEN 1 AND 31),
    CONSTRAINT ck_suscripcion_permanencia
        CHECK (permanencia_hasta >= DATE_ADD(fecha_inicio, INTERVAL 6 MONTH)),
    CONSTRAINT ck_suscripcion_baja
        CHECK (fecha_baja IS NULL OR fecha_baja >= fecha_inicio),
    CONSTRAINT ck_suscripcion_estado
        CHECK (estado IN ('ACTIVA', 'SUSPENDIDA', 'FINALIZADA'))
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS intento_cobro (
    id_intento       BIGINT UNSIGNED AUTO_INCREMENT,
    id_suscripcion   INT UNSIGNED NOT NULL,
    id_cuota         INT UNSIGNED NOT NULL,
    id_usuario       INT UNSIGNED NOT NULL,
    id_sede          SMALLINT UNSIGNED NOT NULL,
    id_pago          BIGINT UNSIGNED,
    numero_intento   TINYINT UNSIGNED NOT NULL,
    fecha_intento    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    importe          DECIMAL(12,2) NOT NULL,
    estado           VARCHAR(20) NOT NULL,
    observacion      VARCHAR(250),
    CONSTRAINT pk_intento_cobro PRIMARY KEY (id_intento),
    CONSTRAINT uk_intento_cobro
        UNIQUE (id_suscripcion, id_cuota, numero_intento),
    CONSTRAINT uk_intento_cobro_pago UNIQUE (id_pago),
    INDEX idx_intento_estado_fecha (estado, fecha_intento),
    CONSTRAINT fk_intento_suscripcion FOREIGN KEY (id_suscripcion)
        REFERENCES suscripcion (id_suscripcion)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_intento_cuota FOREIGN KEY (id_cuota)
        REFERENCES cuota (id_cuota)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_intento_usuario_sede FOREIGN KEY (id_usuario, id_sede)
        REFERENCES usuario_sede (id_usuario, id_sede)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_intento_pago FOREIGN KEY (id_pago)
        REFERENCES pago (id_pago)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_intento_numero CHECK (numero_intento BETWEEN 1 AND 5),
    CONSTRAINT ck_intento_importe CHECK (importe > 0),
    CONSTRAINT ck_intento_estado
        CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO'))
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS notificacion (
    id_notificacion     BIGINT UNSIGNED AUTO_INCREMENT,
    id_intento          BIGINT UNSIGNED NOT NULL,
    id_usuario          INT UNSIGNED NOT NULL,
    fecha_notificacion  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    canal               VARCHAR(30) NOT NULL DEFAULT 'WHATSAPP',
    estado              VARCHAR(20) NOT NULL DEFAULT 'ENVIADA',
    observacion         VARCHAR(250),
    CONSTRAINT pk_notificacion PRIMARY KEY (id_notificacion),
    CONSTRAINT fk_notificacion_intento FOREIGN KEY (id_intento)
        REFERENCES intento_cobro (id_intento)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_notificacion_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT ck_notificacion_estado
        CHECK (estado IN ('PENDIENTE', 'ENVIADA', 'CONFIRMADA'))
) ENGINE = InnoDB;

-- =============================================================
-- 5. DATOS INICIALES DE CONFIGURACION
-- =============================================================

START TRANSACTION;

INSERT INTO rol (nombre, descripcion) VALUES
    ('ADMINISTRADOR', 'Configura el sistema y consulta todas las operaciones'),
    ('RECEPCIONISTA', 'Atiende socios, registra cobros y gestiona suscripciones'),
    ('PROFESOR', 'Realiza tareas de atención cuando no hay recepcionista')
ON DUPLICATE KEY UPDATE
    descripcion = VALUES(descripcion), activo = TRUE;

INSERT INTO sede (nombre, direccion, categoria) VALUES
    ('Alvear', 'Alvear 4167', 'RED'),
    ('9 de Julio', '9 de Julio 2742', 'RED'),
    ('Irigoyen Freyre', 'Irigoyen Freyre 2646', 'BLACK'),
    ('Ituzaingó', 'Ituzaingó 2053', 'BLACK')
ON DUPLICATE KEY UPDATE
    direccion = VALUES(direccion), categoria = VALUES(categoria), activo = TRUE;

INSERT INTO plan (nombre, categoria_acceso, descripcion) VALUES
    ('Red', 'RED', 'Acceso a las dos sedes Skilful Red'),
    ('Black', 'BLACK', 'Acceso a las cuatro sedes de Skilful')
ON DUPLICATE KEY UPDATE
    categoria_acceso = VALUES(categoria_acceso),
    descripcion = VALUES(descripcion), activo = TRUE;

INSERT INTO tipo_beneficio
    (nombre, vigencia_meses, requiere_empresa) VALUES
    ('ESTUDIANTE', 6, FALSE),
    ('CONVENIO', NULL, TRUE)
ON DUPLICATE KEY UPDATE
    vigencia_meses = VALUES(vigencia_meses),
    requiere_empresa = VALUES(requiere_empresa), activo = TRUE;

INSERT INTO empresa_convenio (nombre) VALUES
    ('Empresa Demo S.A.')
ON DUPLICATE KEY UPDATE activo = TRUE;

INSERT INTO medio_pago
    (nombre, recargo_porcentaje, admite_beneficio) VALUES
    ('EFECTIVO', 0, TRUE),
    ('TRANSFERENCIA', 0, FALSE),
    ('TARJETA_DEBITO', 0, FALSE),
    ('TARJETA_CREDITO', 10, FALSE),
    ('MERCADO_PAGO', 0, FALSE)
ON DUPLICATE KEY UPDATE
    recargo_porcentaje = VALUES(recargo_porcentaje),
    admite_beneficio = VALUES(admite_beneficio), activo = TRUE;

-- Tarifas Red vigentes para el prototipo.
INSERT INTO tarifa_plan
    (id_plan, id_tipo_beneficio, codigo_tarifa, es_suscripcion,
     monto, vigencia_desde)
SELECT p.id_plan, NULL, 'GENERAL', FALSE, 53800.00, '2026-09-01'
FROM plan p WHERE p.nombre = 'Red'
ON DUPLICATE KEY UPDATE monto = VALUES(monto), activo = TRUE;

INSERT INTO tarifa_plan
    (id_plan, id_tipo_beneficio, codigo_tarifa, es_suscripcion,
     monto, vigencia_desde)
SELECT p.id_plan, tb.id_tipo_beneficio, 'ESTUDIANTE', FALSE,
       43500.00, '2026-09-01'
FROM plan p
JOIN tipo_beneficio tb ON tb.nombre = 'ESTUDIANTE'
WHERE p.nombre = 'Red'
ON DUPLICATE KEY UPDATE monto = VALUES(monto), activo = TRUE;

INSERT INTO tarifa_plan
    (id_plan, id_tipo_beneficio, codigo_tarifa, es_suscripcion,
     monto, vigencia_desde)
SELECT p.id_plan, tb.id_tipo_beneficio, 'CONVENIO', FALSE,
       43100.00, '2026-09-01'
FROM plan p
JOIN tipo_beneficio tb ON tb.nombre = 'CONVENIO'
WHERE p.nombre = 'Red'
ON DUPLICATE KEY UPDATE monto = VALUES(monto), activo = TRUE;

INSERT INTO tarifa_plan
    (id_plan, id_tipo_beneficio, codigo_tarifa, es_suscripcion,
     monto, vigencia_desde)
SELECT p.id_plan, NULL, 'SUSCRIPCION', TRUE, 43100.00, '2026-09-01'
FROM plan p WHERE p.nombre = 'Red'
ON DUPLICATE KEY UPDATE monto = VALUES(monto), activo = TRUE;

-- Tarifas Black vigentes para el prototipo.
INSERT INTO tarifa_plan
    (id_plan, id_tipo_beneficio, codigo_tarifa, es_suscripcion,
     monto, vigencia_desde)
SELECT p.id_plan, NULL, 'GENERAL', FALSE, 76500.00, '2026-09-01'
FROM plan p WHERE p.nombre = 'Black'
ON DUPLICATE KEY UPDATE monto = VALUES(monto), activo = TRUE;

INSERT INTO tarifa_plan
    (id_plan, id_tipo_beneficio, codigo_tarifa, es_suscripcion,
     monto, vigencia_desde)
SELECT p.id_plan, tb.id_tipo_beneficio, 'ESTUDIANTE', FALSE,
       57400.00, '2026-09-01'
FROM plan p
JOIN tipo_beneficio tb ON tb.nombre = 'ESTUDIANTE'
WHERE p.nombre = 'Black'
ON DUPLICATE KEY UPDATE monto = VALUES(monto), activo = TRUE;

INSERT INTO tarifa_plan
    (id_plan, id_tipo_beneficio, codigo_tarifa, es_suscripcion,
     monto, vigencia_desde)
SELECT p.id_plan, tb.id_tipo_beneficio, 'CONVENIO', FALSE,
       61200.00, '2026-09-01'
FROM plan p
JOIN tipo_beneficio tb ON tb.nombre = 'CONVENIO'
WHERE p.nombre = 'Black'
ON DUPLICATE KEY UPDATE monto = VALUES(monto), activo = TRUE;

INSERT INTO tarifa_plan
    (id_plan, id_tipo_beneficio, codigo_tarifa, es_suscripcion,
     monto, vigencia_desde)
SELECT p.id_plan, NULL, 'SUSCRIPCION', TRUE, 61200.00, '2026-09-01'
FROM plan p WHERE p.nombre = 'Black'
ON DUPLICATE KEY UPDATE monto = VALUES(monto), activo = TRUE;

-- Usuarios iniciales del prototipo.
-- Usuario: admin  Clave: Admin123!
-- Usuario: recepcion  Clave: Recepcion123!
-- Usuario: profesor  Clave: Profesor123!
INSERT INTO usuario
    (id_rol, nombre_completo, nombre_usuario, clave_hash,
     clave_salt, clave_iteraciones)
SELECT r.id_rol, 'Administradora de prueba', 'admin',
       'dGSaCnfpgk6d+NPMNUFmDBkj8Iw2CntCZid0GuwIs/0=',
       'ep8NMcTiWGLVN/kaqMHkCw==', 120000
FROM rol r
WHERE r.nombre = 'ADMINISTRADOR'
  AND NOT EXISTS (
      SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'admin'
  );

INSERT INTO usuario
    (id_rol, nombre_completo, nombre_usuario, clave_hash,
     clave_salt, clave_iteraciones)
SELECT r.id_rol, 'Recepcionista de prueba', 'recepcion',
       'PEo9C57CAmY1rfWOY5CL6WbvUKctJkaNc+G4j/w/fRQ=',
       'o9WEvOqaRZnfj6FkenqiGw==', 120000
FROM rol r
WHERE r.nombre = 'RECEPCIONISTA'
  AND NOT EXISTS (
      SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'recepcion'
  );

INSERT INTO usuario
    (id_rol, nombre_completo, nombre_usuario, clave_hash,
     clave_salt, clave_iteraciones)
SELECT r.id_rol, 'Profesor de prueba', 'profesor',
       'ym2My0ZbAppk5WkKRg5EQ0Qd1p4Xt2UOSTjHUmOjK4E=',
       'dFXQS4JY1LMyexFlJrIiKQ==', 120000
FROM rol r
WHERE r.nombre = 'PROFESOR'
  AND NOT EXISTS (
      SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'profesor'
  );

INSERT INTO usuario_sede (id_usuario, id_sede, activo)
SELECT u.id_usuario, s.id_sede
       , TRUE
FROM usuario u
CROSS JOIN sede s
WHERE u.nombre_usuario IN ('admin', 'recepcion', 'profesor')
ON DUPLICATE KEY UPDATE activo = TRUE;

COMMIT;

-- =============================================================
-- 6. DATOS FICTICIOS PARA LAS PRUEBAS
-- =============================================================

START TRANSACTION;

INSERT INTO socio
    (id_sede_habitual, dni, nombre, apellido, fecha_nacimiento,
     telefono, telefono_emergencia, correo, domicilio)
SELECT se.id_sede, '99000001', 'Ana', 'Prueba', '1998-05-10',
       '342-0000001', '342-0000002', 'ana.prueba@example.com',
       'Domicilio ficticio 1'
FROM sede se
WHERE se.nombre = 'Alvear'
  AND NOT EXISTS (SELECT 1 FROM socio WHERE dni = '99000001');

INSERT INTO socio
    (id_sede_habitual, dni, nombre, apellido, fecha_nacimiento,
     telefono, telefono_emergencia, correo, domicilio)
SELECT se.id_sede, '99000002', 'Bruno', 'Ejemplo', '1994-11-22',
       '342-0000003', '342-0000004', 'bruno.ejemplo@example.com',
       'Domicilio ficticio 2'
FROM sede se
WHERE se.nombre = 'Irigoyen Freyre'
  AND NOT EXISTS (SELECT 1 FROM socio WHERE dni = '99000002');

INSERT INTO socio
    (id_sede_habitual, dni, nombre, apellido, fecha_nacimiento,
     telefono, telefono_emergencia, correo, domicilio)
SELECT se.id_sede, '99000003', 'Carla', 'Demostracion', '2000-02-14',
       '342-0000005', '342-0000006', 'carla.demo@example.com',
       'Domicilio ficticio 3'
FROM sede se
WHERE se.nombre = 'Ituzaingó'
  AND NOT EXISTS (SELECT 1 FROM socio WHERE dni = '99000003');

INSERT INTO membresia (id_socio, id_plan, fecha_inicio, estado)
SELECT so.id_socio, p.id_plan,
       CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE), 'ACTIVA'
FROM socio so
JOIN plan p ON p.nombre = 'Red'
WHERE so.dni = '99000001'
  AND NOT EXISTS (
      SELECT 1 FROM membresia m
      WHERE m.id_socio = so.id_socio AND m.estado = 'ACTIVA'
  );

INSERT INTO membresia (id_socio, id_plan, fecha_inicio, estado)
SELECT so.id_socio, p.id_plan,
       CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE), 'ACTIVA'
FROM socio so
JOIN plan p ON p.nombre = 'Black'
WHERE so.dni IN ('99000002', '99000003')
  AND NOT EXISTS (
      SELECT 1 FROM membresia m
      WHERE m.id_socio = so.id_socio AND m.estado = 'ACTIVA'
  );

-- Beneficio estudiantil con vencimiento cercano para probar la alerta.
INSERT INTO beneficio_socio
    (id_socio, id_tipo_beneficio, fecha_presentacion,
     fecha_vencimiento, estado, observacion)
SELECT so.id_socio, tb.id_tipo_beneficio,
       DATE_SUB(DATE_ADD(CURDATE(), INTERVAL 15 DAY), INTERVAL 6 MONTH),
       DATE_ADD(CURDATE(), INTERVAL 15 DAY), 'ACTIVO',
       'Certificado ficticio para prueba de alerta'
FROM socio so
JOIN tipo_beneficio tb ON tb.nombre = 'ESTUDIANTE'
WHERE so.dni = '99000001'
  AND NOT EXISTS (
      SELECT 1 FROM beneficio_socio bs
      WHERE bs.id_socio = so.id_socio AND bs.estado = 'ACTIVO'
  );

-- Beneficio por convenio con empresa identificada.
INSERT INTO beneficio_socio
    (id_socio, id_tipo_beneficio, id_empresa,
     fecha_presentacion, fecha_vencimiento, estado)
SELECT so.id_socio, tb.id_tipo_beneficio, ec.id_empresa,
       CURDATE(), NULL, 'ACTIVO'
FROM socio so
JOIN tipo_beneficio tb ON tb.nombre = 'CONVENIO'
JOIN empresa_convenio ec ON ec.nombre = 'Empresa Demo S.A.'
WHERE so.dni = '99000002'
  AND NOT EXISTS (
      SELECT 1 FROM beneficio_socio bs
      WHERE bs.id_socio = so.id_socio AND bs.estado = 'ACTIVO'
  );

-- Suscripción ficticia para comprobar intentos y seguimientos.
INSERT INTO suscripcion
    (id_membresia, fecha_inicio, permanencia_hasta, dia_cobro, estado)
SELECT m.id_membresia,
       DATE_SUB(CURDATE(), INTERVAL 2 MONTH),
       DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 MONTH), INTERVAL 6 MONTH),
       3, 'ACTIVA'
FROM membresia m
JOIN socio so ON so.id_socio = m.id_socio
WHERE so.dni = '99000003' AND m.estado = 'ACTIVA'
  AND NOT EXISTS (
      SELECT 1 FROM suscripcion su
      WHERE su.id_membresia = m.id_membresia AND su.estado = 'ACTIVA'
  );

-- Cuotas del mes actual con el precio general como importe original.
INSERT INTO cuota
    (id_membresia, id_tarifa_base, periodo, fecha_emision,
     fecha_vencimiento, importe_original, estado)
SELECT m.id_membresia, tp.id_tarifa,
       CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE),
       CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE),
       DATE_ADD(CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE), INTERVAL 4 DAY),
       tp.monto, 'PENDIENTE'
FROM membresia m
JOIN socio so ON so.id_socio = m.id_socio
JOIN tarifa_plan tp
  ON tp.id_plan = m.id_plan
 AND tp.codigo_tarifa = 'GENERAL'
 AND tp.activo = TRUE
 AND CURDATE() >= tp.vigencia_desde
 AND (tp.vigencia_hasta IS NULL OR CURDATE() <= tp.vigencia_hasta)
WHERE so.dni IN ('99000001', '99000002', '99000003')
  AND m.estado = 'ACTIVA'
ON DUPLICATE KEY UPDATE
    id_tarifa_base = VALUES(id_tarifa_base),
    importe_original = VALUES(importe_original);

-- Primer rechazo de la suscripción ficticia. Queda pendiente de notificar.
INSERT INTO intento_cobro
    (id_suscripcion, id_cuota, id_usuario, id_sede,
     numero_intento, fecha_intento, importe, estado, observacion)
SELECT su.id_suscripcion, c.id_cuota, u.id_usuario, se.id_sede,
       1, CURRENT_TIMESTAMP, tp.monto, 'RECHAZADO',
       'Respuesta simulada de Mercado Pago'
FROM suscripcion su
JOIN membresia m ON m.id_membresia = su.id_membresia
JOIN socio so ON so.id_socio = m.id_socio
JOIN cuota c
  ON c.id_membresia = m.id_membresia
 AND c.periodo = CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE)
JOIN tarifa_plan tp
  ON tp.id_plan = m.id_plan
 AND tp.codigo_tarifa = 'SUSCRIPCION'
 AND tp.activo = TRUE
JOIN usuario u ON u.nombre_usuario = 'admin'
JOIN sede se ON se.id_sede = so.id_sede_habitual
WHERE so.dni = '99000003'
  AND NOT EXISTS (
      SELECT 1 FROM intento_cobro ic
      WHERE ic.id_suscripcion = su.id_suscripcion
        AND ic.id_cuota = c.id_cuota
        AND ic.numero_intento = 1
  );

COMMIT;

-- =============================================================
-- 7. VISTAS PARA CONSULTAS, ALERTAS E INFORMES
-- =============================================================

CREATE OR REPLACE VIEW vw_saldo_socio AS
SELECT
    so.id_socio,
    so.dni,
    CONCAT(so.apellido, ', ', so.nombre) AS socio,
    COALESCE(SUM(
        CASE ms.tipo
            WHEN 'CREDITO' THEN ms.monto
            WHEN 'DEBITO' THEN -ms.monto
            ELSE 0
        END
    ), 0) AS saldo_disponible
FROM socio so
LEFT JOIN movimiento_saldo ms ON ms.id_socio = so.id_socio
GROUP BY so.id_socio, so.dni, so.apellido, so.nombre;

CREATE OR REPLACE VIEW vw_certificados_proximos_vencer AS
SELECT
    so.id_socio,
    so.dni,
    CONCAT(so.apellido, ', ', so.nombre) AS socio,
    bs.fecha_vencimiento,
    DATEDIFF(bs.fecha_vencimiento, CURDATE()) AS dias_restantes,
    se.nombre AS sede_habitual
FROM beneficio_socio bs
JOIN socio so ON so.id_socio = bs.id_socio
JOIN tipo_beneficio tb
  ON tb.id_tipo_beneficio = bs.id_tipo_beneficio
JOIN sede se ON se.id_sede = so.id_sede_habitual
WHERE tb.nombre = 'ESTUDIANTE'
  AND bs.estado = 'ACTIVO'
  AND bs.fecha_vencimiento BETWEEN CURDATE()
      AND DATE_ADD(CURDATE(), INTERVAL 30 DAY);

CREATE OR REPLACE VIEW vw_notificaciones_pendientes AS
SELECT
    ic.id_intento,
    so.dni,
    CONCAT(so.apellido, ', ', so.nombre) AS socio,
    so.telefono,
    ic.numero_intento,
    ic.fecha_intento,
    ic.importe,
    se.nombre AS sede
FROM intento_cobro ic
JOIN suscripcion su ON su.id_suscripcion = ic.id_suscripcion
JOIN membresia m ON m.id_membresia = su.id_membresia
JOIN socio so ON so.id_socio = m.id_socio
JOIN sede se ON se.id_sede = ic.id_sede
LEFT JOIN notificacion n ON n.id_intento = ic.id_intento
WHERE ic.estado = 'RECHAZADO'
  AND n.id_notificacion IS NULL;

CREATE OR REPLACE VIEW vw_resumen_diario_cobros AS
SELECT
    DATE(ic.fecha_intento) AS fecha,
    se.id_sede,
    se.nombre AS sede,
    COUNT(*) AS total_intentos,
    SUM(ic.estado = 'APROBADO') AS aprobados,
    SUM(ic.estado = 'RECHAZADO') AS rechazados,
    SUM(ic.estado = 'PENDIENTE') AS pendientes,
    SUM(CASE WHEN ic.estado = 'APROBADO' THEN ic.importe ELSE 0 END)
        AS importe_aprobado
FROM intento_cobro ic
JOIN sede se ON se.id_sede = ic.id_sede
GROUP BY DATE(ic.fecha_intento), se.id_sede, se.nombre;

CREATE OR REPLACE VIEW vw_historial_pagos AS
SELECT
    p.id_pago,
    p.fecha_pago,
    c.periodo,
    c.estado AS estado_cuota,
    so.dni,
    CONCAT(so.apellido, ', ', so.nombre) AS socio,
    pl.nombre AS plan,
    mp.nombre AS medio_pago,
    p.precio_original,
    p.descuento,
    p.recargo,
    p.saldo_aplicado,
    p.importe_final,
    p.importe_abonado,
    p.saldo_generado,
    p.estado AS estado_pago,
    se.nombre AS sede,
    u.nombre_usuario AS registrado_por
FROM pago p
JOIN cuota c ON c.id_cuota = p.id_cuota
JOIN membresia m ON m.id_membresia = c.id_membresia
JOIN socio so ON so.id_socio = m.id_socio
JOIN plan pl ON pl.id_plan = m.id_plan
JOIN medio_pago mp ON mp.id_medio_pago = p.id_medio_pago
JOIN sede se ON se.id_sede = p.id_sede
JOIN usuario u ON u.id_usuario = p.id_usuario;

-- =============================================================
-- 8. CONSULTAS DE COMPROBACION
-- =============================================================

SELECT 'Base de datos creada correctamente' AS resultado;

SELECT TABLE_NAME
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'proyecto_impulso'
  AND TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;

SELECT p.nombre AS plan, tp.codigo_tarifa, tp.monto
FROM tarifa_plan tp
JOIN plan p ON p.id_plan = tp.id_plan
WHERE tp.activo = TRUE
ORDER BY p.nombre, tp.monto DESC;

SELECT * FROM vw_certificados_proximos_vencer;
SELECT * FROM vw_notificaciones_pendientes;
SELECT * FROM vw_resumen_diario_cobros;
