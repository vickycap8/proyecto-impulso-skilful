package ar.com.skilful.dao;

import ar.com.skilful.conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/** Consultas de solo lectura para las tres tablas de alertas. */
public final class AlertaDAO {

    public ArrayList<Object[]> certificadosProximos() throws SQLException {
        String sql = "SELECT id_socio, dni, socio, fecha_vencimiento, "
                + "dias_restantes, sede_habitual "
                + "FROM vw_certificados_proximos_vencer ORDER BY dias_restantes";
        ArrayList<Object[]> filas = new ArrayList<>();
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                filas.add(new Object[] {
                    resultado.getInt("id_socio"), resultado.getString("dni"),
                    resultado.getString("socio"), resultado.getDate("fecha_vencimiento"),
                    resultado.getInt("dias_restantes"), resultado.getString("sede_habitual")
                });
            }
        }
        return filas;
    }

    public ArrayList<Object[]> notificacionesPendientes() throws SQLException {
        String sql = "SELECT id_intento, dni, socio, telefono, numero_intento, "
                + "fecha_intento, importe, sede FROM vw_notificaciones_pendientes "
                + "ORDER BY fecha_intento";
        ArrayList<Object[]> filas = new ArrayList<>();
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                filas.add(new Object[] {
                    resultado.getLong("id_intento"), resultado.getString("dni"),
                    resultado.getString("socio"), resultado.getString("telefono"),
                    resultado.getInt("numero_intento"), resultado.getTimestamp("fecha_intento"),
                    resultado.getBigDecimal("importe"), resultado.getString("sede")
                });
            }
        }
        return filas;
    }

    public ArrayList<Object[]> resumenDiarioIntentos() throws SQLException {
        String sql = "SELECT fecha, id_sede, sede, total_intentos, aprobados, "
                + "rechazados, pendientes, importe_aprobado "
                + "FROM vw_resumen_diario_cobros ORDER BY fecha DESC, sede";
        ArrayList<Object[]> filas = new ArrayList<>();
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                filas.add(new Object[] {
                    resultado.getDate("fecha"), resultado.getInt("id_sede"),
                    resultado.getString("sede"), resultado.getInt("total_intentos"),
                    resultado.getInt("aprobados"), resultado.getInt("rechazados"),
                    resultado.getInt("pendientes"), resultado.getBigDecimal("importe_aprobado")
                });
            }
        }
        return filas;
    }
}
