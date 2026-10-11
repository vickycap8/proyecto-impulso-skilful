package ar.com.skilful.servicio;

import ar.com.skilful.dao.SocioDAO;
import ar.com.skilful.modelo.Socio;

import java.sql.SQLException;
import java.util.List;

public class SocioServicio {

    private final SocioDAO socioDAO;

    public SocioServicio() {
        this(new SocioDAO());
    }

    public SocioServicio(SocioDAO socioDAO) {
        this.socioDAO = socioDAO;
    }

    public List<Socio> buscarActivos(String busqueda) throws SQLException {
        return socioDAO.buscarActivos(
            busqueda == null ? "" : busqueda.trim()
        );
    }

    public void registrar(Socio socio) throws SQLException {
        validar(socio);
        socioDAO.insertar(socio);
    }

    private void validar(Socio socio) {
        if (socio == null) {
            throw new IllegalArgumentException("Debe informar los datos del socio.");
        }
        if (socio.getDni() == null || !socio.getDni().matches("\\d{7,9}")) {
            throw new IllegalArgumentException("El DNI debe tener entre 7 y 9 dígitos.");
        }
        if (esVacio(socio.getNombre()) || esVacio(socio.getApellido())) {
            throw new IllegalArgumentException("Nombre y apellido son obligatorios.");
        }
        if (socio.getFechaNacimiento() == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria.");
        }
        if (socio.getIdSedeHabitual() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una sede habitual.");
        }
    }

    private boolean esVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
