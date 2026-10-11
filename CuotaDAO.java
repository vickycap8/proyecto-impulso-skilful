package ar.com.skilful.dao;

import ar.com.skilful.conexion.ConexionBD;
import ar.com.skilful.modelo.Cuota;
import ar.com.skilful.modelo.MedioPago;
import ar.com.skilful.modelo.Tarifa;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CuotaDAO {

    public List<MedioPago> listarMediosPagoPresenciales()
            throws SQLException {

        List<MedioPago> medios = new ArrayList<>();
        String sql =
            "SELECT id_medio_pago, nombre, recargo_porcentaje, "
          + "admite_beneficio FROM medio_pago "
          + "WHERE activo = TRUE AND nombre <> 'MERCADO_PAGO' "
          + "ORDER BY id_medio_pago";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                medios.add(new MedioPago(
                    resultado.getInt("id_medio_pago"),
                    resultado.getString("nombre"),
                    resultado.getBigDecimal("recargo_porcentaje"),
                    resultado.getBoolean("admite_beneficio")
                ));
            }
        }

        return medios;
    }

    public Cuota buscarPendientePorSocio(int idSocio) throws SQLException {
        String sql =
            "SELECT c.id_cuota, m.id_plan, p.nombre AS plan, "
          + "c.periodo, c.fecha_vencimiento, c.estado AS estado_cuota, "
          + "c.importe_original, "
          + "COALESCE(tb.nombre, 'NINGUNO') AS beneficio, "
          + "EXISTS (SELECT 1 FROM suscripcion su "
          + "WHERE su.id_membresia = m.id_membresia "
          + "AND su.estado = 'ACTIVA') AS tiene_suscripcion, "
          + "COALESCE((SELECT MAX(ic.numero_intento) "
          + "FROM suscripcion su INNER JOIN intento_cobro ic "
          + "ON ic.id_suscripcion = su.id_suscripcion "
          + "WHERE su.id_membresia = m.id_membresia "
          + "AND su.estado = 'ACTIVA' AND ic.id_cuota = c.id_cuota), 0) "
          + "AS intentos_suscripcion "
          + "FROM membresia m "
          + "INNER JOIN plan p ON p.id_plan = m.id_plan "
          + "INNER JOIN cuota c ON c.id_membresia = m.id_membresia "
          + "LEFT JOIN beneficio_socio bs ON bs.id_socio = m.id_socio "
          + "AND bs.estado = 'ACTIVO' "
          + "AND (bs.fecha_vencimiento IS NULL OR bs.fecha_vencimiento >= CURDATE()) "
          + "LEFT JOIN tipo_beneficio tb "
          + "ON tb.id_tipo_beneficio = bs.id_tipo_beneficio "
          + "WHERE m.id_socio = ? AND m.estado = 'ACTIVA' "
          + "AND c.estado IN ('PENDIENTE', 'VENCIDA') "
          + "ORDER BY c.periodo LIMIT 1";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idSocio);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return null;
                }

                Cuota cuota = new Cuota();
                cuota.setId(resultado.getInt("id_cuota"));
                cuota.setIdPlan(resultado.getInt("id_plan"));
                cuota.setNombrePlan(resultado.getString("plan"));
                cuota.setPeriodo(resultado.getDate("periodo").toLocalDate());
                cuota.setFechaVencimiento(
                    resultado.getDate("fecha_vencimiento").toLocalDate()
                );
                cuota.setEstado(resultado.getString("estado_cuota"));
                cuota.setImporteOriginal(resultado.getBigDecimal("importe_original"));
                cuota.setBeneficioActivo(resultado.getString("beneficio"));
                cuota.setSuscripcionActiva(resultado.getBoolean("tiene_suscripcion"));
                cuota.setIntentosSuscripcion(resultado.getInt("intentos_suscripcion"));
                return cuota;
            }
        }
    }

    public BigDecimal obtenerSaldoSocio(int idSocio) throws SQLException {
        String sql =
            "SELECT saldo_disponible FROM vw_saldo_socio WHERE id_socio = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idSocio);

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next()
                    ? resultado.getBigDecimal("saldo_disponible")
                    : BigDecimal.ZERO;
            }
        }
    }

    public Tarifa buscarTarifaVigente(int idPlan, String codigoTarifa)
            throws SQLException {

        String sql =
            "SELECT id_tarifa, monto FROM tarifa_plan "
          + "WHERE id_plan = ? AND codigo_tarifa = ? AND activo = TRUE "
          + "AND CURDATE() >= vigencia_desde "
          + "AND (vigencia_hasta IS NULL OR CURDATE() <= vigencia_hasta) "
          + "ORDER BY vigencia_desde DESC LIMIT 1";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPlan);
            sentencia.setString(2, codigoTarifa);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return null;
                }

                return new Tarifa(
                    resultado.getInt("id_tarifa"),
                    resultado.getBigDecimal("monto")
                );
            }
        }
    }

    public void generarCuotasMesActual() throws SQLException {
        String sqlActualizarVencidas =
            "UPDATE cuota SET estado = 'VENCIDA' "
          + "WHERE estado = 'PENDIENTE' AND fecha_vencimiento < CURDATE()";

        String sqlGenerarCuotas =
            "INSERT INTO cuota (id_membresia, id_tarifa_base, periodo, "
          + "fecha_emision, fecha_vencimiento, importe_original, estado) "
          + "SELECT m.id_membresia, tp.id_tarifa, "
          + "CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE), "
          + "CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE), "
          + "DATE_ADD(CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE), "
          + "INTERVAL 4 DAY), tp.monto, 'PENDIENTE' "
          + "FROM membresia m INNER JOIN tarifa_plan tp ON tp.id_tarifa = ("
          + "SELECT tp2.id_tarifa FROM tarifa_plan tp2 "
          + "WHERE tp2.id_plan = m.id_plan "
          + "AND tp2.codigo_tarifa = 'GENERAL' AND tp2.activo = TRUE "
          + "AND CURDATE() >= tp2.vigencia_desde "
          + "AND (tp2.vigencia_hasta IS NULL OR CURDATE() <= tp2.vigencia_hasta) "
          + "ORDER BY tp2.vigencia_desde DESC, tp2.id_tarifa DESC LIMIT 1) "
          + "WHERE m.estado = 'ACTIVA' "
          + "AND m.fecha_inicio <= LAST_DAY(CURDATE()) "
          + "AND (m.fecha_fin IS NULL OR m.fecha_fin >= "
          + "CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE)) "
          + "ON DUPLICATE KEY UPDATE id_membresia = VALUES(id_membresia)";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);

            try (PreparedStatement actualizar =
                    conexion.prepareStatement(sqlActualizarVencidas);
                 PreparedStatement generar =
                    conexion.prepareStatement(sqlGenerarCuotas)) {

                actualizar.executeUpdate();
                generar.executeUpdate();
                conexion.commit();

            } catch (SQLException error) {
                conexion.rollback();
                throw error;
            } finally {
                conexion.setAutoCommit(true);
            }
        }
    }
}
