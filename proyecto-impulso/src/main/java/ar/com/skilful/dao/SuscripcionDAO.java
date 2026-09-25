package ar.com.skilful.dao;

import ar.com.skilful.conexion.ConexionBD;
import ar.com.skilful.modelo.Suscripcion;

import java.sql.Connection;
import java.sql.Date;
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
}
