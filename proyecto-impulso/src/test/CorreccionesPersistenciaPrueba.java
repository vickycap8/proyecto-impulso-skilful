package ar.com.skilful.vista;

import ar.com.skilful.conexion.ConexionBD;
import java.sql.*;
import java.util.UUID;

public final class CorreccionesPersistenciaPrueba {
    private static int superadas;

    private interface Caso {
        void ejecutar(Connection c, Datos d) throws SQLException;
    }

    private interface Operacion {
        void ejecutar() throws SQLException;
    }

    private static final class Datos {
        int socio, beneficio, suscripcion, anterior, actual, usuario, sede;
    }

    public static void main(String[] args) throws SQLException {
        probar("Renovación conserva historial y genera nuevo certificado", (c, d) -> {
            String fechas = texto(c, "SELECT CONCAT(fecha_presentacion, '/', fecha_vencimiento) "
                + "FROM beneficio_socio WHERE id_beneficio_socio = ?", d.beneficio);
            VentanaBeneficios.renovarConservandoHistorial(c, d.beneficio);
            verificar(fechas.equals(texto(c,
                "SELECT CONCAT(fecha_presentacion, '/', fecha_vencimiento) "
                + "FROM beneficio_socio WHERE id_beneficio_socio = ?", d.beneficio)), "Fechas anteriores");
            verificar("FINALIZADO".equals(texto(c,
                "SELECT estado FROM beneficio_socio WHERE id_beneficio_socio = ?", d.beneficio)), "Estado anterior");
            verificar(contar(c, "SELECT COUNT(*) FROM beneficio_socio WHERE id_socio = ? "
                + "AND estado = 'ACTIVO' AND fecha_presentacion = CURDATE() "
                + "AND fecha_vencimiento = DATE_ADD(CURDATE(), INTERVAL 6 MONTH)", d.socio) == 1, "Nuevo certificado");
            verificar(contar(c, "SELECT COUNT(*) FROM beneficio_socio WHERE id_socio = ?", d.socio) == 2, "Dos registros");
            esperarError(() -> VentanaBeneficios.renovarConservandoHistorial(c, d.beneficio), "ya está finalizado");
        });
        probar("Otro beneficio activo impide duplicar la renovación", (c, d) -> {
            actualizar(c, "UPDATE beneficio_socio SET estado = 'VENCIDO' WHERE id_beneficio_socio = ?", d.beneficio);
            insertar(c, "INSERT INTO beneficio_socio (id_socio, id_tipo_beneficio, fecha_presentacion, "
                + "fecha_vencimiento, estado) SELECT id_socio, id_tipo_beneficio, CURDATE(), "
                + "DATE_ADD(CURDATE(), INTERVAL 6 MONTH), 'ACTIVO' "
                + "FROM beneficio_socio WHERE id_beneficio_socio = ?", d.beneficio);
            esperarError(() -> VentanaBeneficios.renovarConservandoHistorial(c, d.beneficio), "otro beneficio activo");
            verificar("VENCIDO".equals(texto(c,
                "SELECT estado FROM beneficio_socio WHERE id_beneficio_socio = ?", d.beneficio)), "No altera el anterior");
        });
        probar("Los intentos se numeran por cuota y se limita el sexto", (c, d) -> {
            for (int i = 1; i <= 5; i++) {
                verificar(resultado(c, d, d.anterior, false) == i, "Número del intento anterior");
            }
            esperarError(() -> resultado(c, d, d.anterior, false), "máximo de cinco");
            verificar(resultado(c, d, d.actual, false) == 1, "El período actual empieza en uno");
            verificar(contar(c, "SELECT COUNT(*) FROM intento_cobro WHERE id_cuota = ?", d.anterior) == 5, "Cinco anteriores");
            verificar(contar(c, "SELECT COUNT(*) FROM intento_cobro WHERE id_cuota = ?", d.actual) == 1, "Un intento actual");
        });
        probar("Notificación corresponde al intento y período elegidos", (c, d) -> {
            for (int i = 0; i < 5; i++) {
                resultado(c, d, d.anterior, false);
            }
            resultado(c, d, d.actual, false);
            long intento = contar(c, "SELECT id_intento FROM intento_cobro WHERE id_cuota = ?", d.actual);
            VentanaSuscripciones.registrarNotificacion(c, intento, d.suscripcion, d.actual, d.usuario, "Prueba");
            verificar(contar(c, "SELECT COUNT(*) FROM notificacion WHERE id_intento = ?", intento) == 1, "Notifica el actual");
            verificar(contar(c, "SELECT COUNT(*) FROM notificacion n JOIN intento_cobro ic "
                + "ON ic.id_intento = n.id_intento WHERE ic.id_cuota = ?", d.anterior) == 0, "No notifica el anterior");
            esperarError(() -> VentanaSuscripciones.registrarNotificacion(c, intento,
                d.suscripcion, d.actual, d.usuario, "Duplicado"), "ya fue notificado");
            esperarError(() -> VentanaSuscripciones.registrarNotificacion(c, intento,
                d.suscripcion, d.anterior, d.usuario, "Período incorrecto"), "no corresponde");
        });
        probar("Aprobación paga solo la cuota seleccionada y no se duplica", (c, d) -> {
            resultado(c, d, d.actual, true);
            verificar("PAGADA".equals(texto(c, "SELECT estado FROM cuota WHERE id_cuota = ?", d.actual)), "Cuota pagada");
            verificar("PENDIENTE".equals(texto(c, "SELECT estado FROM cuota WHERE id_cuota = ?", d.anterior)), "Anterior pendiente");
            verificar(contar(c, "SELECT COUNT(*) FROM pago p JOIN intento_cobro ic ON ic.id_pago = p.id_pago "
                + "WHERE p.id_cuota = ? AND ic.id_cuota = p.id_cuota AND ic.estado = 'APROBADO' "
                + "AND p.importe_final = 80 AND p.descuento = 20", d.actual) == 1, "Pago vinculado e importes");
            esperarError(() -> resultado(c, d, d.actual, true), "no está pendiente");
            verificar(contar(c, "SELECT COUNT(*) FROM pago WHERE id_cuota = ?", d.actual) == 1, "Un único pago");
        });
        probar("Se rechaza una cuota ajena a la suscripción", (c, d) -> {
            Datos ajeno = preparar(c);
            esperarError(() -> resultado(c, d, ajeno.actual, false), "no pertenece");
            verificar(contar(c, "SELECT COUNT(*) FROM intento_cobro WHERE id_cuota = ?", ajeno.actual) == 0, "No crea intento ajeno");
        });
        probar("Rollback revierte renovación, pago e intento", (c, d) -> {
            Savepoint inicio = c.setSavepoint();
            VentanaBeneficios.renovarConservandoHistorial(c, d.beneficio);
            resultado(c, d, d.actual, true);
            c.rollback(inicio);
            verificar("ACTIVO".equals(texto(c, "SELECT estado FROM beneficio_socio WHERE id_beneficio_socio = ?", d.beneficio)), "Beneficio restaurado");
            verificar(contar(c, "SELECT COUNT(*) FROM beneficio_socio WHERE id_socio = ?", d.socio) == 1, "Sin nueva renovación");
            verificar("PENDIENTE".equals(texto(c, "SELECT estado FROM cuota WHERE id_cuota = ?", d.actual)), "Cuota restaurada");
            verificar(contar(c, "SELECT COUNT(*) FROM pago WHERE id_cuota = ?", d.actual) == 0, "Sin pago");
            verificar(contar(c, "SELECT COUNT(*) FROM intento_cobro WHERE id_cuota = ?", d.actual) == 0, "Sin intento");
        });
        System.out.println("Pruebas de persistencia superadas: " + superadas + "/7. Datos ficticios revertidos.");
    }

    private static void probar(String nombre, Caso caso) throws SQLException {
        try (Connection c = ConexionBD.conectar()) {
            c.setAutoCommit(false);
            try {
                caso.ejecutar(c, preparar(c));
            } finally {
                c.rollback();
            }
        }
        superadas++;
        System.out.println("OK - " + nombre);
    }

    private static int resultado(Connection c, Datos d, int cuota, boolean aprobado) throws SQLException {
        return VentanaSuscripciones.registrarResultado(c, d.suscripcion, cuota,
            d.usuario, d.sede, aprobado, "Prueba de regresión TP2");
    }

    private static Datos preparar(Connection c) throws SQLException {
        Datos d = new Datos();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT us.id_usuario, us.id_sede FROM usuario_sede us "
                + "JOIN usuario u ON u.id_usuario = us.id_usuario "
                + "WHERE us.activo = TRUE AND u.activo = TRUE ORDER BY us.id_usuario, us.id_sede LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new SQLException("Faltan los usuarios y sedes de configuración del TP2.");
            }
            d.usuario = rs.getInt(1);
            d.sede = rs.getInt(2);
        }
        int tipo = (int) contar(c, "SELECT id_tipo_beneficio FROM tipo_beneficio WHERE nombre = ?", "ESTUDIANTE");
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        int plan = insertar(c, "INSERT INTO plan (nombre, categoria_acceso) VALUES (?, 'RED')", "PRUEBA_TP2_" + token);
        int tarifa = insertar(c, "INSERT INTO tarifa_plan (id_plan, codigo_tarifa, monto, vigencia_desde) "
            + "VALUES (?, 'GENERAL', 100, DATE_SUB(CURDATE(), INTERVAL 1 YEAR))", plan);
        insertar(c, "INSERT INTO tarifa_plan (id_plan, codigo_tarifa, es_suscripcion, monto, vigencia_desde) "
            + "VALUES (?, 'SUSCRIPCION', TRUE, 80, DATE_SUB(CURDATE(), INTERVAL 1 YEAR))", plan);
        d.socio = insertar(c, "INSERT INTO socio (id_sede_habitual, dni, nombre, apellido, fecha_nacimiento, "
            + "telefono, telefono_emergencia, domicilio) VALUES (?, ?, 'Prueba', 'Regresión', "
            + "'2000-01-01', '000', '000', 'Dato ficticio')", d.sede, "PR" + token);
        d.beneficio = insertar(c, "INSERT INTO beneficio_socio (id_socio, id_tipo_beneficio, "
            + "fecha_presentacion, fecha_vencimiento, estado, observacion) "
            + "VALUES (?, ?, DATE_SUB(CURDATE(), INTERVAL 6 MONTH), CURDATE(), 'ACTIVO', 'Certificado anterior')",
            d.socio, tipo);
        int membresia = insertar(c, "INSERT INTO membresia (id_socio, id_plan, fecha_inicio) "
            + "VALUES (?, ?, DATE_SUB(CURDATE(), INTERVAL 2 MONTH))", d.socio, plan);
        d.suscripcion = insertar(c, "INSERT INTO suscripcion (id_membresia, fecha_inicio, permanencia_hasta, dia_cobro) "
            + "VALUES (?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 6 MONTH), 1)", membresia);
        String periodo = "CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE)";
        d.anterior = cuota(c, membresia, tarifa, "DATE_SUB(" + periodo + ", INTERVAL 1 MONTH)");
        d.actual = cuota(c, membresia, tarifa, periodo);
        return d;
    }

    private static int cuota(Connection c, int membresia, int tarifa, String periodo) throws SQLException {
        return insertar(c, "INSERT INTO cuota (id_membresia, id_tarifa_base, periodo, fecha_emision, "
            + "fecha_vencimiento, importe_original) VALUES (?, ?, " + periodo + ", " + periodo
            + ", DATE_ADD(" + periodo + ", INTERVAL 10 DAY), 100)", membresia, tarifa);
    }

    private static int insertar(Connection c, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            parametros(ps, parametros);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo preparar el registro ficticio.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("No se obtuvo la clave del registro ficticio.");
                }
                return rs.getInt(1);
            }
        }
    }

    private static void actualizar(Connection c, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            parametros(ps, parametros);
            ps.executeUpdate();
        }
    }

    private static String texto(Connection c, String sql, Object... parametros) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            parametros(ps, parametros);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Falta un registro necesario para la prueba.");
                }
                return rs.getString(1);
            }
        }
    }

    private static long contar(Connection c, String sql, Object... parametros) throws SQLException {
        return Long.parseLong(texto(c, sql, parametros));
    }

    private static void parametros(PreparedStatement ps, Object... valores) throws SQLException {
        for (int i = 0; i < valores.length; i++) {
            ps.setObject(i + 1, valores[i]);
        }
    }

    private static void esperarError(Operacion operacion, String textoEsperado) throws SQLException {
        try {
            operacion.ejecutar();
        } catch (SQLException error) {
            if (error.getMessage().contains(textoEsperado)) {
                return;
            }
            throw error;
        }
        throw new AssertionError("Se esperaba el rechazo: " + textoEsperado);
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }
}
