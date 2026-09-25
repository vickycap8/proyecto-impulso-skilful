package ar.com.skilful.dao;

import ar.com.skilful.conexion.ConexionBD;
import ar.com.skilful.modelo.Pago;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PagoDAO {

    public long registrar(Pago pago) throws SQLException {
        Connection conexion = null;

        try {
            conexion = ConexionBD.conectar();
            conexion.setAutoCommit(false);

            long idPago = insertarPago(conexion, pago);
            marcarCuotaPagada(conexion, pago.getIdCuota());

            if (pago.getSaldoAplicado().compareTo(BigDecimal.ZERO) > 0) {
                insertarMovimiento(
                    conexion, pago.getIdSocio(), idPago, "DEBITO",
                    pago.getSaldoAplicado(), "Saldo aplicado al pago de cuota"
                );
            }

            if (pago.getSaldoGenerado().compareTo(BigDecimal.ZERO) > 0) {
                insertarMovimiento(
                    conexion, pago.getIdSocio(), idPago, "CREDITO",
                    pago.getSaldoGenerado(), "Saldo generado por cobro de cuota"
                );
            }

            conexion.commit();
            return idPago;

        } catch (SQLException error) {
            if (conexion != null) {
                try {
                    conexion.rollback();
                } catch (SQLException ignorado) {
                    error.addSuppressed(ignorado);
                }
            }
            throw error;

        } finally {
            if (conexion != null) {
                try {
                    conexion.setAutoCommit(true);
                    conexion.close();
                } catch (SQLException ignorado) {
                    // La operación principal ya terminó; no se oculta su resultado.
                }
            }
        }
    }

    private long insertarPago(Connection conexion, Pago pago)
            throws SQLException {

        String sql =
            "INSERT INTO pago "
          + "(id_cuota, id_medio_pago, id_tarifa_aplicada, id_usuario, "
          + "id_sede, precio_original, descuento, recargo, saldo_aplicado, "
          + "importe_final, importe_abonado, saldo_generado, estado) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'REGISTRADO')";

        try (PreparedStatement sentencia = conexion.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            sentencia.setInt(1, pago.getIdCuota());
            sentencia.setInt(2, pago.getIdMedioPago());
            sentencia.setInt(3, pago.getIdTarifaAplicada());
            sentencia.setInt(4, pago.getIdUsuario());
            sentencia.setInt(5, pago.getIdSede());
            sentencia.setBigDecimal(6, pago.getPrecioOriginal());
            sentencia.setBigDecimal(7, pago.getDescuento());
            sentencia.setBigDecimal(8, pago.getRecargo());
            sentencia.setBigDecimal(9, pago.getSaldoAplicado());
            sentencia.setBigDecimal(10, pago.getImporteFinal());
            sentencia.setBigDecimal(11, pago.getImporteAbonado());
            sentencia.setBigDecimal(12, pago.getSaldoGenerado());
            sentencia.executeUpdate();

            try (ResultSet claves = sentencia.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("No se obtuvo el identificador del pago.");
                }
                return claves.getLong(1);
            }
        }
    }

    private void marcarCuotaPagada(Connection conexion, int idCuota)
            throws SQLException {

        String sql =
            "UPDATE cuota SET estado = 'PAGADA' "
          + "WHERE id_cuota = ? AND estado IN ('PENDIENTE', 'VENCIDA')";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idCuota);
            if (sentencia.executeUpdate() != 1) {
                throw new SQLException("La cuota ya no se encuentra pendiente.");
            }
        }
    }

    private void insertarMovimiento(
            Connection conexion,
            int idSocio,
            long idPago,
            String tipo,
            BigDecimal monto,
            String descripcion) throws SQLException {

        String sql =
            "INSERT INTO movimiento_saldo "
          + "(id_socio, id_pago, tipo, monto, descripcion) "
          + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, idSocio);
            sentencia.setLong(2, idPago);
            sentencia.setString(3, tipo);
            sentencia.setBigDecimal(4, monto);
            sentencia.setString(5, descripcion);
            sentencia.executeUpdate();
        }
    }
}
