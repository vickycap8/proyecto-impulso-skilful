package ar.com.skilful.conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConsultaSocios {

    public static void main(String[] args) {

        String sql = "SELECT dni, nombre, apellido, correo "
                   + "FROM socio WHERE activo = TRUE "
                   + "ORDER BY apellido, nombre";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            System.out.println("SOCIOS REGISTRADOS");
            System.out.println("------------------");

            while (resultado.next()) {
                System.out.println(
                    resultado.getString("dni") + " | "
                    + resultado.getString("apellido") + ", "
                    + resultado.getString("nombre") + " | "
                    + resultado.getString("correo")
                );
            }

        } catch (SQLException e) {
            System.out.println("No se pudieron consultar los socios.");
            System.out.println("Detalle: " + e.getMessage());
        }
    }
}