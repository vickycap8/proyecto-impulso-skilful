package ar.com.skilful.dao;

import ar.com.skilful.conexion.ConexionBD;
import ar.com.skilful.modelo.Suscripcion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SuscripcionDAO {

    public void insertar(Suscripcion suscripcion) throws SQLException {
        String sqlSuscripcion =
            "INSERT INTO suscripcion "
          + "(id_membresia, fecha_inicio, permanencia_hasta, dia_cobro, estado) "
          + "SELECT ?, ?, ?, ?, 'ACTIVA' WHERE NOT EXISTS ("
          + "SELECT 1 FROM suscripcion "
          + "WHERE id_membresia = ? AND estado = 'ACTIVA')";

        String sqlCuota =
            "INSERT INTO cuota "
          + "(id_membresia, id_tarifa_base, periodo, fecha_emision, "
          + "fecha_vencimiento, importe_original, estado) "
          + "SELECT m.id_membresia, tp.id_tarifa, "
          + "CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE), "
          + "CURDATE(), DATE_ADD(CURDATE(), INTERVAL 4 DAY), "
          + "tp.monto, 'PENDIENTE' FROM membresia m "
          + "INNER JOIN tarifa_plan tp ON tp.id_plan = m.id_plan "
          + "AND tp.codigo_tarifa = 'GENERAL' AND tp.activo = TRUE "
          + "WHERE m.id_membresia = ? "
          + "AND CURDATE() >= tp.vigencia_desde "
          + "AND (tp.vigencia_hasta IS NULL OR CURDATE() <= tp.vigencia_hasta) "
          + "ON DUPLICATE KEY UPDATE id_membresia = VALUES(id_membresia)";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);

            try (PreparedStatement sentenciaSuscripcion =
                    conexion.prepareStatement(sqlSuscripcion);
                 PreparedStatement sentenciaCuota =
                    conexion.prepareStatement(sqlCuota)) {

                sentenciaSuscripcion.setInt(1, suscripcion.getIdMembresia());
                sentenciaSuscripcion.setDate(
                    2, Date.valueOf(suscripcion.getFechaInicio())
                );
                sentenciaSuscripcion.setDate(
                    3, Date.valueOf(suscripcion.getPermanenciaHasta())
                );
                sentenciaSuscripcion.setInt(4, suscripcion.getDiaCobro());
                sentenciaSuscripcion.setInt(5, suscripcion.getIdMembresia());

                if (sentenciaSuscripcion.executeUpdate() != 1) {
                    throw new SQLException(
                        "El socio ya posee una suscripción activa."
                    );
                }

                sentenciaCuota.setInt(1, suscripcion.getIdMembresia());
                sentenciaCuota.executeUpdate();
                conexion.commit();

            } catch (SQLException error) {
                conexion.rollback();
                throw error;
            } finally {
                conexion.setAutoCommit(true);
            }
        }
    }

    public boolean finalizarSiCumplioPermanencia(int idSuscripcion)
            throws SQLException {

        String sql =
            "UPDATE suscripcion SET estado = 'FINALIZADA', fecha_baja = CURDATE() "
          + "WHERE id_suscripcion = ? AND estado = 'ACTIVA' "
          + "AND CURDATE() >= permanencia_hasta";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idSuscripcion);
            return sentencia.executeUpdate() == 1;
        }
    }
    public static final class MembresiaDisponible {
        private final int id;
        private final String plan;
        private final BigDecimal tarifa;
        private final boolean yaSuscripto;
        public MembresiaDisponible(int id, String plan, BigDecimal tarifa, boolean yaSuscripto) {
            this.id = id; this.plan = plan; this.tarifa = tarifa; this.yaSuscripto = yaSuscripto;
        }
        public int getId() { return id; }
        public String getPlan() { return plan; }
        public BigDecimal getTarifa() { return tarifa; }
        public boolean isYaSuscripto() { return yaSuscripto; }
    }

    public MembresiaDisponible membresiaActiva(int idSocio) throws SQLException {
        String sql =
            "SELECT m.id_membresia, p.nombre AS plan, " +
            "tp.monto AS tarifa_suscripcion, " +
            "EXISTS (" +
            "SELECT 1 FROM suscripcion su " +
            "WHERE su.id_membresia = m.id_membresia " +
            "AND su.estado = 'ACTIVA'" +
            ") AS ya_suscripto " +
            "FROM membresia m " +
            "INNER JOIN plan p " +
            "ON p.id_plan = m.id_plan " +
            "INNER JOIN tarifa_plan tp " +
            "ON tp.id_plan = p.id_plan " +
            "AND tp.codigo_tarifa = 'SUSCRIPCION' " +
            "AND tp.activo = TRUE " +
            "WHERE m.id_socio = ? " +
            "AND m.estado = 'ACTIVA' " +
            "AND CURDATE() >= tp.vigencia_desde " +
            "AND (tp.vigencia_hasta IS NULL " +
            "OR CURDATE() <= tp.vigencia_hasta) " +
            "ORDER BY m.fecha_inicio DESC, " +
            "tp.vigencia_desde DESC " +
            "LIMIT 1";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idSocio);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) return null;
                return new MembresiaDisponible(resultado.getInt("id_membresia"),
                    resultado.getString("plan"), resultado.getBigDecimal("tarifa_suscripcion"),
                    resultado.getBoolean("ya_suscripto"));
            }
        }
    }

    public int guardarResultado(int idSuscripcion, int idCuota, int idUsuario,
            int idSede, boolean aprobado, String observacion) throws SQLException {
        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                int numero = registrarResultado(conexion, idSuscripcion, idCuota,
                    idUsuario, idSede, aprobado, observacion);
                conexion.commit();
                return numero;
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    public void guardarNotificacion(long idIntento, int idSuscripcion,
            int idCuota, int idUsuario, String observacion) throws SQLException {
        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                registrarNotificacion(conexion, idIntento, idSuscripcion,
                    idCuota, idUsuario, observacion);
                conexion.commit();
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    /** Opera sobre la cuota indicada; el llamador confirma o revierte la transacción. */
    public static int registrarResultado(Connection conexion, int idSuscripcion, int idCuota,
            int idUsuario, int idSede, boolean aprobado, String observacion) throws SQLException {
        exigirTransaccion(conexion);
        if (observacion != null && observacion.length() > 250) {
            throw new SQLException("La observación admite hasta 250 caracteres.");
        }
        String sqlDatos = "SELECT c.importe_original, tp.id_tarifa, tp.monto "
            + "FROM suscripcion su JOIN membresia m ON m.id_membresia = su.id_membresia "
            + "JOIN cuota c ON c.id_membresia = m.id_membresia "
            + "JOIN tarifa_plan tp ON tp.id_plan = m.id_plan "
            + "AND tp.codigo_tarifa = 'SUSCRIPCION' AND tp.activo = TRUE "
            + "WHERE su.id_suscripcion = ? AND c.id_cuota = ? "
            + "AND su.estado = 'ACTIVA' AND c.estado IN ('PENDIENTE', 'VENCIDA') "
            + "AND CURDATE() >= tp.vigencia_desde "
            + "AND (tp.vigencia_hasta IS NULL OR CURDATE() <= tp.vigencia_hasta) "
            + "ORDER BY tp.vigencia_desde DESC, tp.id_tarifa DESC LIMIT 1 FOR UPDATE";
        int idTarifa;
        BigDecimal precioOriginal;
        BigDecimal importe;
        try (PreparedStatement ps = conexion.prepareStatement(sqlDatos)) {
            ps.setInt(1, idSuscripcion);
            ps.setInt(2, idCuota);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("La cuota seleccionada no está pendiente, "
                        + "no pertenece a esta suscripción activa o no hay tarifa vigente.");
                }
                idTarifa = rs.getInt("id_tarifa");
                precioOriginal = rs.getBigDecimal("importe_original");
                importe = rs.getBigDecimal("monto");
            }
        }
        // Se vuelve a consultar MySQL, sin confiar en un contador viejo de la tabla.
        int ultimoNumero = 0;
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT numero_intento, estado FROM intento_cobro "
                + "WHERE id_suscripcion = ? AND id_cuota = ? FOR UPDATE")) {
            ps.setInt(1, idSuscripcion);
            ps.setInt(2, idCuota);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if ("APROBADO".equals(rs.getString("estado"))) {
                        throw new SQLException("Esta cuota ya tiene un intento aprobado.");
                    }
                    ultimoNumero = Math.max(ultimoNumero, rs.getInt("numero_intento"));
                }
            }
        }
        if (ultimoNumero >= 5) {
            throw new SQLException("Esta cuota alcanzó el máximo de cinco intentos. "
                + "Corresponde registrar el pago por un medio alternativo.");
        }
        long idPago = 0;
        if (aprobado) {
            String sqlPago = "INSERT INTO pago (id_cuota, id_medio_pago, id_tarifa_aplicada, "
                + "id_usuario, id_sede, precio_original, descuento, recargo, saldo_aplicado, "
                + "importe_final, importe_abonado, saldo_generado, estado) "
                + "SELECT ?, mp.id_medio_pago, ?, ?, ?, ?, ?, 0, 0, ?, ?, 0, 'REGISTRADO' "
                + "FROM medio_pago mp WHERE mp.nombre = 'MERCADO_PAGO' AND mp.activo = TRUE";
            try (PreparedStatement ps = conexion.prepareStatement(sqlPago, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idCuota);
                ps.setInt(2, idTarifa);
                ps.setInt(3, idUsuario);
                ps.setInt(4, idSede);
                ps.setBigDecimal(5, precioOriginal);
                ps.setBigDecimal(6, precioOriginal.subtract(importe).max(BigDecimal.ZERO));
                ps.setBigDecimal(7, importe);
                ps.setBigDecimal(8, importe);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("No se encontró el medio de pago MERCADO_PAGO activo.");
                }
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (!claves.next()) {
                        throw new SQLException("No se pudo obtener el identificador del pago.");
                    }
                    idPago = claves.getLong(1);
                }
            }
        }
        int numero = ultimoNumero + 1;
        try (PreparedStatement ps = conexion.prepareStatement(
                "INSERT INTO intento_cobro (id_suscripcion, id_cuota, id_usuario, id_sede, "
                + "id_pago, numero_intento, importe, estado, observacion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, idSuscripcion);
            ps.setInt(2, idCuota);
            ps.setInt(3, idUsuario);
            ps.setInt(4, idSede);
            if (aprobado) {
                ps.setLong(5, idPago);
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            ps.setInt(6, numero);
            ps.setBigDecimal(7, importe);
            ps.setString(8, aprobado ? "APROBADO" : "RECHAZADO");
            ps.setString(9, observacion);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo registrar el intento.");
            }
        }
        if (aprobado) {
            try (PreparedStatement ps = conexion.prepareStatement(
                    "UPDATE cuota SET estado = 'PAGADA' "
                    + "WHERE id_cuota = ? AND estado IN ('PENDIENTE', 'VENCIDA')")) {
                ps.setInt(1, idCuota);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("La cuota ya no está pendiente. Se revierte el cobro.");
                }
            }
        }
        return numero;
    }

    private static void exigirTransaccion(Connection conexion) throws SQLException {
        if (conexion.getAutoCommit()) {
            throw new SQLException("La operación requiere una transacción.");
        }
    }

    /** Registra el contacto manual referido al intento exacto que mostraba la fila. */
    public static void registrarNotificacion(Connection conexion, long idIntento,
            int idSuscripcion, int idCuota, int idUsuario, String observacion) throws SQLException {
        exigirTransaccion(conexion);
        if (observacion != null && observacion.length() > 250) {
            throw new SQLException("La observación admite hasta 250 caracteres.");
        }
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT id_intento FROM intento_cobro WHERE id_intento = ? "
                + "AND id_suscripcion = ? AND id_cuota = ? AND estado = 'RECHAZADO' FOR UPDATE")) {
            ps.setLong(1, idIntento);
            ps.setInt(2, idSuscripcion);
            ps.setInt(3, idCuota);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El intento no corresponde al rechazo seleccionado.");
                }
            }
        }
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT estado FROM notificacion WHERE id_intento = ? FOR UPDATE")) {
            ps.setLong(1, idIntento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!"PENDIENTE".equals(rs.getString("estado"))) {
                        throw new SQLException("Este intento ya fue notificado.");
                    }
                }
            }
        }
        try (PreparedStatement ps = conexion.prepareStatement(
                "UPDATE notificacion SET estado = 'ENVIADA', id_usuario = ?, "
                + "fecha_notificacion = CURRENT_TIMESTAMP, canal = 'WHATSAPP', observacion = ? "
                + "WHERE id_intento = ? AND estado = 'PENDIENTE'")) {
            ps.setInt(1, idUsuario);
            ps.setString(2, observacion);
            ps.setLong(3, idIntento);
            if (ps.executeUpdate() > 0) {
                return;
            }
        }
        try (PreparedStatement ps = conexion.prepareStatement(
                "INSERT INTO notificacion (id_intento, id_usuario, canal, estado, observacion) "
                + "VALUES (?, ?, 'WHATSAPP', 'ENVIADA', ?)")) {
            ps.setLong(1, idIntento);
            ps.setInt(2, idUsuario);
            ps.setString(3, observacion);
            ps.executeUpdate();
        }
    }

    public ArrayList<Object[]> buscarParaPantalla(String busqueda) throws SQLException {
        String sql = "SELECT su.id_suscripcion, so.dni, "
            + "CONCAT(so.apellido, ', ', so.nombre) AS socio, so.telefono, "
            + "se.nombre AS sede, p.nombre AS plan, su.dia_cobro, "
            + "su.permanencia_hasta, su.estado, c.id_cuota, "
            + "DATE_FORMAT(c.periodo, '%Y-%m') AS periodo, c.estado AS estado_cuota, "
            + "COALESCE(ic.numero_intento, 0) AS intentos, "
            + "COALESCE(ic.estado, 'SIN INTENTOS') AS ultimo_resultado, "
            + "CASE WHEN ic.estado = 'RECHAZADO' THEN "
            + "CASE WHEN EXISTS (SELECT 1 FROM notificacion n "
            + "WHERE n.id_intento = ic.id_intento AND n.estado IN ('ENVIADA', 'CONFIRMADA')) "
            + "THEN 'ENVIADA' ELSE 'PENDIENTE' END ELSE '-' END AS notificacion, "
            + "ic.id_intento FROM suscripcion su "
            + "JOIN membresia m ON m.id_membresia = su.id_membresia "
            + "JOIN socio so ON so.id_socio = m.id_socio "
            + "JOIN sede se ON se.id_sede = so.id_sede_habitual "
            + "JOIN plan p ON p.id_plan = m.id_plan "
            + "LEFT JOIN cuota c ON c.id_membresia = m.id_membresia "
            + "LEFT JOIN intento_cobro ic ON ic.id_suscripcion = su.id_suscripcion "
            + "AND ic.id_cuota = c.id_cuota AND ic.numero_intento = ("
            + "SELECT MAX(ultimo.numero_intento) FROM intento_cobro ultimo "
            + "WHERE ultimo.id_suscripcion = su.id_suscripcion AND ultimo.id_cuota = c.id_cuota) "
            + "WHERE su.estado = 'ACTIVA' "
            + "AND (so.dni LIKE ? OR so.nombre LIKE ? OR so.apellido LIKE ?) "
            + "ORDER BY so.apellido, so.nombre, su.id_suscripcion, c.periodo DESC";
        ArrayList<Object[]> filas = new ArrayList<>();
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            String filtro = "%" + busqueda + "%";
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            ps.setString(3, filtro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String notificacion = rs.getString("notificacion");
                    int idCuota = rs.getInt("id_cuota");
                    filas.add(new Object[] {
                        rs.getInt("id_suscripcion"), rs.getString("dni"), rs.getString("socio"),
                        idCuota == 0 ? "SIN CUOTA" : rs.getString("periodo"),
                        idCuota == 0 ? "-" : rs.getString("estado_cuota"),
                        rs.getString("telefono"), rs.getString("sede"), rs.getString("plan"),
                        rs.getInt("dia_cobro"), rs.getDate("permanencia_hasta"),
                        rs.getInt("intentos"), rs.getString("ultimo_resultado"),
                        notificacion, rs.getString("estado"), idCuota,
                        "PENDIENTE".equals(notificacion) ? rs.getLong("id_intento") : 0L
                    });
                }
            }
        }
        return filas;
    }

}
