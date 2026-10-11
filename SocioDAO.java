package ar.com.skilful.dao;

import ar.com.skilful.conexion.ConexionBD;
import ar.com.skilful.modelo.Socio;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SocioDAO {

    public List<Socio> buscarActivos(String busqueda) throws SQLException {
        List<Socio> socios = new ArrayList<>();

        String sql =
            "SELECT so.id_socio, so.dni, so.apellido, so.nombre, "
          + "so.telefono, so.correo, se.nombre AS sede "
          + "FROM socio so "
          + "INNER JOIN sede se ON se.id_sede = so.id_sede_habitual "
          + "WHERE so.activo = TRUE "
          + "AND (so.dni LIKE ? OR so.nombre LIKE ? OR so.apellido LIKE ?) "
          + "ORDER BY so.apellido, so.nombre";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            String filtro = "%" + busqueda + "%";
            sentencia.setString(1, filtro);
            sentencia.setString(2, filtro);
            sentencia.setString(3, filtro);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    Socio socio = new Socio();
                    socio.setId(resultado.getInt("id_socio"));
                    socio.setDni(resultado.getString("dni"));
                    socio.setApellido(resultado.getString("apellido"));
                    socio.setNombre(resultado.getString("nombre"));
                    socio.setTelefono(resultado.getString("telefono"));
                    socio.setCorreo(resultado.getString("correo"));
                    socio.setNombreSede(resultado.getString("sede"));
                    socios.add(socio);
                }
            }
        }

        return socios;
    }

    public void insertar(Socio socio) throws SQLException {
        String sql =
            "INSERT INTO socio "
          + "(id_sede_habitual, dni, nombre, apellido, fecha_nacimiento, "
          + "telefono, telefono_emergencia, correo, domicilio, activo) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, socio.getIdSedeHabitual());
            sentencia.setString(2, socio.getDni());
            sentencia.setString(3, socio.getNombre());
            sentencia.setString(4, socio.getApellido());
            sentencia.setDate(5, Date.valueOf(socio.getFechaNacimiento()));
            sentencia.setString(6, socio.getTelefono());
            sentencia.setString(7, socio.getTelefonoEmergencia());
            sentencia.setString(8, socio.getCorreo());
            sentencia.setString(9, socio.getDomicilio());
            sentencia.executeUpdate();
        }
    }
}
