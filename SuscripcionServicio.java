package ar.com.skilful.servicio;

import ar.com.skilful.dao.SuscripcionDAO;
import ar.com.skilful.modelo.Suscripcion;

import java.sql.SQLException;
import java.time.LocalDate;

public class SuscripcionServicio {

    private final SuscripcionDAO suscripcionDAO;

    public SuscripcionServicio() {
        this(new SuscripcionDAO());
    }

    public SuscripcionServicio(SuscripcionDAO suscripcionDAO) {
        this.suscripcionDAO = suscripcionDAO;
    }

    public void registrar(Suscripcion suscripcion) throws SQLException {
        if (suscripcion == null || suscripcion.getIdMembresia() <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una membresía.");
        }
        if (suscripcion.getDiaCobro() < 1 || suscripcion.getDiaCobro() > 31) {
            throw new IllegalArgumentException("El día de cobro debe estar entre 1 y 31.");
        }

        LocalDate permanenciaMinima =
            suscripcion.getFechaInicio().plusMonths(6);

        if (suscripcion.getPermanenciaHasta().isBefore(permanenciaMinima)) {
            throw new IllegalArgumentException(
                "La permanencia mínima debe ser de seis meses."
            );
        }

        suscripcionDAO.insertar(suscripcion);
    }

    public boolean finalizar(int idSuscripcion) throws SQLException {
        return suscripcionDAO.finalizarSiCumplioPermanencia(idSuscripcion);
    }
}
