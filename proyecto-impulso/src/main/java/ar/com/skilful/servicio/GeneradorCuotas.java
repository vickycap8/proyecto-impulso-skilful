package ar.com.skilful.servicio;

import ar.com.skilful.conexion.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class GeneradorCuotas {

    private GeneradorCuotas() {
    }

    public static void generarCuotasMesActual()
            throws SQLException {

        String sqlActualizarVencidas =
            "UPDATE cuota " +
            "SET estado = 'VENCIDA' " +
            "WHERE estado = 'PENDIENTE' " +
            "AND fecha_vencimiento < CURDATE()";

        String sqlGenerarCuotas =
            "INSERT INTO cuota (" +
            "id_membresia, id_tarifa_base, periodo, " +
            "fecha_emision, fecha_vencimiento, " +
            "importe_original, estado" +
            ") " +
            "SELECT " +
            "m.id_membresia, " +
            "tp.id_tarifa, " +
            "CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE), " +
            "CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE), " +
            "DATE_ADD(" +
            "CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE), " +
            "INTERVAL 4 DAY" +
            "), " +
            "tp.monto, " +
            "'PENDIENTE' " +
            "FROM membresia m " +
            "INNER JOIN tarifa_plan tp " +
            "ON tp.id_tarifa = (" +
            "SELECT tp2.id_tarifa " +
            "FROM tarifa_plan tp2 " +
            "WHERE tp2.id_plan = m.id_plan " +
            "AND tp2.codigo_tarifa = 'GENERAL' " +
            "AND tp2.activo = TRUE " +
            "AND CURDATE() >= tp2.vigencia_desde " +
            "AND (tp2.vigencia_hasta IS NULL " +
            "OR CURDATE() <= tp2.vigencia_hasta) " +
            "ORDER BY tp2.vigencia_desde DESC, " +
            "tp2.id_tarifa DESC " +
            "LIMIT 1" +
            ") " +
            "WHERE m.estado = 'ACTIVA' " +
            "AND m.fecha_inicio <= LAST_DAY(CURDATE()) " +
            "AND (m.fecha_fin IS NULL " +
            "OR m.fecha_fin >= " +
            "CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE)) " +
            "ON DUPLICATE KEY UPDATE " +
            "id_membresia = VALUES(id_membresia)";

        try (Connection conexion = ConexionBD.conectar()) {

            conexion.setAutoCommit(false);

            try (
                PreparedStatement actualizarVencidas =
                    conexion.prepareStatement(
                        sqlActualizarVencidas
                    );

                PreparedStatement generarCuotas =
                    conexion.prepareStatement(
                        sqlGenerarCuotas
                    )
            ) {
                actualizarVencidas.executeUpdate();
                generarCuotas.executeUpdate();

                conexion.commit();

            } catch (SQLException error) {
                conexion.rollback();
                throw error;
            }
        }
    }
}