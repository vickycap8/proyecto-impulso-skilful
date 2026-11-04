package ar.com.skilful.dao;

import ar.com.skilful.conexion.ConexionBD;
import ar.com.skilful.modelo.Sede;
import ar.com.skilful.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public Usuario buscarActivoPorNombre(String nombreUsuario)
            throws SQLException {

        String sql =
            "SELECT u.id_usuario, u.nombre_completo, "
          + "u.nombre_usuario, u.clave_hash, u.clave_salt, "
          + "u.clave_iteraciones, r.nombre AS rol "
          + "FROM usuario u "
          + "INNER JOIN rol r ON r.id_rol = u.id_rol "
          + "WHERE u.nombre_usuario = ? "
          + "AND u.activo = TRUE AND r.activo = TRUE";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, nombreUsuario);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    return null;
                }

                return new Usuario(
                    resultado.getInt("id_usuario"),
                    resultado.getString("nombre_completo"),
                    resultado.getString("nombre_usuario"),
                    resultado.getString("rol"),
                    resultado.getString("clave_hash"),
                    resultado.getString("clave_salt"),
                    resultado.getInt("clave_iteraciones")
                );
            }
        }
    }

    public List<Sede> listarSedesAutorizadas(int idUsuario)
            throws SQLException {

        List<Sede> sedes = new ArrayList<>();

        String sql =
            "SELECT s.id_sede, s.nombre "
          + "FROM usuario_sede us "
          + "INNER JOIN sede s ON s.id_sede = us.id_sede "
          + "WHERE us.id_usuario = ? "
          + "AND us.activo = TRUE AND s.activo = TRUE "
          + "ORDER BY s.nombre";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idUsuario);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    sedes.add(new Sede(
                        resultado.getInt("id_sede"),
                        resultado.getString("nombre")
                    ));
                }
            }
        }

        return sedes;
    }
}
