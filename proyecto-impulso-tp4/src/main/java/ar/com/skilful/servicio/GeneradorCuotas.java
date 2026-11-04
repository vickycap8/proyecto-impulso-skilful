package ar.com.skilful.servicio;

import ar.com.skilful.dao.CuotaDAO;
import java.sql.SQLException;

public final class GeneradorCuotas {

    private GeneradorCuotas() {
    }

    public static void generarCuotasMesActual()
            throws SQLException {
        new CuotaDAO().generarCuotasMesActual();
    }
}
