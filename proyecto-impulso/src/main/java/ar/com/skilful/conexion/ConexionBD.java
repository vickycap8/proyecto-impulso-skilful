package ar.com.skilful.conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL =
        "jdbc:mysql://localhost:3306/proyecto_impulso"
        + "?useSSL=false"
        + "&allowPublicKeyRetrieval=true"
        + "&serverTimezone=UTC";

    private static final String VARIABLE_USUARIO =
        "IMPULSO_DB_USUARIO";

    private static final String VARIABLE_CONTRASENA =
        "IMPULSO_DB_CONTRASENA";

    public static Connection conectar()
            throws SQLException {

        String usuario =
            System.getenv(VARIABLE_USUARIO);

        String contrasena =
            System.getenv(VARIABLE_CONTRASENA);

        if (usuario == null || usuario.trim().isEmpty()
                || contrasena == null
                || contrasena.isEmpty()) {

            throw new SQLException(
                "No se configuraron las credenciales "
                    + "de conexión a la base de datos."
            );
        }

        return DriverManager.getConnection(
            URL,
            usuario,
            contrasena
        );
    }

    public static void main(String[] args) {

        try (Connection conexion = conectar()) {

            System.out.println(
                "Conexión realizada correctamente."
            );

        } catch (SQLException error) {

            System.out.println(
                "No se pudo conectar con la base de datos."
            );

            System.out.println(
                "Detalle: " + error.getMessage()
            );
        }
    }
}