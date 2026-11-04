package ar.com.skilful.dao;

import ar.com.skilful.conexion.ConexionBD;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;

/** Acceso a beneficios, incluidos los cambios transaccionales. */
public final class BeneficioDAO {

    public static final class Opcion {
        private final int id;
        private final String nombre;

        public Opcion(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() { return id; }
        public String getNombre() { return nombre; }
        @Override public String toString() { return nombre; }
    }

    public ArrayList<Opcion> tiposActivos() throws SQLException {
        return opciones("SELECT id_tipo_beneficio, nombre FROM tipo_beneficio "
                + "WHERE activo = TRUE ORDER BY nombre DESC", "id_tipo_beneficio");
    }

    public ArrayList<Opcion> empresasActivas() throws SQLException {
        return opciones("SELECT id_empresa, nombre FROM empresa_convenio "
                + "WHERE activo = TRUE ORDER BY nombre", "id_empresa");
    }

    private ArrayList<Opcion> opciones(String sql, String columnaId) throws SQLException {
        ArrayList<Opcion> lista = new ArrayList<>();
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                lista.add(new Opcion(resultado.getInt(columnaId), resultado.getString("nombre")));
            }
        }
        return lista;
    }

    public ArrayList<Object[]> buscar(String busqueda) throws SQLException {
        String sql = "SELECT bs.id_beneficio_socio, so.dni, "
                + "CONCAT(so.apellido, ', ', so.nombre) AS socio, "
                + "tb.nombre AS beneficio, COALESCE(ec.nombre, '-') AS empresa, "
                + "bs.fecha_presentacion, bs.fecha_vencimiento, bs.estado "
                + "FROM beneficio_socio bs INNER JOIN socio so ON so.id_socio = bs.id_socio "
                + "INNER JOIN tipo_beneficio tb ON tb.id_tipo_beneficio = bs.id_tipo_beneficio "
                + "LEFT JOIN empresa_convenio ec ON ec.id_empresa = bs.id_empresa "
                + "WHERE (so.dni LIKE ? OR so.nombre LIKE ? OR so.apellido LIKE ? "
                + "OR tb.nombre LIKE ?) ORDER BY bs.estado, bs.fecha_vencimiento";
        ArrayList<Object[]> filas = new ArrayList<>();
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            String filtro = "%" + busqueda + "%";
            for (int i = 1; i <= 4; i++) sentencia.setString(i, filtro);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    filas.add(new Object[] {
                        resultado.getInt("id_beneficio_socio"), resultado.getString("dni"),
                        resultado.getString("socio"), resultado.getString("beneficio"),
                        resultado.getString("empresa"), resultado.getDate("fecha_presentacion"),
                        resultado.getDate("fecha_vencimiento"), resultado.getString("estado")
                    });
                }
            }
        }
        return filas;
    }

    public void asignar(int idSocio, int idTipo, Integer idEmpresa,
                        LocalDate fechaPresentacion, boolean estudiante,
                        String observacion) throws SQLException {
        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                // Serializa las asignaciones del mismo socio antes de comprobar el beneficio activo.
                try (PreparedStatement bloqueo = conexion.prepareStatement(
                        "SELECT id_socio FROM socio WHERE id_socio = ? FOR UPDATE")) {
                    bloqueo.setInt(1, idSocio);
                    try (ResultSet socio = bloqueo.executeQuery()) {
                        if (!socio.next()) throw new SQLException("El socio seleccionado ya no existe.");
                    }
                }
                try (PreparedStatement consulta = conexion.prepareStatement(
                        "SELECT COUNT(*) FROM beneficio_socio WHERE id_socio = ? AND estado = 'ACTIVO'")) {
                    consulta.setInt(1, idSocio);
                    try (ResultSet resultado = consulta.executeQuery()) {
                        resultado.next();
                        if (resultado.getInt(1) > 0) {
                            throw new SQLException("El socio ya posee un beneficio activo. Los beneficios no son acumulables.");
                        }
                    }
                }
                String sql = "INSERT INTO beneficio_socio (id_socio, id_tipo_beneficio, "
                        + "id_empresa, fecha_presentacion, fecha_vencimiento, estado, observacion) "
                        + "VALUES (?, ?, ?, ?, ?, 'ACTIVO', ?)";
                try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
                    sentencia.setInt(1, idSocio);
                    sentencia.setInt(2, idTipo);
                    if (idEmpresa == null) sentencia.setNull(3, Types.SMALLINT);
                    else sentencia.setInt(3, idEmpresa);
                    sentencia.setDate(4, Date.valueOf(fechaPresentacion));
                    if (estudiante) sentencia.setDate(5, Date.valueOf(fechaPresentacion.plusMonths(6)));
                    else sentencia.setNull(5, Types.DATE);
                    sentencia.setString(6, observacion);
                    if (sentencia.executeUpdate() != 1) throw new SQLException("No se pudo asignar el beneficio.");
                }
                conexion.commit();
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    public boolean finalizar(int idBeneficio) throws SQLException {
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(
                     "UPDATE beneficio_socio SET estado = 'FINALIZADO' WHERE id_beneficio_socio = ?")) {
            sentencia.setInt(1, idBeneficio);
            return sentencia.executeUpdate() == 1;
        }
    }

    public void renovar(int idBeneficio) throws SQLException {
        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                renovarConservandoHistorial(conexion, idBeneficio);
                conexion.commit();
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    /** El llamador inicia la transacción y confirma o revierte ambos cambios. */
    public static void renovarConservandoHistorial(Connection conexion, int idBeneficio)
            throws SQLException {
        if (conexion.getAutoCommit()) throw new SQLException("La renovación requiere una transacción.");
        int idSocio;
        int idTipo;
        String estado;
        String consulta = "SELECT b.id_socio, b.id_tipo_beneficio, b.estado, "
            + "t.nombre FROM beneficio_socio b JOIN tipo_beneficio t "
            + "ON t.id_tipo_beneficio = b.id_tipo_beneficio "
            + "WHERE b.id_beneficio_socio = ? FOR UPDATE";
        try (PreparedStatement ps = conexion.prepareStatement(consulta)) {
            ps.setInt(1, idBeneficio);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("El beneficio seleccionado ya no existe.");
                if (!"ESTUDIANTE".equalsIgnoreCase(rs.getString("nombre"))) {
                    throw new SQLException("Solo se renuevan certificados estudiantiles.");
                }
                idSocio = rs.getInt("id_socio");
                idTipo = rs.getInt("id_tipo_beneficio");
                estado = rs.getString("estado");
            }
        }
        if (!"ACTIVO".equals(estado) && !"VENCIDO".equals(estado)) {
            throw new SQLException("Este registro ya está finalizado. "
                + "Seleccione el beneficio vigente o vencido que desea renovar.");
        }
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT id_beneficio_socio FROM beneficio_socio "
                + "WHERE id_socio = ? AND estado = 'ACTIVO' "
                + "AND id_beneficio_socio <> ? FOR UPDATE")) {
            ps.setInt(1, idSocio);
            ps.setInt(2, idBeneficio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) throw new SQLException("El socio ya tiene otro beneficio activo.");
            }
        }
        try (PreparedStatement ps = conexion.prepareStatement(
                "UPDATE beneficio_socio SET estado = 'FINALIZADO' WHERE id_beneficio_socio = ?")) {
            ps.setInt(1, idBeneficio);
            if (ps.executeUpdate() != 1) throw new SQLException("No se pudo cerrar el beneficio anterior.");
        }
        try (PreparedStatement ps = conexion.prepareStatement(
                "INSERT INTO beneficio_socio (id_socio, id_tipo_beneficio, "
                + "fecha_presentacion, fecha_vencimiento, estado, observacion) "
                + "VALUES (?, ?, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 6 MONTH), "
                + "'ACTIVO', ?)")) {
            ps.setInt(1, idSocio);
            ps.setInt(2, idTipo);
            ps.setString(3, "Renovación del beneficio " + idBeneficio
                + ". Nuevo certificado estudiantil presentado.");
            if (ps.executeUpdate() != 1) throw new SQLException("No se pudo registrar el nuevo certificado.");
        }
    }
}
