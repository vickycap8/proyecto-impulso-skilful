package ar.com.skilful.dao;

import ar.com.skilful.conexion.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/** Consulta el historial de pagos sin depender de componentes Swing. */
public final class HistorialCobrosDAO {

    public ArrayList<Object[]> buscar(String busqueda) throws SQLException {
        String sql = "SELECT fecha_pago, periodo, dni, socio, plan, "
                + "medio_pago, importe_final, saldo_aplicado, "
                + "importe_abonado, saldo_generado, estado_pago, "
                + "sede, registrado_por FROM vw_historial_pagos "
                + "WHERE dni LIKE ? OR socio LIKE ? ORDER BY fecha_pago DESC";
        ArrayList<Object[]> filas = new ArrayList<>();
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            String filtro = "%" + busqueda + "%";
            consulta.setString(1, filtro);
            consulta.setString(2, filtro);
            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    filas.add(new Object[] {
                        resultado.getTimestamp("fecha_pago"),
                        resultado.getDate("periodo"),
                        resultado.getString("dni"),
                        resultado.getString("socio"),
                        resultado.getString("plan"),
                        resultado.getString("medio_pago"),
                        resultado.getBigDecimal("importe_final"),
                        resultado.getBigDecimal("saldo_aplicado"),
                        resultado.getBigDecimal("importe_abonado"),
                        resultado.getBigDecimal("saldo_generado"),
                        resultado.getString("estado_pago"),
                        resultado.getString("sede"),
                        resultado.getString("registrado_por")
                    });
                }
            }
        }
        return filas;
    }
}
